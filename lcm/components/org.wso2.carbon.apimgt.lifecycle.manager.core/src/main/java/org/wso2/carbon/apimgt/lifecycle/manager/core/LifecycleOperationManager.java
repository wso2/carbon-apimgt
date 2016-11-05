package org.wso2.carbon.apimgt.lifecycle.manager.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.apimgt.lifecycle.manager.core.beans.CheckItemBean;
import org.wso2.carbon.apimgt.lifecycle.manager.core.beans.CustomCodeBean;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.impl.LifecycleState;
import org.wso2.carbon.apimgt.lifecycle.manager.core.util.LifecycleOperationUtil;
import org.wso2.carbon.apimgt.lifecycle.manager.core.util.LifecycleUtils;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LifecycleStateBean;

import java.util.List;

import static org.wso2.carbon.apimgt.lifecycle.manager.core.util.LifecycleOperationUtil.changeCheckListItem;
import static org.wso2.carbon.apimgt.lifecycle.manager.core.util.LifecycleOperationUtil.getInitialState;
import static org.wso2.carbon.apimgt.lifecycle.manager.core.util.LifecycleOperationUtil.populateItems;
import static org.wso2.carbon.apimgt.lifecycle.manager.core.util.LifecycleOperationUtil.removeLifecycleStateData;

/**
 * This is the class provides all the logic related to lifecycle operations. (Associate, Dissociate, State change
 * event and Check list item event)
 */
public class LifecycleOperationManager {

    private static Logger log = LoggerFactory.getLogger(LifecycleOperationManager.class);

    /**
     * This method need to call for each and event life cycle state changes.
     *
     * @param targetState                       {@code String} Required target state.
     * @param uuid                              {@code String} Lifecycle id that maps with the asset.
     * @param resource                          {@code Object} The current object to which lifecycle is attached to.
     * @param user                              The user who invoked the action. This will be used for auditing
     *                                          purposes.
     * @return                                  {@code LifecycleState} object of updated life cycle state.
     * @throws LifecycleException               If exception occurred while execute life cycle state change.
     */
    protected static LifecycleState executeLifecycleEvent(String targetState, String uuid, String user, Object resource)
            throws LifecycleException {
        LifecycleState nextState = new LifecycleState();
        LifecycleState currentState = LifecycleOperationUtil.getCurrentLifecycleState(uuid);
        if (!validateCheckListItemSelected(currentState, targetState)) {
            throw new LifecycleException(
                    "Required checklist items are not selected to perform the state transition " + "operation from "
                            + currentState.getState() + " to " + targetState);
        }
        String lcName = currentState.getLcName();
        Document lcContent = LifecycleUtils.getLifecycleConfiguration(lcName);
        runCustomExecutorsCode(resource, currentState.getCustomCodeBeanList(), currentState.getState(), targetState);
        populateItems(nextState, lcContent);
        nextState.setState(targetState);
        nextState.setLcName(currentState.getLcName());
        nextState.setLifecycleId(currentState.getLifecycleId());
        LifecycleOperationUtil.changeLifecycleState(currentState.getState(), targetState, uuid, user);

        if (log.isDebugEnabled()) {
            log.debug("Lifecycle state was changed from " + currentState.getState() + " to " + targetState
                    + " for lifecycle id " + uuid);
        }
        return nextState;
    }

    /**
     * This method need to call for each check list item operation.
     *
     * @param uuid                              Object that can use to uniquely identify resource.
     * @param currentState                      The state which the checklist item is associated with.
     * @param checkListItemName                 Name of the check list item as specified in the lc config.
     * @param value                             Value of the check list item. Either selected or not.
     *
     * @throws LifecycleException               If exception occurred while execute life cycle update.
     */
    protected static LifecycleState checkListItemEvent(String uuid, String currentState, String checkListItemName,
            boolean value) throws LifecycleException {
        changeCheckListItem(uuid, currentState, checkListItemName, value);
        LifecycleState currentStateObject = LifecycleOperationUtil.getCurrentLifecycleState(uuid);
        for (CheckItemBean checkItemBean : currentStateObject.getCheckItemBeanList()) {
            if (checkListItemName.equals(checkItemBean.getName())) {
                checkItemBean.setValue(value);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Check list item " + checkListItemName + " is set to " + value);
        }
        return currentStateObject;
    }


    /**
     * This method is used to associate a lifecycle with an asset.
     *
     * @param lcName                        LC name which associates with the resource.
     * @param user                          The user who invoked the action. This will be used for auditing purposes.
     * @return                              Object of added life cycle state.
     * @throws LifecycleException  If failed to associate life cycle with asset.
     */
    protected static LifecycleState associateLifecycle(String lcName, String user) throws LifecycleException {
        LifecycleState lifecycleState;
        Document lcContent = LifecycleUtils.getLifecycleConfiguration(lcName);
        lifecycleState = new LifecycleState();

        String initialState = getInitialState(lcContent, lcName);
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
    protected static void dissociateLifecycle(String uuid) throws LifecycleException {
        removeLifecycleStateData(uuid);
    }

    /**
     * Get current life cycle state object.
     *
     * @return {@code LifecycleState} object represent current life cycle.
     */
    public static LifecycleState getCurrentLifecycleState(String uuid) throws LifecycleException {
        LifecycleState currentLifecycleState = new LifecycleState();
        LifecycleStateBean lifecycleStateBean = LifecycleOperationUtil.getLCStateDataFromID(uuid);
        String lcName = lifecycleStateBean.getLcName();
        Document lcContent = LifecycleUtils.getLifecycleConfiguration(lcName);
        currentLifecycleState.setLcName(lcName);
        currentLifecycleState.setLifecycleId(uuid);
        currentLifecycleState.setState(lifecycleStateBean.getPostStatus());
        populateItems(currentLifecycleState, lcContent);
        LifecycleOperationUtil.setCheckListItemData(currentLifecycleState, lifecycleStateBean.getCheckListData());
        return currentLifecycleState;
    }


    /**
     * This method is used to run custom executor codes.
     *
     * @param resource                      The asset to which the lc is attached
     * @return                              success of execution class.
     * @throws LifecycleException  if failed to run custom executors.
     */
    private static boolean runCustomExecutorsCode(Object resource, List<CustomCodeBean> customCodeBeans,
            String currentState, String nextState) throws LifecycleException {
        if (customCodeBeans != null) {
            for (CustomCodeBean customCodeBean : customCodeBeans) {
                if (customCodeBean.getTargetName().equals(nextState)) {
                    Executor customExecutor = (Executor) customCodeBean.getClassObject();
                    customExecutor.execute(resource, currentState, nextState);
                }
            }
        }
        return true;
    }

    private static boolean validateCheckListItemSelected(LifecycleState lifecycleState, String nextState) {
        for (CheckItemBean checkItemBean : lifecycleState.getCheckItemBeanList()) {
            if (checkItemBean.getTargets().contains(nextState) && !checkItemBean.isValue()) {
                return false;
            }
        }
        return true;
    }
}

