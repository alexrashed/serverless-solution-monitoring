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

/**
 * Contains Constant variable values
 */
public class Constants {

    public static final String ACTIVATION_DURATION_COUNTER = "activation_duration_counter";
    public static final String NAMESPACE = "namespace";
    public static final String SOURCE = "source";
    public static final String USER_ID = "userId";
    public static final String ACTION = "action";
    public static final String STATUS_CODE = "statusCode";
    public static final String KIND = "kind";

    public static final String ACTIVATIONS_TOTAL_COUNTER = "activations_total_counter";

    public static final String EVENT_TYPE_ACTIVATION = "Activation";
    public static final String EVENT_TYPE_METRIC = "Metric";

    public static final String EVENT_TYPE = "eventType";

    public static final String JSON_BODY = "body";
    public static final String ACTION_NAME = "name";
    public static final String ACTION_KIND = "kind";
    public static final String DURATION = "duration";
    public static final String OPENWHISK = "openwhisk";

    public static final String DEFAULT_KAFKA_TOPIC = "events";
    public static final String DEFAULT_APPLICATION_ID = "openwhisk-stats-collector";

    public static final String CMD_OPTION_NAME_KAFKA = "-kafka";
    public static final String CMD_OPTION_NAME_TOPIC = "-topic";
    public static final String CMD_OPTION_NAME_APP = "-app";
    public static final String CMD_OPTION_NAME_PUSHGATEWAY = "-pushGateway";

    public static final int HTTP_SERVER_PORT = 8080;
}
