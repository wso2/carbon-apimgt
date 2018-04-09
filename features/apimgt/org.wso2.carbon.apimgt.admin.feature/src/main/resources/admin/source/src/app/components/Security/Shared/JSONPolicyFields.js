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

import TextField from 'material-ui/TextField';
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';

import React, { Component } from 'react';

class JSONPolicyFields extends Component {
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
        if (this.props.policy) {
            return (
                <Paper elevation={20}>
                    <br />
                    <Grid item xs={6} className='grid-item'>
                        <TextField
                            id='name'
                            required
                            label='Policy Name'
                            defaultValue={this.props.policy.name}
                            className='text-field-half'
                            onChange={this.handleChange('name')}
                        />
                    </Grid>
                    <br />
                    <Grid item xs={6} className='grid-item'>
                        <TextField
                            id='maxFieldCount'
                            required
                            label='Max Field Count'
                            defaultValue={this.props.policy.policy.maxFieldCount.toString()}
                            className='text-field-half'
                            onChange={this.handleChange('maxFieldCount')}
                        />
                    </Grid>
                    <br />
                    <Grid item xs={6} className='grid-item'>
                        <TextField
                            id='maxStringLength'
                            required
                            label='Max String Length'
                            defaultValue={this.props.policy.policy.maxStringLength.toString()}
                            className='text-field-half'
                            onChange={this.handleChange('maxStringLength')}
                        />
                    </Grid>
                    <br />
                    <Grid item xs={6} className='grid-item'>
                        <TextField
                            id='maxArrayElementCount'
                            required
                            label='Max Array Element Count'
                            defaultValue={this.props.policy.policy.maxArrayElementCount.toString()}
                            className='text-field-half'
                            onChange={this.handleChange('maxArrayElementCount')}
                        />
                    </Grid>
                    <br />
                    <Grid item xs={6} className='grid-item'>
                        <TextField
                            id='maxFieldLength'
                            required
                            label='Max Field Length'
                            defaultValue={this.props.policy.policy.maxFieldLength.toString()}
                            className='text-field-half'
                            onChange={this.handleChange('maxFieldLength')}
                        />
                    </Grid>
                    <br />
                    <Grid item xs={6} className='grid-item'>
                        <TextField
                            id='maxDepth'
                            required
                            label='Max Depth'
                            defaultValue={this.props.policy.policy.maxDepth.toString()}
                            className='text-field-half'
                            onChange={this.handleChange('maxDepth')}
                        />
                    </Grid>
                    <br />
                </Paper>
            );
        } else {
            return <h1>Please wait...</h1>;
        }
    }
}

export default JSONPolicyFields;
