/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.rest.api.util.interceptors.eTag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.ETagGenerator;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil.checkETagSkipList;

public class ETagInInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final Log log = LogFactory.getLog(ETagInInterceptor.class);

    public ETagInInterceptor() {
        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {

        if (checkETagSkipList(message.get(Message.PATH_INFO).toString(),
                message.get(Message.HTTP_REQUEST_METHOD).toString())) {
            if (log.isDebugEnabled()){
                log.debug("Skipping ETagInInterceptor for URI : " + message.get(Message.PATH_INFO).toString());
            }
            return;
        }

        OperationResourceInfo operationResource = message.getExchange().get(OperationResourceInfo.class);
        Map<String, List<String>> headers = CastUtils.cast((Map) message.get(Message.PROTOCOL_HEADERS));
        List<Object> arguments = MessageContentsList.getContentsList(message);
        try {
            Class aClass = Class.forName(operationResource.getMethodToInvoke().getDeclaringClass().getName());
            Method aClassMethod = aClass.getMethod(operationResource.getMethodToInvoke().getName() +
                    RestApiConstants.GET_LAST_UPDATED, operationResource.getMethodToInvoke().getParameterTypes());
            Object o = aClass.newInstance();
            String lastUpdatedTime = String.valueOf(aClassMethod.invoke(o, arguments.toArray()));
            if (message.get(Message.HTTP_REQUEST_METHOD).equals(RestApiConstants.GET)) {
                if (!Objects.equals(lastUpdatedTime, "null")) {
                    String eTag = ETagGenerator.getETag(lastUpdatedTime);
                    if (headers.containsKey(HttpHeaders.IF_NONE_MATCH)) {
                        String headerValue = headers.get(HttpHeaders.IF_NONE_MATCH).get(0);
                        if (Objects.equals(eTag, headerValue)) {
                            Response response = Response.notModified(eTag).build();
                            message.getExchange().put(Response.class, response);
                            return;
                        }
                    }
                    message.getExchange().put(RestApiConstants.ETAG, eTag);
                }
            }
            /*
            If the request method is a PUT or a DELETE and the If-Match header is given then the ETag value for the
            resource and the header value will be compared and if they do not match then the flow will be terminated
            with 412 PRECONDITION FAILED
             */
            if (((RestApiConstants.PUT.equals(message.get(Message.HTTP_REQUEST_METHOD))
                    || RestApiConstants.DELETE.equals(message.get(Message.HTTP_REQUEST_METHOD))))
                    && headers.containsKey(HttpHeaders.IF_MATCH)) {
                String ifMatchHeaderValue;
                ifMatchHeaderValue = String.valueOf(headers.get(HttpHeaders.IF_MATCH).get(0));
                if (!Objects.equals(lastUpdatedTime, "null")) {
                    String eTag = ETagGenerator.getETag(lastUpdatedTime);
                    if (!Objects.equals(ifMatchHeaderValue, eTag)) {
                        Response response = Response.status(Response.Status.PRECONDITION_FAILED).build();
                        message.getExchange().put(Response.class, response);
                    }
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                InvocationTargetException | InstantiationException e) {
            if (log.isDebugEnabled()) {
                log.debug(" Error while retrieving the ETag Resource timestamps due to " + e.getMessage(), e);
            }
        }

    }

}
