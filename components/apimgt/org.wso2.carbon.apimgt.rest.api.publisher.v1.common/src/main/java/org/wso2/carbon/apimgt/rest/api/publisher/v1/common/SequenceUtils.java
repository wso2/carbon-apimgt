/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.impl.dto.SoapToRestMediationDto;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPToRESTConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.ConfigContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.SOAPToRESTAPIConfigContext;

import java.util.List;

/**
 * Util class used for sequence generation of the soap to rest converted operations.
 */
public class SequenceUtils {

    private static final Log log = LogFactory.getLog(SequenceUtils.class);

    /**
     * Gets the velocity template config context with sequence data populated
     *
     * @param soapToRestMediationDtoList  registry resource path
     * @param seqType       sequence type whether in or out sequence
     * @param configContext velocity template config context
     * @return {@link ConfigContext} sequences populated velocity template config context
     */
    public static ConfigContext getSequenceTemplateConfigContext(
            List<SoapToRestMediationDto> soapToRestMediationDtoList, String seqType, ConfigContext configContext) {

        if (soapToRestMediationDtoList.size()>0) {
            JSONObject pathObj = new JSONObject();
            for (SoapToRestMediationDto soapToRestMediationDto : soapToRestMediationDtoList) {
                String method = soapToRestMediationDto.getMethod();
                String resourceName = soapToRestMediationDto.getResource();
                JSONObject contentObj = new JSONObject();
                contentObj.put(method, soapToRestMediationDto.getContent());
                pathObj.put(SOAPToRESTConstants.SequenceGen.PATH_SEPARATOR.concat(resourceName), contentObj);
            }
            configContext = new SOAPToRESTAPIConfigContext(configContext, pathObj, seqType);
        }
        return configContext;
    }

}
