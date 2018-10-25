/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';;
import IconButton from '@material-ui/core/IconButton';
import Button from '@material-ui/core/Button';
import MenuIcon from '@material-ui/icons/Menu';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import { withStyles } from '@material-ui/core/styles';

import API from 'AppData/api'
import Alert from 'AppComponents/Shared/Alert'

const styles = theme => ({
    addNewWrapper: {
        backgroundColor: theme.palette.background.paper,
        color: theme.palette.getContrastText(theme.palette.background.paper),
        border: 'solid 1px ' + theme.palette.grey['300'],
        borderRadius: theme.shape.borderRadius,
        marginTop: theme.spacing.unit*2,
    },
    addNewHeader: {
        padding: theme.spacing.unit*2,
        backgroundColor: theme.palette.grey['300'],
        fontSize: theme.typography.h6.fontSize,
        color: theme.typography.h6.color,
        fontWeight: theme.typography.h6.fontWeight,
    },
    addNewOther: {
        padding: theme.spacing.unit*2,
    },
    button: {
        marginLeft: theme.spacing.unit*2,
        textTransform: theme.custom.leftMenuTextStyle,
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
    },
    addJsonContent: {
        whiteSpace: 'pre',
    }
});

class AddPolicy extends Component {
    state = {
        selectedPolicy : {uuid: '', name: 'Select', policy: '', type: ''},
        policies: []
    };

    constructor(props) {
        super(props);
        let api = new API();
        let promisedApi = api.get(this.props.id);
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
                    this.props.updateData();
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

    formatPolicy = (policy) => {
        policy = policy.replace(":", " : ");
        policy = policy.split(',').join(",\n");
        return policy;
    }

    render() {
        const {classes} = this.props;
        return (
            <div className={classes.contentWrapper}>
                <div className={classes.addNewWrapper}>
                    <Typography className={classes.addNewHeader}>
                        Add New Threat Protection Policy
                    </Typography>
                    <Divider className={classes.divider} />
                    <div className={classes.addNewOther}>
                        <InputLabel htmlFor="selectedPolicy">Policy</InputLabel>
                        &nbsp;&nbsp;
                        <Select
                            value={this.state.selectedPolicy.uuid}
                            onChange={this.handleChange("selectedPolicy")}
                            input={<Input name="selectedPolicy" id="selectedPolicy" />}>
                            {this.state.policies.map(n => {
                                return (
                                    <MenuItem key={n.uuid} value={n.uuid}>{n.name}</MenuItem>
                                );
                            })};
                        </Select>
                        <br/>
                        <br/>
                        <p>Policy Type: {this.state.selectedPolicy.type}</p>
                        <div>
                            <p>Policy: </p>
                            <div className={classes.addJsonContent}>
                                {this.formatPolicy(this.state.selectedPolicy.policy)}
                            </div>
                        </div>
                    </div>
                    <Divider className={classes.divider} />
                    <div className={classes.addNewOther}>
                        <Button variant="contained" color="primary" onClick={() => this.handlePolicyAdd()}>
                            Add Policy to API
                        </Button>
                        <Button className={classes.button} onClick={this.props.toggleShowAddPolicy}>
                            Cancel
                        </Button>
                    </div>
                </div>
            </div>
        );
    }
}

export default withStyles(styles)(AddPolicy)
