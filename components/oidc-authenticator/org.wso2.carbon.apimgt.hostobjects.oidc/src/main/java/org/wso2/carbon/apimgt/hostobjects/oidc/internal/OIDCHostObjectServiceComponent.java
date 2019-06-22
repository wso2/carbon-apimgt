/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.hostobjects.oidc.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="hostobject.oidc.component" immediate="true"
 */
public class OIDCHostObjectServiceComponent {

    private static Log log = LogFactory.getLog(OIDCHostObjectServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("SSO host bundle is activated");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("SSO host bundle is deactivated");
        }
    }


}
