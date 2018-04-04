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
import List, { ListItem, ListItemText } from 'material-ui/List';
import Menu, { MenuItem } from 'material-ui/Menu';

import './Shared.css';

const burstControlUnits = ['Request/s', 'Request/min'];
const burstControlUnitsMap = ['sec', 'min'];

class BurstControl extends Component {
    constructor(props) {
        super(props);
        this.state = {
            anchorElBurst: null,
            openBurst: false,
            selectedIndexBurst: 0,
        };
        this.handleBurstClickListItem = this.handleBurstClickListItem.bind(this);
        this.handleBurstMenuItemClick = this.handleBurstMenuItemClick.bind(this);
        this.handleBurstRequestClose = this.handleBurstRequestClose.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }

    handleBurstClickListItem(event) {
        this.setState({ openBurst: true, anchorElBurst: event.currentTarget });
    }

    handleBurstMenuItemClick(event, index) {
        this.setState({ selectedIndexBurst: index, openBurst: false });
        this.props.handleChangeChild('rateLimitTimeUnit', burstControlUnitsMap[index]);
    }

    handleBurstRequestClose() {
        this.setState({ openBurst: false });
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
                        Burst Conrtol (Rate Limiting)
                    </Typography>
                </Grid>
                <Grid item xs={6} className='grid-item'>
                    <Divider />
                    <div className='container'>
                        <TextField
                            id='burstContromReqCount'
                            label='Request Count'
                            value={this.props.policy.rateLimitCount}
                            onChange={this.handleChange('rateLimitCount')}
                            className='text-field-half'
                            margin='normal'
                        />

                        <List>
                            <ListItem
                                button
                                aria-haspopup='true'
                                aria-controls='lock-menu'
                                aria-label='Burst Control Units'
                                onClick={this.handleBurstClickListItem}
                            >
                                <ListItemText primary={burstControlUnits[this.state.selectedIndexBurst]} />
                            </ListItem>
                        </List>
                        <Menu
                            id='lock-menu'
                            anchorEl={this.state.anchorElBurst}
                            open={this.state.openBurst}
                            onClose={this.handleBurstRequestClose}
                        >
                            {burstControlUnits.map((option, index) => (
                                <MenuItem
                                    key={option}
                                    selected={index === this.state.selectedIndexBurst}
                                    onClick={event => this.handleBurstMenuItemClick(event, index)}
                                >
                                    {option}
                                </MenuItem>
                            ))}
                        </Menu>
                    </div>
                </Grid>
            </Paper>
        );
    }
}

export default BurstControl;
