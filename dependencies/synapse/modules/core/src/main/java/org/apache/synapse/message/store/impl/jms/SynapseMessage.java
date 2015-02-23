/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.synapse.message.store.impl.jms;

import org.apache.synapse.SynapseConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class serves as a container for the Synapse Message Context parameters/properties
 * , and it will be saved as a JMS message in the JMS Store.
 */
public class SynapseMessage implements Serializable {
    private ArrayList<String> localEntries = new ArrayList<String>();

    private HashMap<String, String> properties = new HashMap<String, String>();

    private HashMap<String, byte[]> propertyObjects = new HashMap<String, byte[]>();

    private boolean response = false;

    private boolean faultResponse = false;

    private int tracingState = SynapseConstants.TRACING_UNSET;

    public boolean isResponse() {
        return response;
    }

    public void setResponse(boolean response) {
        this.response = response;
    }

    public boolean isFaultResponse() {
        return faultResponse;
    }

    public void setFaultResponse(boolean faultResponse) {
        this.faultResponse = faultResponse;
    }

    public int getTracingState() {
        return tracingState;
    }

    public void setTracingState(int tracingState) {
        this.tracingState = tracingState;
    }

    public List<String> getLocalEntries() {
        return localEntries;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public HashMap<String, byte[]> getPropertyObjects() {
        return propertyObjects;
    }

    public void addProperty(String key,String value) {
        properties.put(key,value);
    }

    public void addPropertyObject(String key , byte[] value){
        propertyObjects.put(key, value);
    }

    public void addLocalEntry(String key) {
        localEntries.add(key);
    }
}
