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
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';

import API from 'AppData/api';
import { ScopeValidation, resourceMethod, resourcePath } from 'AppData/ScopeValidation';
import ApiPermissionValidation from 'AppData/ApiPermissionValidation';
import Alert from 'AppComponents/Shared/Alert';
import LifeCycleImage from './LifeCycleImage';


const styles = theme => ({
    buttonsWrapper: {
        marginTop: 40,
    },
    stateButton: {
        marginRight: theme.spacing.unit,
    },
});


/**
 *
 *
 * @class LifeCycleUpdate
 * @extends {Component}
 */
class LifeCycleUpdate extends Component {
    constructor() {
        super();
        this.updateLifeCycleState = this.updateLifeCycleState.bind(this);
        this.api = new API();
        this.state = {
            newState: null,
        };
    }


    /**
     *
     *
     * @param {*} apiUUID
     * @param {*} newState
     * @memberof LifeCycleUpdate
     */
    updateLCStateOfAPI(apiUUID, newState) {
        let promisedUpdate;
        const lifecycleChecklist = this.props.checkList.map(item => item.value + ':' + item.checked);
        if (lifecycleChecklist.length > 0) {
            promisedUpdate = this.api.updateLcState(apiUUID, newState, lifecycleChecklist);
        } else {
            promisedUpdate = this.api.updateLcState(apiUUID, newState);
        }
        promisedUpdate.then((response) => { /* TODO: Handle IO erros ~tmkb */
            this.props.handleUpdate(true);
            this.setState({ newState });
            Alert.info('Lifecycle state updated successfully');
            /* TODO: add i18n ~tmkb */
        }).catch((error_response) => {
            console.log(error_response);
            Alert.error(JSON.stringify(error_response));
        });
    }


    /**
     *
     *
     * @param {*} event
     * @memberof LifeCycleUpdate
     */
    updateLifeCycleState(event) {
        event.preventDefault();
        let newState = event.currentTarget.getAttribute('data-value');
        const apiUUID = this.props.api.id;
        const { privateJetModeEnabled } = this.props;
        if (privateJetModeEnabled) {
            if (newState == 'Published In Private Jet Mode') {
                const promised_hasOwnGatewayForAPI = this.api.getHasOwnGateway(apiUUID);

                promised_hasOwnGatewayForAPI.then((getResponse) => {
                    const hasOwnGatewayForAPI = getResponse.body.isEnabled;

                    if (!hasOwnGatewayForAPI) {
                        newState = 'Published';
                        const body = { isEnabled: 'true' };
                        const promisedUpdateDedicatedGW = this.api.updateHasOwnGateway(apiUUID, body);

                        promisedUpdateDedicatedGW.then((response) => {
                            Alert.info('Dedicated Gateway status updated successfully');
                            this.updateLCStateOfAPI(apiUUID, newState);
                        }).catch((error_response) => {
                            console.log(error_response);
                            Alert.error(JSON.stringify(error_response));
                        });
                    }
                }).catch((error_response) => {
                    console.log(error_response);
                    Alert.error(JSON.stringify(error_response));
                });
            } else if (newState == 'Deprecated' || newState == 'Retired') {
                this.updateLCStateOfAPI(apiUUID, newState);
            } else {
                let promisedUpdate;
                const lifecycleChecklist = this.props.checkList.map(item => item.value + ':' + item.checked);
                if (lifecycleChecklist.length > 0) {
                    promisedUpdate = this.api.updateLcState(apiUUID, newState, lifecycleChecklist);
                } else {
                    promisedUpdate = this.api.updateLcState(apiUUID, newState);
                }
                promisedUpdate.then((response) => {
                    Alert.info('Lifecycle state updated successfully');

                    const promised_hasOwnGatewayForAPI = this.api.getHasOwnGateway(apiUUID);
                    const that = this;
                    promised_hasOwnGatewayForAPI.then((result) => {
                        const hasOwnGatewayForAPI = result.body.isEnabled;
                        if (hasOwnGatewayForAPI) {
                            const body = { 'isEnabled': 'false' };
                            const promisedUpdateDedicatedGW = that.api.updateHasOwnGateway(apiUUID, body);

                            promisedUpdateDedicatedGW.then((response) => {
                                that.props.handleUpdate(true);
                                Alert.info('Dedicated Gateway status updated successfully');
                            }).catch((error_response) => {
                                    console.log(error_response);
                                    Alert.error(JSON.stringify(error_response));
                                });
                        }
                        that.props.handleUpdate(true);
                    }, (err) => {
                        console.log(err);
                    });
                }).catch((error_response) => {
                    console.log(error_response);
                    Alert.error(JSON.stringify(error_response));
                });
            }
        } else {
            this.updateLCStateOfAPI(apiUUID, newState);
        }
    }


    render() {
        const {
            api, lcState, classes, theme, handleChangeCheckList, checkList,
        } = this.props;
        const { newState } = this.state;
        const is_workflow_pending = api.workflowStatus.toLowerCase() === 'pending';
        return (
            <Grid container>
                {
                    is_workflow_pending ?
                        (
                            <Grid item xs={12}>
                                <Typography variant='h5'>
                                        Pending lifecycle state change.
                                </Typography>
                                <Typography>
                                        adjective
                                </Typography>
                            </Grid>

                        ) :
                        (
                            <Grid item xs={12}>
                                {theme.custom.lifeCycleImage ?
                                    <img src={theme.custom.lifeCycleImage} alt='Lifecycle image' />
                                    :
                                    <LifeCycleImage lifeCycleStatus={newState || api.lifeCycleStatus} />
                                }
                            </Grid>

                        )
                }
                <Grid item xs={12}>
                    {
                        !is_workflow_pending &&
                        <FormGroup row>
                        {checkList.map((checkItem, index) => (<FormControlLabel
                            key={index}
                            control={
                                <Checkbox
                                    checked={checkList[index].checked}
                                    onChange={handleChangeCheckList(index)}
                                    value={checkList[index].value}
                                />
                            }
                            label={checkList[index].label}
                        />))}

                    </FormGroup>
                    }
                    <ScopeValidation resourcePath={resourcePath.API_CHANGE_LC} resourceMethod={resourceMethod.POST}>
                        <ApiPermissionValidation userPermissions={api.userPermissionsForApi}>
                                <div className={classes.buttonsWrapper}>
                                {
                                    is_workflow_pending ?
                                        (
                                            <div className='btn-group' role='group'>
                                                <input
                                                                                                        type='button'
                                                                                                        className='btn btn-primary wf-cleanup-btn'
                                                    defaultValue='Delete pending lifecycle state change request'
                                                />
                                            </div>
                                        ) :
                                        (
                                            lcState.availableTransitionBeanList.map(transition_state => lcState.state !== transition_state.targetState &&
                                                <Button
                                                                                                        variant='outlined'
                                                                                                        className={classes.stateButton}
                                                                                                        key={transition_state.targetState}
                                                                                                        data-value={transition_state.targetState}
                                                    onClick={this.updateLifeCycleState}
                                                >
                                                    {transition_state.event}
                                                </Button> ) /* Skip when transitions available for current state , this occurs in states where have allowed re-publishing in prototype and published sates */
                                        )
                                }
                            </div>
                            </ApiPermissionValidation>
                    </ScopeValidation>
                </Grid>
            </Grid>
        );
    }
}

LifeCycleUpdate.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(LifeCycleUpdate);
