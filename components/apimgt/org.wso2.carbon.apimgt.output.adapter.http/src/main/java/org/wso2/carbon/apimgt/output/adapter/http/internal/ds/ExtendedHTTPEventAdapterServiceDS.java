/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.output.adapter.http.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.output.adapter.http.ExtendedHTTPEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;

@Component(
        name = "output.http.extended.AdapterService.component",
        immediate = true)
public class ExtendedHTTPEventAdapterServiceDS {

    private static final Log log = LogFactory.getLog(ExtendedHTTPEventAdapterServiceDS.class);

    @Activate
    protected void activate(ComponentContext context) {
        ExtendedHTTPEventAdapterFactory extendedHttpEventAdaptorFactory = new ExtendedHTTPEventAdapterFactory();
        BundleContext bundleContext = context.getBundleContext();
        bundleContext.registerService(OutputEventAdapterFactory.class.getName(),extendedHttpEventAdaptorFactory, null);
        if (log.isDebugEnabled()) {
            log.debug("Successfully deployed the Extended Http event adaptor service");
        }
    }
}
