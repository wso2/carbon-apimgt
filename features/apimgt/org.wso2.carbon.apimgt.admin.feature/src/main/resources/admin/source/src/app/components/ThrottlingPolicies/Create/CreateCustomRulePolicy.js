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
import { withRouter } from 'react-router-dom'

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
import TextField from 'material-ui/TextField'

import GeneralDetails from '../Shared/GeneralDetails';

import API from '../../../data/api';
import Message from '../../Shared/Message';
import '../Shared/Shared.css';
import Alert from '../../Shared/Alert'
import CustomRuleDetails from '../Shared/CustomRuleDetails';

const messages = {
    success: 'Created API rate limit successfully',
    failure: 'Error while creating API rate limit',
};

class CreateCustomRulePolicy extends Component {
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
            anchorEl: null,
            popperOpen: false,
        };
        this.handleChangeChild = this.handleChangeChild.bind(this);
        this.handlePolicySave = this.handlePolicySave.bind(this);
        this.handlePopoverOpen = this.handlePopoverOpen.bind(this);
        this.handlePopoverClose = this.handlePopoverClose.bind(this);
    }


    handleChangeChild(name, value) {
        const policy = this.state.policy;
        policy[name] = value;
        this.setState({
            policy,
        });
    }


    handlePolicySave() {
        const api = new API();
        const promised_policies = api.createCustomRulePolicy(this.state.policy);
        const props = this.props;
        promised_policies
            .then((response) => {
                Alert.info(messages.success);
                let redirect_url = "/policies/custom_rules";
                this.props.history.push(redirect_url);
            })
            .catch((error) => {
                Alert.error(messages.failure);
            });
    }

    handlePopoverOpen(event) {
        this.setState({ anchorEl: event.target });
    };

    handlePopoverClose() {
        this.setState({ anchorEl: null });
    };

    render() {
        return (
            <div>
                <AppBar position='static'>
                    <Toolbar style={{ minHeight: '30px' }}>
                        <IconButton color='default' aria-label='Menu'>
                            <MenuIcon />
                        </IconButton>
                        <Link to='/policies/custom_rules'>
                            <Button color='default'>Go Back</Button>
                        </Link>
                    </Toolbar>
                </AppBar>
                <Message ref={a => (this.msg = a)} />
                <Paper>
                    <Grid container className='root' direction='column'>
                        <Grid item xs={12} className='grid-item'>
                            <Typography className='page-title' type='display1' gutterBottom>
                                Create Custom Rule
                            </Typography>
                        </Grid>
                        <GeneralDetails policy={this.state.policy} handleChangeChild={this.handleChangeChild} />

                        <CustomRuleDetails
                            policy={this.state.policy}
                            state={this.state}
                            handleChangeChild={this.handleChangeChild}
                            handlePopoverOpen={this.handlePopoverOpen}
                            handlePopoverClose={this.handlePopoverClose}
                        />

                        <Paper elevation={20}>
                            <Grid item xs={6} className='grid-item'>
                                <Divider />
                                <div>
                                    <Button color='primary' onClick={() => this.handlePolicySave()}>
                                        Save
                                    </Button>
                                    <Link to='/policies/custom_rules'>
                                        <Button >Cancel</Button>
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

export default withRouter(CreateCustomRulePolicy);
