package org.wso2.carbon.apimgt.core.exception;

public class CookieNotFoundException extends Exception {

    // Constructor without parameters
    public CookieNotFoundException() {

    }

    //Constructor that accepts a message
    public CookieNotFoundException(String message) {
        super(message);
    }

    // Constructor accepts a message and a cause
    public CookieNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor accepts a cause
    public CookieNotFoundException(Throwable cause) {
        super(cause);
    }
}
