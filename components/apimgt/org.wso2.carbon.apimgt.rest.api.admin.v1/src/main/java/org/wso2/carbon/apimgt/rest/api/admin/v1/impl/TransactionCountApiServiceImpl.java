package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.TransactionCountDAO;
import org.wso2.carbon.apimgt.impl.dto.TransactionCountDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class TransactionCountApiServiceImpl implements TransactionCountApiService {

    private static final Log log = LogFactory.getLog(TransactionCountApiServiceImpl.class);

    public Response transactionCountGet(String startTime, String endTime, MessageContext messageContext) {
        try {
            ZoneId zoneId = ZoneId.systemDefault();
            // Convert start and end times to the start and end of the respective days
            String startTimeTimestamp = Timestamp.from(
                    Instant.ofEpochSecond(Long.parseLong(startTime)).atZone(zoneId).toLocalDate().atStartOfDay(zoneId)
                            .toInstant()).toString();
            String endTimeTimestamp = Timestamp.from(
                    Instant.ofEpochSecond(Long.parseLong(endTime)).atZone(zoneId).toLocalDate().atTime(LocalTime.MAX)
                            .atZone(zoneId).toInstant()).toString();
            TransactionCountDAO transactionCountDAO = TransactionCountDAO.getInstance();
            TransactionCountDTO transactionCountDTO = transactionCountDAO.getTransactionCount(startTimeTimestamp,
                    endTimeTimestamp);

            return Response.ok().entity(transactionCountDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving transaction count.";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
