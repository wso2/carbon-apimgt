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
import Table, {TableBody, TableCell, TableRow} from 'material-ui/Table';
import Radio, {RadioGroup} from 'material-ui/Radio';
import Switch from 'material-ui/Switch';
import Button from 'material-ui/Button';
import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import NotificationSystem from 'react-notification-system';
import {FormControlLabel} from 'material-ui/Form';
import TextField from 'material-ui/TextField';

import API from '../../../data/api'

export default class EndpointCreate extends Component {

    constructor(props) {
        super(props);
        this.state = {
            endpointType: 'http',
            secured: false,
            creating: false,
            securityType: null,
            maxTPS: 10
        };
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleInputs = this.handleInputs.bind(this);
    }

    handleChange = name => (event, checked) => {
        this.setState({[name]: checked});
    };

    handleInputs(e) {
        this.setState({[e.target.name]: e.target.value});
    }

    handleSubmit(e) {
        this.setState({loading: true});
        let endpointSecurity = {enabled: false};
        if (this.state.secured) {
            endpointSecurity = {
                enabled: true,
                username: this.state.username,
                password: this.state.password,
                type: this.state.securityType
            }
        }
        let endpointDefinition = {
            endpointConfig: JSON.stringify({serviceUrl: this.state.endpointType + '://' + this.state.serviceUrl}),
            endpointSecurity: endpointSecurity,
            type: this.state.endpointType,
            name: this.state.name,
            maxTps: this.state.maxTPS
        };
        const api = new API();
        const promisedEndpoint = api.addEndpoint(endpointDefinition);
        return promisedEndpoint.then(
            response => {
                const {name, id} = response.obj;
                this.refs.notificationSystem.addNotification({
                    message: 'New endpoint ' + name + ' created successfully', position: 'tc',
                    level: 'success'
                });
                let redirect_url = "/endpoints/" + id + "/";
                this.props.history.push(redirect_url);
            }
        ).catch(
            error => {
                console.error(error);
                this.refs.notificationSystem.addNotification({
                    message: 'Error occurred while creating the endpoint!', position: 'tc',
                    level: 'error'
                });
                this.setState({loading: false});
            }
        )
    }

    render() {
        return (
            <Grid container justify="center" spacing={0}>
                <Grid item xs={10}>
                    <Paper>
                        <Grid item>
                            <h4>
                                Add new Global Endpoint
                            </h4>
                        </Grid>
                        {/*<Grid item><form onSubmit={this.handleSubmit}>*/}<Grid>
                            <Table><TableBody>
                                <TableRow style={{marginBottom: "10px"}} type="flex">
                                    <TableCell span={4}>Name</TableCell>
                                    <TableCell span={8}>
                                        <TextField name="name" onChange={this.handleInputs}
                                                   placeholder="Endpoint Name"/>
                                    </TableCell>
                                </TableRow>
                                <TableRow style={{marginBottom: "10px"}} type="flex">
                                    <TableCell span={4}>Type</TableCell>
                                    <TableCell span={8}>
                                        <RadioGroup value={this.state.endpointType}
                                                    onChange={e => {
                                                        e.target["name"] = "endpointType";
                                                        this.handleInputs(e)
                                                    }}>
                                            <FormControlLabel value={"http"} control={<Radio/>} label="HTTP"/>
                                            <FormControlLabel value={"https"} control={<Radio/>} label="HTTPS"/>
                                        </RadioGroup>
                                    </TableCell>
                                </TableRow>
                                <TableRow style={{marginBottom: "10px"}} type="flex">
                                    <TableCell span={4}>Max TPS</TableCell>
                                    <TableCell span={8}>
                                        <TextField type="number" onChange={e => {
                                            e.target["name"] = "maxTPS";
                                            this.handleInputs(e)
                                        }}/>
                                    </TableCell>
                                </TableRow>
                                <TableRow style={{marginBottom: "10px"}} type="flex">
                                    <TableCell span={4}>Service URL</TableCell>
                                    <TableCell span={8}>
                                        <TextField name="serviceUrl" onChange={this.handleInputs}
                                                   style={{width: '100%'}}/>
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell span={4}>Secured</TableCell>
                                    <TableCell span={8}>
                                        <Switch
                                            checked={this.state.secured}
                                            onChange={this.handleChange('secured')}
                                        />
                                    </TableCell>
                                </TableRow>
                                {this.state.secured && (
                                    <TableRow style={{marginBottom: "10px"}}>
                                        <TableRow style={{marginBottom: "10px"}}>
                                            <TableCell span={4}>Type</TableCell>
                                            <TableCell span={8}>
                                                <RadioGroup value={this.state.securityType}
                                                            onChange={e => {
                                                                e.target["name"] = "securityType";
                                                                this.handleInputs(e)
                                                            }}>
                                                    <FormControlLabel value={"basic"} control={<Radio/>}
                                                                      label="Basic"/>
                                                    <FormControlLabel value={"digest"} control={<Radio/>}
                                                                      label="Digest"/>
                                                </RadioGroup>
                                            </TableCell>
                                        </TableRow>
                                        <TableRow style={{marginBottom: "10px"}}>
                                            <TableCell span={4}>Username</TableCell>
                                            <TableCell span={8}>
                                                <TextField name="username" onChange={this.handleInputs}
                                                           placeholder="Username"/>
                                            </TableCell>
                                        </TableRow>
                                        <TableRow style={{marginBottom: "10px"}}>
                                            <TableCell span={4}>Password</TableCell>
                                            <TableCell span={8}>
                                                <TextField name="password" onChange={this.handleInputs}
                                                           placeholder="Password"/>
                                            </TableCell>
                                        </TableRow>
                                    </TableRow>
                                )}</TableBody></Table>
                            <Button type="primary" onClick={this.handleSubmit}>
                                <NotificationSystem ref="notificationSystem"/>
                                Create</Button>
                        </Grid>
                    </Paper>
                </Grid>
            </Grid>
        );
    }
}
