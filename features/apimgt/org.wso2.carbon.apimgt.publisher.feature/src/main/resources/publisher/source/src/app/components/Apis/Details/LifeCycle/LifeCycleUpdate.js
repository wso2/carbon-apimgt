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
'use strict';

import React, {Component} from 'react'

import API from '../../../../data/api'
import {ScopeValidation , resourceMethod, resourcePath} from '../../../../data/ScopeValidation'
import ApiPermissionValidation from '../../../../data/ApiPermissionValidation'
import Alert from '../../../Shared/Alert'

import Button from 'material-ui/Button';
import Typography from 'material-ui/Typography';
import Grid from 'material-ui/Grid'
import { FormGroup, FormControlLabel } from 'material-ui/Form';
import Checkbox from 'material-ui/Checkbox';
import LifeCycleImage from './LifeCycleImage';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';

const styles = theme => ({
    buttonsWrapper: {
        marginTop: 40
    },
});

 class LifeCycleUpdate extends Component {
    constructor() {
        super();
        this.updateLifeCycleState = this.updateLifeCycleState.bind(this);
        this.api = new API();
        this.state = {
            checkList: []
        };
    }
    componentDidMount(){
        const {lcState} = this.props;
        const {privateJetModeEnabled} = this.props;
        const checkList = [];
        let index = 0;
        for (let item of lcState.checkItemBeanList) {
            checkList.push({index: index, label: item.name, value: item.name, checked: false});
            index++;
        }
        this.setState({checkList});
    }
    componentWillReceiveProps(nextProps){
        if(this.props.api.lifeCycleStatus !== nextProps.api.lifeCycleStatus){
            const checkList = [];
            let index = 0;
            for (let item of nextProps.lcState.checkItemBeanList) {
                checkList.push({index: index, label: item.name, value: item.name, checked: false});
                index++;
            }
            this.setState({checkList});
        }
    }

    updateLCStateOfAPI(apiUUID, newState){

        let promisedUpdate;
        const lifecycleChecklist = this.state.checkList.map(item => item.value + ":" + item.checked);
         if (lifecycleChecklist.length > 0) {
             promisedUpdate = this.api.updateLcState(apiUUID, newState, lifecycleChecklist);
         } else {
             promisedUpdate = this.api.updateLcState(apiUUID, newState);
         }
         promisedUpdate.then(response => { /*TODO: Handle IO erros ~tmkb*/
             this.props.handleUpdate(true);
             Alert.info("Lifecycle state updated successfully");
             /*TODO: add i18n ~tmkb*/
         }).catch(
             error_response => {
                 console.log(error_response);
                 Alert.error(JSON.stringify(error_response));
         });
    }

    updateLifeCycleState(event) {
        event.preventDefault();
        let newState = event.currentTarget.getAttribute("data-value");
        const apiUUID = this.props.api.id;
        const {privateJetModeEnabled} = this.props;

        if (privateJetModeEnabled) {
            if(newState == "Published In Private Jet Mode") {
                let promised_hasOwnGatewayForAPI = this.api.getHasOwnGateway(apiUUID);

                promised_hasOwnGatewayForAPI.then(getResponse => {
                    let hasOwnGatewayForAPI = getResponse.body.isEnabled;

                    if (!hasOwnGatewayForAPI) {
                        newState = "Published";
                        let body = {"isEnabled":"true"};
                        let promisedUpdateDedicatedGW = this.api.updateHasOwnGateway(apiUUID, body);

                        promisedUpdateDedicatedGW.then(response => {
                            Alert.info("Dedicated Gateway status updated successfully");
                            this.updateLCStateOfAPI(apiUUID, newState);
                        }).catch(
                        error_response => {
                            console.log(error_response);
                            Alert.error(JSON.stringify(error_response));
                        });
                    }
                }).catch(
                error_response => {
                    console.log(error_response);
                    Alert.error(JSON.stringify(error_response));
                });

             } else if(newState == "Deprecated" || newState == "Retired") {
                this.updateLCStateOfAPI(apiUUID, newState);

             } else {
                    let promisedUpdate;
                    const lifecycleChecklist = this.state.checkList.map(item => item.value + ":" + item.checked);
                    if (lifecycleChecklist.length > 0) {
                        promisedUpdate = this.api.updateLcState(apiUUID, newState, lifecycleChecklist);
                    } else {
                        promisedUpdate = this.api.updateLcState(apiUUID, newState);
                    }
                    promisedUpdate.then(response => {
                        Alert.info("Lifecycle state updated successfully");

                        let promised_hasOwnGatewayForAPI = this.api.getHasOwnGateway(apiUUID);
                        let that=this;
                        promised_hasOwnGatewayForAPI.then(function(result) {

                          let hasOwnGatewayForAPI = result.body.isEnabled;
                          if (hasOwnGatewayForAPI) {
                              let body = {"isEnabled":"false"};
                              let promisedUpdateDedicatedGW = that.api.updateHasOwnGateway(apiUUID, body);

                              promisedUpdateDedicatedGW.then(response => {
                                that.props.handleUpdate(true);
                                Alert.info("Dedicated Gateway status updated successfully");
                              }).catch(
                                error_response => {
                                  console.log(error_response);
                                  Alert.error(JSON.stringify(error_response));
                              });
                          }
                          that.props.handleUpdate(true);
                        }, function(err) {
                          console.log(err);
                        });

                    }).catch(
                        error_response => {
                         console.log(error_response);
                         Alert.error(JSON.stringify(error_response));
                    });
             }
        } else {
            this.updateLCStateOfAPI(apiUUID, newState);
        }
    }

    handleChange = index => (event, checked) => {
        let checkList = this.state.checkList;
        checkList[index].checked = checked;
        this.setState({ checkList });
    };

    render() {
        const {api,lcState} = this.props;
        const is_workflow_pending = api.workflowStatus.toLowerCase() === "pending";
        const { classes } = this.props;
        return (
                <Grid container>
                {
                    is_workflow_pending ?
                        (
                            <Grid item xs={12}>
                                    <Typography type="headline" component="h2">
                                        Pending lifecycle state change.
                                    </Typography>
                                    <Typography type="body1">
                                        adjective
                                    </Typography>
                            </Grid>

                        ) :
                        (
                            <Grid item xs={12}>
                                <LifeCycleImage  lifeCycleStatus={api.lifeCycleStatus} />
                            </Grid>

                        )
                }
                    <Grid item xs={12}>
                {
                    !is_workflow_pending &&
                    <FormGroup row>
                        {this.state.checkList.map( (checkItem, index)  => <FormControlLabel
                            key={index}
                            control={
                                <Checkbox
                                    checked={this.state.checkList[index].checked}
                                    onChange={this.handleChange(index)}
                                    value={this.state.checkList[index].value}
                                />
                            }
                            label={this.state.checkList[index].label}
                        />)}

                    </FormGroup>
                }
                        <ScopeValidation resourcePath={resourcePath.API_CHANGE_LC} resourceMethod={resourceMethod.POST}>
                        <ApiPermissionValidation userPermissions={api.userPermissionsForApi}>
                            <div className={classes.buttonsWrapper}>
                                {
                                    is_workflow_pending ?
                                        (
                                            <div className="btn-group" role="group">
                                                <input type="button" className="btn btn-primary wf-cleanup-btn"
                                                       defaultValue="Delete pending lifecycle state change request"/>
                                            </div>
                                        ) :
                                        (
                                            lcState.availableTransitionBeanList.map(
                                                transition_state => lcState.state !== transition_state.targetState &&
                                                <Button key={transition_state.targetState} raised data-value={transition_state.targetState}
                                                        onClick={this.updateLifeCycleState}>
                                                    {transition_state.event}
                                                </Button>
                                            ) /* Skip when transitions available for current state , this occurs in states where have allowed re-publishing in prototype and published sates*/
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
};

export default withStyles(styles)(LifeCycleUpdate);