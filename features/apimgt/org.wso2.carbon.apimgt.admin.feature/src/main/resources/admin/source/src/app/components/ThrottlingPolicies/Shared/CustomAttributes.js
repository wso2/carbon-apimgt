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
import Grid from 'material-ui/Grid';
import Divider from 'material-ui/Divider';
import TextField from 'material-ui/TextField';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import DeleteIcon from '@material-ui/icons/Delete';
import IconButton from 'material-ui/IconButton';
import Tooltip from 'material-ui/Tooltip';

import './Shared.css';

class CustomAttributes extends Component {
    constructor(props) {
        super(props);
        this.addAttribute = this.addAttribute.bind(this);
        this.removeAttribute = this.removeAttribute.bind(this);
        this.handleAttributeChange = this.handleAttributeChange.bind(this);
    }

    addAttribute() {
        const attribute = {
            name: '',
            value: '',
        };
        const attributes = this.props.attributes;
        attributes.push(attribute);
        this.props.handleAttributeChange(attributes);
    }

    removeAttribute(attrIndex) {
        const attributes = this.props.attributes;
        attributes.splice(attrIndex, 1);
        this.props.handleAttributeChange(attributes);
    }

    handleAttributeChange(id) {
        return (event) => {
            const value = event.target.value;
            const elemId = event.target.id;
            const attributes = this.props.attributes;

            const attribute = attributes[id];
            if (elemId == 'attrName') {
                attribute.name = value;
            } else if (elemId == 'attrValue') {
                attribute.value = value;
            }

            attributes[id] = attribute;
            this.props.handleAttributeChange(attributes);
        };
    }

    render() {
        return (
            <Paper elevation={20}>
                <Grid item xs={12}>
                    <Typography className='page-title' type='subheading' gutterBottom>
                        Custom Attributes
                    </Typography>
                </Grid>
                <Grid item xs={6} className='grid-item'>
                    <Divider />
                    <div className='container'>
                        <Button raised color='accent' onClick={() => this.addAttribute()}>
                            Add Custom Attribute
                        </Button>
                    </div>
                    <div>
                        {this.props.attributes.map((n) => {
                            return (
                                <Grid item xs={6} className='grid-item'>
                                    <TextField
                                        id='attrName'
                                        label='name'
                                        value={n.name}
                                        onChange={this.handleAttributeChange(this.props.attributes.indexOf(n))}
                                    />
                                    <TextField
                                        id='attrValue'
                                        label='value'
                                        value={n.value}
                                        onChange={this.handleAttributeChange(this.props.attributes.indexOf(n))}
                                    />
                                    <IconButton
                                        aria-label='Delete'
                                        onClick={() => this.removeAttribute(this.props.attributes.indexOf(n))}
                                    >
                                        <DeleteIcon />
                                    </IconButton>
                                </Grid>
                            );
                        })}
                    </div>
                </Grid>
            </Paper>
        );
    }
}

export default CustomAttributes;
