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

package org.apache.synapse.mediators.builtin;

import org.apache.synapse.mediators.AbstractTestCase;
import org.apache.synapse.config.xml.ValidateMediatorFactory;
import org.apache.synapse.config.xml.ValidateMediatorSerializer;

public class ValidateMediatorSerializationTest extends AbstractTestCase {

    private ValidateMediatorFactory validateMediatorFactory = null;
    private ValidateMediatorSerializer validateMediatorSerializer = null;


    public void testValidateMediatorSerialization() throws Exception {

        validateMediatorFactory = new ValidateMediatorFactory();
        validateMediatorSerializer = new ValidateMediatorSerializer();

        String validateConfiguration = "<syn:validate xmlns:syn=\"http://ws.apache.org/ns/synapse\" source=\"//regRequest\">" +
                "<syn:schema key=\"file:synapse_repository/conf/sample/validate.xsd\"/>" +
                "<syn:feature name=\"http://javax.xml.XMLConstants/feature/secure-processing\" value=\"true\"/>" +
                "<syn:on-fail>" +
                "<syn:drop/>" +
                "</syn:on-fail>" +
                "</syn:validate>";

        assertTrue(serialization(validateConfiguration, validateMediatorFactory, validateMediatorSerializer));
    }

    public void testValidateMediatorSerializationWithExternalResources() throws Exception {

        validateMediatorFactory = new ValidateMediatorFactory();
        validateMediatorSerializer = new ValidateMediatorSerializer();

        String validateConfiguration = "<validate xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "source=\"//regRequest\">" +
                "<schema key=\"file:synapse_repository/conf/sample/validate.xsd\" />" +
                "<resource location=\"resource2.xsd\" key=\"resource2_xsd\" />" +
                "<resource location=\"resource1.xsd\" key=\"resource1_xsd\" />" +
                "<feature name=\"http://javax.xml.XMLConstants/feature/secure-processing\" value=\"true\" />" +
                "<on-fail><drop /></on-fail>" +
                "</validate>";
        assertTrue(serialization(validateConfiguration, validateMediatorFactory, validateMediatorSerializer));
    }

}

