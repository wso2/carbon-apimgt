package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.TransactionCountDAO;
import org.wso2.carbon.apimgt.impl.dto.TransactionCountDTO;
import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import java.util.List;

import org.wso2.carbon.apimgt.internal.service.dto.TransactionRecordDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

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
