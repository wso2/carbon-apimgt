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
import MenuIcon from 'material-ui-icons/Menu';
import Typography from 'material-ui/Typography';
import Divider from 'material-ui/Divider';
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';

import API from '../../../data/api';
import Message from '../../Shared/Message';
import XMLPolicyFields from '../Shared/XMLPolicyFields';

class EditXMLThreatProtectionPolicy extends Component {
    constructor(props) {
        super(props);
        this.state = {
            policy: null,
        };
    }

    componentDidMount() {
        const api = new API();
        const promised_policy = api.getThreatProtectionPolicy(this.props.match.params.policy_uuid);
        promised_policy.then((response) => {
            const policy = response.obj;
            policy.policy = JSON.parse(policy.policy);
            this.setState({ policy });
        });
    }

    handleChangeChild(name, value) {
        const policy = this.state.policy;
        if (name == 'name') {
            policy.name = value;
        } else {
            policy.policy[name] = value;
        }
        this.setState({ policy });
    }

    handlePolicyUpdate(name, value) {
        const api = new API();
        const policy = this.state.policy;
        policy.policy = JSON.stringify(policy.policy);
        const promised_policy = api.addThreatProtectionPolicy(policy);
        promised_policy.then((response) => {
            if (response.status == 200) {
                this.msg.info('Threat protection policy updated successfully.');
            } else {
                this.msg.error('Failed to update threat protection policy.');
                console.log(response.statusText);
            }
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
                        <Link to='/security/xml_threat_protection'>
                            <Button color='contrast'>Go Back</Button>
                        </Link>
                    </Toolbar>
                </AppBar>
                <Message ref={a => (this.msg = a)} />
                <Paper>
                    <Grid container className='root' direction='column'>
                        <Grid item xs={12} className='grid-item'>
                            <Typography className='page-title' type='display1' gutterBottom>
                                Edit Threat Protection Policy
                            </Typography>
                        </Grid>
                        <XMLPolicyFields
                            policy={this.state.policy}
                            handleChangeChild={this.handleChangeChild.bind(this)}
                        />
                        <Paper elevation={20}>
                            <Grid item xs={6} className='grid-item'>
                                <Divider />
                                <div>
                                    <Button raised color='primary' onClick={() => this.handlePolicyUpdate()}>
                                        Update
                                    </Button>
                                    <Link to='/security/xml_threat_protection'>
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

export default EditXMLThreatProtectionPolicy;
