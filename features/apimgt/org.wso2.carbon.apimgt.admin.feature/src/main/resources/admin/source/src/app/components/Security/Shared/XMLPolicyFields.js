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

import TextField from 'material-ui/TextField';
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';
import Select from 'material-ui/Select';
import Input, { InputLabel } from 'material-ui/Input';
import { MenuItem } from 'material-ui/Menu';

class XMLPolicyFields extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
    }

    handleChange(name) {
        return (event) => {
            switch (name) {
                case 'dtdEnabled':
                case 'externalEntitiesEnabled':
                    this.props.handleChangeChild(name, event.target.value == 'true');
                    break;

                case 'name':
                    this.props.handleChangeChild(name, event.target.value);
                    break;

                default:
                    this.props.handleChangeChild(name, parseInt(event.target.value));
                    break;
            }
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
                        <InputLabel htmlFor='dtdEnabled'>DTD Enabled</InputLabel>
                        &nbsp;&nbsp;
                        <Select
                            value={this.props.policy.policy.dtdEnabled.toString()}
                            onChange={this.handleChange('dtdEnabled')}
                            input={<Input name='dtdEnabled' id='dtdEnabled' />}
                        >
                            <MenuItem value='true'>true</MenuItem>
                            <MenuItem value='false'>false</MenuItem>
                        </Select>
                    </Grid>
                    <br />
                    <Grid item xs={6} className='grid-item'>
                        <InputLabel htmlFor='externalEntitiesEnabled'>External Entities Enabled</InputLabel>
                        &nbsp;&nbsp;
                        <Select
                            value={this.props.policy.policy.externalEntitiesEnabled.toString()}
                            onChange={this.handleChange('externalEntitiesEnabled')}
                            input={<Input name='externalEntitiesEnabled' id='externalEntitiesEnabled' />}
                        >
                            <MenuItem value='true'>true</MenuItem>
                            <MenuItem value='false'>false</MenuItem>
                        </Select>
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
                    <Grid item xs={6} className='grid-item'>
                        <TextField
                            id='maxElementCount'
                            required
                            label='Max Element Count'
                            defaultValue={this.props.policy.policy.maxElementCount.toString()}
                            className='text-field-half'
                            onChange={this.handleChange('maxElementCount')}
                        />
                    </Grid>
                    <br />
                    <Grid item xs={6} className='grid-item'>
                        <TextField
                            id='maxAttributeCount'
                            required
                            label='Max Attribute Count'
                            defaultValue={this.props.policy.policy.maxAttributeCount.toString()}
                            className='text-field-half'
                            onChange={this.handleChange('maxAttributeCount')}
                        />
                    </Grid>
                    <br />
                    <Grid item xs={6} className='grid-item'>
                        <TextField
                            id='maxAttributeLength'
                            required
                            label='Max Attribute Length'
                            defaultValue={this.props.policy.policy.maxAttributeLength.toString()}
                            className='text-field-half'
                            onChange={this.handleChange('maxAttributeLength')}
                        />
                    </Grid>
                    <br />
                    <Grid item xs={6} className='grid-item'>
                        <TextField
                            id='entityExpansionLimit'
                            required
                            label='Entity Expansion Limit'
                            defaultValue={this.props.policy.policy.entityExpansionLimit.toString()}
                            className='text-field-half'
                            onChange={this.handleChange('entityExpansionLimit')}
                        />
                    </Grid>
                    <br />
                    <Grid item xs={6} className='grid-item'>
                        <TextField
                            id='maxChildrenPerElement'
                            required
                            label='Max Children Per Element'
                            defaultValue={this.props.policy.policy.maxChildrenPerElement.toString()}
                            className='text-field-half'
                            onChange={this.handleChange('maxChildrenPerElement')}
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

export default XMLPolicyFields;
