package org.wso2.carbon.apimgt.impl;

/**
 *
 * This is the custom exception class for Endpoint files handling in the gateway
 *
 */
public class EndpointAdminException extends Exception {

    public EndpointAdminException() {
        super();
    }

    public EndpointAdminException(String message) {
        super(message);
    }

    public EndpointAdminException(String message, Exception e) {
        super(message, e);
    }
}
