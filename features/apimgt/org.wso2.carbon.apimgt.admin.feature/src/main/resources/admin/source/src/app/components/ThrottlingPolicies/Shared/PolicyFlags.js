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
import { FormControl, FormLabel, FormGroup, FormControlLabel } from 'material-ui/Form';
import Checkbox from 'material-ui/Checkbox';
import List, { ListItem, ListItemText } from 'material-ui/List';
import Menu, { MenuItem } from 'material-ui/Menu';

import './Shared.css';

const billingOptions = ['Free', 'Commercial'];

const billingOptionsMap = ['FREE', 'COMMERCIAL'];

class PolicyFlags extends Component {
    constructor(props) {
        super(props);
        this.state = {
            value: '',
            anchorElBilling: null,
            openBilling: false,
            selectedIndexBilling: 0,
        };
        this.handlePolicyFlageChange = this.handlePolicyFlageChange.bind(this);
        this.handleBillingClickListItem = this.handleBillingClickListItem.bind(this);
        this.handleBillingRequestClose = this.handleBillingRequestClose.bind(this);
        this.handleBillingMenuItemClick = this.handleBillingMenuItemClick.bind(this);
    }

    // get the index values to load the menus
    componentWillReceiveProps(nextProps) {
        // get the relevent id related to plan.
        const planId = billingOptionsMap.indexOf(nextProps.policy.billingPlan);
        this.setState({ selectedIndexBilling: planId });
    }
    handlePolicyFlageChange(name) {
        return (event, checked) => {
            this.props.handleChangeChild(name, checked);
        };
    }

    handleBillingClickListItem(event) {
        this.setState({ openBilling: true, anchorElBilling: event.currentTarget });
    }

    handleBillingRequestClose() {
        this.setState({ openBilling: false });
    }

    handleBillingMenuItemClick(event, index) {
        this.setState({ selectedIndexBilling: index, openBilling: false });
        this.props.handleChangeChild('billingPlan', billingOptionsMap[index]);
    }

    render() {
        return (
            <Paper elevation={20}>
                <Grid item xs={12}>
                    <Typography className='page-title' type='subheading' gutterBottom>
                        Policy Flags
                    </Typography>
                </Grid>
                <Grid item xs={6} className='grid-item'>
                    <Divider />
                    <FormControlLabel
                        control={
                            <Checkbox
                                checked={this.props.policy.stopOnQuotaReach}
                                onChange={this.handlePolicyFlageChange('stopOnQuotaReach')}
                                value='stopOnQuotaReach'
                            />
                        }
                        label='Stop On Quota Reach'
                    />
                </Grid>
                <Grid item xs={6} className='grid-item'>
                    <List>
                        <ListItem
                            button
                            aria-haspopup='true'
                            aria-controls='lock-menu'
                            aria-label='Billing Plan'
                            onClick={this.handleBillingClickListItem}
                        >
                            <ListItemText
                                primary='Billing Plan'
                                secondary={billingOptions[this.state.selectedIndexBilling]}
                            />
                        </ListItem>
                    </List>
                    <Menu
                        id='lock-menu'
                        anchorEl={this.state.anchorElBilling}
                        open={this.state.openBilling}
                        onClose={this.handleBillingRequestClose}
                    >
                        {billingOptions.map((option, index) => (
                            <MenuItem
                                key={option}
                                selected={index === this.state.selectedIndexBilling}
                                onClick={event => this.handleBillingMenuItemClick(event, index)}
                            >
                                {option}
                            </MenuItem>
                        ))}
                    </Menu>
                </Grid>
            </Paper>
        );
    }
}
export default PolicyFlags;
