/*
 * Copyright 2016 Moritz Becker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codelawine.wit4j;

import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 23.08.2016.
 */
public class Wit {

    public static final String SEND_ACTION = "send";

    private static final Logger LOG = Logger.getLogger(Wit.class.getName());

    private final WitConfiguration configuration;
    private final AuthenticationFilter authenticationFilter;

    public Wit(WitConfiguration configuration) {
        this.configuration = configuration;
        this.authenticationFilter = new AuthenticationFilter(configuration.getWitAccessToken());
    }

    /**
     * Invokes wit.ai's /message endpoint.
     * https://wit.ai/docs/http/20160526#get--message-link
     *
     * @param message the message string to be analyzed by wit.ai.
     * @return the JSON response of the invocation
     */
    public JsonObject message(String message) {
        return createClient().target(configuration.getWitUrl())
                .register(authenticationFilter)
                .path("message")
                .queryParam("q", message)
                .queryParam("v", configuration.getWitApiVersion())
                .register(WitLogger.class)
                .register(UTF8EnforcerInterceptor.class)
                .request(MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8"))
                .get(JsonObject.class);
    }

    /**
     * Invokes wit.ai's converse endpoint.
     * https://wit.ai/docs/http/20160526#post--converse-link
     *
     * @param sessionId session id to link to previous conversation
     * @param message current message from the user
     * @param context current context
     * @return the JSON response of the invocation
     */
    public JsonObject converse(String sessionId, String message, JsonObject context) {
        return converse0(sessionId, message)
                .post(Entity.json(context), JsonObject.class);
    }

    private Invocation.Builder converse0(String sessionId, String message) {
        return createClient().target(configuration.getWitUrl())
                .register(authenticationFilter)
                .path("converse")
                .queryParam("session_id", sessionId)
                .queryParam("q", message)
                .queryParam("v", configuration.getWitApiVersion())
                .register(WitLogger.class)
                .register(UTF8EnforcerInterceptor.class)
                .request(MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8"));
    }

    /**
     * Like {@link Wit#runActions(String, String, JsonObject, int)} but uses
     * the DEFAULT_MAX_STEPS constant as the maximum number of steps.
     *
     * @param sessionId session id to link to previous conversation
     * @param message current message from the user
     * @param context current context
     * @return new context
     */
    public JsonObject runActions(String sessionId, String message, JsonObject context) {
        return runActions(sessionId, message, context, configuration.getMaxSteps());
    }

    /**
     * Runs all bot actions required by the current user message.
     * Terminates after at most {@code maxSteps} steps.
     *
     * @param sessionId session id to link to previous conversation
     * @param message current message from the user
     * @param context current context
     * @param maxSteps max number of steps to perform
     * @return new context
     */
    public JsonObject runActions(String sessionId, String message, JsonObject context, int maxSteps) {
        return continueRunActions(sessionId, message, context, converse(sessionId, message, context), maxSteps);
    }

    private JsonObject continueRunActions(String sessionId, String message, JsonObject context, JsonObject converseResponse, int step) {
        if (step <= 0) {
            LOG.warning("Max steps reached, stopping.");
            return context;
        }
        String type = converseResponse.getString("type");
        WitRequest request = new WitRequest(sessionId, context, message, converseResponse.getJsonObject("entities"));
        if ("stop".equals(type)) {
            return context;
        } else if ("msg".equals(type)) {
            WitResponse response = new WitResponse(converseResponse.getString("msg"), converseResponse.getJsonObject("quickreplies"));
            Action send = configuration.getActions().get(SEND_ACTION);
            JsonObject ctx = send.perform(request, response);
            if (ctx != null) {
                throw new RuntimeException("Cannot context after '" + SEND_ACTION + "' action.");
            }
            return continueRunActions(sessionId, message, context, converse(sessionId, null, context), step - 1);
        } else if ("action".equals(type)) {
            if (converseResponse.isNull("action")) {
                return context;
            } else {
                Action action = configuration.getActions().get(converseResponse.getString("action"));
                if (action == null) {
                    throw new RuntimeException("Unknown action '" + converseResponse.getString("action") + "' requested.");
                }
                JsonObject nextContext = action.perform(request);
                return continueRunActions(sessionId, message, nextContext, converse(sessionId, null, nextContext), step - 1);
            }
        } else {
            throw new RuntimeException("Unknown response type '" + type + "'");
        }
    }

    private Client createClient() {
        if (configuration.getProvidedClientBuilder() == null) {
            return ClientBuilder.newClient();
        } else {
            return configuration.getProvidedClientBuilder().newClient();
        }
    }
}
