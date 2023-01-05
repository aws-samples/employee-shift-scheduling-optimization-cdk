// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

public class ScheduleObjectWriter {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleObjectWriter.class);

    public static void writeJsonFile(Object object, String filePath) {
        try {
            // create object mapper instance
            ObjectMapper mapper = new ObjectMapper();

            // convert object to JSON file
            mapper.writeValue(Paths.get(filePath).toFile(), object);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public static void printObjectAsJsonString(Object object) {
        try {
            // create object mapper instance
            ObjectMapper mapper = new ObjectMapper();

            // print JSON object
            logger.info("print object \n{}", mapper.writeValueAsString(object));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private ScheduleObjectWriter() {
        throw new AssertionError("Utility class");
    }
}
