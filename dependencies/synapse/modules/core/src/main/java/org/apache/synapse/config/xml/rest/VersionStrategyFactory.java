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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.version.DefaultStrategy;
import org.apache.synapse.rest.version.URLBasedVersionStrategy;
import org.apache.synapse.rest.version.VersionStrategy;

import javax.xml.namespace.QName;

public class VersionStrategyFactory {
    private static final Log log = LogFactory.getLog(VersionStrategyFactory.class);
    public static final String TYPE_URL = "url";
    public static final String TYPE_NULL = "";

    public static VersionStrategy createVersioningStrategy(API api, OMElement apiElt) {
        OMAttribute versionAtt = apiElt.getAttribute(new QName("version"));
        if (versionAtt == null || "".equals(versionAtt.getAttributeValue())) {
//            handleException("Attribute 'version' is required for an API definition");
        }

        OMAttribute versionTypeAtt = apiElt.getAttribute(new QName("version-type"));
        if (versionAtt != null && (versionTypeAtt == null || "".equals(versionTypeAtt.getAttributeValue()))) {
            handleException("Attribute 'version-type' is required for an API definition");
        }

        OMAttribute versionParamAtt = apiElt.getAttribute(new QName("version-param"));

        String version = "";
        if (versionAtt != null) {
            version = versionAtt.getAttributeValue();
        }
        String versionType = "";
        if (versionTypeAtt != null) {
            versionType = versionTypeAtt.getAttributeValue();
        }
        String versionParam = "";
        if (versionParamAtt != null) {
            versionParam = versionParamAtt.getAttributeValue();
        }

        return selectVersionStrategy(api, version, versionType, versionParam);
    }

    private static VersionStrategy selectVersionStrategy(API api, String version, String versionType,
                                                         String versionParam) {
        if (versionType != null && TYPE_URL.equals(versionType.trim())) {
            return new URLBasedVersionStrategy(api, version, versionParam);
        }
        if (versionType == null || TYPE_NULL.equals(versionType.trim())) {
            //no versioning supported here
            //intended for backward compatability with API's
            return new DefaultStrategy(api);
        }
        return null;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }
}
