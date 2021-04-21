/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React, { Component } from 'react';
import Button from '@material-ui/core/Button';
import Grid from '@material-ui/core/Grid';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import ApiContext from 'AppComponents/Apis/Details/components/ApiContext';
import { injectIntl } from 'react-intl';
import API from 'AppData/api';
import { CircularProgress } from '@material-ui/core';
import { ScopeValidation, resourceMethod, resourcePath } from 'AppData/ScopeValidation';
import Alert from 'AppComponents/Shared/Alert';
import Banner from 'AppComponents/Shared/Banner';
import PublishWithoutDeploy from 'AppComponents/Apis/Details/LifeCycle/Components/PublishWithoutDeploy';
import Configurations from 'Config';
import LifeCycleImage from './LifeCycleImage';
import CheckboxLabels from './CheckboxLabels';
import LifecyclePending from './LifecyclePending';
import { API_SECURITY_MUTUAL_SSL_MANDATORY, API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY }
    from '../Configuration/components/APISecurity/components/apiSecurityConstants';

const styles = (theme) => ({
    buttonsWrapper: {
        marginTop: 40,
    },
    stateButton: {
        marginRight: theme.spacing(),
    },
    paperCenter: {
        padding: theme.spacing(2),
        display: 'flex',
        alignItems: 'left',
        justifyContent: 'left',
    },
    subHeading: {
        fontSize: '1rem',
        fontWeight: 400,
        margin: 0,
        display: 'inline-flex',
        lineHeight: '38px',
    },
});

/**
 *
 *
 * @class LifeCycleUpdate
 * @extends {Component}
 */
class LifeCycleUpdate extends Component {
    /**
     * @param {*} props @inheritdoc
     */
    constructor(props) {
        super(props);
        this.updateLifeCycleState = this.updateLifeCycleState.bind(this);
        this.api = new API();
        this.WORKFLOW_STATUS = {
            CREATED: 'CREATED',
            APPROVED: 'APPROVED',
        };
        this.state = {
            newState: null,
            isUpdating: null,
            pageError: null,
            isOpen: false,
            deploymentsAvailable: false,
        };
        this.setIsOpen = this.setIsOpen.bind(this);
        this.handleClick = this.handleClick.bind(this);
    }

    /**
     *
     * Set Deployment availability
     */
    componentDidMount() {
        const {
            api: { id: apiUUID },
        } = this.props;
        this.api.getRevisionsWithEnv(apiUUID).then((result) => {
            this.setState({ deploymentsAvailable: result.body.count > 0 });
        });
    }

    /**
     *
     * Set isOpen state of the dialog box which shows the caution message when publish without deploying
     * @param {Boolean} isOpen Should dialog box is open or not
     */
    setIsOpen(isOpen) {
        this.setState({ isOpen });
    }

    /**
     * @param {*} apiUUID api UUID
     * @param {*} action life cycle action
     * @memberof LifeCycleUpdate
     */
    updateLCStateOfAPI(apiUUID, action) {
        this.setState({ isUpdating: action });
        let promisedUpdate;
        const lifecycleChecklist = this.props.checkList.map((item) => item.value + ':' + item.checked);
        if (lifecycleChecklist.length > 0) {
            promisedUpdate = this.api.updateLcState(apiUUID, action, lifecycleChecklist);
        } else {
            promisedUpdate = this.api.updateLcState(apiUUID, action);
        }
        promisedUpdate
            .then((response) => {
                /* TODO: Handle IO erros ~tmkb */
                // get the latest state of the API
                this.context.updateAPI();
                const newState = response.body.lifecycleState.state;
                const { workflowStatus } = response.body;
                this.setState({ newState });
                const { intl } = this.props;

                if (workflowStatus === this.WORKFLOW_STATUS.CREATED) {
                    Alert.info(intl.formatMessage({
                        id: 'Apis.Details.LifeCycle.LifeCycleUpdate.success.createStatus',
                        defaultMessage: 'Lifecycle state change request has been sent',
                    }));
                } else {
                    Alert.info(intl.formatMessage({
                        id: 'Apis.Details.LifeCycle.LifeCycleUpdate.success.otherStatus',
                        defaultMessage: 'Lifecycle state updated successfully',
                    }));
                }
            })
            .catch((error) => {
                if (error.response) {
                    Alert.error(error.response.body.description);
                    this.setState({ pageError: error.response.body });
                } else {
                    // TODO add i18n ~tmkb
                    const message = 'Something went wrong while updating the lifecycle';
                    Alert.error(message);
                    this.setState({ pageError: error.response.body });
                }
                console.error(error);
            })
            .finally(() => {
                this.setState({ isUpdating: null });
            });
    }

    /**
     *
     * Set handle click warning
     */
    handleClick() {
        const {
            api: { id: apiUUID },
        } = this.props;
        this.setIsOpen(false);
        this.updateLCStateOfAPI(apiUUID, 'Publish');
    }

    /**
     *
     *
     * @param {*} event event
     * @memberof LifeCycleUpdate
     */
    updateLifeCycleState(event) {
        const { deploymentsAvailable } = this.state;
        event.preventDefault();
        let action = event.currentTarget.getAttribute('data-value');
        if (action === 'Deploy To Test') {
            action = 'Deploy as a Prototype';
        }
        const {
            api: { id: apiUUID },
        } = this.props;
        if (action === 'Publish' && !deploymentsAvailable) {
            this.setIsOpen(true);
        } else {
            this.updateLCStateOfAPI(apiUUID, action);
        }
    }

    /**
     * @inheritdoc
     * @memberof LifeCycleUpdate
     */
    render() {
        const {
            api, lcState, classes, theme, handleChangeCheckList, checkList, certList,
        } = this.props;
        const lifecycleStates = [...lcState.availableTransitions];
        const { newState, pageError, isOpen } = this.state;
        const isWorkflowPending = api.workflowStatus && api.workflowStatus === this.WORKFLOW_STATUS.CREATED;
        const lcMap = new Map();
        lcMap.set('Published', 'Publish');
        lcMap.set('Prototyped', 'Deploy as a prototype');
        lcMap.set('Deprecated', 'Deprecate');
        lcMap.set('Blocked', 'Block');
        lcMap.set('Created', 'Create');
        lcMap.set('Retired', 'Retire');
        const isPrototype = api.endpointConfig && api.endpointConfig.implementation_status === 'prototyped';
        const isMutualSSLEnabled = api.securityScheme.includes(API_SECURITY_MUTUAL_SSL_MANDATORY);
        const isAppLayerSecurityMandatory = api.securityScheme.includes(
            API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY,
        );
        const isCertAvailable = certList.length !== 0;
        const isBusinessPlanAvailable = api.policies.length !== 0;
        const lifecycleButtons = lifecycleStates.map((item) => {
            const state = { ...item, displayName: item.event };
            if (state.event === 'Deploy as a Prototype') {
                if (state.displayName === 'Deploy as a Prototype') {
                    state.displayName = 'Prototype';
                }
                return {
                    ...state,
                    disabled: !isPrototype || (api.type !== 'WEBSUB' && api.endpointConfig == null),
                };
            }
            if (state.event === 'Publish') {
                return {
                    ...state,
                    disabled:
                        (api.type !== 'WEBSUB' && api.endpointConfig === null)
                        || (isMutualSSLEnabled && !isCertAvailable)
                        || (isAppLayerSecurityMandatory && !isBusinessPlanAvailable)
                        || (api.type !== 'WEBSUB' && api.endpointConfig != null
                            && api.endpointConfig.implementation_status === 'prototyped'),
                };
            }
            return {
                ...state,
                disabled: false,
            };
        });

        return (
            <Grid container>
                {isWorkflowPending ? (
                    <Grid item xs={12}>
                        <LifecyclePending currentState={lcState.state} />
                    </Grid>
                ) : (
                    <Grid item xs={12}>
                        {theme.custom.lifeCycleImage ? (
                            <img
                                src={Configurations.app.context + theme.custom.lifeCycleImage}
                                alt='life cycles'
                            />
                        ) : (
                            <Grid container spacing={3}>
                                <Grid item xs={8}>
                                    <LifeCycleImage lifeCycleStatus={newState || api.lifeCycleStatus} />
                                </Grid>
                                {(api.lifeCycleStatus === 'CREATED'
                                    || api.lifeCycleStatus === 'PROTOTYPED') && (
                                    <Grid item xs={3}>
                                        <CheckboxLabels
                                            api={api}
                                            isMutualSSLEnabled={isMutualSSLEnabled}
                                            isAppLayerSecurityMandatory={isAppLayerSecurityMandatory}
                                            isCertAvailable={isCertAvailable}
                                            isBusinessPlanAvailable={isBusinessPlanAvailable}
                                        />
                                    </Grid>
                                )}
                            </Grid>
                        )}
                    </Grid>
                )}
                <Grid item xs={12}>
                    {!isWorkflowPending && (
                        <FormGroup row>
                            {checkList.map((checkItem, index) => (
                                <FormControlLabel
                                    key={checkList[index].value}
                                    control={(
                                        <Checkbox
                                            checked={checkList[index].checked}
                                            onChange={handleChangeCheckList(index)}
                                            value={checkList[index].value}
                                            color='primary'
                                        />
                                    )}
                                    label={checkList[index].label}
                                />
                            ))}
                        </FormGroup>
                    )}
                    <ScopeValidation resourcePath={resourcePath.API_CHANGE_LC} resourceMethod={resourceMethod.POST}>
                        <div className={classes.buttonsWrapper}>
                            {!isWorkflowPending
                                && lifecycleButtons.map((transitionState) => {
                                    /* Skip when transitions available for current state ,
                            this occurs in states where have allowed re-publishing in prototype and published sates */
                                    return (
                                        <Button
                                            disabled={transitionState.disabled
                                                || this.state.isUpdating || api.isRevision}
                                            variant='contained'
                                            color='primary'
                                            className={classes.stateButton}
                                            key={transitionState.event}
                                            data-value={transitionState.event}
                                            onClick={this.updateLifeCycleState}
                                        >
                                            {transitionState.displayName}
                                            {this.state.isUpdating === transitionState.event && (
                                                <CircularProgress size={18} />
                                            )}
                                        </Button>
                                    );
                                })}
                        </div>
                    </ScopeValidation>
                </Grid>
                {/* Page error banner */}
                {pageError && (
                    <Grid item xs={11}>
                        <Banner
                            onClose={() => this.setState({ pageError: null })}
                            disableActions
                            dense
                            paperProps={{ elevation: 1 }}
                            type='error'
                            message={pageError}
                        />
                    </Grid>
                )}
                {/* end of Page error banner */}
                <PublishWithoutDeploy
                    apiID={api.id}
                    handleClick={this.handleClick}
                    handleClose={() => this.setIsOpen(false)}
                    open={isOpen}
                />
            </Grid>
        );
    }
}

LifeCycleUpdate.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
    checkList: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
    lcState: PropTypes.shape({}).isRequired,
    handleChangeCheckList: PropTypes.func.isRequired,
    theme: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

LifeCycleUpdate.contextType = ApiContext;

export default withStyles(styles, { withTheme: true })(injectIntl(LifeCycleUpdate));
