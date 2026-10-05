/*
 *   Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com)
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.api;

import org.apache.commons.fileupload.util.LimitedInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@code SizeLimitedInputStream} is a wrapper around an {@link InputStream} that enforces a maximum
 * number of bytes that can be read. When the configured limit (in bytes) is exceeded, the overridden
 * {@link #raiseError(long, long)} method throws a {@link FileSizeLimitExceededException}.
 */
public class SizeLimitedInputStream extends LimitedInputStream {
    private final long maxSize;

    public SizeLimitedInputStream(InputStream in, long maxSize) {
        super(in, maxSize);
        this.maxSize = maxSize;
    }

    @Override
    protected void raiseError(long l, long l1) throws IOException {
        throw new FileSizeLimitExceededException("File size exceeds maximum allowed limit of " + maxSize + " bytes");
    }
}
