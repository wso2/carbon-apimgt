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
import BlackListDetails from '../Shared/BlackListDetail';

const messages = {
    success: 'Created API rate limit successfully',
    failure: 'Error while creating API rate limit',
};

const helperTextObject = [
    ['API', { format: 'Format : ${context}', example: 'Eg : /test/1.0.0' }],
    ['APPLICATION',
        { format: 'Format : ${userName}:${applicationName}', example: 'Eg : admin:DefaultApplication' }],
    ['USER', { format: 'Format : ${userName}', example: 'Eg : admin' }],
    ['IP', { format: 'Format : ${ip}', example: 'Eg : 127.0.0.1' }],
    ['IP_RANGE', { format: 'Format : ${ip}', example: 'Eg : 127.0.0.1' }]
];
const helperTextMap = new Map(helperTextObject);

class CreateBlackListPolicy extends Component {
    constructor(props) {
        super(props);
        this.state = {
            policy: {
                conditionId: '',
                conditionType: 'API',
                conditionValue: '',
                status: true,
                ipCondition: {
                    ipConditionType: '',
                    specificIP: '',
                    startingIP: '',
                    endingIP: ''
                }

            },
            value: 'API',
            helperText: {
                format: 'Format : ${context}',
                example: 'Eg : /test/1.0.0'
            }
        };
        this.handleChangeChild = this.handleChangeChild.bind(this);
        this.handleChangeChildValue = this.handleChangeChildValue.bind(this);
        this.handlePolicySave = this.handlePolicySave.bind(this);
    }


    handleChangeChild(event) {
        const policy = this.state.policy;
        const { value } = event.target;
        const helperText = this.getHelperText(value);
        policy.conditionType = value;

        if (value === 'IP' || value === 'IP_RANGE') {
            policy.ipCondition.ipConditionType = value;
        }
        this.setState({ value, policy, helperText });
    };

    getHelperText(policyType) {
        return helperTextMap.get(policyType);
    }

    handleChangeChildValue(event) {
        const policy = this.state.policy;
        const { id, value } = event.target;
        policy.conditionValue = value;
        if (id === 'ip') {
            policy.ipCondition.specificIP = value;
        } else if (id === 'start_ip') {
            policy.ipCondition.startingIP = value;
        } else if (id === 'end_ip') {
            policy.ipCondition.endingIP = value;
        }
        this.setState({ policy: policy });
    };

    handlePolicySave() {
        const api = new API();
        const promisedPolicies = api.createBlackListPolicy(this.state.policy);
        promisedPolicies
            .then((response) => {
                Alert.info(messages.success);
                let redirect_url = "/policies/black_list";
                this.props.history.push(redirect_url);
            })
            .catch((error) => {
                Alert.error(messages.failure);
            });
    }

    render() {
        return (
            <div>
                <AppBar position='static'>
                    <Toolbar style={{ minHeight: '30px' }}>
                        <IconButton color='default' aria-label='Menu'>
                            <MenuIcon />
                        </IconButton>
                        <Link to='/policies/black_list'>
                            <Button color='default'>Go Back</Button>
                        </Link>
                    </Toolbar>
                </AppBar>
                <Paper>
                    <Grid container className='root' direction='column'>
                        <Grid item xs={12} className='grid-item'>
                            <Typography className='page-title' type='display1' gutterBottom>
                                Create Black List Policy
                            </Typography>
                        </Grid>
                        <BlackListDetails policy={this.state.policy}
                            selectedValue={this.state.value}
                            helperText={this.state.helperText}
                            handleChangeChild={this.handleChangeChild}
                            handleChangeChildValue={this.handleChangeChildValue} />

                        <Paper elevation={20}>
                            <Grid item xs={6} className='grid-item'>
                                <Divider />
                                <div>
                                    <Button color='primary' onClick={this.handlePolicySave}>
                                        Save
                                    </Button>
                                    <Link to='policies/black_list'>
                                        <Button>Cancel</Button>
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

export default withRouter(CreateBlackListPolicy);
