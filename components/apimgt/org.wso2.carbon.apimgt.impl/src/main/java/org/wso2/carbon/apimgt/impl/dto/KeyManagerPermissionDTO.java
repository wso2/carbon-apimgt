/*
 *  Copyright (c) 2005-2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.dto;

import java.io.Serializable;

public class KeyManagerPermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int keyManagerPermissionID;
    private String keyManagerUUID;
    private String permissionType;
    private String role;

    public int getKeyManagerPermissionID () {
        return keyManagerPermissionID;
    }

    public void setKeyManagerPermissionID (int keyManagerPermissionID) {
        this.keyManagerPermissionID = keyManagerPermissionID;
    }

    public String getKeyManagerUUID () {
        return keyManagerUUID;
    }

    public void setKeyManagerUUID (String keyManagerUUID) {
        this.keyManagerUUID = keyManagerUUID;
    }

    public String getPermissionType () {
        return permissionType;
    }

    public void setPermissionType (String permissionType) {
        this.permissionType = permissionType;
    }

    public String getRole () {
        return role;
    }

    public void setRole (String role) {
        this.role = role;
    }
}
