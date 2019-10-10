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
import { ScopeValidation, resourceMethod, resourcePath } from 'AppData/ScopeValidation';
import Alert from 'AppComponents/Shared/Alert';
import LifeCycleImage from './LifeCycleImage';
import CheckboxLabels from './CheckboxLabels';
import LifecyclePending from './LifecyclePending';

const styles = theme => ({
    buttonsWrapper: {
        marginTop: 40,
    },
    stateButton: {
        marginRight: theme.spacing.unit,
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
        };
    }

    /**
     * @param {*} apiUUID api UUID
     * @param {*} action life cycle action
     * @memberof LifeCycleUpdate
     */
    updateLCStateOfAPI(apiUUID, action) {
        let promisedUpdate;
        const lifecycleChecklist = this.props.checkList.map(item => item.value + ':' + item.checked);
        if (lifecycleChecklist.length > 0) {
            promisedUpdate = this.api.updateLcState(apiUUID, action, lifecycleChecklist);
        } else {
            promisedUpdate = this.api.updateLcState(apiUUID, action);
        }
        promisedUpdate
            .then((response) => {
                /* TODO: Handle IO erros ~tmkb */
                this.props.handleUpdate(true);
                const newState = response.body.lifecycleState.state;
                const { workflowStatus } = response.body;
                this.context.updateAPI();
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
                /* TODO: add i18n ~tmkb */
            })
            .catch((errorResponse) => {
                console.log(errorResponse);
                Alert.error(JSON.stringify(errorResponse.message));
            });
    }

    /**
     *
     *
     * @param {*} event event
     * @memberof LifeCycleUpdate
     */
    updateLifeCycleState(event) {
        event.preventDefault();
        const action = event.currentTarget.getAttribute('data-value');
        const {
            api: { id: apiUUID },
        } = this.props;
        this.updateLCStateOfAPI(apiUUID, action);
    }


    /**
     * @inheritdoc
     * @memberof LifeCycleUpdate
     */
    render() {
        const {
            api, lcState, classes, theme, handleChangeCheckList, checkList,
        } = this.props;
        const lifecycleStates = [...lcState.availableTransitions];
        const { newState } = this.state;
        const isWorkflowPending = api.workflowStatus && api.workflowStatus === this.WORKFLOW_STATUS.CREATED;
        const lcMap = new Map();
        lcMap.set('Published', 'Publish');
        lcMap.set('Prototyped', 'Deploy as a prototype');
        lcMap.set('Deprecated', 'Deprecate');
        lcMap.set('Blocked', 'Block');
        lcMap.set('Created', 'Create');
        lcMap.set('Retired', 'Retire');
        const isPrototype = api.endpointConfig && api.endpointConfig.implementation_status === 'prototyped';
        const lifecycleButtons = lifecycleStates
            .filter(item => item.event !== lcMap.get(lcState.state))
            .map((item) => {
                if (item.event === 'Deploy as a Prototype') {
                    return {
                        ...item,
                        disabled: !isPrototype || api.endpointConfig == null,
                    };
                }
                if (item.event === 'Publish') {
                    return {
                        ...item,
                        disabled: api.endpointConfig == null || api.policies.length === 0,
                    };
                }
                return {
                    ...item,
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
                            <img src={theme.custom.lifeCycleImage} alt='life cycles' />
                        ) : (
                            <Grid container spacing={3}>
                                <Grid item xs={8}>
                                    <LifeCycleImage lifeCycleStatus={newState || api.lifeCycleStatus} />
                                </Grid>
                                {(api.lifeCycleStatus === 'CREATED' || api.lifeCycleStatus === 'PUBLISHED' ||
                                api.lifeCycleStatus === 'PROTOTYPED') && (
                                    <Grid item xs={4}>
                                        <CheckboxLabels api={api} />
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
                                    control={
                                        <Checkbox
                                            checked={checkList[index].checked}
                                            onChange={handleChangeCheckList(index)}
                                            value={checkList[index].value}
                                        />
                                    }
                                    label={checkList[index].label}
                                />
                            ))}
                        </FormGroup>
                    )}
                    <ScopeValidation resourcePath={resourcePath.API_CHANGE_LC} resourceMethod={resourceMethod.POST}>
                        <div className={classes.buttonsWrapper}>
                            {!isWorkflowPending && (
                                lifecycleButtons.map((transitionState) => {
                                    return (
                                        <Button
                                            disabled={transitionState.disabled}
                                            variant='outlined'
                                            className={classes.stateButton}
                                            key={transitionState.event}
                                            data-value={transitionState.event}
                                            onClick={this.updateLifeCycleState}
                                        >
                                            {transitionState.event}
                                        </Button>
                                    );
                                }))
                            /* Skip when transitions available for current state ,
                            this occurs in states where have allowed re-publishing in prototype and published sates */
                            }
                        </div>
                    </ScopeValidation>
                </Grid>
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
    handleUpdate: PropTypes.func.isRequired,
    theme: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

LifeCycleUpdate.contextType = ApiContext;

export default withStyles(styles, { withTheme: true })(injectIntl(LifeCycleUpdate));
