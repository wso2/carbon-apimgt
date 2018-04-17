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
import React, { Component } from 'react';

import { Link } from 'react-router-dom';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import IconButton from 'material-ui/IconButton';
import Button from 'material-ui/Button';
import MenuIcon from '@material-ui/icons/Menu';
import Menu, { MenuItem } from 'material-ui/Menu';
import Typography from 'material-ui/Typography';
import Divider from 'material-ui/Divider';
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';

import GeneralDetails from '../Shared/GeneralDetails';
import CustomRuleDetails from '../Shared/CustomRuleDetails';

import API from '../../../data/api';
import Message from '../../Shared/Message';
import '../Shared/Shared.css';

const messages = {
    success: 'Update API rate limit successfully',
    failure: 'Error while updating API rate',
    retrieveError: 'Error while retrieving API rate limits',
};

class CustomRule extends Component {
    constructor(props) {
        super(props);
        this.state = {
            policy: {
                policyName: '',
                displayName: '',
                description: '',
                isDeployed: true,
                siddhiQuery: '',
                keyTemplate: ''
            },
        };
        this.handleChangeChild = this.handleChangeChild.bind(this);
        this.handlePolicyUpdate = this.handlePolicyUpdate.bind(this);
    }

    componentDidMount() {
        const api = new API();

        const promised_policy = api.getCustomRulePolicy(this.props.match.params.policy_uuid);
        promised_policy
            .then((response) => {
                const policy = response.obj;
                this.setState({ policy });
            })
            .catch((error) => {
                this.msg.error(message.retrieveError);
                console.error(error);
            });
    }

    handleChangeChild(name, value) {
        const policy = this.state.policy;
        policy[name] = value
        this.setState({
            policy,
        });
    }


    handlePolicyUpdate() {
        const api = new API();
        const uuid = this.props.match.params.policy_uuid;
        const promisedPolicies = api.updateCustomRulePolicy(uuid, this.state.policy);
        promisedPolicies
            .then((response) => {
                this.msg.info(messages.success);
                let redirect_url = "/policies/custom_rules";
                this.props.history.push(redirect_url);
            })
            .catch((error) => {
                this.msg.error(messages.failure);
                console.error(error);
            });
    }

    render() {
        return (
            <div>
                <AppBar position='static'>
                    <Toolbar style={{ minHeight: '30px' }}>
                        <IconButton color='contrast' aria-label='Menu'>
                            <MenuIcon />
                        </IconButton>
                        <Link to='/policies/api_policies'>
                            <Button color='contrast'>Go Back</Button>
                        </Link>
                    </Toolbar>
                </AppBar>
                <Message ref={a => (this.msg = a)} />
                <Paper>
                    <Grid container className='root' direction='column'>
                        <Grid item xs={12} className='grid-item'>
                            <Typography className='page-title' type='display1' gutterBottom>
                                Edit Custom Rule
                            </Typography>
                        </Grid>
                        <GeneralDetails policy={this.state.policy} handleChangeChild={this.handleChangeChild} />

                        <CustomRuleDetails
                            policy={this.state.policy}
                            handleChangeChild={this.handleChangeChild}
                        />

                        <Paper elevation={20}>
                            <Grid item xs={6} className='grid-item'>
                                <Divider />
                                <div>
                                    <Button raised color='primary' onClick={() => this.handlePolicyUpdate()}>
                                        Update
                                    </Button>
                                    <Link to='/policies/custom_rules'>
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

export default CustomRule;
