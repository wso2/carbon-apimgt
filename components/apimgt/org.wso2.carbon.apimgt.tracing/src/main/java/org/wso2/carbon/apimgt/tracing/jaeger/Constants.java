package org.wso2.carbon.apimgt.tracing.jaeger;

public class Constants {

    static final String CONFIG_NAME = "OpenTracer.Name";
    static final String CONFIG_PORT = "OpenTracer.Port";
    static final String CONFIG_HOST = "OpenTracer.Hostname";
    static final String CONFIG_ENABLED = "OpenTracer.Enabled";
    static final String CONFIG_SAMPLER_PARAM = "OpenTracer.SamplerParam";
    static final String CONFIG_SAMPLER_TYPE = "OpenTracer.SamplerType";
    static final String CONFIG_REPORTER_FLUSH_INTERVAL = "OpenTracer.ReporterFlushInterval";
    static final String CONFIG_REPORTER_BUFFER_SIZE = "OpenTracer.ReporterBufferSize";

    static final int DEFAULT_PORT = 5775;
    static final String DEFAULT_HOST = "localhost";
    static boolean DEFAULT_ENABLED = true;
    static final int DEFAULT_SAMPLER_PARAM = 1;
    static final String DEFAULT_SAMPLER_TYPE = "const";
    static final int DEFAULT_REPORTER_FLUSH_INTERVAL = 1000;
    static final int DEFAULT_REPORTER_BUFFER_SIZE = 1000;


}
