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

package org.apache.synapse.mediators.ext;

import org.apache.synapse.mediators.annotations.Namespaces;
import org.apache.synapse.mediators.annotations.ReadFromMessage;

@Namespaces({"soapenv:http://schemas.xmlsoap.org/soap/envelope/"})
public class AnnotatedCommand2 {

    static String fieldResult;
    static String methodResult;
    
    @Namespaces({"m:http://services.samples/xsd"})
    @ReadFromMessage("/soapenv:Envelope/soapenv:Body/m:getQuote/m:request/m:symbol")
    String beforeField;

    @Namespaces({"m:http://services.samples/xsd"})
    @ReadFromMessage("/soapenv:Envelope/soapenv:Body/m:getQuote/m:request/m:symbol")
    public void setSymbol(String s) {
        methodResult = s;
    }
    
    public void execute() {
        fieldResult = beforeField.toString();    
    }

}
