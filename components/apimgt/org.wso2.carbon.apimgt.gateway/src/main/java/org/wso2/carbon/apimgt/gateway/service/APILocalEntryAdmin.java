/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.gateway.service;


import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.utils.LocalEntryClient;

public class APILocalEntryAdmin extends org.wso2.carbon.core.AbstractAdmin {

    private static Log log = LogFactory.getLog(APILocalEntryAdmin.class);


    /**
     * Add Local Entry to the gateway.
     * @param content
     * @return
     * @throws AxisFault
     */
    public boolean addLocalEntry(String content) throws AxisFault {
        LocalEntryClient localEntryClient = getLocalEntryAdminClient();
        return  localEntryClient.addLocalEntry(content);
    }

    /**
     * Get the Local entry client.
     * @return
     * @throws AxisFault
     */
    protected LocalEntryClient getLocalEntryAdminClient() throws AxisFault {
        return  new LocalEntryClient();
    }

    /**
     * Get the Local entry.
     * @param key key of the existing local entry.
     * @return
     * @throws AxisFault
     */
    public Object getEntry(String key) throws AxisFault {
        LocalEntryClient localEntryAdminClient = getLocalEntryAdminClient();
        return localEntryAdminClient.getEntry(key);
    }

    /**
     *  Delete the local entry.
     * @param key Key of the local entry to be deleted.
     * @return
     * @throws AxisFault
     */
    public Boolean deleteLocalEntry(String key) throws AxisFault {
        LocalEntryClient localEntryAdminClient = getLocalEntryAdminClient();
        return localEntryAdminClient.deleteEntry(key);
    }
}