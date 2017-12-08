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

import Api from '../../../../data/api'

import Typography from 'material-ui/Typography';
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';
import Table, { TableBody, TableCell, TableHead, TableRow } from 'material-ui/Table';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import IconButton from 'material-ui/IconButton';
import Button from 'material-ui/Button';
import MenuIcon from 'material-ui-icons/Menu';

class Security extends Component {
    constructor(props) {
        super(props);
        this.state = {
            api: {
                name: ''
            },
            policies: []
        };
        this.updateData = this.updateData.bind(this);
    }

    componentDidMount() {
        this.updateData();
    }

    updateData() {
        let api = new Api();
        let promised_api = api.get(this.props.match.params.api_uuid);
        promised_api.then(response => {
            this.setState({api: response.obj});

            let policyIds = this.state.api.threatProtectionPolicies;
            var policies = [];
            for (var i=0; i<policyIds.length; i++) {
                let id = policyIds[i];
                let promisedPolicies = api.getThreatProtectionPolicy(id);
                promisedPolicies.then(response => {
                   policies.push(response.obj);
                });
            }
            this.setState({policies: policies});
        });
    }

    deletePolicy(id) {

    }

    render() {
        let data = [];
        if (this.state.policies) {
            data = this.state.policies;
        }

        return (
            <Grid container>
                <Grid item xs={12}>
                    <AppBar position="static" >
                        <Toolbar style={{minHeight:'30px'}}>
                            <Link to={"/security/json_threat_protection/create/"}>
                                <Button color="contrast">Add Policy</Button>
                            </Link>
                        </Toolbar>
                    </AppBar>
                </Grid>
                <Grid item xs={12}>
                    <Paper>
                        <Typography className="page-title" type="display2">
                            {this.state.api.name} - <span>Threat Protection Policies</span>
                        </Typography>
                        <Typography type="caption" gutterBottom align="left" className="page-title-help">
                           Add or Remove Threat Protection Policies from APIs
                        </Typography>

                    </Paper>
                </Grid>
                <Grid item xs={12} className="page-content">
                    <Paper>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Policy Name</TableCell>
                                    <TableCell>Policy</TableCell>
                                    <TableCell></TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {data.map(n => {
                                    return (
                                        <TableRow key={n.uuid}>
                                            <TableCell>{n.name + (n.uuid=="GLOBAL-JSON"? " (GLOBAL)": '')}</TableCell>
                                            <TableCell>{n.policy}</TableCell>
                                            <TableCell>
                                              <span>
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
        );
    }
}

export default Security