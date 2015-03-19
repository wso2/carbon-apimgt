/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.apache.synapse.rest.version;

import org.apache.synapse.MessageContext;
import org.apache.synapse.config.xml.rest.VersionStrategyFactory;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.RESTUtils;

public class ContextVersionStrategy extends AbstractVersionStrategy {
    String versionParam;

    public ContextVersionStrategy(API api, String version, String versionParam) {
        super(api, version, VersionStrategyFactory.TYPE_CONTEXT);
        this.versionParam = versionParam;

        // We resolve the API Context here.
        String context = api.getContext();

        // We are resolving the context here. No need to check whether the context param exists.
        context = context.replace(RESTConstants.SYNAPSE_REST_CONTEXT_VERSION_VARIABLE, version);
        api.setContext(context);
    }

    /*
    * This method will return whether the given version string matches the incoming one.
    * Since in ContextVersionStrategy, the version is part of the context, there is no requirement to check whether
    * the version matches.
    * We only need to check whether the request path starts with the context. If so the version is matched.
    *
    * Ex:- context - /1.0.0/foo
    *      incoming path - /1.0.0/foo/bar
    * */
    public boolean isMatchingVersion(Object versionInfoObj) {
        MessageContext msgContext = (MessageContext) versionInfoObj;

        String path = RESTUtils.getFullRequestPath(msgContext);
        String context = getAPI().getContext();

        return path.startsWith(context);
    }

    public String getVersionParam() {
        return versionParam;
    }
}

