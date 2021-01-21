/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.util.interceptors.request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.jaxrs.impl.UriInfoImpl;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.model.Parameter;
import org.apache.cxf.jaxrs.model.ParameterType;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.api.model.HistoryEvent;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.sql.Timestamp;
import java.util.List;

import javax.ws.rs.core.UriInfo;

public class RequestInHistoryInterceptor extends AbstractPhaseInterceptor {

    private static final Log log = LogFactory.getLog(RequestInHistoryInterceptor.class);

    public RequestInHistoryInterceptor() {

        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {

        String basePath = (String) message.get(Message.BASE_PATH);
        String resourcePath = ((String) message.get(Message.REQUEST_URI)).substring(basePath.length() - 1);
        String verb = (String) message.get(Message.HTTP_REQUEST_METHOD);
        boolean recordHistory = message.getExchange().get(APIConstants.SWAGGER_X_RECORD_HISTORY) != null &&
                (boolean) message.getExchange().get(APIConstants.SWAGGER_X_RECORD_HISTORY);
        boolean recordPayload = message.getExchange().get(APIConstants.SWAGGER_X_RECORD_HISTORY_PAYLOAD) != null &&
                (boolean) message.getExchange().get(APIConstants.SWAGGER_X_RECORD_HISTORY_PAYLOAD);
        String operationId = (String) message.getExchange().get(APIConstants.SWAGGER_OPERATION_ID);
        String loggedInUser = (String) message.getExchange().get(RestApiConstants.LOGGED_IN_USERNAME);

        if (recordHistory) {
            UriInfo uriInfo = new UriInfoImpl(message);
            MetadataMap<String, String> metadataMap = (MetadataMap<String, String>) uriInfo.getPathParameters();
            String apiId = metadataMap.getFirst(RestApiConstants.PARAM_API_ID);
            String queryParamString = StringUtils.EMPTY;

            MetadataMap<String, String> queryDataMap = (MetadataMap<String, String>) uriInfo.getQueryParameters();
            List<Parameter> parameterList =
                    message.getExchange().get(OperationResourceInfo.class).getParameters();
            if (queryDataMap != null && !queryDataMap.isEmpty() && parameterList != null
                    && !parameterList.isEmpty()) {
                StringBuilder queryParamBuilder = new StringBuilder();
                boolean isFirst = true;
                for (Parameter param : parameterList) {
                    if (param.getType() == ParameterType.QUERY) {
                        String paramName = param.getName();
                        if (queryDataMap.containsKey(paramName)) {
                            if (isFirst) {
                                queryParamBuilder.append("?");
                                isFirst = false;
                            } else {
                                queryParamBuilder.append("&");
                            }
                            queryParamBuilder.append(paramName).append("=").append(queryDataMap.get(paramName));
                        }
                    }
                }
                queryParamString = queryParamBuilder.toString();
            }

            //Create a HistoryEvent object and assign values
            HistoryEvent historyEvent = new HistoryEvent();
            historyEvent.setApiId(apiId);
            historyEvent.setUser(loggedInUser);
            historyEvent.setDescription(verb + StringUtils.SPACE + resourcePath + queryParamString);
            historyEvent.setOperationId(operationId);
            historyEvent.setCreatedTime(new Timestamp(System.currentTimeMillis()));
            if (recordPayload) {
                if (log.isDebugEnabled()) {
                    log.debug("Recording payload for: " + StringUtils.SPACE
                            + verb + StringUtils.SPACE + resourcePath);
                }
                MessageContentsList messageContentsList = MessageContentsList.getContentsList(message);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                StringBuilder payloadBuilder = new StringBuilder();
                for (Object messageContent : messageContentsList) {
                    if (messageContent != null) {
                        String messageContentObjType = messageContent.getClass().getSimpleName();
                        if (RestApiConstants.DTO_CLASSES_TO_RECORD_HISTORY.contains(messageContentObjType)) {
                            payloadBuilder.append(gson.toJson(messageContent));
                        }
                    }
                }
                String payload = payloadBuilder.toString();
                payload.replaceAll("\"password\": \".*\"", "\"password\": \"*****\"")
                        .replaceAll("\"clientSecret\": \".*\"", "\"clientSecret\": \"*****\"")
                        .replaceAll("\"amznAccessKey\": \".*\"", "\"amznAccessKey\": \"*****\"")
                        .replaceAll("\"amznSecretKey\": \".*\"", "\"amznSecretKey\": \"*****\"");
                historyEvent.setPayload(payload.getBytes());
            }
            message.getExchange().put(RestApiConstants.HISTORY_EVENT, historyEvent);
        }
    }
}