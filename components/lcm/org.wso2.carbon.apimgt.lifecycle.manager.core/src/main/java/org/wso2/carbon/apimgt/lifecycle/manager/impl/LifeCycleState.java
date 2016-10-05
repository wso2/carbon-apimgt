package org.wso2.carbon.apimgt.lifecycle.manager.impl;

/**
 * This is life cycle state class and life cycle state related information such as
 * next executables and possible state changes etc should stored with this class.
 */
public class LifeCycleState {
    String state;

    /**
     * @return current state of life cycle state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state of current lifecycle state object.
     * @return
     */
    public void setState(String state) {
        this.state = state;
    }

    //TODO Add allowed check items and other information should be here.

}
