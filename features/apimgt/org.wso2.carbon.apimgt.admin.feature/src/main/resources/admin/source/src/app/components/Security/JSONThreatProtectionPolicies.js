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
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';
import Table, { TableBody, TableCell, TableHead, TableRow } from 'material-ui/Table';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import IconButton from 'material-ui/IconButton';
import Divider from 'material-ui/Divider';
import Button from 'material-ui/Button';
import MenuIcon from '@material-ui/icons/Menu';
import TextField from 'material-ui/TextField';

import API from '../../data/api'
import Message from '../Shared/Message'

const messages = {
    success: 'Threat protection policy deleted successfully',
    failure: 'Error while deleting threat protection policy',
    retrieveError: 'Error while retrieving threat protection policies'
};

const styles = theme => ({
    globalPolicy: {
        backgroundColor: 'blue',
    },
});

export default class JSONThreatProtectionPolicies extends Component {
    constructor(props) {
        super(props);
        this.state = {
            policies: null,
            message: ''
        };
    }

    componentDidMount() {
        var api = new API();
        var promised_policies = api.getThreatProtectionPolicies();
        promised_policies.then(
            response => {
                var policies = response.obj.list.filter(item => item.type == "JSON");
                //parsing policy string
                for(var i=0; i<policies.length; i++) {
                    policies[i].policy = JSON.parse(policies[i].policy);
                }
                this.setState({policies: policies});
            }
        ).catch(
            error => {
                this.msg.error(messages.retrieveError);
            }
        );
    }

    deletePolicy(id) {
        const api = new API();
        const promised_policies = api.deleteThreatProtectionPolicy(id);
        promised_policies.then(
            response => {
                this.msg.info(messages.success);
            }
        ).catch(
            error => {
                this.msg.error(messages.failure);
                console.log(error);
            }
        );
    }

    render() {
        const policies = this.state.policies;
        let data = [];
        if(policies) {
            data = policies;
        }

        return (
            <div>
                <AppBar position="static" >
                    <Toolbar style={{minHeight:'30px'}}>
                        <IconButton color="contrast" aria-label="Menu">
                            <MenuIcon />
                        </IconButton>
                        <Link to={"/security/json_threat_protection/create/"}>
                            <Button color="contrast">Add Policy</Button>
                        </Link>
                    </Toolbar>
                </AppBar>
                <Message ref={a => this.msg = a}/>
                <Grid container>
                    <Grid item xs={12}>
                        <Paper>
                            <Typography className="page-title" type="display1" gutterBottom>
                                JSON Threat Protection Policies
                            </Typography>
                            <Typography type="caption" gutterBottom align="left" className="page-title-help">
                                Discription goes here.
                            </Typography>

                            <Divider />
                            <div className="page-content">
                                <TextField
                                    label="Search"
                                    margin="normal"
                                />
                            </div>
                            <Divider />
                        </Paper>
                    </Grid>
                    <Grid item xs={12} className="page-content">
                        <Paper>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>Name</TableCell>
                                        <TableCell>Property Limit</TableCell>
                                        <TableCell>Max String Length</TableCell>
                                        <TableCell>Max Array Elements</TableCell>
                                        <TableCell>Max Property Length</TableCell>
                                        <TableCell>Max Depth</TableCell>
                                        <TableCell></TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {data.map(n => {
                                        return (
                                            <TableRow key={n.uuid}>
                                                <TableCell>{n.name + (n.uuid=="GLOBAL-JSON"? " (GLOBAL)": '')}</TableCell>
                                                <TableCell>{n.policy.maxFieldCount}</TableCell>
                                                <TableCell>{n.policy.maxStringLength}</TableCell>
                                                <TableCell>{n.policy.maxArrayElementCount}</TableCell>
                                                <TableCell>{n.policy.maxFieldLength}</TableCell>
                                                <TableCell>{n.policy.maxDepth}</TableCell>
                                                <TableCell>
                                      <span>
                                         <Link to={"/security/json_threat_protection/" + n.uuid}>
                                              <Button color="primary">Edit</Button>
                                         </Link>
                                         <Button color="accent"
                                                 onClick={() => this.deletePolicy(n.uuid)} >Delete</Button>
                                      </span>
                                                </TableCell>
                                            </TableRow>
                                        );
                                    })}
                                </TableBody>
                            </Table>
                        </Paper>
                    </Grid>
                </Grid>
            </div>
        );
    }
}