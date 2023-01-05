#!/bin/sh

set -e

export DEPLOYMENT_PROFILE=your-aws-peofile
export DEPLOYMENT_REGION=ap-northeast-2
export DEPLOYMENT_NAMESPACE=devproto

STS=$(aws sts get-caller-identity --profile $DEPLOYMENT_PROFILE --region $DEPLOYMENT_REGION --output json)
printf "Based on your DEPLOYMENT_PROFILE and DEPLOYMENT_REGION, this is your caller identity:\n"
printf "$(echo $STS | jq)\n"
read -p "Is this correct (YyNn)? " -n 1 -r
[[ $REPLY =~ ^[Yy]$ ]] || exit 0
printf "\n"

export ACCOUNT_ID=$(echo $STS | jq .Account --raw-output)

# .npmrc in HOME
if [ -f "$HOME/.npmrc" ]; then
    printf "\nDetected $HOME/.npmrc file that may interfere with the installation\n"
    printf "Make sure that you have all settings properly setup (or delete it from $HOME/.npmrc)"
    read -p "My .npmrc is correct (YyNn)?\n " -n 1 -r
    [[ $REPLY =~ ^[Yy]$ ]] || exit 0
    printf "\n"
fi

# DOCKER check
if (! docker stats --no-stream &> /dev/null); then
    printf "Docker daemon is not running. Docker is mandatory to run for this deployment\n"
    read -p "Start docker (YyNn)? " -n 1 -r
    [[ $REPLY =~ ^[Yy]$ ]] || exit 0
    printf "\n"

    # Mac OS launch Docker
    open /Applications/Docker.app
    # Wait until Docker daemon is running and has completed initialisation
    while (! docker stats --no-stream &> /dev/null); do
        # Docker takes a few seconds to initialize
        echo "Waiting for Docker to launch..."
        sleep 5
    done
fi

# Deploy project
printf "=====================================================================\n"
printf "Deployment for Schedule Optimization Application\n"
printf "=====================================================================\n"
printf "The following deployment steps will be performed:\n"
printf "\t1. Build the whole project\n"
printf "\t2. Build docker image for processing step and push it to ECR\n"
printf "\t3. CDK deploy the whole infrastructure\n"
printf "\n"
read -p "Ready (YyNn)? " -n 1 -r
[[ $REPLY =~ ^[Yy]$ ]] || exit 0
printf "\n"

printf "\n=====================================================================\n"
printf "Application Build\n"
printf "=====================================================================\n"
pushd opt_engine
sh build_schedule_optimization_app.sh
popd

printf "\n=====================================================================\n"
printf "CDK Application Build\n"
printf "=====================================================================\n"
yarn init-project
yarn build:infra

printf "\n=====================================================================\n"
printf "Create ECR \n"
printf "=====================================================================\n"
pushd infra
# Create Repository
cdk deploy EcrStack --profile "${DEPLOYMENT_PROFILE}" --region "${DEPLOYMENT_REGION}" --outputs-file ./ecr-stack.json
# Tag container image
export ECR_NAME_KEY=.[\""${DEPLOYMENT_NAMESPACE}-EcrStack"\"].EcrRepository
export DEPLOYMENT_ECR=$(cat ecr-stack.json | jq -r $ECR_NAME_KEY)
export DEPLOYMENT_CONTAINER_IMAGE=$ACCOUNT_ID.dkr.ecr.$DEPLOYMENT_REGION.amazonaws.com/$DEPLOYMENT_ECR:latest
rm ecr-stack.json
popd


printf "\n=====================================================================\n"
printf "Build Container image\n"
printf "=====================================================================\n"
pushd opt_engine/dist/schedule-optimization-app
# Docker Build
docker build --platform linux/amd64 -t "${DEPLOYMENT_CONTAINER_IMAGE}" .

printf "\n=====================================================================\n"
printf "Upload Container image\n"
printf "=====================================================================\n"
# ECR login
aws ecr get-login-password --profile "${DEPLOYMENT_PROFILE}" --region "${DEPLOYMENT_REGION}" | docker login --username AWS --password-stdin "${ACCOUNT_ID}.dkr.ecr.${DEPLOYMENT_REGION}.amazonaws.com"
# Push image
docker push "${DEPLOYMENT_CONTAINER_IMAGE}"
popd

printf "\n=====================================================================\n"
printf "Deploy infra structures\n"
printf "=====================================================================\n"

pushd infra
yarn cdk:bootstrap --profile "${DEPLOYMENT_PROFILE}" --region "${DEPLOYMENT_REGION}"
yarn dev:deploy --profile "${DEPLOYMENT_PROFILE}" --region "${DEPLOYMENT_REGION}"
popd

printf "\n"
printf "\nCDK deployment done."
printf "\n"