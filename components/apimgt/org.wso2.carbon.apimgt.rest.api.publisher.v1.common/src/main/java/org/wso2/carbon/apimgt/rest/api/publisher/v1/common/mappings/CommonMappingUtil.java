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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PaginationDTO;

/**
 * The class that handles common mapping related functionality
 * 
 */
public class CommonMappingUtil {

    /**
     * Retreives a pagination DTO object given 
     * 
     * @param limit limit value
     * @param offset offset value (starting position)
     * @param total total number of resources available
     * @param next link to the next page
     * @param previous link to the previous page
     * @return a pagination DTO object given above properties 
     */
    public static PaginationDTO getPaginationDTO(int limit, int offset, int total, String next, String previous) {
        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setLimit(limit);
        paginationDTO.setOffset(offset);
        paginationDTO.setTotal(total);
        paginationDTO.setNext(next);
        paginationDTO.setPrevious(previous);
        return paginationDTO;
    }
}
