package org.wso2.carbon.apimgt.lifecycle.manager.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.apimgt.lifecycle.manager.core.util.LifecycleUtils;
import org.wso2.carbon.apimgt.lifecycle.manager.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.beans.CustomCodeBean;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LifecycleOperationUtil;
import org.wso2.carbon.apimgt.lifecycle.manager.interfaces.Executor;
import org.wso2.carbon.apimgt.lifecycle.manager.interfaces.ManagedLifecycle;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LifecycleStateBean;

import java.util.List;

import static org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LifecycleOperationUtil.getInitialState;
import static org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LifecycleOperationUtil.populateAvailableStates;
import static org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LifecycleOperationUtil.populateTransitionExecutors;
import static org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LifecycleOperationUtil.populateTransitionInputs;
import static org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LifecycleOperationUtil.populateTransitionPermission;

/**
 * This is the base ManagedLifecycle class. If users need to extend life cycle management
 * feature to any of the class they created they can extend this class and implement required
 * stuff.
 */
public class ManagedLifecycleImpl implements ManagedLifecycle {

    public static final String CHECKLIST_ITEM_CLICK = "checklistItemClick";
    private Log log = LogFactory.getLog(ManagedLifecycleImpl.class);

    private String lifecycleID;
    private LifecycleState currentLifecycleState;

    public ManagedLifecycleImpl() {
        currentLifecycleState = new LifecycleState();
    }

    /**
     * This method will be used to get life cycle state of current object
     *
     * @return {@code LifecycleState} object which holds current life cycle state data.
     */

    /**
     * This method will be used to get lifecycle ID_ATTRIBUTE of current object.
     *
     * @return {@code String} object that can use to uniquely identify resource.
     */
    public String getLifecycleID() {
        return lifecycleID;
    }

    /**
     * This method will be used to set lifecycle ID_ATTRIBUTE of current object.
     *
     * @param lifecycleID {@code String} object that can use to uniquely identify resource.
     */
    public void setLifecycleID(String lifecycleID) throws LifecycleException {
        this.lifecycleID = lifecycleID;

        //Fetch life cycle data from the lifecycle management engine and set it here.
        setCurrentLifecycleState(new LifecycleState());
        //When we set life cycle ID_ATTRIBUTE to managed life cycle we also need to fetch life cycle state object and
        //set reference to managed life cycle object. All life cycle settings should go through executeLifecycleEvent
        //method only.
    }

    /**
     * Get current life cycle state object.
     *
     * @return {@code LifecycleState} object represent current life cycle.
     */
    public LifecycleState getCurrentLifecycleState() {
        return currentLifecycleState;
    }

    /**
     * This method will be used to set life cycle state of current object
     *
     * @param currentLifecycleState {@code LifecycleState} object which holds
     *                              current life cycle state data.
     */
    private void setCurrentLifecycleState(LifecycleState currentLifecycleState) throws LifecycleException {
        LifecycleStateBean lifecycleStateBean = LifecycleOperationUtil.getLCStateDataFromID(this.lifecycleID);
        String lcName = lifecycleStateBean.getLcName();
        String lcContent;
            lcContent = LifecycleUtils.getLifecycleConfiguration(lcName);
        currentLifecycleState.setLcName(lcName);
        currentLifecycleState.setLifecycleId(this.lifecycleID);
        currentLifecycleState.setState(lifecycleStateBean.getPostStatus());
        populateItems(currentLifecycleState, lcContent);
        this.currentLifecycleState = currentLifecycleState;
    }

    /**
     * This method need to call for each and event life cycle state changes.
     *
     * @param action                            {@code String} lifecycle action.
     * @param nextState                         {@code LifecycleState} object represent next life cycle state.
     * @param user                              The user who invoked the action. This will be used for auditing
     *                                          purposes.
     * @return                                  {@code LifecycleState} object of updated life cycle state.
     * @throws LifecycleException      if exception occurred while execute life cycle update.
     */
    public LifecycleState executeLifecycleEvent(LifecycleState nextState, String action, String user, Object resource)
            throws LifecycleException {
        LifecycleState currentState = this.currentLifecycleState;
        String lcName = currentState.getLcName();
        String lcContent;
            lcContent = LifecycleUtils.getLifecycleConfiguration(lcName);
        // identifies the state change operation
        if (!CHECKLIST_ITEM_CLICK.equals(action)) {
            runCustomExecutorsCode(action, resource, currentState.getCustomCodeBeanList(), currentState.getState(),
                    nextState.getState());
            populateItems(nextState, lcContent);
            nextState.setLcName(currentState.getLcName());
            nextState.setLifecycleId(currentState.getLifecycleId());
            LifecycleOperationUtil
                    .changeLifecycleState(currentState.getState(), nextState.getState(), this.lifecycleID, user);

        } else {           // identifies check list item selecting event
            // TODO : implement check list item logic here
        }
        this.currentLifecycleState = nextState;
        if (log.isDebugEnabled()) {
            log.debug("Lifecycle state was changed from " + currentState.getState() + "to " + nextState.getState()
                    + "for lifecycle id " + this.lifecycleID);
        }
        return nextState;
    }

    /**
     * This method is used to associate a lifecycle with an asset.
     *
     * @param lcName                        LC name which associates with the resource.
     * @param user                          The user who invoked the action. This will be used for auditing purposes.
     * @return                              Object of added life cycle state.
     * @throws LifecycleException  If failed to associate life cycle with asset.
     */
    public LifecycleState associateLifecycle(String lcName, String user)
            throws LifecycleException {
        LifecycleState lifecycleState;
            String lcContent = LifecycleUtils.getLifecycleConfiguration(lcName);
            lifecycleState = new LifecycleState();

            String initialState = getInitialState(LifecycleOperationUtil.getLifecycleElement(lcContent), lcName);
            lifecycleState.setLcName(lcName);
            lifecycleState.setState(initialState);
            populateItems(lifecycleState, lcContent);
            String lifecycleId = LifecycleOperationUtil.associateLifecycle(lcName, initialState, user);

            lifecycleState.setLifecycleId(lifecycleId);
            this.currentLifecycleState = lifecycleState;
            this.lifecycleID = lifecycleId;
            if (log.isDebugEnabled()) {
                log.debug("Id : " + lifecycleId + "associated with lifecycle " + lcName + "and initial state set to "
                        + initialState);
            }
        return lifecycleState;
    }

    /**
     * This method add state data like, transition inputs, custom executors etc to the lifecycle state object.
     *
     * @param lifecycleState                lc state object which is being populated.
     * @param lcConfig                      lc configuration.
     * @throws LifecycleException           If failed to get lifecycle list.
     */
    private void populateItems(LifecycleState lifecycleState, String lcConfig) throws LifecycleException {

        String lcState = lifecycleState.getState();
        Document document = LifecycleOperationUtil.getLifecycleElement(lcConfig);
        lifecycleState.setInputBeanList(populateTransitionInputs(document, lcState));
        lifecycleState.setCustomCodeBeanList(populateTransitionExecutors(document, lcState));
        lifecycleState.setAvailableTransitionBeanList(populateAvailableStates(document, lcState));
        lifecycleState.setPermissionBeanList(populateTransitionPermission(document, lcState));

    }

    /**
     * This method is used to run custom executor codes.
     *
     * @param action                        Current lc action (Promote/Demote)
     * @param resource                      The asset to which the lc is attached
     * @return                              success of execution class.
     * @throws LifecycleException  if failed to run custom executors.
     */
    private boolean runCustomExecutorsCode(String action, Object resource, List<CustomCodeBean> customCodeBeans,
            String currentState, String nextState) throws LifecycleException {
        if (customCodeBeans != null) {
            for (CustomCodeBean customCodeBean : customCodeBeans) {
                if (customCodeBean.getEventName().equals(action)) {
                    Executor customExecutor = (Executor) customCodeBean.getClassObject();

                    if (!customExecutor.execute(resource, currentState, nextState)) {
                        String message = "Execution failed for action : " + customCodeBean.getEventName() + "for the "
                                + "execution class " + customCodeBean.getClassObject().getClass();

                        customCodeBean.setCustomMessage(message);
                        throw new LifecycleException(message);
                    }
                }
            }
        }
        return true;
    }

}

