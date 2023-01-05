// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.appcore.solution;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.optaplanner.core.api.score.AbstractScore;

import java.util.UUID;

@Data
@NoArgsConstructor
@SuperBuilder
public abstract class SolutionBase<TScore extends AbstractScore> {

    protected UUID id;

    protected long createdAt;

    protected TScore score;
}
