/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.tracing;

import io.opentracing.Tracer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

public class TracingServiceImpl implements TracingService {

    private static final Log log = LogFactory.getLog(TracingServiceImpl.class);
    private static APIManagerConfiguration configuration = new APIManagerConfiguration();
    private static TracingServiceImpl instance = new TracingServiceImpl();

    public static TracingServiceImpl getInstance() {
        return instance;
    }

    @Override
    public TracingTracer buildTracer(String serviceName) {
        try {
            String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "api-manager.xml";
            configuration.load(filePath);

        } catch (APIManagementException e) {
            log.error("Error in reading configuration file", e);
        }

        String openTracerName = getConfiguration().getFirstProperty(TracingConstants.OPEN_TRACER_NAME) != null ?
                configuration.getFirstProperty(TracingConstants.OPEN_TRACER_NAME)
                : TracingConstants.DEFAULT_OPEN_TRACER_NAME;
        String enabled = getConfiguration().getFirstProperty(TracingConstants.OPEN_TRACER_ENABLED) != null ?
                configuration.getFirstProperty(TracingConstants.OPEN_TRACER_ENABLED)
                : TracingConstants.DEFAULT_OPEN_TRACER_ENABLED;

        Tracer tracer;
        if (openTracerName.equalsIgnoreCase(TracingConstants.JAEGER) && enabled.equalsIgnoreCase("TRUE")) {
            tracer = new JaegerTracer().getTracer(serviceName);
            return new TracingTracer(tracer);

        } else if (openTracerName.equalsIgnoreCase(TracingConstants.ZIPKIN) &&
                enabled.equalsIgnoreCase("TRUE")) {
            tracer = new ZipkinTracer().getTracer(serviceName);
            return new TracingTracer(tracer);

        } else {
            log.error("Invalid test Configuration");
        }
        return null;
    }

    public APIManagerConfiguration getConfiguration() { return configuration; }
}
