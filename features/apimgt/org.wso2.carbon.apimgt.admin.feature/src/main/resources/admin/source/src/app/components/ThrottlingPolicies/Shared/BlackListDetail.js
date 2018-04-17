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
import green from 'material-ui/colors/green';
import Radio, { RadioGroup } from 'material-ui/Radio';
import { FormLabel, FormControl, FormControlLabel, FormHelperText } from 'material-ui/Form';
import RadioButtonUncheckedIcon from '@material-ui/icons/RadioButtonUnchecked';
import RadioButtonCheckedIcon from '@material-ui/icons/RadioButtonChecked';

import './Shared.css';
import { withStyles } from 'material-ui/styles';

const styles = theme => ({
    root: {
        display: 'flex',
        color: 'green'
    },
    formControl: {
        margin: theme.spacing.unit * 3,
    },
    group: {
        margin: `${theme.spacing.unit}px 0`,
    }
});

class BlackListDetails extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleValueChange = this.handleValueChange.bind(this);
    }

    handleChange(event) {
        this.props.handleChangeChild(event);
    }

    handleValueChange(event) {
        this.props.handleChangeChildValue(event);
    }

    render() {
        const { classes } = this.props;
        return (
            <Paper elevation={20}>

                <Grid item xs={6} className='grid-item'>
                    <Divider />
                    <FormControl component="fieldset" required className={classes.formControl}>
                        <FormLabel component="legend">Select Item to Blacklist</FormLabel>
                        <RadioGroup
                            aria-label="blackListItem"
                            name="blackListItem"
                            className={classes.group}
                            value={this.props.selectedValue}
                            onClick={this.handleChange}
                        >
                            <FormControlLabel value="API" control={<Radio />} label="API Context" />
                            <FormControlLabel value="APPLICATION" control={<Radio />} label="Application" />
                            <FormControlLabel value="USER" control={<Radio />} label="User" />
                            <FormControlLabel value="IP" control={<Radio />} label="IP Address" />
                            <FormControlLabel value="IP_RANGE" control={<Radio />} label="IP Range" />
                        </RadioGroup>
                    </FormControl>
                </Grid>
                <Grid item xs={3} className='grid-item'>
                    {this.props.selectedValue !== "IP_RANGE" && this.props.selectedValue !== "IP" &&
                        <TextField
                            id='value'
                            required
                            label='Value'
                            value={this.props.policy.conditionValue}
                            onChange={this.handleValueChange}
                            className='text-field-full'
                            margin='normal'
                        />
                    }
                    {this.props.selectedValue === "IP" &&
                        <TextField
                            id='ip'
                            required
                            label='IP Address'
                            value={this.props.policy.ipCondition.specificIP}
                            onChange={this.handleValueChange}
                            className='text-field-full'
                            margin='normal'
                        />
                    }
                    {this.props.selectedValue === "IP_RANGE" &&
                        <div>
                            <TextField
                                id='start_ip'
                                required
                                label='Start IP'
                                value={this.props.policy.ipCondition.startingIP}
                                onChange={this.handleValueChange}
                                className='text-field-full'
                                margin='normal'
                            />
                            <FormHelperText className={classes.root} id="format-helper-text">{this.props.helperText.format}</FormHelperText>
                            <FormHelperText className={classes.root} id="example-helper-text">{this.props.helperText.example}</FormHelperText>
                            <TextField
                                id='end_ip'
                                required
                                label='End IP'
                                value={this.props.policy.ipCondition.endingIP}
                                onChange={this.handleValueChange}
                                className='text-field-full'
                                margin='normal'
                            />
                        </div>}
                    <FormHelperText className={classes.root} id="format-helper-text">{this.props.helperText.format}</FormHelperText>
                    <FormHelperText className={classes.root} id="example-helper-text">{this.props.helperText.example}</FormHelperText>
                </Grid>

            </Paper>
        );
    }
}

export default withStyles(styles)(BlackListDetails);
