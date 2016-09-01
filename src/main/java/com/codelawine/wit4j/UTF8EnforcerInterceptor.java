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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import java.io.IOException;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 01.09.2016.
 *
 * Explicitely appends UTF-8 charset to the content-type as workaround for RESTEASY-1482
 */
public class UTF8EnforcerInterceptor implements ReaderInterceptor {

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        if (MediaType.APPLICATION_JSON_TYPE.equals(context.getMediaType())) {
            context.setMediaType(context.getMediaType().withCharset("utf-8"));
        }
        return context.proceed();
    }

}
