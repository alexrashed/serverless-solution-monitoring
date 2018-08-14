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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains sample actions for different kinds
 */
public class Actions {

    public static final Map<String, String> ACTIONS = new HashMap<>();

    static {
        ACTIONS.put("helloPhp", getPhpAction());
        ACTIONS.put("helloNode", getNodeJSAction());
        ACTIONS.put("helloFailingNode", getFailingNodeJSAction());
    }

    /**
     * Generates a sample PHP action
     *
     * @return action
     */
    public static String getPhpAction() {
        String code = "<?php\n" +
                "function main(array $args) : array {" +
                "    $name = $args[\"name\"] ?? \"stranger\";\n" +
                "    $greeting = \"Hello $name!\";\n" +
                "    echo $greeting;\n" +
                "    return [\"greeting\" => $greeting];\n" +
                "}";

        JSONObject action = new JSONObject();
        action.put("version", "1.0.0");
        action.put("publish", true);

        JSONObject exec = new JSONObject();
        exec.put("kind", "php:7.1");
        exec.put("code", code);
        exec.put("image", "");
        exec.put("init", "");
        action.put("exec", exec);

        action.put("annotations", new JSONArray());
        action.put("parameters", new JSONArray());

        JSONObject limits = new JSONObject();
        limits.put("timeout", 10000);
        limits.put("memory", 256);
        action.put("limits", limits);

        return action.toString();
    }

    /**
     * Generates a sample NodeJS action
     *
     * @return action
     */
    public static String getNodeJSAction() {
        String code = "function main() {\n" +
                "\n" +
                "    console.log('Hello World');\n" +
                "\n" +
                "    return {msg: 'Hello World'};\n" +
                "\n" +
                "}";

        JSONObject action = new JSONObject();
        action.put("version", "1.0.0");
        action.put("publish", true);

        JSONObject exec = new JSONObject();
        exec.put("kind", "nodejs:6");
        exec.put("code", code);
        exec.put("image", "");
        exec.put("init", "");
        action.put("exec", exec);

        action.put("annotations", new JSONArray());
        action.put("parameters", new JSONArray());

        JSONObject limits = new JSONObject();
        limits.put("timeout", 10000);
        limits.put("memory", 256);
        action.put("limits", limits);

        return action.toString();
    }

    /**
     * Generates a sample NodeJS action which throws an error
     *
     * @return action
     */
    public static String getFailingNodeJSAction() {
        String code = "function main() {\n" +
                "\n" +
                "    throw new Error('listId does not exist');\n" +
                "\n" +
                "    return {msg: 'Hello World'};\n" +
                "\n" +
                "}";

        JSONObject action = new JSONObject();
        action.put("version", "1.0.0");
        action.put("publish", true);

        JSONObject exec = new JSONObject();
        exec.put("kind", "nodejs:6");
        exec.put("code", code);
        exec.put("image", "");
        exec.put("init", "");
        action.put("exec", exec);

        action.put("annotations", new JSONArray());
        action.put("parameters", new JSONArray());

        JSONObject limits = new JSONObject();
        limits.put("timeout", 10000);
        limits.put("memory", 256);
        action.put("limits", limits);

        return action.toString();
    }
}
