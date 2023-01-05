// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.appcore.solution.inspect;

import lombok.Getter;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.solver.SolverManager;

import java.util.Map;

public abstract class AbstractSolutionInspector<T_Solution, T_Report, T_Score extends Score<T_Score>> {

    @Getter
    protected SolverManager<T_Solution, ?> solverManager;
    @Getter
    protected T_Solution solution;

    protected ScoreManager<T_Solution, T_Score> scoreManager;
    protected ScoreExplanation<T_Solution, T_Score> scoreExplanation;
    @Getter
    protected Map<Object, Indictment<T_Score>> indictmentMap;

    public AbstractSolutionInspector(SolverManager<T_Solution, ?> solverManager, T_Solution solution) {
        this.solverManager = solverManager;
        this.solution = solution;

        this.scoreManager = ScoreManager.create(solverManager);
        this.scoreExplanation = scoreManager.explainScore(solution);
        this.indictmentMap = scoreExplanation.getIndictmentMap();
    }

    /**
     * Inspect solution
     * Explaining the score: which constraints are broken?
     * https://www.optaplanner.org/docs/optaplanner/latest/score-calculation/score-calculation.html#explainingTheScore
     */
    public abstract T_Report inspect();
}
