/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.TransactionCountDAO;
import org.wso2.carbon.apimgt.impl.dto.TransactionCountDTO;
import org.wso2.carbon.apimgt.internal.service.TransactionRecordsApiService;

import org.apache.cxf.jaxrs.ext.MessageContext;

import java.util.List;

import org.wso2.carbon.apimgt.internal.service.dto.TransactionRecordDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class TransactionRecordsApiServiceImpl implements TransactionRecordsApiService {

    private static final Log log = LogFactory.getLog(TransactionRecordsApiServiceImpl.class);

    public Response insertTransactionRecords(List<TransactionRecordDTO> body, MessageContext messageContext) {
        TransactionCountDAO transactionCountDAO = TransactionCountDAO.getInstance();
        try {
            TransactionCountDTO[] transactionCountDTOArray = body.stream().map(recordDTO -> {
                TransactionCountDTO transactionCountDTO = new TransactionCountDTO();
                transactionCountDTO.setId(recordDTO.getId());
                transactionCountDTO.setHost(recordDTO.getHost());
                transactionCountDTO.setServerID(recordDTO.getServerID());
                transactionCountDTO.setServerType(recordDTO.getServerType());
                transactionCountDTO.setCount(recordDTO.getCount());
                transactionCountDTO.setRecordedTime(recordDTO.getRecordedTime());
                return transactionCountDTO;
            }).toArray(TransactionCountDTO[]::new);

            Boolean result = transactionCountDAO.insertTransactionRecords(transactionCountDTOArray);
            return Response.ok().entity(result).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while inserting transaction records.";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
