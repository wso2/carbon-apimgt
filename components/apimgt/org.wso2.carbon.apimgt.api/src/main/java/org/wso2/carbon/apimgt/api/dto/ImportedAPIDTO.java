/*
 *  Copyright (c) 2024, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.dto;

import org.wso2.carbon.apimgt.api.model.API;

/**
 * This class represents the data that represents an imported API
 */
public class ImportedAPIDTO {

    /**
     * API object
     */
    private API api;
    /**
     * Revision of the API
     */
    private String revision;

    public API getApi() {

        return api;
    }

    public void setApi(API api) {

        this.api = api;
    }

    public String getRevision() {

        return revision;
    }

    public void setRevision(String revision) {

        this.revision = revision;
    }

    public ImportedAPIDTO(API api, String revision) {

        this.api = api;
        this.revision = revision;
    }
}
