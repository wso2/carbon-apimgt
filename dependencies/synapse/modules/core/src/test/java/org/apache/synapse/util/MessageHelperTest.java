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

package org.apache.synapse.util;

import javax.activation.DataHandler;

import junit.framework.TestCase;
import org.apache.axis2.context.MessageContext;

/**
 * 
 */
public class MessageHelperTest extends TestCase {

    public void testClonePartially() throws Exception {
        String key = "propKey";
        MessageContext origMc = new MessageContext();
        origMc.setProperty(key, "propValue");
        MessageContext newMc = MessageHelper.clonePartially(origMc);
        Object result = newMc.getProperty(key);
        assertEquals(result, "propValue");
    }
    
    // Regression test for SYNAPSE-309
    public void testClonePartiallyWithAttachments() throws Exception {
        MessageContext origMc = new MessageContext();
        String contentId = origMc.addAttachment(new DataHandler("test", "text/html"));
        MessageContext newMc = MessageHelper.clonePartially(origMc);
        DataHandler dh = newMc.getAttachment(contentId);
        assertNotNull(dh);
        assertEquals("test", dh.getContent());
    }
}
