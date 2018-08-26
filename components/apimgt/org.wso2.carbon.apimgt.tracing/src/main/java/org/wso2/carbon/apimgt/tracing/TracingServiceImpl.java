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
import org.wso2.carbon.apimgt.tracing.jaeger.JaegerTracerImpl;
import org.wso2.carbon.apimgt.tracing.zipkin.ZipkinTracerImpl;
import org.wso2.carbon.utils.CarbonUtils;
import java.io.File;

public class TracingServiceImpl implements TracingService {

    private static final Log log = LogFactory.getLog(TracingServiceImpl.class);
    private APIManagerConfiguration configuration = new APIManagerConfiguration();
    private Tracer tracer;

    @Override
    public TracingTracer buildTracer(String serviceName) {
        try {
            String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "api-manager.xml";
            configuration.load(filePath);

        } catch (APIManagementException e) {
            e.printStackTrace();
        }

        String openTracerName = configuration.getFirstProperty("OpenTracer.Name");
        String enabled = configuration.getFirstProperty("OpenTracer.Enabled");

        if (openTracerName.equalsIgnoreCase("JAEGER") && enabled.equalsIgnoreCase("TRUE")) {

            tracer = new JaegerTracerImpl().getTracer(configuration, serviceName);
            return new TracingTracer(tracer);
        } else if (openTracerName.equalsIgnoreCase("ZIPKIN") && enabled.equalsIgnoreCase("TRUE")) {

            tracer = new ZipkinTracerImpl().getTracer(configuration, serviceName);
            return new TracingTracer(tracer);
        } else {
            log.error("Invalid Configuration");
        }

        return null;
    }

}
