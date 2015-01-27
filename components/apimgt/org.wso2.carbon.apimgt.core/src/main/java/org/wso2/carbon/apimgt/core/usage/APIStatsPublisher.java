/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.core.usage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.dto.RequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ResponsePublisherDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class APIStatsPublisher {

    private static final Log log = LogFactory.getLog(APIStatsPublisher.class);

    private APIMgtUsageDataPublisher publisher;
    
    private String hostName;

    /*Private default constructor to force using the overloaded constructor*/
    private APIStatsPublisher(){}

    public APIStatsPublisher(APIMgtUsageDataPublisher publisher, String hostName){
        this.publisher = publisher;
        this.hostName = hostName;
    }

    public boolean publishRequestStatistics(APIKeyValidationInfoDTO keyValidationDTO, String requestPath,
                                     String contextPath, String pathInfo, String httpMethod, long requestTime){
        if(publisher == null){
            return false;
        }

        int tenantDomainIndex = requestPath.indexOf("/t/");

        //Currently the tenant domain is considered as the publisher. This is wrong. The correct implementation
        //would be to get the publisher from the APIKeyValidationDTO. (getApiPublisher())
        String apiPublisher = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (tenantDomainIndex != -1) {
            String temp = requestPath.substring(tenantDomainIndex + 3, requestPath.length());
            apiPublisher = temp.substring(0, temp.indexOf("/"));
        }

        RequestPublisherDTO requestPublisherDTO = new RequestPublisherDTO();
        requestPublisherDTO.setConsumerKey(keyValidationDTO.getConsumerKey());
        requestPublisherDTO.setContext(contextPath);
        //TODO Remove Hard Coded Version and use proper
        requestPublisherDTO.setApi_version(keyValidationDTO.getApiName() + ":v1.0.0");
        requestPublisherDTO.setApi(keyValidationDTO.getApiName());
        //TODO Remove Hard Coded Version and use proper
        requestPublisherDTO.setVersion("1.0.0");
        requestPublisherDTO.setResource(pathInfo);
        requestPublisherDTO.setMethod(httpMethod);
        requestPublisherDTO.setRequestTime(requestTime);
        requestPublisherDTO.setUsername(keyValidationDTO.getEndUserName());
        requestPublisherDTO.setHostName(hostName);
        requestPublisherDTO.setApiPublisher(apiPublisher);

        try{
            publisher.publishEvent(requestPublisherDTO);
        }catch (Throwable e){
            //Log the error and continue since we do not want the message flow to be effected due to stats not being published.
            log.error("Could not publish request event to BAM. " + e.getMessage());
            return false;
        }

        return true;
    }

    public boolean publishResponseStatistics(APIKeyValidationInfoDTO keyValidationDTO, String requestPath,
                                             String contextPath, String pathInfo, String httpMethod, long requestTime){

        if(publisher == null){
            return false;
        }

        long currentTime = System.currentTimeMillis();

        int tenantDomainIndex = requestPath.indexOf("/t/");

        //Currently the tenant domain is considered as the publisher. This is wrong. The correct implementation
        //would be to get the publisher from the APIKeyValidationDTO. (getApiPublisher())
        String apiPublisher = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (tenantDomainIndex != -1) {
            String temp = requestPath.substring(tenantDomainIndex + 3, requestPath.length());
            apiPublisher = temp.substring(0, temp.indexOf("/"));
        }

        ResponsePublisherDTO responsePublisherDTO = new ResponsePublisherDTO();
        responsePublisherDTO.setConsumerKey(keyValidationDTO.getConsumerKey());
        responsePublisherDTO.setUsername(keyValidationDTO.getEndUserName());
        responsePublisherDTO.setContext(contextPath);
        //TODO Remove Hard Coded Version and use proper
        responsePublisherDTO.setApi_version(keyValidationDTO.getApiName() + ":v1.0.0");
        responsePublisherDTO.setApi(keyValidationDTO.getApiName());
        //TODO Remove Hard Coded Version and use proper
        responsePublisherDTO.setVersion("1.0.0");
        responsePublisherDTO.setResource(pathInfo);
        responsePublisherDTO.setMethod(httpMethod);
        responsePublisherDTO.setResponseTime(currentTime);
        responsePublisherDTO.setServiceTime(currentTime - requestTime);
        responsePublisherDTO.setHostName(hostName);
        responsePublisherDTO.setApiPublisher(apiPublisher);

        try{
            publisher.publishEvent(responsePublisherDTO);
        }catch (Throwable e){
            //Log the error and continue since we do not want the message flow to be effected due to stats not being published.
            log.error("Could not publish response event to BAM. " + e.getMessage());
            return false;
        }

        return true;
    }
}
