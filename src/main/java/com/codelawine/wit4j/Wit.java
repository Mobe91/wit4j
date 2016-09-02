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
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 23.08.2016.
 */
public class Wit {

    public static final String SEND_ACTION = "send";

    private static final String DEFAULT_URL = "https://api.wit.ai";
    private static final Integer DEFAULT_MAX_STEPS = 5;

    private static final Logger LOG = Logger.getLogger(Wit.class.getName());

    private final String witUrl;
    private final String witApiVersion;
    private final AuthenticationFilter authenticationFilter;
    private final Map<String, Action> actions;

    public Wit(String witAccessToken, String witUrl, String witApiVersion, Set<Action> actions) {
        this.authenticationFilter = new AuthenticationFilter(witAccessToken);
        this.witUrl = witUrl;
        this.witApiVersion = witApiVersion;
        this.actions = actions.stream().collect(Collectors.toMap(action -> action.getName(), Function.identity()));
        validateActions(this.actions);
    }

    public Wit(String witAccessToken, String witApiVersion, Set<Action> actions) {
        this(witAccessToken, DEFAULT_URL, witApiVersion, actions);
    }

    /**
     * Invokes wit.ai's /message endpoint.
     * https://wit.ai/docs/http/20160526#get--message-link
     *
     * @param message the message string to be analyzed by wit.ai.
     * @return the JSON response of the invocation
     */
    public JsonObject message(String message) {
        return ClientBuilder.newClient().target(witUrl)
                .register(authenticationFilter)
                .path("message")
                .queryParam("q", message)
                .queryParam("v", witApiVersion)
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
        return ClientBuilder.newClient().target(witUrl)
                .register(authenticationFilter)
                .path("converse")
                .queryParam("session_id", sessionId)
                .queryParam("q", message)
                .queryParam("v", witApiVersion)
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
        return runActions(sessionId, message, context, DEFAULT_MAX_STEPS);
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
            Action send = actions.get(SEND_ACTION);
            JsonObject ctx = send.perform(request, response);
            if (ctx != null) {
                throw new RuntimeException("Cannot context after '" + SEND_ACTION + "' action.");
            }
            return continueRunActions(sessionId, message, context, converse(sessionId, null, context), step - 1);
        } else if ("action".equals(type)) {
            if (converseResponse.isNull("action")) {
                return context;
            } else {
                Action action = actions.get(converseResponse.getString("action"));
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

    private static void validateActions(Map<String, Action> actions) {
        if (!actions.containsKey(SEND_ACTION)) {
            throw new RuntimeException("The '" + SEND_ACTION + "' action is missing.");
        }

        for (String actionName : actions.keySet()) {
            if ("say".equals(actionName) ||
                    "merge".equals(actionName) ||
                    "error".equals(actionName)) {
                throw new RuntimeException("The '" + actionName + "' action has been deprecated.");
            }
        }
    }
}
