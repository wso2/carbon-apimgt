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
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';;
import IconButton from 'material-ui/IconButton';
import Button from 'material-ui/Button';
import MenuIcon from '@material-ui/icons/Menu';
import Typography from 'material-ui/Typography';
import Divider from 'material-ui/Divider';
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';
import Input, { InputLabel } from 'material-ui/Input';
import Select from 'material-ui/Select';
import { MenuItem } from 'material-ui/Menu';

import API from '../../../../data/api'
import Alert from '../../../Shared/Alert'

class AddPolicy extends Component {
    state = {
        selectedPolicy : {uuid: '', name: 'Select', policy: '', type: ''},
        policies: []
    };

    constructor(props) {
        super(props);
        let api = new API();
        let promisedApi = api.get(this.props.match.params.api_uuid);
        promisedApi.then(response => {
           this.setState({currentApi: response.obj});
        });
    }

    componentDidMount() {
        let api = new API();
        let promisedPolicies = api.getThreatProtectionPolicies();
        promisedPolicies.then(response => {
            this.setState({policies: response.obj.list});
        });
    }

    handlePolicyAdd() {
        let policy = this.state.selectedPolicy;
        if (policy.uuid === '' || policy.name === '') {
            Alert.error("Please select a policy");
            return;
        }

        if (this.state.currentApi) {
            let currentApi = this.state.currentApi;
            let api = new API();
            let promisedPolicyAdd = api.addThreatProtectionPolicyToApi(currentApi.id, this.state.selectedPolicy.uuid);
            promisedPolicyAdd.then(response => {
                if (response.status === 200) {
                    Alert.info("Threat protection policy added successfully.");
                } else {
                    Alert.error("Failed to add threat protection policy.");
                }
            });
        }
    }

    handleChange = name => event => {
        let policyId = event.target.value;
        let api = new API();
        let promisedPolicy = api.getThreatProtectionPolicy(policyId);
        promisedPolicy.then(response => {
            this.setState({selectedPolicy: response.obj});
        });
    }

    render() {
        return (
            <div>
                <AppBar position="static" >
                    <Toolbar style={{minHeight:'30px'}}>
                        <IconButton color="default" aria-label="Menu">
                            <MenuIcon />
                        </IconButton>
                        <Link to={"/apis/" + this.props.match.params.api_uuid + "/security"}>
                            <Button color="contrast">Go Back</Button>
                        </Link>
                    </Toolbar>
                </AppBar>
                <Paper>
                    <Grid container className="root" direction="column">
                        <Grid item xs={12} className="grid-item">
                            <Typography className="page-title" type="display1" gutterBottom>
                                Add Threat Protection Policy
                            </Typography>
                        </Grid>
                        <br/>
                        <br/>
                        <Paper elevation ={20}>
                            <Grid item xs={6} className="grid-item">
                                <InputLabel htmlFor="selectedPolicy">Policy</InputLabel>
                                &nbsp;&nbsp;
                                <Select
                                    value={this.state.selectedPolicy.uuid}
                                    onChange={this.handleChange("selectedPolicy")}
                                    input={<Input name="selectedPolicy" id="selectedPolicy" />}
                                >
                                    {this.state.policies.map(n => {
                                        return (
                                            <MenuItem key={n.uuid} value={n.uuid}>{n.name}</MenuItem>
                                        );
                                    })};
                                </Select>
                            </Grid>
                            <br/>
                            <br/>
                            <Grid item xs={6} className="grid-item">
                                <p>Policy Type: {this.state.selectedPolicy.type}</p>
                                <p>{this.state.selectedPolicy.policy}</p>
                            </Grid>
                            <br/>
                            <Grid item xs={6} className="grid-item">
                                <Divider />
                                <div >
                                    <Button raised color="primary" onClick = {
                                        () => this.handlePolicyAdd()}>
                                        Add
                                    </Button>
                                    <Link to={"/apis/" + this.props.match.params.api_uuid + "/security"}>
                                        <Button raised>Cancel</Button>
                                    </Link>
                                </div>
                            </Grid>
                        </Paper>
                    </Grid>
                </Paper>
            </div>
        );
    }
}

export default AddPolicy
