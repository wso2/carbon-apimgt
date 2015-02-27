/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.transport.vfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import de.schlichtherle.io.FileInputStream;

public class VFSTestUtils {
    private VFSTestUtils() {}
    
    public static boolean waitForFileDeletion(File file, int timeout) throws InterruptedException {
        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() < time + timeout) {
            if (!file.exists()) {
                return true;
            }
            Thread.sleep(100);
        }
        return false;
    }
    
    public static byte[] readFile(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            return IOUtils.toByteArray(in);
        } finally {
            in.close();
        }
    }
    
    public static byte[] waitForFile(File file, int timeout) throws IOException, InterruptedException {
        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() < time + timeout) {
            if (file.exists()) {
                return readFile(file);
            }
            Thread.sleep(100);
        }
        return null;
    }
}
