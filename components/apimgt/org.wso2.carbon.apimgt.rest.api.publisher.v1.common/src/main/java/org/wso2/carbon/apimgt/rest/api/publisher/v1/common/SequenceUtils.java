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
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPToRESTConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.ConfigContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.SOAPToRESTAPIConfigContext;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

/**
 * Util class used for sequence generation of the soap to rest converted operations.
 */
public class SequenceUtils {

    private static final Log log = LogFactory.getLog(SequenceUtils.class);

    /**
     * Gets the velocity template config context with sequence data populated
     *
     * @param registry      user registry reference
     * @param resourcePath  registry resource path
     * @param seqType       sequence type whether in or out sequence
     * @param configContext velocity template config context
     * @return {@link ConfigContext} sequences populated velocity template config context
     * @throws org.wso2.carbon.registry.api.RegistryException throws when getting registry resource content
     */
    public static ConfigContext getSequenceTemplateConfigContext(UserRegistry registry, String resourcePath,
                                                                 String seqType, ConfigContext configContext)
            throws org.wso2.carbon.registry.api.RegistryException {

        Resource regResource;
        if (registry.resourceExists(resourcePath)) {
            regResource = registry.get(resourcePath);
            String[] resources = ((Collection) regResource).getChildren();
            JSONObject pathObj = new JSONObject();
            if (resources != null) {
                for (String path : resources) {
                    Resource resource = registry.get(path);
                    String method = resource.getProperty(SOAPToRESTConstants.METHOD);
                    String registryResourceProp = resource.getProperty(SOAPToRESTConstants.Template.RESOURCE_PATH);
                    String resourceName;
                    if (registryResourceProp != null) {
                        resourceName = SOAPToRESTConstants.SequenceGen.PATH_SEPARATOR + registryResourceProp;
                    } else {
                        resourceName = ((ResourceImpl) resource).getName();
                        resourceName = resourceName.replaceAll(SOAPToRESTConstants.SequenceGen.XML_FILE_RESOURCE_PREFIX,
                                SOAPToRESTConstants.EMPTY_STRING);
                        resourceName = resourceName
                                .replaceAll(SOAPToRESTConstants.SequenceGen.RESOURCE_METHOD_SEPERATOR + method,
                                        SOAPToRESTConstants.EMPTY_STRING);
                        resourceName = SOAPToRESTConstants.SequenceGen.PATH_SEPARATOR + resourceName;
                    }
                    String content = RegistryUtils.decodeBytes((byte[]) resource.getContent());
                    JSONObject contentObj = new JSONObject();
                    contentObj.put(method, content);
                    pathObj.put(resourceName, contentObj);
                }
            } else {
                log.error("No sequences were found on the resource path: " + resourcePath);
            }
            configContext = new SOAPToRESTAPIConfigContext(configContext, pathObj, seqType);
        }
        return configContext;
    }

}
