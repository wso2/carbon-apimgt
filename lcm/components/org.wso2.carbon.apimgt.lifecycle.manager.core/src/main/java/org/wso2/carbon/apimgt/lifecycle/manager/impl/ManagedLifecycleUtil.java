package org.wso2.carbon.apimgt.lifecycle.manager.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.apimgt.lifecycle.manager.core.util.LifecycleUtils;
import org.wso2.carbon.apimgt.lifecycle.manager.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.beans.CustomCodeBean;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LifecycleOperationUtil;
import org.wso2.carbon.apimgt.lifecycle.manager.interfaces.Executor;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LifecycleStateBean;

import java.util.List;

import static org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LifecycleOperationUtil.getInitialState;
import static org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LifecycleOperationUtil.populateAvailableStates;
import static org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LifecycleOperationUtil.populateTransitionExecutors;
import static org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LifecycleOperationUtil.populateTransitionInputs;
import static org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LifecycleOperationUtil.populateTransitionPermission;
import static org.wso2.carbon.apimgt.lifecycle.manager.impl.util.LifecycleOperationUtil.removeLifecycleStateData;

/**
 * This is the base ManagedLifecycle class. If users need to extend life cycle management
 * feature to any of the class they created they can extend this class and implement required
 * stuff.
 */
public class ManagedLifecycleUtil {

    public static final String CHECKLIST_ITEM_CLICK = "checklistItemClick";
    private static Logger log = LoggerFactory.getLogger(ManagedLifecycleUtil.class);


    /**
     * Get current life cycle state object.
     *
     * @return {@code LifecycleState} object represent current life cycle.
     */
    public static LifecycleState getCurrentLifecycleState(String uuid) throws LifecycleException {
        return setCurrentLifecycleState(new LifecycleState(), uuid);
    }

    /**
     * This method will be used to set life cycle state of current object
     *
     * @param currentLifecycleState {@code LifecycleState} object which holds
     *                              current life cycle state data.
     */
    private static LifecycleState setCurrentLifecycleState(LifecycleState currentLifecycleState, String uuid)
            throws LifecycleException {
        LifecycleStateBean lifecycleStateBean = LifecycleOperationUtil.getLCStateDataFromID(uuid);
        String lcName = lifecycleStateBean.getLcName();
        String lcContent;
        lcContent = LifecycleUtils.getLifecycleConfiguration(lcName);
        currentLifecycleState.setLcName(lcName);
        currentLifecycleState.setLifecycleId(uuid);
        currentLifecycleState.setState(lifecycleStateBean.getPostStatus());
        populateItems(currentLifecycleState, lcContent);
        return currentLifecycleState;
    }

    /**
     * This method need to call for each and event life cycle state changes.
     *
     * @param action                            {@code String} lifecycle action.
     * @param nextState                         {@code LifecycleState} object represent next life cycle state.
     * @param uuid                              {@code String} Lifecycle id that maps with the asset.
     * @param resource                          {@code Object} The current object to which lifecycle is attached to.
     * @param user                              The user who invoked the action. This will be used for auditing
     *                                          purposes.
     * @return                                  {@code LifecycleState} object of updated life cycle state.
     * @throws LifecycleException               If exception occurred while execute life cycle state change.
     */
    public static LifecycleState executeLifecycleEvent(LifecycleState nextState, String uuid, String action,
            String user, Object resource) throws LifecycleException {
        LifecycleState currentState = getCurrentLifecycleState(uuid);
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
                    .changeLifecycleState(currentState.getState(), nextState.getState(), uuid, user);

        } else {           // identifies check list item selecting event
            // TODO : implement check list item logic here
        }
        if (log.isDebugEnabled()) {
            log.debug("Lifecycle state was changed from " + currentState.getState() + "to " + nextState.getState()
                    + "for lifecycle id " + uuid);
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
    public static LifecycleState associateLifecycle(String lcName, String user) throws LifecycleException {
        LifecycleState lifecycleState;
        String lcContent = LifecycleUtils.getLifecycleConfiguration(lcName);
        lifecycleState = new LifecycleState();

        String initialState = getInitialState(LifecycleOperationUtil.getLifecycleElement(lcContent), lcName);
        lifecycleState.setLcName(lcName);
        lifecycleState.setState(initialState);
        populateItems(lifecycleState, lcContent);
        String lifecycleId = LifecycleOperationUtil.associateLifecycle(lcName, initialState, user);

        lifecycleState.setLifecycleId(lifecycleId);
        if (log.isDebugEnabled()) {
            log.debug("Id : " + lifecycleId + "associated with lifecycle " + lcName + "and initial state set to "
                    + initialState);
        }
        return lifecycleState;
    }

    /**
     * This method is used to detach a lifecycle from an asset.
     *
     * @param uuid                      Lifecycle id that maps with the asset.
     * @throws LifecycleException       If failed to associate life cycle with asset.
     */
    public static void dissociateLifecycle(String uuid) throws LifecycleException {
        removeLifecycleStateData(uuid);
    }

    /**
     * This method add state data like, transition inputs, custom executors etc to the lifecycle state object.
     *
     * @param lifecycleState                lc state object which is being populated.
     * @param lcConfig                      lc configuration.
     * @throws LifecycleException           If failed to get lifecycle list.
     */
    private static void populateItems(LifecycleState lifecycleState, String lcConfig) throws LifecycleException {

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
    private static boolean runCustomExecutorsCode(String action, Object resource, List<CustomCodeBean> customCodeBeans,
            String currentState, String nextState) throws LifecycleException {
        if (customCodeBeans != null) {
            for (CustomCodeBean customCodeBean : customCodeBeans) {
                if (customCodeBean.getEventName().equals(action)) {
                    Executor customExecutor = (Executor) customCodeBean.getClassObject();
                    customExecutor.execute(resource, currentState, nextState);
                }
            }
        }
        return true;
    }

}

