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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings;

import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ExternalStoreDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ExternalStoreListDTO;

import java.util.ArrayList;
import java.util.Collection;
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
        if (externalStoreDTOS == null) {
            externalStoreDTOS = new ArrayList<>();
        }
        for (APIStore externalStore : externalStoreCollection) {
            externalStoreDTOS.add(fromExternalStoreToDTO(externalStore));
        }
        externalStoreListDTO.setList(externalStoreDTOS);
        externalStoreListDTO.setCount(externalStoreDTOS.size());
        return externalStoreListDTO;
    }
}
