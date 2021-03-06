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

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 23.08.2016.
 *
 * Interface for implementing custom actions.
 */
public interface Action {

    /**
     * The returned name refers to a custom action in wit.ai.
     * @return the name of the custom action
     */
    public String getName();

    /**
     * Performs the action.
     *
     * @param params
     * For a {@link Wit#SEND_ACTION}: WitRequest, WitResponse
     * For other actions: WitRequest
     * @return new context or null in case of {@link Wit#SEND_ACTION}
     */
    public JsonObject perform(Object... params);

}
