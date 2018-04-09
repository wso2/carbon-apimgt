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

import Radio, { RadioGroup } from 'material-ui/Radio';
import Paper from 'material-ui/Paper';
import TextField from 'material-ui/TextField';
import List, { ListItem, ListItemText } from 'material-ui/List';
import Menu, { MenuItem } from 'material-ui/Menu';
import Typography from 'material-ui/Typography';
import Divider from 'material-ui/Divider';
import Grid from 'material-ui/Grid';
import { FormControl, FormLabel, FormGroup, FormControlLabel } from 'material-ui/Form';

import './Shared.css';

const bandwidthUnits = ['KB', 'MB'];

const unitTimeUnits = ['Minute(s)', 'Hour(s)', 'Day(s)', 'Week(s)', 'Month(s)', 'Year(s)'];

const unitTimeMap = ['min', 'hour', 'day', 'week', 'month', 'year'];

class QuotaLimits extends Component {
    constructor(props) {
        super(props);
        this.state = {
            anchorElQuota: null,
            openQuota: false,
            selectedIndexQuota: 1,
            anchorElUnit: null,
            openUnit: false,
            selectedIndexUnit: 0,
            error:true
        };
        this.handleDefaultQuotaChange = this.handleDefaultQuotaChange.bind(this);
        this.handleQuotaClickListItem = this.handleQuotaClickListItem.bind(this);
        this.handleUnitClickListItem = this.handleUnitClickListItem.bind(this);
        this.handleQuotaRequestClose = this.handleQuotaRequestClose.bind(this);
        this.handleUnitRequestClose = this.handleUnitRequestClose.bind(this);
        this.handleQuotaMenuItemClick = this.handleQuotaMenuItemClick.bind(this);
        this.handleUnitMenuItemClick = this.handleUnitMenuItemClick.bind(this);
        this.handleLimitTypeRadioButton = this.handleLimitTypeRadioButton.bind(this);
    }

    // get the index values to load the menus
    componentWillReceiveProps(nextProps) {
        // get the relevent id related to bandwith limit units.
        const quotaIndex = bandwidthUnits.indexOf(nextProps.policy.defaultLimit.bandwidthLimit.dataUnit);
        // get the relevent id related to quota limit unit.
        const unitIndex = unitTimeMap.indexOf(nextProps.policy.defaultLimit.timeUnit);
        this.setState({ selectedIndexQuota: quotaIndex, selectedIndexUnit: unitIndex });
    }

    handleDefaultQuotaChange(name) {
        return (event) => {
            this.props.handleDefaultQuotaChangeChild(name, event.target.value);
        };
    }
    handleQuotaClickListItem(event) {
        this.setState({ openQuota: true, anchorElQuota: event.currentTarget });
    }
    handleUnitClickListItem(event) {
        this.setState({ openUnit: true, anchorElUnit: event.currentTarget });
    }
    handleQuotaRequestClose() {
        this.setState({ openQuota: false });
    }
    handleUnitRequestClose() {
        this.setState({ openUnit: false });
    }
    handleQuotaMenuItemClick(event, index) {
        this.setState({ selectedIndexQuota: index, openQuota: false });
        this.props.setBandwithDataUnit(bandwidthUnits[index]);
    }
    handleUnitMenuItemClick(event, index) {
        this.setState({ selectedIndexUnit: index, openUnit: false });
        this.props.setRateLimitUnit(unitTimeMap[index]);
    }
    handleLimitTypeRadioButton(event, value) {
        this.props.handleLimitTypeRadioButtonChild(value);
    }

    render() {
        let quota = null;
        if (this.props.policy.defaultLimit.type === 'BandwidthLimit') {
            quota = (
                <Grid item xs={6} className='grid-item'>
                    <div className='container'>
                        <TextField
                            id='quotaLimits'
                            required
                            label='Data Bandwidth'
                            value={this.props.policy.defaultLimit.bandwidthLimit.dataAmount}
                            onChange={this.handleDefaultQuotaChange('BandwidthLimit')}
                            className='text-field-half'
                            margin='normal'
                        />

                        <List>
                            <ListItem
                                button
                                aria-haspopup='true'
                                aria-controls='bandwidth-menu'
                                onClick={this.handleQuotaClickListItem}
                            >
                                <ListItemText
                                    primary='Units'
                                    secondary={bandwidthUnits[this.state.selectedIndexQuota]}
                                />
                            </ListItem>
                        </List>
                        <Menu
                            id='bandwith-menu'
                            anchorEl={this.state.anchorElQuota}
                            open={this.state.openQuota}
                            onClose={this.handleQuotaRequestClose}
                        >
                            {bandwidthUnits.map((option, index) => (
                                <MenuItem
                                    key={option}
                                    selected={index === this.state.selectedIndexQuota}
                                    onClick={event => this.handleQuotaMenuItemClick(event, index)}
                                >
                                    {option}
                                </MenuItem>
                            ))}
                        </Menu>
                    </div>
                </Grid>
            );
        } else {
            quota = (
                <Grid item xs={6} className='grid-item'>
                    <TextField
                        id='quotaLimits'
                        required
                        label='Request Count'
                        value={this.props.policy.defaultLimit.requestCountLimit.requestCount}
                        onChange={this.handleDefaultQuotaChange('RequestCountLimit')}
                        className='text-field-full'
                        margin='normal'
                        
                    />
                </Grid>
            );
        }

        return (
            <Paper elevation={20}>
                <Grid item xs={12}>
                    <Typography className='page-title' type='subheading' gutterBottom>
                        Quota Limits
                    </Typography>
                </Grid>
                <Grid item xs={6} className='grid-item'>
                    <Divider />
                    <FormControl component='fieldset' required>
                        <RadioGroup
                            aria-label='quota-limits'
                            name='quota-limits'
                            className='container'
                            value={this.props.policy.defaultLimit.type}
                            onChange={this.handleLimitTypeRadioButton}
                        >
                            <FormControlLabel value='RequestCountLimit' control={<Radio />} label='Request Count' />
                            <FormControlLabel value='BandwidthLimit' control={<Radio />} label='Request Bandwidth ' />
                        </RadioGroup>
                    </FormControl>
                </Grid>
                {quota}
                <Grid item xs={6} className='grid-item'>
                    <div className='container'>
                        <TextField
                            id='quotaUnitTime'
                            label='Unit Time'
                            value={this.props.policy.defaultLimit.unitTime}
                            onChange={this.handleDefaultQuotaChange('unitTime')}
                            className='text-field-half'
                            margin='normal'
                            required
                        />

                        <List>
                            <ListItem
                                button
                                aria-haspopup='true'
                                aria-controls='unit-time-menu'
                                onClick={this.handleUnitClickListItem}
                            >
                                <ListItemText primary={unitTimeUnits[this.state.selectedIndexUnit]} />
                            </ListItem>
                        </List>
                        <Menu
                            id='unit-menu'
                            anchorEl={this.state.anchorElUnit}
                            open={this.state.openUnit}
                            onClose={this.handleUnitRequestClose}
                        >
                            {unitTimeUnits.map((option, index) => (
                                <MenuItem
                                    key={option}
                                    selected={index === this.state.selectedIndexUnit}
                                    onClick={event => this.handleUnitMenuItemClick(event, index)}
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

export default QuotaLimits;
