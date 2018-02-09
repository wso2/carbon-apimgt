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

import React from 'react'
import API from '../../../data/api'

import Card, {CardActions, CardContent, CardMedia} from 'material-ui/Card';
import {Redirect, Switch} from 'react-router-dom'
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import Dialog, {DialogActions, DialogContent, DialogContentText, DialogTitle,} from 'material-ui/Dialog';
import Slide from 'material-ui/transitions/Slide';
import Grid from 'material-ui/Grid';
import NotificationSystem from 'react-notification-system';
import {resourceMethod, resourcePath, ScopeValidation} from "../../../data/ScopeValidation";
import {LifeCycleStatus} from "../../../data/LifeCycle";
import Utils from "../../../data/Utils";
import Confirm from "../../Shared/Confirm";


class ApiThumb extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            active: true,
            loading: false,
            open: false,
            openUserMenu: false,
            overview_link: '',
            isRedirect: false,
            showRedirectConfirmDialog: false,
            redirectConfirmDialogDetails: {},
        };
        this.handleApiDelete = this.handleApiDelete.bind(this);
        this.handleRedirectToAPIOverview = this.handleRedirectToAPIOverview.bind(this);
        this.confirmDialogcallback = this.confirmDialogcallback.bind(this);
    }

    componentDidMount() {
        const lifeCycleStatus = this.props.api.lifeCycleStatus;
        const lifeCycleStatusColor = LifeCycleStatus.filter(status => status.name === lifeCycleStatus)[0].color;
        this.setState({lifeCycleStatusColor});
    }

    handleRequestClose = () => {
        this.setState({openUserMenu: false});
    };

    handleRequestOpen = () => {
        this.setState({openUserMenu: true});
    };

    handleApiDelete(e) {
        this.setState({loading: true});
        const api = new API();
        const api_uuid = this.props.api.id;
        const name = this.props.api.name;
        let promised_delete = api.deleteAPI(api_uuid);
        promised_delete.then(
            response => {
                if (response.status !== 200) {
                    console.log(response);
                    this.refs.notificationSystem.addNotification({
                        message: 'Something went wrong while deleting the ' + name + ' API!', position: 'tc',
                        level: 'error'
                    });
                    this.setState({open: false, openUserMenu: false});
                    return;
                }
                this.refs.notificationSystem.addNotification({
                    message: name + ' API deleted Successfully', position: 'tc', level: 'success'
                });
                this.props.updateApi(api_uuid);
                this.setState({active: false, loading: false});
            }
        );
    }

    handleRedirectToAPIOverview() {
        const {api, environmentName, rootAPIVersion} = this.props;
        const currentEnvironmentName = Utils.getCurrentEnvironment().label;
        // If environment name or version is not defined then consider as same environment or version.
        const isSameEnvironment = !environmentName || environmentName === currentEnvironmentName;
        const isSameVersion = !rootAPIVersion || rootAPIVersion.version === api.version;

        if (isSameEnvironment && isSameVersion) {
            this.setState({
                overview_link: `/apis/${api.id}`,
                isRedirect: true
            });
        } else { // Ask for confirmation to switch environment or version of the API
            // Set details for the confirmation dialog
            const title = `Switch to ${api.name} ${api.version}` +
                `${isSameEnvironment ? '?' : ` in ${environmentName} Environment?`}`;
            const message = 'We are going to switch the ' +
                `${isSameEnvironment ? '' : `environment "${currentEnvironmentName}" to "${environmentName}"`}` +
                `${!isSameEnvironment && !isSameVersion ? ' and ' : ''}` +
                `${isSameVersion ? '' : `API version "${rootAPIVersion.version}" to "${api.version}"`}`;
            const labelCancel = 'Cancel';
            const labelOk = 'Switch';

            this.setState({
                overview_link: `/apis/${api.id}/overview?environment=${environmentName}`,
                isRedirect: false,
                showRedirectConfirmDialog: true,
                redirectConfirmDialogDetails: {title, message, labelCancel, labelOk},
            });
        }
    }

    confirmDialogcallback(result) {
        this.setState({
            isRedirect: result,
            showRedirectConfirmDialog: false
        });
    }

    render() {
        const {api, environmentOverview} = this.props;
        let heading, content;

        if (!this.state.active) { // Controls the delete state, We set the state to inactive on delete success call
            return null;
        }

        if (this.state.isRedirect) {
            return (
                <Switch>
                    <Redirect to={this.state.overview_link}/>
                </Switch>
            );
        }

        if (environmentOverview) { // API Thumb for "environment overview" page
            heading = api.version;
            content = (
                <Typography component="div">
                    <p>{api.context}</p>
                    <div style={{display: "flex"}}>
                        <div style={{
                            backgroundColor: this.state.lifeCycleStatusColor,
                            width: "20px",
                            height: "20px",
                            borderRadius: "50%",
                            marginRight: "5px"
                        }}/>
                        {api.lifeCycleStatus}
                    </div>
                </Typography>
            );
        } else { // Standard API Thumb view for "API listing" page
            heading = api.name;
            content = (
                <Typography component="div">
                    <p>{api.version}</p>
                    <p>{api.context}</p>
                    <p className="description">{api.description}</p>
                </Typography>
            );
        }

        return (
            <Grid item xs={6} sm={4} md={3} lg={2} xl={2}>
                <Card>
                    <CardMedia image="/publisher/public/app/images/api/api-default.png">
                        <img src="/publisher/public/app/images/api/api-default.png" style={{width: "100%"}}/>
                    </CardMedia>
                    <CardContent>
                        <Typography type="headline" component="h2">
                            {heading}
                        </Typography>
                        {content}
                    </CardContent>
                    <CardActions>
                        <Button onClick={this.handleRedirectToAPIOverview} dense color="primary">
                            More...
                        </Button>

                        {/*Do not render for environment overview page*/}
                        {!environmentOverview ?
                            <div>
                                <ScopeValidation resourcePath={resourcePath.SINGLE_API}
                                                 resourceMethod={resourceMethod.DELETE}>
                                    <Button dense color="primary" onClick={this.handleRequestOpen}>Delete</Button>
                                </ScopeValidation>
                                <Dialog open={this.state.openUserMenu} transition={Slide}
                                        onRequestClose={this.handleRequestClose}>
                                    <DialogTitle>
                                        {"Confirm"}
                                    </DialogTitle>
                                    <DialogContent>
                                        <DialogContentText>
                                            Are you sure you want to delete the API ({api.name} - {api.version})?
                                        </DialogContentText>
                                    </DialogContent>
                                    <DialogActions>
                                        <Button dense color="primary" onClick={this.handleApiDelete}>
                                            <NotificationSystem ref="notificationSystem"/>Delete
                                        </Button>
                                        <Button dense color="primary" onClick={this.handleRequestClose}>
                                            Cancel
                                        </Button>
                                    </DialogActions>
                                </Dialog>
                            </div>
                            :
                            <div/>
                        }
                    </CardActions>
                </Card>
                <Confirm
                    {...this.state.redirectConfirmDialogDetails}
                    callback={this.confirmDialogcallback}
                    open={this.state.showRedirectConfirmDialog}
                />
            </Grid>
        );
    }
}

const SwitchEnvOrVersion = {
    DO: 'do',
    DO_NOT: 'do_not',
    CONFIRM: 'confirm'
};

export default ApiThumb;
