// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@ApplicationScoped
@Path("/")
public class HealthCheck {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheck.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String isLive() {
        logger.info("Service OK");
        return "Service OK";
    }
}
