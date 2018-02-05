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

import React, {Component} from 'react'
import {Link} from 'react-router-dom'
import {Form, message} from 'antd';
import Loading from '../../Base/Loading/Loading'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import Api from '../../../data/api'
import {resourceMethod, resourcePath, ScopeValidation} from '../../../data/ScopeValidation'
import ApiPermissionValidation from '../../../data/ApiPermissionValidation'

import Dialog, {DialogActions, DialogContent, DialogContentText, DialogTitle} from 'material-ui/Dialog';
import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import Card, {CardActions, CardContent, CardMedia} from 'material-ui/Card';
import Table, {TableBody, TableCell, TableRow} from 'material-ui/Table';
import blueGrey from 'material-ui/colors/blueGrey';
import {CreateNewFolder, Delete, Description, Edit} from 'material-ui-icons';
import Slide from "material-ui/transitions/Slide";
import Utils from "../../../data/Utils";

const FormItem = Form.Item;

class Overview extends Component {
    constructor(props) {
        super(props);
        this.state = {
            api: null,
            notFound: false,
            openMenu: false
        };
        this.api_uuid = this.props.match.params.api_uuid;
        this.downloadWSDL = this.downloadWSDL.bind(this);
        this.handleApiDelete = this.handleApiDelete.bind(this);
        this.handleRequestClose = this.handleRequestClose.bind(this);
        this.handleRequestOpen = this.handleRequestOpen.bind(this);
    }

    componentDidMount() {
        const api = new Api();
        let promised_api = api.get(this.api_uuid);
        promised_api.then(
            response => {
                this.setState({api: response.obj});
            }
        ).catch(
            error => {
                if (process.env.NODE_ENV !== "production") {
                    console.log(error);
                }
                let status = error.status;
                if (status === 404) {
                    this.setState({notFound: true});
                }
            }
        );
    }

    handleRequestClose() {
        this.setState({openMenu: false});
    };

    handleRequestOpen() {
        this.setState({openMenu: true});
    };

    handleApiDelete(e) {
        this.setState({loading: true});
        const api = new Api();
        let promised_delete = api.deleteAPI(this.api_uuid);
        promised_delete.then(
            response => {
                if (response.status !== 200) {
                    console.log(response);
                    message.error("Something went wrong while deleting the API!");
                    this.setState({loading: false});
                    return;
                }
                let redirect_url = "/apis/";
                this.props.history.push(redirect_url);
                message.success("API " + this.state.api.name + " was deleted successfully!");
                this.setState({active: false, loading: false});
            }
        );
    }

    downloadWSDL() {
        const api = new Api();
        let promised_wsdl = api.getWSDL(this.api_uuid);
        promised_wsdl.then(
            response => {
                let windowUrl = window.URL || window.webkitURL;
                let binary = new Blob([response.data]);
                let url = windowUrl.createObjectURL(binary);
                let anchor = document.createElement('a');
                anchor.href = url;
                if (response.headers['content-disposition']) {
                    anchor.download = Overview.getWSDLFileName(response.headers['content-disposition']);
                } else {
                    //assumes a single WSDL in text format
                    anchor.download = this.state.api.provider +
                        "-" + this.state.api.name + "-" + this.state.api.version + ".wsdl"
                }
                anchor.click();
                windowUrl.revokeObjectURL(url);
            }
        )
    }

    static getWSDLFileName(content_disposition_header) {
        let filename = "default.wsdl";
        if (content_disposition_header && content_disposition_header.indexOf('attachment') !== -1) {
            let filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            let matches = filenameRegex.exec(content_disposition_header);
            if (matches !== null && matches[1]) {
                filename = matches[1].replace(/['"]/g, '');
            }
        }
        return filename;
    }

    render() {
        let redirect_url = "/apis/" + this.props.match.params.api_uuid + "/overview";
        const api = this.state.api;
        if (this.state.notFound) {
            return <ResourceNotFound/>
        }
        if (!this.state.api) {
            return <Loading/>
        }
        return (
            <Grid container>
                <Grid item xs={12}>
                    <Paper>
                        <Typography type="display2" gutterBottom>
                            {api.name} - <span>Overview</span>
                        </Typography>
                        {/* allowing edit based on scopes */}
                        <ScopeValidation resourceMethod={resourceMethod.PUT} resourcePath={resourcePath.SINGLE_API}>
                            <ApiPermissionValidation userPermissions={this.state.api.userPermissionsForApi}>
                                <Button aria-owns="simple-menu" aria-haspopup="true">
                                    <Edit/> Edit
                                </Button>
                            </ApiPermissionValidation>
                        </ScopeValidation>
                        {/* allowing delet based on scopes */}
                        <ScopeValidation resourceMethod={resourceMethod.DELETE}
                                         resourcePath={resourcePath.SINGLE_API}>
                            <ApiPermissionValidation
                                checkingPermissionType={ApiPermissionValidation.permissionType.DELETE}
                                userPermissions={this.state.api.userPermissionsForApi}>
                                <Button onClick={this.handleRequestOpen} color="accent" aria-owns="simple-menu"
                                        aria-haspopup="true">
                                    <Delete/> Delete
                                </Button>
                            </ApiPermissionValidation>
                        </ScopeValidation>
                        <Dialog open={this.state.openMenu} transition={Slide}>
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
                                    Delete
                                </Button>
                                <Button dense color="primary" onClick={this.handleRequestClose}>
                                    Cancel
                                </Button>
                            </DialogActions>
                        </Dialog>
                        {/* allowing to create new version based on scopes */}
                        <ScopeValidation resourcePath={resourcePath.API_COPY} resourceMethod={resourceMethod.POST}>
                            <Button aria-owns="simple-menu" aria-haspopup="true">
                                <CreateNewFolder/> Create New Version
                            </Button>
                        </ScopeValidation>
                        <Button aria-owns="simple-menu" aria-haspopup="true">
                            <Description/> View Swagger
                        </Button>
                    </Paper>
                </Grid>
                <Grid item xs={12}>
                    <Card>
                        <CardMedia
                            src=""
                            style={{backgroundColor: blueGrey[50]}}
                            title="Contemplative Reptile">
                            <img alt="API thumb" width="10%"
                                 src="/publisher/public/app/images/api/api-default.png"/>
                        </CardMedia>
                        <CardContent>

                        </CardContent>
                        <CardActions>
                            {api.lifeCycleStatus}

                            <Button dense color="primary">
                                <a href={`/store/apis/${this.api_uuid}/overview?environment=${Utils.getCurrentEnvironment().label}`}
                                   target="_blank" title="Store">
                                    View in store
                                </a>
                            </Button>

                            {Utils.isAutoLoginEnabled() ?
                                <Link to={`/apis/${this.api_uuid}/environment view`}>
                                    <Button dense color="primary">
                                        Multi-Environment Overview
                                    </Button>
                                </Link>
                                :
                                null
                            }
                        </CardActions>
                    </Card>
                </Grid>
                <Grid item xs={12}>
                    <Paper>
                        <Table>
                            <TableBody>

                                <TableRow>
                                    <TableCell
                                        style={{width: "100px"}}>Visibility</TableCell><TableCell>{api.visibility}</TableCell>
                                </TableRow>

                                <TableRow>
                                    <TableCell>Version</TableCell><TableCell>{api.version}</TableCell>
                                </TableRow>

                                <TableRow>
                                    <TableCell>Context</TableCell><TableCell>{api.context}</TableCell>
                                </TableRow>
                                {
                                    api.endpoint.map(ep => <TableRow key={ep.inline.id}>
                                        <TableCell>{ep.type}</TableCell>
                                        <TableCell>{ep.inline ? ep.inline.endpointConfig.serviceUrl : ''}</TableCell>
                                    </TableRow>)
                                }
                                <TableRow>
                                    <TableCell>Date Created</TableCell><TableCell>{api.createdTime}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Date Last
                                        Updated</TableCell><TableCell>{api.lastUpdatedTime}</TableCell>
                                </TableRow>

                                <TableRow>
                                    <TableCell>Default API
                                        Version</TableCell><TableCell>{api.isDefaultVersion}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Published
                                        Environments</TableCell><TableCell>not-supported-yet</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Policies</TableCell>
                                    <TableCell>
                                        {api.policies.map(policy => policy + ", ")}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>WSDL URLs</TableCell>
                                    <TableCell>
                                        {
                                            api.wsdlUri && (
                                                <a onClick={this.downloadWSDL}>Download</a>
                                            )
                                        }
                                    </TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </Paper>
                </Grid>
            </Grid>
        );
    }
}

export default Overview
