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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.json.JSONObject;
import org.wso2.serverless.stats.listeners.OpenwhiskEventListener;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.wso2.serverless.stats.Constants.DEFAULT_APPLICATION_ID;
import static org.wso2.serverless.stats.Constants.DEFAULT_KAFKA_TOPIC;

/**
 * Collects events from Kafka topic {@link #kafkaTopic}
 */
public class OpenwhiskEventCollector {

    private static final Log log = LogFactory.getLog(OpenwhiskEventCollector.class);

    private String applicationId;
    private String kafkaTopic;
    private String kafkaServerIp;

    private Properties config;
    private KafkaStreams streams;
    private Set<OpenwhiskEventListener> listeners = Collections.synchronizedSet(new HashSet<>());

    /**
     * OpenwhiskEventCollector subscribes to a Kafka topic
     *
     * @param kafkaServerIp IP of Kafka Server
     */
    public OpenwhiskEventCollector(String kafkaServerIp) {
        this(DEFAULT_APPLICATION_ID, DEFAULT_KAFKA_TOPIC, kafkaServerIp);
    }

    /**
     * OpenwhiskEventCollector subscribes to a Kafka topic
     *
     * @param applicationId Application ID name
     * @param kafkaTopic    Kafka Topic
     * @param kafkaServerIp IP of Kafka Server
     */
    public OpenwhiskEventCollector(String applicationId, String kafkaTopic, String kafkaServerIp) {
        if (applicationId == null) {
            throw new IllegalArgumentException("Application ID is required");
        }

        if (kafkaTopic == null) {
            throw new IllegalArgumentException("Kafka topic is required");
        }

        if (kafkaServerIp == null) {
            throw new IllegalArgumentException("Kafka Server IP is required");
        }

        this.applicationId = applicationId;
        this.kafkaTopic = kafkaTopic;
        this.kafkaServerIp = kafkaServerIp;

        config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerIp);
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    }

    /**
     * Starts collecting and processing events
     */
    public void start() {
        KStreamBuilder builder = new KStreamBuilder();
        builder.stream(kafkaTopic)
                .foreach((key, val) -> {
                    if(log.isDebugEnabled()){
                        log.debug(String.format("Received message %s -> %s", key, val));
                    }
                    process(val.toString());
                });

        streams = new KafkaStreams(builder, config);
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        streams.start();
    }

    /**
     * Process Kafka events and sends to listeners
     *
     * @param val Kafka event values
     */
    private void process(String val) {
        OpenwhiskEvent event = new OpenwhiskEvent(new JSONObject(val));
        listeners.forEach(listener -> listener.onEvent(event));
    }

    /**
     * Adds listeners
     *
     * @param listener listener
     */
    public void addListener(OpenwhiskEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes listeners
     *
     * @param listener listener
     */
    public void removeListener(OpenwhiskEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Stops streaming
     */
    public void stop() {
        streams.close();
    }
}
