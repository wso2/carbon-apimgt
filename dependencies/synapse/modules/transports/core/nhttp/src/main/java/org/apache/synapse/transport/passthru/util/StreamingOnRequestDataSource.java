/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.synapse.transport.passthru.util;

import org.apache.axiom.attachments.ByteArrayDataSource;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class has a special flag, which says this is it's last use and not to cache data
 * if it did not have done so already.
 *
 * @author Srinath Perera (srinath@wso2.com)
 */

public class StreamingOnRequestDataSource implements DataSource {
    private InputStream in;

    public StreamingOnRequestDataSource(InputStream in) {
        super();
        this.in = in;
    }

    private boolean lastUse = false;

    private ByteArrayDataSource cachedData = null;

    public String getContentType() {
        return "application/octet-stream";
    }

    public InputStream getInputStream() throws IOException {
        if (cachedData != null) {
            return cachedData.getInputStream();
        } else {
            if (lastUse && in != null) {
                InputStream returnStram = in;
                this.in = null;
                return returnStram;
            } else if (in != null) {
                byte[] data = BinaryRelayBuilder.readAllFromInputSteam(in);
                cachedData = new ByteArrayDataSource(data);
                return cachedData.getInputStream();
            } else {
                throw new IOException("Input stream has being already consumed");
            }
        }
    }

    public String getName() {
        return "StreamingOnRequestDataSource";
    }

    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * This flag says that this is the last use of the stream, hence do not
     * need to cache it if it has not
     * already cached.
     *
     * @param lastUse
     */
    public void setLastUse(boolean lastUse) {
        this.lastUse = lastUse;
	}
}
