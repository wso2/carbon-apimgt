package org.wso2.carbon.apimgt.core.exception;

/**
 * This is the Exception class for Service Discovery related exceptions..
 */
public class ServiceDiscoveryException extends APIManagementException {

    public ServiceDiscoveryException(String msg) {
        super(msg);
    }

    public ServiceDiscoveryException(Throwable throwable) {
        super(throwable);
    }

    public ServiceDiscoveryException(String message, Throwable e) {
        super(message, e);
    }

    public ServiceDiscoveryException(String msg, ExceptionCodes code) {
        super(msg, code);
    }

    public ServiceDiscoveryException(String msg, Throwable e, ExceptionCodes code) {
        super(msg, e, code);
    }
}
