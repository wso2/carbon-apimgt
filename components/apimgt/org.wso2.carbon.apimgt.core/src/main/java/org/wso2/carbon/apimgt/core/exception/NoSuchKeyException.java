package org.wso2.carbon.apimgt.core.exception;

public class NoSuchKeyException extends Exception {

    // Constructor without parameters
    public NoSuchKeyException() {

    }

    //Constructor that accepts a message
    public NoSuchKeyException(String message) {
        super(message);
    }

    // Constructor accepts a message and a cause
    public NoSuchKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor accepts a cause
    public NoSuchKeyException(Throwable cause) {
        super(cause);
    }

}
