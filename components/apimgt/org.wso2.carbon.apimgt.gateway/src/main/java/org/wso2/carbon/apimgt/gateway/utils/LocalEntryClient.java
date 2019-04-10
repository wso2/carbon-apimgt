/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.utils;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.localentry.stub.types.LocalEntryAdminException;
import org.wso2.carbon.localentry.stub.types.LocalEntryAdminServiceStub;

import java.rmi.RemoteException;

/**
 * This LocalEntryClient class for operating the synapse localEntries
 */
public class LocalEntryClient {

    private LocalEntryAdminServiceStub localEntryAdminServiceStub;

    static final String backendURLl = "local:///services/";

    public LocalEntryClient() throws AxisFault {
        localEntryAdminServiceStub = new LocalEntryAdminServiceStub(null,
                backendURLl + "LocalEntryAdmin");
    }

    /**
     * Add Local entry
     *
     * @param content Swagger Content
     * @return Status of the add operation
     * @throws AxisFault
     */
    public Boolean addLocalEntry(String content) throws AxisFault {
        Boolean value;
        try {
            value = localEntryAdminServiceStub.addEntry(content);
        } catch (RemoteException e) {
            throw new AxisFault("Error occurred while generating the response ", e);
        } catch (LocalEntryAdminException e) {
            throw new AxisFault("Error occurred while adding the local entry", e);
        }
        return value;
    }

    /**
     * Get Local entry for given API
     *
     * @param key API Id to be retrived
     * @return LocalEntry for the given API
     * @throws AxisFault
     */
    public Object getEntry(String key) throws AxisFault {
        Object object;
        try {
            object = localEntryAdminServiceStub.getEntry(key);
        } catch (RemoteException e) {
            throw new AxisFault("Error occurred while retrieving the local entry", e);
        } catch (LocalEntryAdminException e) {
            throw new AxisFault("Error occurred while create the admin client", e);
        }
        return object;
    }

    /**
     * Delete the local entry
     *
     * @param key APT Id to be deleted
     * @return Stataus of the delete operation
     * @throws AxisFault
     */
    public boolean deleteEntry(String key) throws AxisFault {
        try {
            return localEntryAdminServiceStub.deleteEntry(key);
        } catch (RemoteException e) {
            throw new AxisFault("Error occurred while create the admin client", e);
        } catch (LocalEntryAdminException e) {
            throw new AxisFault("Error occurred while deleting the local entry", e);
        }
    }
}