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
package org.wso2.carbon.apimgt.rest.api.util.interceptors.response;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.HistoryEvent;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.interceptors.request.RequestInHistoryInterceptor;

public class ResponseOutHistoryInterceptor extends AbstractPhaseInterceptor {

    private static final Log log = LogFactory.getLog(RequestInHistoryInterceptor.class);

    public ResponseOutHistoryInterceptor() {

        super(Phase.SEND);
    }

    @Override
    public void handleMessage(Message message) throws Fault {

        if (message.get(Message.RESPONSE_CODE) != null
                && ((Integer) message.get(Message.RESPONSE_CODE) == 200
                || (Integer) message.get(Message.RESPONSE_CODE) == 201)) {
            HistoryEvent historyEvent = (HistoryEvent) message.getExchange().get(RestApiConstants.HISTORY_EVENT);
            if (historyEvent != null) {
                try {

                    if (historyEvent.getApiId() == null) {
                        String apiId = (String) message.getExchange().getInMessage().get(RestApiConstants.PARAM_API_ID);
                        if (StringUtils.isNotBlank(apiId)) {
                            historyEvent.setApiId(apiId);
                        } else {
                            log.error("API Id not found to record history event for :" + historyEvent.getOperationId()
                                    + StringUtils.SPACE + historyEvent.getDescription());
                            return;
                        }
                    }
//                    String revisionKey =
//                            (String) message.getExchange().getInMessage().get(RestApiConstants.PARAM_REVISION_KEY);
//                    if (StringUtils.isNotBlank(revisionKey)) {
//                        historyEvent.setRevisionKey(revisionKey);
//                    }
                    ApiMgtDAO.getInstance().addHistoryEvent(historyEvent);
                } catch (APIManagementException e) {
                    log.error("Error while recoding history event for: " + historyEvent.getOperationId()
                            + StringUtils.SPACE + historyEvent.getDescription());
                }
            }
        }
    }
}

