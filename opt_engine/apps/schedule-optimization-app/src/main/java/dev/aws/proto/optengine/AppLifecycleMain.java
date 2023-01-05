// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class AppLifecycleMain {
    private static final Logger logger = Logger.getLogger(AppLifecycleMain.class);

    void onStart(@Observes StartupEvent ev) {
        logger.info("Optimization Engine -->> application is starting...");
    }

    void onStop(@Observes ShutdownEvent ev) {
        logger.info("Optimization Engine -->> application is stopping...");
    }
}
