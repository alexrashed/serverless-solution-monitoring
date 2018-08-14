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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Test class to invoke actions and check status.
 * Variables like {@link #OPENWHISK_HOST} are hardcoded.
 * If required to test Openwhisk manually, feel free to change those values and
 * run this utility class.
 */
public class OpenwhiskInvoker {

    private static final Log log = LogFactory.getLog(OpenwhiskInvoker.class);

    private static final String TRIGGER_URL_FORMAT = "https://%s@%s/api/v1/namespaces/_/triggers/%s";
    private static final String INVOKE_ACTION_URL_FORMAT = "https://%s@%s/api/v1/namespaces/_/actions/%s";
    private static final String CREATE_ACTION_URL_FORMAT = "https://%s@%s/api/v1/namespaces/_/actions/%s?overwrite=true";
    private static final String OPENWHISK_HOST = "172.17.0.1:443";
    private static final String OPENWHISK_AUTH_STRING = "23bc46b1-71f6-4ed5-8c54-816aa4f8c502:123zO3xZCLrMN6v2BKK1dXYFpXlPkccOFqm12CdAsMgRU4VrNZ9lyGVCGuMDGIwP";

    private static final int TIME_INTERVAL = 30000;

    /**
     * Invokes triggers for actions
     *
     * @param trigger trigger
     */
    private static void invokeTrigger(CloseableHttpClient httpClient, String trigger)
            throws IOException {
        String endpoint = String.format(TRIGGER_URL_FORMAT,
                OPENWHISK_AUTH_STRING, OPENWHISK_HOST, trigger);

        log.info(String.format("Invoking %s -> %s", trigger, endpoint));
        HttpPost request = new HttpPost(endpoint);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (log.isDebugEnabled()) {
                log.debug("Response: " + response.toString());
            }
        } catch (ClientProtocolException e) {
            log.error("Protocol error", e);
        }
    }

    /**
     * Creates actions to invoke
     *
     * @param actionName name of the action
     * @param action     action
     * @return true if success, false otherwise
     */
    private static boolean createAction(CloseableHttpClient httpClient, String actionName, String action)
            throws IOException {
        String endpoint = String.format(CREATE_ACTION_URL_FORMAT,
                OPENWHISK_AUTH_STRING, OPENWHISK_HOST, actionName);

        log.info(String.format("Creating action %s -> %s", actionName, action));

        HttpPut request = new HttpPut(endpoint);
        request.setEntity(new StringEntity(action));
        request.setHeader("Content-type", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (log.isDebugEnabled()) {
                log.debug("Status: " + response.getStatusLine().getStatusCode());
                log.debug("Response: " + EntityUtils.toString(response.getEntity()));
            }
            if (response.getStatusLine().getStatusCode() == 200) {
                log.info("Created/updated action: " + actionName);
                return true;
            }
        } catch (ClientProtocolException e) {
            log.error("Protocol error", e);
        }
        return false;
    }

    /**
     * Invokes actions
     *
     * @param action action created
     */
    private static void invokeAction(CloseableHttpClient httpClient, String action) throws IOException {
        String endpoint = String.format(INVOKE_ACTION_URL_FORMAT,
                OPENWHISK_AUTH_STRING, OPENWHISK_HOST, action);

        log.info(String.format("Invoking action: %s -> %s", action, endpoint));
        HttpPost request = new HttpPost(endpoint);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (log.isDebugEnabled()) {
                log.debug("Response: " + response.toString());
            }
        } catch (ClientProtocolException e) {
            log.error("Protocol error", e);
        }
    }

    /**
     * Invokes actions per defined time interval
     */
    private static void start() {
        Random random = new Random();

        List<String> actions = new ArrayList<>(Actions.ACTIONS.keySet());
        try (CloseableHttpClient httpClient = getClient()) {
            while (true) {
                try {
                    Thread.sleep(TIME_INTERVAL);
                    // To execute actions continuously, uncomment the following 3 lines
                    // and comment the lines after the catch block.

                    // actions.forEach(action -> {
                    //    executorService.submit(() -> invokeAction(action));
                    // });
                } catch (InterruptedException e) {
                    log.warn("Interrupted: " + e.getMessage() + ". Exiting");
                    break;
                }
                String action = actions.get(random.nextInt(actions.size()));
                invokeAction(httpClient, action);
            }
        } catch (IOException e) {
            log.error("Error occurred when executing", e);
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            log.error("Error occurred when creating client", e);
        } catch (Exception e) {
            log.error("Error occurred when getting client", e);
        }
    }

    /**
     * Main method to start execution
     *
     * @param args Commandline arguments
     * @throws InterruptedException
     */
    public static void main(String[] args) throws Exception {
        try (CloseableHttpClient httpClient = getClient()) {
            Actions.ACTIONS.forEach((actionName, action) -> {
                try {
                    OpenwhiskInvoker.createAction(httpClient, actionName, action);
                } catch (IOException e) {
                    log.error("Failed to create action!", e);
                }
            });
        }
        start();
    }

    private static CloseableHttpClient getClient() throws Exception {
        return HttpClients.custom()
                .setSSLContext(new SSLContextBuilder()
                        .loadTrustMaterial(null, (certificate, authType) -> true)
                        .build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
    }
}
