/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.common.interceptors;

//import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;
//import org.wso2.msf4j.internal.router.HandlerException;
//import org.wso2.msf4j.internal.router.HttpMethodInfo;
//import org.wso2.msf4j.internal.router.HttpMethodInfoBuilder;
//import org.wso2.msf4j.internal.router.HttpResourceModel;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.Locale;

/**
 * Security Interceptor that does basic authentication for REST ApI requests.
 *
 */
//@Component(
//        name = "org.wso2.carbon.apimgt.rest.api.common.interceptors.ETagInterceptor",
//        service = Interceptor.class,
//        immediate = true
//)
public class ETagInterceptor implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(ETagInterceptor.class);

    /**
     * preCall is run before a handler method call is made. If any of the preCalls throw exception or return false then
     * no other subsequent preCalls will be called and the request processing will be terminated,
     * also no postCall interceptors will be called.
     *
     * @param request           HttpRequest being processed.
     * @param response          HttpResponder to send response.
     * @param serviceMethodInfo Info on handler method that will be called.
     * @return true if the request processing can continue, otherwise the hook should send response and return false to
     * stop further processing.
     * @throws APIMgtSecurityException if error occurs while executing the preCall
     */
    @Override
    public boolean preCall(Request request, Response response, ServiceMethodInfo serviceMethodInfo) throws
            APIMgtSecurityException {
        return true;
        
        
// todo NOTE: This class implementation is commented due to missing feature from MSF4J. 
//
//        String requestURI = request.getUri().toLowerCase(Locale.ENGLISH);
//        if (!requestURI.contains("/api/am/store")) {
//            return true;
//        }

//        Method serviceMethod = serviceMethodInfo.getMethod();
//        HttpResourceModel resourceModel = serviceMethodInfo.getHttpResourceModel();
//        HttpMethodInfoBuilder httpMethodInfoBuilder =
//                new HttpMethodInfoBuilder().
//                        httpResourceModel(resourceModel).
//                        httpRequest(request).
//                        httpResponder(response).
//                        requestInfo(serviceMethodInfo.getGroupNameValues());
//
//        try {
//            HttpMethodInfo httpMethodInfo = httpMethodInfoBuilder.build();
//            String targetMethodName = serviceMethod.getName() + "LastUpdatedTime";
//            Method targetMethod = httpMethodInfo.getHandler().getClass().getMethod(targetMethodName,
//                    serviceMethod.getParameterTypes());
//            
//            String lastUpdatedTime = String
//                    .valueOf(targetMethod.invoke(httpMethodInfo.getHandler(), httpMethodInfo.getArgs()));
//            log.info(lastUpdatedTime);
//
//        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
//            log.error("Error while obtaining last updated time", e);
//        }
//
//        return true;
    }

    /**
     * postCall is run after a handler method call is made. If any of the postCalls throw and exception then the
     * remaining postCalls will still be called. If the handler method was not called then postCall interceptors will
     * not be called.
     *
     * @param request           HttpRequest being processed.
     * @param status            Http status returned to the client.
     * @param serviceMethodInfo Info on handler method that was called.
     * @throws Exception if error occurs while executing the postCall
     */
    @Override
    public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo) throws Exception {

    }

}
