/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.internal.service.model;

import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTConsumerKeyDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTSubjectEntityDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal revocation data with the ownership information required by the internal REST service.
 */
public class RevokedJWTEventData {

    private final List<RevokedJWTData> revokedJWTList = new ArrayList<>();
    private final List<RevokedJWTSubjectEntityDTO> revokedJWTSubjectEntityList = new ArrayList<>();
    private final List<RevokedJWTConsumerKeyDTO> revokedJWTConsumerKeyList = new ArrayList<>();

    public List<RevokedJWTData> getRevokedJWTList() {

        return revokedJWTList;
    }

    public List<RevokedJWTSubjectEntityDTO> getRevokedJWTSubjectEntityList() {

        return revokedJWTSubjectEntityList;
    }

    public List<RevokedJWTConsumerKeyDTO> getRevokedJWTConsumerKeyList() {

        return revokedJWTConsumerKeyList;
    }

    /**
     * Internal direct JWT revocation record with its owning tenant ID.
     */
    public static class RevokedJWTData {

        private final String jwtSignature;
        private final Long expiryTime;
        private final int tenantId;

        public RevokedJWTData(String jwtSignature, Long expiryTime, int tenantId) {

            this.jwtSignature = jwtSignature;
            this.expiryTime = expiryTime;
            this.tenantId = tenantId;
        }

        public String getJwtSignature() {

            return jwtSignature;
        }

        public Long getExpiryTime() {

            return expiryTime;
        }

        public int getTenantId() {

            return tenantId;
        }
    }
}
