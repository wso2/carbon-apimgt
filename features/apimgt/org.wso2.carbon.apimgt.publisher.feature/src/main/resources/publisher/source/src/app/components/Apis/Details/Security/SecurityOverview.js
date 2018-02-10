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
import Alert from '../../../Shared/Alert'

import Typography from 'material-ui/Typography';
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';
import Table, { TableBody, TableCell, TableHead, TableRow } from 'material-ui/Table';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import Button from 'material-ui/Button';

class Security extends Component {
    constructor(props) {
        super(props);
        this.api = new Api();
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
        let promised_api = this.api.get(this.props.match.params.api_uuid);
        promised_api.then(response => {
            this.setState({api: response.obj});
            this.updatePolicyData();
        });
    }

    updatePolicyData() {
        this.setState({policies: []})
        let policyIds = this.state.api.threatProtectionPolicies.list;
        for (var i=0; i<policyIds.length; i++) {
            let id = policyIds[i].policyId;
            let promisedPolicies = this.api.getThreatProtectionPolicy(id);
            promisedPolicies.then(response => {
                let policies = this.state.policies;
                policies.push(response.obj);
                this.setState({policies: policies});
            });
        }
    }

    deletePolicy(id) {
        let associatedApi = this.state.api;
        let promisedPolicyDelete = this.api.deleteThreatProtectionPolicyFromApi(associatedApi.id, id);
        promisedPolicyDelete.then(response => {
           if (response.status === 200) {
               Alert.info("Policy removed successfully.");

               //remove policy from local api
               let index = associatedApi.threatProtectionPolicies.list.indexOf({policyId: id});
               associatedApi.threatProtectionPolicies.list.splice(index, 1);
               this.setState({api: associatedApi});
               this.updatePolicyData();
           } else {
               Alert.error("Failed to remove policy.");
           }
        });
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
                            <Link to={"/apis/:api_uuid/security/add-policy".replace(":api_uuid",
                                this.props.match.params.api_uuid)}>
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
                                    <TableCell>Policy Type</TableCell>
                                    <TableCell>Policy</TableCell>
                                    <TableCell></TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {data.map(n => {
                                    return (
                                        <TableRow key={n.uuid}>
                                            <TableCell>{n.name + (n.uuid=="GLOBAL-JSON"? " (GLOBAL)": "")}</TableCell>
                                            <TableCell>{n.type}</TableCell>
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