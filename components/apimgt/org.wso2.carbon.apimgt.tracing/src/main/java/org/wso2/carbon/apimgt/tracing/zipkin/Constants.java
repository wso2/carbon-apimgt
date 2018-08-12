package org.wso2.carbon.apimgt.tracing.zipkin;

public class Constants {

    static final String CONFIG_NAME = "OpenTracer.Name";
    static final String CONFIG_PORT = "OpenTracer.Port";
    static final String CONFIG_HOST = "OpenTracer.Hostname";
    static final String CONFIG_ENABLED = "OpenTracer.Enabled";
    static final String CONFIG_API_CONTEXT = "OpenTracer.APIContext";

    static final int DEFAULT_PORT = 9411;
    static final String DEFAULT_HOST = "localhost";
    static final String DEFAULT_API_CONTEXT = "/api/v1/spans";

}
