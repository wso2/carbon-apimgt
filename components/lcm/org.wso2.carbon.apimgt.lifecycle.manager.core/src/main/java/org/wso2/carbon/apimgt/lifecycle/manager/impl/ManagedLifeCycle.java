package org.wso2.carbon.apimgt.lifecycle.manager.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.apimgt.lifecycle.manager.LifeCycleExecutionException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifeCycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.util.LCUtils;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.beans.CustomCodeBean;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.interfaces.Execution;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LCOperationUtil;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LCStateBean;

import java.util.List;

import static org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LCOperationUtil.getInitialState;
import static org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LCOperationUtil.populateAvailableStates;
import static org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LCOperationUtil.populateTransitionExecutors;
import static org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LCOperationUtil.populateTransitionInputs;

/**
 * This is the base ManagedLifeCycle class. If users need to extend life cycle management
 * feature to any of the class they created they can extend this class and implement required
 * stuff.
 */
public class ManagedLifeCycle implements IManageLifeCycle{

    private Log log = LogFactory.getLog(ManagedLifeCycle.class);

    private String lifeCycleID;
    private LifeCycleState currentLifecycleState;

    public ManagedLifeCycle(){
        currentLifecycleState = new LifeCycleState();
    }

    /**
     * This method will be used to get life cycle state of current onject
     *
     * @return {@code LifeCycleState} object which holds current life cycle state data.
     */
    public LifeCycleState getCurrentLifecycleState() {
        return currentLifecycleState;
    }

    /**
     * This method will be used to set life cycle state of current object
     *
     * @param currentLifecycleState {@code LifeCycleState} object which holds
     *                              current life cycle state data.
     */
    private void setCurrentLifecycleState(LifeCycleState currentLifecycleState) throws LifeCycleExecutionException {
        LCStateBean lcStateBean = LCOperationUtil.getLCStateDataFromID(this.lifeCycleID);
        String lcName = lcStateBean.getLcName();
        String lcContent;
        try {
            lcContent = LCUtils.getLifecycleConfiguration(lcName);
        } catch (LifeCycleException e) {
            throw new LifeCycleExecutionException("Error while getting lifecycle config for lifecycle name : " + lcName,
                    e);
        }
        currentLifecycleState.setLcName(lcName);
        currentLifecycleState.setLifecycleId(this.lifeCycleID);
        currentLifecycleState.setState(lcStateBean.getStatus());
        populateItems(currentLifecycleState, lcContent);
        this.currentLifecycleState = currentLifecycleState;
    }

    /**
     * This method will be used to get lifecycle ID_ATTRIBUTE of current object.
     *
     * @return {@code String} object that can use to uniquely identify resource.
     */
    public String getLifeCycleID() {
        return lifeCycleID;
    }

    /**
     * This method will be used to set lifecycle ID_ATTRIBUTE of current object.
     *
     * @param lifeCycleID {@code String} object that can use to uniquely identify resource.
     */
    public void setLifeCycleID(String lifeCycleID) throws LifeCycleExecutionException {
        this.lifeCycleID = lifeCycleID;

        //TODO Fetch life cycle data from the lifecycle management engine and set it here.
        setCurrentLifecycleState(new LifeCycleState());
        //When we set life cycle ID_ATTRIBUTE to managed life cycle we also need to fetch life cycle state object and
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
     * @param action {@code String} lifecycle action.
     * @param nextState    {@code LifeCycleState} object represent next life cycle state.
     * @return {@code LifeCycleState} object of updated life cycle state.
     * @throws LifeCycleExecutionException if exception occurred while execute life cycle update.
     */
    public LifeCycleState executeLifeCycleEvent(LifeCycleState nextState, String action, Object resource)
            throws LifeCycleExecutionException {
        LifeCycleState currentState = this.currentLifecycleState;
        String lcName = currentState.getLcName();
        String lcContent;
        try {
            lcContent = LCUtils.getLifecycleConfiguration(lcName);
        } catch (LifeCycleException e) {
            throw new LifeCycleExecutionException("Error while getting lifecycle config for lifecycle name : " + lcName,
                    e);
        }
        // identifies the state change operation
        if (!"checklistItemClick".equals(action)) {
            runCustomExecutorsCode(action, resource, currentState.getCustomCodeBeanList(), currentState.getState(),
                    nextState.getState());
            populateItems(nextState, lcContent);
            nextState.setLcName(currentState.getLcName());
            nextState.setLifecycleId(currentState.getLifecycleId());
            LCOperationUtil.changeLifecycleState(nextState.getState(), this.lifeCycleID);

        } else {           // identifies check list item selecting event
            // TODO : implement check list item logic here
        }
        this.currentLifecycleState = nextState;
        return nextState;
    }

    /**
     * This method add state data like, transition inputs, custom executors etc to the lifecycle state object.
     *
     * @param lcName                        lc name which associates with the resource.
     * @return                              object of added life cycle state.
     * @throws LifeCycleExecutionException  if failed to get lifecycle list.
     */
    public LifeCycleState associateLifecycle (String lcName) throws LifeCycleExecutionException {
        LifeCycleState lifeCycleState ;
        try {
            String lcContent = LCUtils.getLifecycleConfiguration(lcName);
            lifeCycleState = new LifeCycleState();

            String initialState = getInitialState(LCOperationUtil.getLifecycleElement(lcContent),lcName);
            lifeCycleState.setLcName(lcName);
            lifeCycleState.setState(initialState);
            populateItems(lifeCycleState,lcContent);
            String lifeCycleId = LCOperationUtil.associateLifecycle(lcName, initialState);

            lifeCycleState.setLifecycleId(lifeCycleId);
            this.currentLifecycleState = lifeCycleState;
            this.lifeCycleID = lifeCycleId;
        } catch (LifeCycleException e) {
            throw new LifeCycleExecutionException("Error while associating lifecycle : "+ lcName,e);
        }
        return lifeCycleState;
    }

    /**
     * This method add state data like, transition inputs, custom executors etc to the lifecycle state object.
     *
     * @param lifeCycleState                lc state object which is being populated.
     * @param lcConfig                      lc configuration.
     * @throws LifeCycleExecutionException  if failed to get lifecycle list.
     */
    private void populateItems(LifeCycleState lifeCycleState, String lcConfig) throws
            LifeCycleExecutionException {

            String lcState = lifeCycleState.getState();
            Document document = LCOperationUtil.getLifecycleElement(lcConfig);
            lifeCycleState.setInputBeanList(populateTransitionInputs(document, lcState));
            lifeCycleState.setCustomCodeBeanList(populateTransitionExecutors(document, lcState));
            lifeCycleState.setAvailableTransitionBeanList(populateAvailableStates(document, lcState));

    }

    /**
     * This method is used to run custom executor codes.
     *
     * @param action                        Current lc action (Promote/Demote)
     * @param resource                      The asset to which the lc is attached
     * @return                              success of execution class.
     * @throws LifeCycleExecutionException  if failed to run custom executors.
     */
    private boolean runCustomExecutorsCode(String action,Object resource, List<CustomCodeBean> customCodeBeans
            ,String currentState,String nextState)
            throws LifeCycleExecutionException{
        if (customCodeBeans != null) {
            for (CustomCodeBean customCodeBean : customCodeBeans) {
                if (customCodeBean.getEventName().equals(action)) {
                    Execution customExecutor = (Execution) customCodeBean.getClassObject();


                    if (!customExecutor.execute(resource,currentState,nextState)) {
                        String message = "Execution failed for action : " + customCodeBean.getEventName()+"for the "
                                + "execution class "+ customCodeBean.getClassObject().getClass();

                       customCodeBean.setCustomMessage(message);
                        throw new LifeCycleExecutionException(message);
                    }
                }
            }
        }
        return true;
    }


}

