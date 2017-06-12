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
import {Col, Popconfirm, Row, Form, Select, Dropdown, Tag, Menu, Badge, message} from 'antd';

const FormItem = Form.Item;
import Loading from '../../Base/Loading/Loading'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import Api from '../../../data/api'
import {Redirect} from 'react-router-dom'
import {ScopeValidation, resourceMethod, resourcePath} from '../../../data/ScopeValidation'
import ApiPermissionValidation from '../../../data/ApiPermissionValidation'

import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import Card, { CardActions, CardContent, CardMedia } from 'material-ui/Card';
import Table, { TableBody, TableCell, TableRow } from 'material-ui/Table';
import IconButton from 'material-ui/IconButton';
import { Delete, Edit, CreateNewFolder, Description  }from 'material-ui-icons';
import Confirm from '../../Shared/Confirm'

class Overview extends Component {
    constructor(props) {
        super(props);
        this.state = {
            api: null,
            notFound: false
        };
        this.api_uuid = this.props.match.params.api_uuid;
        this.downloadWSDL = this.downloadWSDL.bind(this);
        this.handleApiDelete = this.handleApiDelete.bind(this);
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
                        <Paper style={{display:"flex"}}>
                            <Typography type="display2" gutterBottom className="page-title">
                                {api.name} - <span style={{fontSize:"50%"}}>Overview</span>
                            </Typography>
                            <ScopeValidation resourceMethod={resourceMethod.PUT} resourcePath={resourcePath.SINGLE_API}>
                                <ApiPermissionValidation userPermissions={this.state.api.userPermissionsForApi}>
                                    <Button aria-owns="simple-menu" aria-haspopup="true" >
                                        <Edit /> Edit
                                    </Button>
                                </ApiPermissionValidation>
                            </ScopeValidation>

                            <ScopeValidation resourceMethod={resourceMethod.DELETE} resourcePath={resourcePath.SINGLE_API}>
                                <ApiPermissionValidation checkingPermissionType={ApiPermissionValidation.permissionType.DELETE}
                                                         userPermissions={this.state.api.userPermissionsForApi}>
                                    <Popconfirm title="Do you want to delete this api?" onConfirm={this.handleApiDelete}>
                                        <Button aria-owns="simple-menu" aria-haspopup="true" >
                                            <Delete /> Delete
                                        </Button>
                                    </Popconfirm>
                                </ApiPermissionValidation>
                            </ScopeValidation>

                            <Button aria-owns="simple-menu" aria-haspopup="true" >
                                <CreateNewFolder /> Create New Version
                            </Button>
                            <Button aria-owns="simple-menu" aria-haspopup="true" >
                                <Description /> View Swagger
                            </Button>
                        </Paper>
                    </Grid>
                    <Grid item xs={12} sm={6} md={3} lg={3} xl={2} style={{paddingLeft:"40px"}}>

                        <Card>
                            <CardMedia
                                image="/publisher/public/images/api/api-default.png"
                                title="Contemplative Reptile"
                            >
                                <img alt="API thumb" width="100%" src="/publisher/public/images/api/api-default.png"/>
                            </CardMedia>
                            <CardContent>

                            </CardContent>
                            <CardActions>
                                {api.lifeCycleStatus}

                                <Button dense color="primary">
                                    <a href={"/store/apis/" + this.api_uuid} target="_blank" title="Store">View in store</a>
                                </Button>
                            </CardActions>
                        </Card>
                    </Grid>
                    <Grid item xs={12} sm={6} md={9} lg={9} xl={10} >
                        <Paper>
                            <Table>
                                <TableBody>

                                    <TableRow>
                                        <TableCell style={{width:"100px"}}>Visibility</TableCell><TableCell>{api.visibility}</TableCell>
                                    </TableRow>

                                    <TableRow>
                                        <TableCell>Version</TableCell><TableCell>{api.version}</TableCell>
                                    </TableRow>

                                    <TableRow>
                                        <TableCell>Context</TableCell><TableCell>{api.context}</TableCell>
                                    </TableRow>
                                    {
                                        api.endpoint.map( ep => <TableRow>
                                            <TableCell>{ep.type}</TableCell>
                                            <TableCell>{ep.inline ? ep.inline.endpointConfig.serviceUrl : ''}</TableCell>
                                        </TableRow>)
                                    }
                                    <TableRow>
                                        <TableCell>Date Created</TableCell><TableCell>{api.createdTime}</TableCell>
                                    </TableRow>
                                    <TableRow>
                                        <TableCell>Date Last Updated</TableCell><TableCell>{api.lastUpdatedTime}</TableCell>
                                    </TableRow>

                                    <TableRow>
                                        <TableCell>Default API Version</TableCell><TableCell>{api.isDefaultVersion}</TableCell>
                                    </TableRow>
                                    <TableRow>
                                        <TableCell>Published Environments</TableCell><TableCell>not-supported-yet</TableCell>
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