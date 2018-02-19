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
import Utils from "../../../data/Utils";
import ConfirmDialog from "../../Shared/ConfirmDialog";
import {withStyles} from 'material-ui/styles';

const styles = theme => ({
    lifeCycleState: {
        width: "1.5em",
        height: "1.5em",
        borderRadius: "50%",
        marginRight: "0.5em"
    },
    lifeCycleState_Created: {backgroundColor: "#0000ff"},
    lifeCycleState_Prototyped: {backgroundColor: "#42dfff"},
    lifeCycleState_Published: {backgroundColor: "#41830A"},
    lifeCycleState_Maintenance: {backgroundColor: "#cecece"},
    lifeCycleState_Deprecated: {backgroundColor: "#D7C850"},
    lifeCycleState_Retired: {backgroundColor: "#000000"},
});

class ApiThumb extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            active: true,
            loading: false,
            open: false,
            overview_link: '',
            isRedirect: false,
            openDeleteConfirmDialog: false,
            openRedirectConfirmDialog: false,
            redirectConfirmDialogDetails: {},
        };

        this.handleApiDelete = this.handleApiDelete.bind(this);
        this.handleRedirectToAPIOverview = this.handleRedirectToAPIOverview.bind(this);
        this.deleteConfirmDialogCallback = this.deleteConfirmDialogCallback.bind(this);
        this.redirectConfirmDialogCallback = this.redirectConfirmDialogCallback.bind(this);
    }

    handleRequestClose = () => {
        this.setState({openDeleteConfirmDialog: false});
    };

    openDeleteConfirmDialog = () => {
        this.setState({openDeleteConfirmDialog: true});
    };

    handleApiDelete() {
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
                    this.setState({open: false, openDeleteConfirmDialog: false});
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
        const {api, environmentName, rootAPI} = this.props;
        const currentEnvironmentName = Utils.getCurrentEnvironment().label;
        // If environment name or version is not defined then consider as same environment or version.
        const isSameEnvironment = !environmentName || environmentName === currentEnvironmentName;
        const isSameVersion = !rootAPI || rootAPI.version === api.version;

        if (isSameEnvironment && isSameVersion) {
            this.setState({
                overview_link: `/apis/${api.id}`,
                isRedirect: true
            });
        } else { // Ask for confirmation to switch environment or version of the API
            const redirectConfirmDialogDetails = ApiThumb.getRedirectConfirmDialogDetails({
                api, rootAPI, environmentName, currentEnvironmentName, isSameEnvironment, isSameVersion
            });

            this.setState({
                overview_link: `/apis/${api.id}/overview?environment=${environmentName}`,
                isRedirect: false,
                openRedirectConfirmDialog: true,
                redirectConfirmDialogDetails,
            });
        }
    }

    deleteConfirmDialogCallback(result) {
        this.setState({
            openDeleteConfirmDialog: false
        });
        if(result) this.handleApiDelete();
    }

    redirectConfirmDialogCallback(result) {
        this.setState({
            isRedirect: result,
            openRedirectConfirmDialog: false
        });
    }

    render() {
        const {api, environmentOverview, classes} = this.props;
        const gridItemSizes = environmentOverview ?
            {xs: 6, sm: 4, md: 3, lg: 2, xl: 2} : {xs: 6, sm: 4, md: 3, lg: 2, xl: 2};
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
                        <div className={
                            `${classes.lifeCycleState} ${classes[`lifeCycleState_${api.lifeCycleStatus}`]}`
                        }/>
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
            <Grid item {...gridItemSizes}>
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
                                <NotificationSystem ref="notificationSystem"/>
                                <ScopeValidation resourcePath={resourcePath.SINGLE_API}
                                                 resourceMethod={resourceMethod.DELETE}>
                                    <Button dense color="primary" onClick={this.openDeleteConfirmDialog}>Delete</Button>
                                </ScopeValidation>
                                <ConfirmDialog
                                    title={`Delete API "${api.name} - ${api.version}"?`}
                                    message={"Are you sure you want to delete the API?"}
                                    labelOk={"Delete"}
                                    callback={this.deleteConfirmDialogCallback}
                                    open={this.state.openDeleteConfirmDialog}
                                />
                            </div>
                            :
                            <div/>
                        }
                    </CardActions>
                </Card>
                <ConfirmDialog
                    {...this.state.redirectConfirmDialogDetails}
                    callback={this.redirectConfirmDialogCallback}
                    open={this.state.openRedirectConfirmDialog}
                />
            </Grid>
        );
    }

    static getRedirectConfirmDialogDetails(details) {
        const {api, rootAPI, environmentName, currentEnvironmentName, isSameEnvironment, isSameVersion} = details;

        let title = `Switch to ${api.name} ${api.version}` +
            `${isSameEnvironment ? '?' : ` in ${environmentName} Environment?`}`;
        let message = 'We are going to switch the ' +
            `${isSameEnvironment ? '' : `environment "${currentEnvironmentName}" to "${environmentName}"`}` +
            `${!isSameEnvironment && !isSameVersion ? ' and ' : ''}` +
            `${isSameVersion ? '' : `API version "${rootAPI.version}" to "${api.version}"`}`;
        let labelCancel = 'Cancel';
        let labelOk = 'Switch';

        return {title, message, labelCancel, labelOk};
    }
}

export default withStyles(styles)(ApiThumb);
