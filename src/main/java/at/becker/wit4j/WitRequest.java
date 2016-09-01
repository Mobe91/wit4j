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

package at.becker.wit4j;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 23.08.2016.
 */
public class WitRequest {
    private String sessionId;
    private JsonObject context;
    private String message;
    private JsonObject entities;

    public WitRequest(String sessionId, JsonObject context, String message, JsonObject entities) {
        this.sessionId = sessionId;
        if (context == null) {
            this.context = Json.createObjectBuilder().build();
        } else {
            this.context = context;
        }
        this.message = message;
        this.entities = entities;
    }

    public String getSessionId() {
        return sessionId;
    }

    public JsonObject getContext() {
        return context;
    }

    public String getMessage() {
        return message;
    }

    public JsonObject getEntities() {
        return entities;
    }
}
