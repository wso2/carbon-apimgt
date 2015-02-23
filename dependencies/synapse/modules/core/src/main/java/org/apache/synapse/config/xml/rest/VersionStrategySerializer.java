/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
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
package org.apache.synapse.config.xml.rest;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.version.DefaultStrategy;
import org.apache.synapse.rest.version.URLBasedVersionStrategy;
import org.apache.synapse.rest.version.VersionStrategy;

import javax.xml.namespace.QName;

public class VersionStrategySerializer {
    private static final Log log = LogFactory.getLog(VersionStrategySerializer.class);

    public static OMElement serializeVersioningStrategy(VersionStrategy strategy, OMElement apiElement) {
        assert strategy != null;

        if (strategy.getVersion() != null && !"".equals(strategy.getVersion())) {
            apiElement.addAttribute("version", strategy.getVersion(), null);
        }
        if (strategy.getVersionType() != null && !"".equals(strategy.getVersionType())) {
            apiElement.addAttribute("version-type", strategy.getVersionType(), null);
        }
        if (strategy.getVersionParam() != null && !"".equals(strategy.getVersionParam())) {
            apiElement.addAttribute("version-param", strategy.getVersionParam(), null);
        }
        return apiElement;
    }

}
