package org.wso2.carbon.apimgt.tracing;

import java.util.logging.Logger;

public class TracingServiceImpl implements TracingService {

    private static final Logger LOGGER = Logger.getLogger(TracingServiceImpl.class.getName());

    @Override
    public void produce(String name) {

        LOGGER.info("Successfully produced: "+ name);
    }
}
