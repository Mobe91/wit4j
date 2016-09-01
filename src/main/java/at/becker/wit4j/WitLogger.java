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

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 26.08.2016.
 *
 * Logs wit.ai request/responses
 */
public class WitLogger implements ClientResponseFilter {

    private static final Logger LOG = Logger.getLogger(WitLogger.class.getName());

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("\nRequest:\n");
            sb.append(requestContext.getUri().toString());

            InputStream is = responseContext.getEntityStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int _byte;
            while ((_byte = is.read()) != -1) {
                bos.write(_byte);
            }
            byte[] rawEntity = bos.toByteArray();
            responseContext.setEntityStream(new ByteArrayInputStream(rawEntity));
            sb.append("\nResponse:\n");
            sb.append(requestContext.getHeaders().toString() + "\n" + new String(rawEntity, Charset.forName("UTF-8")));
            LOG.fine(sb.toString());
        }
    }
}
