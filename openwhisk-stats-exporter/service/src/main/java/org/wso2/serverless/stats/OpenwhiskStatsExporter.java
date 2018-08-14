/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.serverless.stats;

import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.exporter.PushGateway;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.wso2.serverless.stats.listeners.OpenwhiskEventListener;

import java.io.IOException;

import static org.wso2.serverless.stats.Constants.*;

/**
 * Exports Statistics based on Kafka events received
 */
public class OpenwhiskStatsExporter implements OpenwhiskEventListener {

    private static final Log log = LogFactory.getLog(OpenwhiskStatsExporter.class);

    private static final Counter durationCounter = Counter.build()
            .name(ACTIVATION_DURATION_COUNTER)
            .labelNames(NAMESPACE, SOURCE, USER_ID, ACTION, STATUS_CODE, KIND)
            .help("Activation Duration Counter")
            .register();

    private static final Counter activationsCounter = Counter.build()
            .name(ACTIVATIONS_TOTAL_COUNTER)
            .labelNames(NAMESPACE, SOURCE, USER_ID, ACTION, STATUS_CODE, KIND)
            .help("Total Activations Counter")
            .register();

    private Config config;
    private OpenwhiskEventCollector collector;
    private PushGateway pushGateway;

    /**
     * OpenwhiskStatsExporter exports statistics
     *
     * @param config Configurations
     */
    public OpenwhiskStatsExporter(Config config) {
        if (config == null) {
            throw new IllegalArgumentException("Config is empty");
        }
        this.config = config;
    }

    /**
     * Sets configurations and starts OpenwhiskEventCollector
     */
    public void start() {
        pushGateway = new PushGateway(config.getPushGateway());
        collector = new OpenwhiskEventCollector(config.getApplicationId(),
                config.getKafkaTopic(), config.getKafkaServer());
        collector.addListener(this);
        collector.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    /**
     * Removes listeners and stops the process
     */
    public void stop() {
        collector.removeListener(this);
        collector.stop();
    }

    /**
     * Main function to execute the process
     *
     * @param args Commandline arguments
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        Config options = new Config();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            parser.printUsage(System.err);
        }

        OpenwhiskStatsExporter statsExporter = new OpenwhiskStatsExporter(options);
        statsExporter.start();
        HTTPServer server;
        try {
            server = new HTTPServer(HTTP_SERVER_PORT);
            log.info("Metrics HTTP server started on port " + HTTP_SERVER_PORT);
        } catch (IOException e) {
            log.error("Failed to start Metrics HTTP Server", e);
            // If the server is not starting, no need to continue the program.
            // Therefore stop the stats exporter and throws the exception
            statsExporter.stop();
            throw new IllegalStateException("Unable to start the HTTP server", e);
        }

        // If the program was closed forcefully, need to close the server and stats exporter
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            statsExporter.stop();
        }));

        Thread.currentThread().join();
    }

    /**
     * Receives events, processes counters and pushes metrics to Pushgateway
     *
     * @param event
     */
    @Override
    public void onEvent(OpenwhiskEvent event) {
        String source = event.getEvent().getString(SOURCE);
        String namespace = event.getEvent().getString(NAMESPACE);
        String userId = event.getEvent().getString(USER_ID);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Received event from source: %s, namespace: %s - %s",
                    source, namespace, event.getEvent().toString()));
        }

        switch (event.getEvent().getString(EVENT_TYPE)) {
            case EVENT_TYPE_ACTIVATION:
                JSONObject body = event.getEvent().getJSONObject(JSON_BODY);
                String statusCode = String.valueOf(body.getInt(STATUS_CODE));
                String action = body.getString(ACTION_NAME);
                String kind = body.getString(ACTION_KIND);
                long duration = body.getLong(DURATION);

                activationsCounter
                        .labels(namespace, source, userId, action, statusCode, kind)
                        .inc();
                durationCounter
                        .labels(namespace, source, userId, action, statusCode, kind)
                        .inc(duration);
                try {
                    log.debug("Pushing to Pushgateway");
                    pushGateway.pushAdd(activationsCounter, OPENWHISK);
                    pushGateway.pushAdd(durationCounter, OPENWHISK);
                } catch (IOException e) {
                    log.error("Error occurred when pushing", e);
                }
                break;

            case EVENT_TYPE_METRIC:
                break;
        }
    }

    /**
     * Maps CLI options passed
     */
    private static class Config {

        @Option(name = CMD_OPTION_NAME_KAFKA, required = true, usage = "IP and port of Kafka server")
        private String kafkaServer;

        @Option(name = CMD_OPTION_NAME_TOPIC, usage = "Kafka topic to listen for events")
        private String kafkaTopic = DEFAULT_KAFKA_TOPIC;

        @Option(name = CMD_OPTION_NAME_APP, usage = "Kafka streams app ID. Will be used as the consumer group name as well.")
        private String applicationId = DEFAULT_APPLICATION_ID;

        @Option(name = CMD_OPTION_NAME_PUSHGATEWAY, required = true, usage = "IP and port of Prometheus Pushgateway")
        private String pushGateway;

        /**
         * Gets Kafka Server IP
         *
         * @return Kafka Server IP
         */
        public String getKafkaServer() {
            return kafkaServer;
        }

        /**
         * Sets Kafka Server IP
         *
         * @param kafkaServer Kafka Server IP
         */
        public void setKafkaServer(String kafkaServer) {
            this.kafkaServer = kafkaServer;
        }

        /**
         * Gets Kafka Topic
         *
         * @return Kafka Topic
         */
        public String getKafkaTopic() {
            return kafkaTopic;
        }

        /**
         * Sets Kafka Topic
         *
         * @param kafkaTopic Kafka Topic
         */
        public void setKafkaTopic(String kafkaTopic) {
            this.kafkaTopic = kafkaTopic;
        }

        /**
         * Gets Application ID
         *
         * @return Application ID
         */
        public String getApplicationId() {
            return applicationId;
        }

        /**
         * Sets Application ID
         *
         * @param applicationId Application ID
         */
        public void setApplicationId(String applicationId) {
            this.applicationId = applicationId;
        }

        /**
         * Gets Pushgateway IP
         *
         * @return Pushgateway IP
         */
        public String getPushGateway() {
            return pushGateway;
        }

        /**
         * Sets Pushgateway IP
         *
         * @param pushGateway Pushgateway IP
         */
        public void setPushGateway(String pushGateway) {
            this.pushGateway = pushGateway;
        }
    }
}
