package dev.aws.proto.optengine.solution.inspect;

import dev.aws.proto.appcore.solution.inspect.AbstractSolutionInspector;
import dev.aws.proto.optengine.solution.EmployeeScheduleSolution;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.score.stream.ConstraintJustification;
import org.optaplanner.core.api.solver.SolverManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Solution inspector
 * @seealso https://www.optaplanner.org/docs/optaplanner/latest/score-calculation/score-calculation.html#explainingTheScore
 */
public class EmployeeScheduleSolutionInspector extends AbstractSolutionInspector {

    public EmployeeScheduleSolutionInspector(SolverManager solverManager, EmployeeScheduleSolution solution) {
        super(solverManager, solution);
    }

    @Override
    public InspectionReport inspect() {
        return InspectionReport.builder()
                .problemId(getSolution().getId().toString())        // 1. Problem ID
                .totalScore(scoreExplanation.getScore().toString()) // 2. Total score
                .constraintReport(createConstraintReport())         // 3. Constraint report
                .employeeReport(createEmployeeReport())             // 4. Employee report
                .build();
    }

    // Break down the score by constraint
    // see also - constraint justification : https://www.optaplanner.org/docs/optaplanner/latest/score-calculation/score-calculation.html#constraintMatchTotal
    private List<ReportItem> createConstraintReport() {
        var constraintReport = new ArrayList<ReportItem>();

        Collection<ConstraintMatchTotal<HardMediumSoftScore>> constraintMatchTotalMap = scoreExplanation.getConstraintMatchTotalMap().values();
        for (var constraintMatchTotal : constraintMatchTotalMap) {
            // constraint score
            String constraintName = constraintMatchTotal.getConstraintName();
            HardMediumSoftScore score = constraintMatchTotal.getScore();
            List<ReportItemDetail> detail = new ArrayList<>();

            ReportItem item = new ReportItem(constraintName, score.toString(), detail);
            constraintReport.add(item);

            // skip if no penalties
            if(score.equals(HardMediumSoftScore.ZERO)) continue;

            // collect reason of penalties
            for(var constraintMatch : constraintMatchTotal.getConstraintMatchSet()) {
                // skip no penalties and soft score presents
                HardMediumSoftScore detailScore = constraintMatch.getScore();
                if(detailScore.equals(HardMediumSoftScore.ZERO)) continue;

                // detail
                String detailTitle = constraintMatch.getConstraintName();
                ConstraintJustification justification = constraintMatch.getJustification();
                detail.add(new ReportItemDetail(detailTitle, detailScore.toString(), justification.toString()));
            }
        }

        return constraintReport;
    }

    // Indictment heat map : visualize the hot planning entities
    // see also - indictment : https://www.optaplanner.org/docs/optaplanner/latest/score-calculation/score-calculation.html#indictmentHeatMap
    private List<ReportItem> createEmployeeReport() {
        var employeeReport = new ArrayList<ReportItem>();

        var bestSolution = getSolution();
        var solutionIndictment = getIndictmentMap();
        for(var employee : bestSolution.getEmployeeList()) {
            var indictment = solutionIndictment.get(employee);

            // skip if no indictment by employee
            if (indictment == null) continue;

            HardMediumSoftScore indictmentScore = indictment.getScore();

            // skip if no penalties
            if(indictmentScore.equals(HardMediumSoftScore.ZERO)) continue;

            // indictment
            String indictmentName = employee.toString();
            List<ReportItemDetail> detail = new ArrayList<>();

            ReportItem item = new ReportItem(indictmentName, indictmentScore.toString(), detail);
            employeeReport.add(item);

            // collect constraint detail by indictment
            for (var constraintMatch : indictment.getConstraintMatchSet()) {
                String constraintName = constraintMatch.getConstraintName();
                HardMediumSoftScore constraintScore = constraintMatch.getScore();

                // skip if no penalties
                if(constraintScore.equals(HardMediumSoftScore.ZERO)) continue;

                detail.add(new ReportItemDetail(constraintName, constraintScore.toString(), null));
            }
        }

        return employeeReport;
    }

    @Override
    public EmployeeScheduleSolution getSolution() {
        return (EmployeeScheduleSolution)super.getSolution();
    }

    @Override
    public SolverManager getSolverManager() {
        return super.getSolverManager();
    }

    @Override
    public Map<Object, Indictment<HardMediumSoftScore>> getIndictmentMap() {
        return super.getIndictmentMap();
    }
}
