/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.persistence;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.persistence.utils.RegistryLCManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.xml.sax.SAXException;

public class LCManagerFactory {

    private static final Log log = LogFactory.getLog(LCManagerFactory.class);
    private static Map<Integer, RegistryLCManager> managers = new HashMap<Integer, RegistryLCManager>();
    public static RegistryLCManager getLCManager(int tenantId) {
        if(!managers.containsKey(tenantId)) {
            try {
                RegistryLCManager lcManager = new RegistryLCManager(tenantId);
                managers.put(tenantId, lcManager);
            } catch (RegistryException | XMLStreamException | ParserConfigurationException | SAXException
                    | IOException e) {
                log.error("Error while retrieving lifecycle manager ", e);
            }
        } 
        return managers.get(tenantId);
    }
}
