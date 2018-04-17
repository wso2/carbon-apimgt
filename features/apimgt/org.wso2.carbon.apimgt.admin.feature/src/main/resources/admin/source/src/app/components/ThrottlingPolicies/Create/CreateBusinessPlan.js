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
import QuotaLimits from '../Shared/QuotaLimits';
import BurstControl from '../Shared/BurstControl';
import PolicyFlags from '../Shared/PolicyFlags';
import CustomAttributes from '../Shared/CustomAttributes';

import API from '../../../data/api';
import Message from '../../Shared/Message';
import '../Shared/Shared.css';
import Alert from '../../Shared/Alert'

const messages = {
    success: 'Created business plan successfully',
    failure: 'Error while creating business plan',
};

class CreateBusinessPlan extends Component {
    constructor(props) {
        super(props);
        this.state = {
            policy: {
                id: '',
                policyName: '',
                displayName: '',
                description: '',
                isDeployed: true,
                rateLimitCount: 0,
                rateLimitTimeUnit: 'sec',
                stopOnQuotaReach: true,
                billingPlan: 'FREE',
                defaultLimit: {
                    bandwidthLimit: {
                        dataAmount: 0,
                        dataUnit: 'MB',
                    },
                    requestCountLimit: {
                        requestCount: 0,
                    },
                    type: 'RequestCountLimit',
                    timeUnit: 'min',
                    unitTime: 0,
                },
                customAttributes: [],
            },
        };
        this.setBandwithDataUnit = this.setBandwithDataUnit.bind(this);
        this.setRateLimitUnit = this.setRateLimitUnit.bind(this);
        this.handleLimitTypeRadioButtonChild = this.handleLimitTypeRadioButtonChild.bind(this);
        this.handleChangeChild = this.handleChangeChild.bind(this);
        this.handleDefaultQuotaChangeChild = this.handleDefaultQuotaChangeChild.bind(this);
        this.handleAttributeChange = this.handleAttributeChange.bind(this);
        this.handlePolicySave = this.handlePolicySave.bind(this);
    }

    setBandwithDataUnit(value) {
        const policy = this.state.policy;
        policy.defaultLimit.bandwidthLimit.dataUnit = value;
        this.setState({ policy });
    }

    setRateLimitUnit(value) {
        const policy = this.state.policy;
        policy.defaultLimit.timeUnit = value;
        this.setState({ policy });
    }

    handleLimitTypeRadioButtonChild(value) {
        const policy = this.state.policy;
        policy.defaultLimit.type = value;
        this.setState({ policy });
    }

    handleChangeChild(name, value) {
        const policy = this.state.policy;
        policy[name] = value
        this.setState({
            policy,
        });
    }

    handleDefaultQuotaChangeChild(name, value) {
        const policy = this.state.policy;
        const intValue = parseInt(value);
        var value = isNaN(intValue) ? value : intValue;
        if (name == 'RequestCountLimit') {
            policy.defaultLimit.requestCountLimit.requestCount = value;
        } else if (name == 'BandwidthLimit') {
            policy.defaultLimit.bandwidthLimit.dataAmount = value;
        } else if (name == 'unitTime') {
            policy.defaultLimit.unitTime = value;
        }
        this.setState({
            policy,
        });
    }

    handleAttributeChange(attributes) {
        const policy = this.state.policy;
        policy.customAttributes = attributes;
        this.setState({
            policy,
        });
    }

    handlePolicySave() {
        const api = new API();
        const promised_policies = api.createSubscriptionLevelPolicy(this.state.policy);
        const props = this.props;
        promised_policies
            .then((response) => {
                Alert.info(messages.success);
                let redirect_url = "/policies/business_plans";
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
                        <IconButton color='contrast' aria-label='Menu'>
                            <MenuIcon />
                        </IconButton>
                        <Link to='/policies/business_plans'>
                            <Button color='contrast'>Go Back</Button>
                        </Link>
                    </Toolbar>
                </AppBar>
                <Message ref={a => (this.msg = a)} />
                <Paper>
                    <Grid container className='root' direction='column'>
                        <Grid item xs={12} className='grid-item'>
                            <Typography className='page-title' type='display1' gutterBottom>
                                Create Business Plan
                            </Typography>
                        </Grid>
                        <GeneralDetails policy={this.state.policy} handleChangeChild={this.handleChangeChild} />

                        <QuotaLimits
                            policy={this.state.policy}
                            setBandwithDataUnit={this.setBandwithDataUnit}
                            handleLimitTypeRadioButtonChild={this.handleLimitTypeRadioButtonChild}
                            handleDefaultQuotaChangeChild={this.handleDefaultQuotaChangeChild}
                            setRateLimitUnit={this.setRateLimitUnit}
                        />

                        <BurstControl policy={this.state.policy} handleChangeChild={this.handleChangeChild} />

                        <PolicyFlags policy={this.state.policy} handleChangeChild={this.handleChangeChild} />

                        <CustomAttributes
                            attributes={this.state.policy.customAttributes}
                            handleAttributeChange={this.handleAttributeChange}
                        />

                        <Paper elevation={20}>
                            <Grid item xs={6} className='grid-item'>
                                <Divider />
                                <div>
                                    <Button raised color='primary' onClick={() => this.handlePolicySave()}>
                                        Save
                                    </Button>
                                    <Link to='/policies/business_plans'>
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

export default CreateBusinessPlan;
