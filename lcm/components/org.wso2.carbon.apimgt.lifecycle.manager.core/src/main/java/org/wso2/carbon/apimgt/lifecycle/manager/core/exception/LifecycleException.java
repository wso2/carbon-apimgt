package org.wso2.carbon.apimgt.lifecycle.manager.core.exception;

/**
 * The class {@code LifecycleException} and its subclasses are a form of
 * {@code Exception} that indicates conditions that a reasonable
 * life cycle management application might want to catch.
 **/
public class LifecycleException extends Exception {
    private static final long serialVersionUID = 595805804854058405L;

    /**
     * Constructs a new Lifecycle Execution Exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     *
     * @param message the detail message. The detail message is saved for later retrieval
     *                by the {@link #getMessage()} method.
     */
    public LifecycleException(String message) {
        super(message);
    }

    /**
     * Constructs a new Lifecycle Execution Exception with the specified detail message and
     * cause.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param e the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).
     */
    public LifecycleException(String message, Throwable e) {
        super(message, e);
    }
}
