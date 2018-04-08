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

import Grid from 'material-ui/Grid';
import Divider from 'material-ui/Divider';
import TextField from 'material-ui/TextField';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';

import './Shared.css';

class CustomRuleDetails extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
    }

    handleChange(name) {
        return (event) => {
            this.props.handleChangeChild(name, event.target.value);
        };
    }

    render() {
        return (
            <Paper elevation={20}>
                    <Grid item xs={12}>
                        <Typography className='page-title' type='subheading' gutterBottom>
                            Custom Query Details
                        </Typography>
                    </Grid>
                    <Grid item xs={6} className='grid-item'>
                        <Divider />
                        <TextField
                            id='keyTemplate'
                            required
                            label='Key Template'
                            value={this.props.policy.keyTemplate}
                            onChange={this.handleChange('keyTemplate')}
                            className='text-field-full'
                            margin='normal'
                        />
                    </Grid>
                    <Grid item xs={6} className='grid-item'>
                        <TextField
                            id='siddhiQuery'
                            required
                            label='Siddhi Query'
                            value={this.props.policy.siddhiQuery}
                            onChange={this.handleChange('siddhiQuery')}
                            className='text-field-full'
                            multiline
                            margin='normal'
                        />
                    </Grid>
                </Paper>
        );
    }
}

export default CustomRuleDetails;
