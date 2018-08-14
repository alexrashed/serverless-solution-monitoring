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

import org.json.JSONObject;

/**
 * Handles kafka events into JSON objects
 */
public class OpenwhiskEvent {

    private JSONObject event;

    /**
     * Constructor of OpenwhiskEvent
     *
     * @param event
     */
    public OpenwhiskEvent(JSONObject event) {
        this.event = event;
    }

    /**
     * Gets events as a JSON object
     *
     * @return JSON object
     */
    public JSONObject getEvent() {
        return event;
    }

    /**
     * Sets event
     **/
    public void setEvent(JSONObject event) {
        this.event = event;
    }
}
