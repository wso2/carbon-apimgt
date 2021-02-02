package org.wso2.carbon.apimgt.gateway.common.exception;

/**
 * This error handler interface must use in all exceptions class, for example please see APIManagementException class.
 */
public interface ErrorHandler {
    /**
     * Get error code that defined in the enum
     *
     * @return error code
     */
    long getErrorCode();

    /**
     * Get error message that defined in the enum
     *
     * @return error message
     */
    String getErrorMessage();

    /**
     * Get error description that defined in the enum
     *
     * @return error description.
     */
    String getErrorDescription();

    /**
     * Get Http status code that defined in the enum
     *
     * @return error code.
     */
    int getHttpStatusCode();

    /**
     * Return true if stack trace to print;
     *
     * @return
     */
    boolean printStackTrace();
}
