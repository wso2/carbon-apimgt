package org.wso2.carbon.apimgt.lifecycle.manager;

/**
 * The class {@code LifeCycleExecutionException} and its subclasses are a form of
 * {@code Exception} that indicates conditions that a reasonable
 * life cycle management application might want to catch.
 **/
public class LifeCycleExecutionException extends Exception {
    private static final long serialVersionUID = 595805804854058405L;

    /**
     * Constructs a new LifeCycle Execution Exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     *
     * @param message the detail message. The detail message is saved for later retrieval
     *                by the {@link #getMessage()} method.
     */
    public LifeCycleExecutionException(String message) {
        super(message);
    }

    /**
     * Constructs a new LifeCycle Execution Exception with the specified detail message and
     * cause.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param e the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).
     */
    public LifeCycleExecutionException(String message, Throwable e) {
        super(message, e);
    }
}
