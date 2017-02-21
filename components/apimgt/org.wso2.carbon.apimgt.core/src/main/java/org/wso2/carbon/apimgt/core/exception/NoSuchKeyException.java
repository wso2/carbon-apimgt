package org.wso2.carbon.apimgt.core.exception;

/**
 * This exception has to be thrown when key is not found in the keystore or in case of other exceptions when trying to
 * get key.
 */
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
