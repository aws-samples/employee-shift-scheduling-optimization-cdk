// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.util;

import dev.aws.proto.optengine.rest.data.DataBestScheduleSolution;
import dev.aws.proto.optengine.rest.data.DataEmployeeSchedule;
import dev.aws.proto.optengine.solution.EmployeeScheduleSolution;

import java.util.*;
import java.util.stream.Collectors;

public class ScheduleSolutionResultUtil {

    public static String createSolutionOverview(EmployeeScheduleSolution solution) {
        Set<String> date = new HashSet<>();
        Map<String, String> name = new HashMap<>();
        Map<String, Map<String, String>> scheduleMap = new HashMap<>();
        Map<String, Map<String, String>> onRqMap = new HashMap<>();
        Map<String, Map<String, String>> offRqMap = new HashMap<>();

        // collect data
        for(var item : solution.getShiftAssignmentList()) {
            String shiftDate = item.getShiftDate().format(Constants.DefaultDateFormatter);
            String empNo = item.getEmployee().getEmpNo();
            String shiftType = item.getShiftCode().getType();

            var empSchedule = scheduleMap.get(empNo);
            if(empSchedule == null) {
                empSchedule = new HashMap<>();
                scheduleMap.put(empNo, empSchedule);
                name.put(empNo, item.getEmployee().getName());
            }

            date.add(shiftDate);
            empSchedule.put(shiftDate, shiftType);
        }

        // employee request : day off
        for(var item : solution.getDayOffRqList()) {
            String rqDate = item.getShiftDate().format(Constants.DefaultDateFormatter);
            var rq = offRqMap.computeIfAbsent(rqDate, k -> new HashMap<>());
            rq.put(item.getEmpNo(), "#");
        }

        // employee request : shift off
        for(var item : solution.getShiftOffRqList()) {
            String rqDate = item.getShiftDate().format(Constants.DefaultDateFormatter);
            var rq = offRqMap.computeIfAbsent(rqDate, k -> new HashMap<>());
            rq.put(item.getEmpNo(), "/");
        }

        // employee request : shift off
        for(var item : solution.getShiftOnRqList()) {
            String rqDate = item.getShiftDate().format(Constants.DefaultDateFormatter);
            var rq = onRqMap.computeIfAbsent(rqDate, k -> new HashMap<>());
            rq.put(item.getEmpNo(), item.getType());
        }

        // sort key
        List<String> dateList = date.stream().sorted().collect(Collectors.toList());
        List<String> empList = scheduleMap.keySet().stream().sorted().collect(Collectors.toList());

        // format data
        StringBuilder sb = new StringBuilder();
        StringBuilder st = new StringBuilder();
        StringBuilder sl = new StringBuilder();
        Map<String, StringBuilder> sbEmpMap = new HashMap<>();
        sb.append("                    |");
        st.append("                    |");
        sl.append("--------------------+");
        for(var d : dateList) {
            sb.append(d).append("|");
            st.append(" A| P| L|");
            sl.append("--------+");

            // employee schedules per day
            var dailyOffRq = offRqMap.get(d);
            var dailyOnRq = onRqMap.get(d);
            for(var empNo : empList) {
                var scheduleType = scheduleMap.get(empNo).get(d);

                // employee name
                var sbEmp = sbEmpMap.get(empNo);
                if(sbEmp == null) {
                    sbEmp = new StringBuilder();
                    sbEmpMap.put(empNo, sbEmp);

                    String empName = name.get(empNo);
                    int numLength = 2+empNo.length(); // length of (bracket + empNo)
                    int nameLengthOffset = (empName.getBytes().length - empName.length()) / 3; // CJK character adjustments
                    int padSize = 20 - numLength - nameLengthOffset;

                    String empInfo = String.format("%s(%s)", leftPad(empName, padSize), empNo);
                    sbEmp.append(empInfo).append("|");
                }

                // assigned schedule
                String onRq = dailyOnRq==null?null: dailyOnRq.get(empNo);
                String dailyAttend = String.format("%s%s %s%s %s%s|",
                        mark(onRq,"A"),
                        matchOrBlank(scheduleType,"A"),
                        mark(onRq,"P"),
                        matchOrBlank(scheduleType,"P"),
                        mark(onRq,"L"),
                        matchOrBlank(scheduleType, "L"));

                // off request
                String offRq = dailyOffRq==null?null: dailyOffRq.get(empNo);
                if(offRq != null) dailyAttend = dailyAttend.replace(" ", offRq);

                sbEmp.append(dailyAttend);
            }
        }

        StringBuilder collector = new StringBuilder().append(System.lineSeparator());
        collector.append(sb).append(System.lineSeparator());
        collector.append(st).append(System.lineSeparator());
        collector.append(sl).append(System.lineSeparator());
        for(var empNo : empList) {
            collector.append(sbEmpMap.get(empNo)).append(System.lineSeparator());
            collector.append(sl).append(System.lineSeparator());
        }

        return collector.toString();
    }

    public static DataBestScheduleSolution extractBestSolution(EmployeeScheduleSolution bestSolution, long solverDurationInMs) {
        final int NUM_OF_SCHEDULES = bestSolution.getShiftAssignmentList().size();
        var scheduleList = new DataEmployeeSchedule[NUM_OF_SCHEDULES];
        for(int i = 0 ; i < NUM_OF_SCHEDULES ; i++) {
            var sa = bestSolution.getShiftAssignmentList().get(i);
            scheduleList[i] =  DataEmployeeSchedule.builder()
                    .date(sa.getShiftDate().format(Constants.DefaultDateFormatter))
                    .atndCode(sa.getShiftCode().getCode())
                    .empNo(sa.getEmployee().getEmpNo())
                    .build();
        }

        return DataBestScheduleSolution.builder()
                .problemId(bestSolution.getId().toString())
                .score(bestSolution.getScore().toString())
                .createdAt(bestSolution.getCreatedAt())
                .solverDurationInMs(solverDurationInMs)
                .schedule(scheduleList)
                .build();
    };

    private static String mark(String input, final String matchedTo) {
        if(input == null || !input.equals(matchedTo)) return " ";
        else return "*";
    }

    private static String matchOrBlank(String input, final String matchedTo) {
        if(input != null && input.equals(matchedTo)) return input;
        else return " ";
    }

    private static String leftPad(String text, int length) {
        return String.format("%" + length + "." + length + "s", text);
    }

    private ScheduleSolutionResultUtil() {
        throw new AssertionError("Utility class");
    }
}
