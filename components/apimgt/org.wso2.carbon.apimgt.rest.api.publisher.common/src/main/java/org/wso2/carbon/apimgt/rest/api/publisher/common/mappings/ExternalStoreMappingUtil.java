/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIExternalStoreDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIExternalStoreListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ExternalStoreDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ExternalStoreListDTO;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * This class is responsible for mapping APIStore objects into REST API External Store related DTOs
 */
public class ExternalStoreMappingUtil {

    private ExternalStoreMappingUtil() {
        throw new IllegalStateException("Utility class to map ExternalStore Objects");
    }

    /**
     * Converts APIStore object into a ExternalStoreDTO.
     *
     * @param apiStore APIStore object
     * @return ExternalStoreDTO object corresponding to APIStore object
     */
    public static ExternalStoreDTO fromExternalStoreToDTO(APIStore apiStore) {
        ExternalStoreDTO externalStoreDTO = new ExternalStoreDTO();
        externalStoreDTO.setDisplayName(apiStore.getDisplayName());
        externalStoreDTO.setId(apiStore.getName());
        externalStoreDTO.setType(apiStore.getType());
        externalStoreDTO.setEndpoint(apiStore.getEndpoint());
        return externalStoreDTO;
    }

    /**
     * Converts a list of APIStore objects into a ExternalStoreListDTO.
     *
     * @param externalStoreCollection a collection of APIStore objects
     * @return ExternalStoreListDTO object containing ExternalStoreDTOs
     */
    public static ExternalStoreListDTO fromExternalStoreCollectionToDTO(Collection<APIStore> externalStoreCollection) {
        ExternalStoreListDTO externalStoreListDTO = new ExternalStoreListDTO();
        List<ExternalStoreDTO> externalStoreDTOS = externalStoreListDTO.getList();
        if (externalStoreCollection == null) {
            externalStoreCollection = new HashSet<>();
        }
        for (APIStore externalStore : externalStoreCollection) {
            externalStoreDTOS.add(fromExternalStoreToDTO(externalStore));
        }
        externalStoreListDTO.setList(externalStoreDTOS);
        externalStoreListDTO.setCount(externalStoreDTOS.size());
        return externalStoreListDTO;
    }

    /**
     * Converts list of APIStore object into APIExternalStoreListDTO object.
     *
     * @param externalStoreCollection a collection of APIStore objects
     * @return APIExternalStoreListDTO object containing APIExternalStoreDTOs
     */
    public static APIExternalStoreListDTO fromAPIExternalStoreCollectionToDTO(
            Collection<APIStore> externalStoreCollection) {
        APIExternalStoreListDTO apiExternalStoreListDTO = new APIExternalStoreListDTO();
        List<APIExternalStoreDTO> apiExternalStoreDTOS = apiExternalStoreListDTO.getList();
        if (externalStoreCollection == null) {
            externalStoreCollection = new HashSet<>();
        }
        for (APIStore externalStore : externalStoreCollection) {
            apiExternalStoreDTOS.add(fromAPIExternalStoreToDTO(externalStore));
        }
        apiExternalStoreListDTO.setList(apiExternalStoreDTOS);
        apiExternalStoreListDTO.setCount(apiExternalStoreDTOS.size());
        return apiExternalStoreListDTO;
    }

    /**
     * Converts APIStore object to APIExternalStoreDTO object.
     *
     * @param apiStore API Store
     * @return APIExternalStoreDTO
     */
    public static APIExternalStoreDTO fromAPIExternalStoreToDTO(APIStore apiStore) {
        APIExternalStoreDTO apiExternalStoreDTO = new APIExternalStoreDTO();
        apiExternalStoreDTO.setId(apiStore.getName());
        if (apiStore.getLastUpdated() != null) {
            Date lastUpdateDate = apiStore.getLastUpdated();
            Timestamp timeStamp = new Timestamp(lastUpdateDate.getTime());
            apiExternalStoreDTO.setLastUpdatedTime(String.valueOf(timeStamp));
        }
        return apiExternalStoreDTO;
    }
}
