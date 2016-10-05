package org.wso2.carbon.apimgt.lifecycle.manager.impl;

import org.wso2.carbon.apimgt.lifecycle.manager.LifeCycleExecutionException;

/**
 * This is the base ManagedLifeCycle class. If users need to extend life cycle management
 * feature to any of the class they created they can extend this class and implement required
 * stuff.
 */
public class ManagedLifeCycle {

    private String lifeCycleID;
    private LifeCycleState currentLifecycleState;

    /**
     * This method will be used to get life cycle state of current onject
     *
     * @return {@code LifeCycleState} object which holds current life cycle state data.
     */
    public LifeCycleState getCurrentLifecycleState() {
        return currentLifecycleState;
    }

    /**
     * This method will be used to set life cycle state of current onject
     *
     * @param currentLifecycleState {@code LifeCycleState} object which holds
     *                              current life cycle state data.
     */
    private void setCurrentLifecycleState(LifeCycleState currentLifecycleState) {
        this.currentLifecycleState = currentLifecycleState;
    }

    /**
     * This method will be used to get lifecycle ID of current object.
     *
     * @return {@code String} object that can use to uniquely identify resource.
     */
    public String getLifeCycleID() {
        return lifeCycleID;
    }

    /**
     * This method will be used to set lifecycle ID of current object.
     *
     * @param lifeCycleID {@code String} object that can use to uniquely identify resource.
     */
    public void setLifeCycleID(String lifeCycleID) {
        this.lifeCycleID = lifeCycleID;

        //TODO Fetch life cycle data from the lifecycle management engine and set it here.
        setCurrentLifecycleState(new LifeCycleState());
        //When we set life cycle ID to managed life cycle we also need to fetch life cycle state object and
        //set reference to managed life cycle object. All life cycle settings should go through executeLifeCycleEvent
        //method only.
    }

    /**
     * Get current life cycle state object.
     *
     * @return {@code LifeCycleState} object represent current life cycle.
     */
    public LifeCycleState getCurrentLifeCycleState() {
        return currentLifecycleState;
    }

    /**
     * This method need to call for each and event life cycle state changes.
     *
     * @param currentState {@code LifeCycleState} object represent current life cycle.
     * @param nextState    {@code LifeCycleState} object represent next life cycle state.
     * @param resource     {@code Object} object represent artifact to be engaged with this life cycle.
     * @return {@code LifeCycleState} object of updated life cycle state.
     * @throws LifeCycleExecutionException if exception occurred while execute life cycle update.
     */
    public LifeCycleState executeLifeCycleEvent(LifeCycleState currentState, LifeCycleState nextState, Object resource)
            throws LifeCycleExecutionException {
        //If execution is initial then we just create life cycle entry and return UUID.
        //In that case both current and next states should be null. And also lifeCycleID should be null.

        //
        return new LifeCycleState();
        //return "state of" + "Changed from:" + currentState + "  to:" + nextState;
    }

}

