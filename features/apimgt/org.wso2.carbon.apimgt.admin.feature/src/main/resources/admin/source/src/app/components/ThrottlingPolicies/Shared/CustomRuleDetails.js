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
import Icon from 'material-ui/Icon';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';
import { withStyles } from 'material-ui/styles';
import IconButton from 'material-ui/IconButton';
import Input, { InputLabel, InputAdornment } from 'material-ui/Input';
import { FormControl, FormHelperText } from 'material-ui/Form';



import './Shared.css';
import Grow from 'material-ui/transitions/Grow';
import { Manager, Target, Popper } from 'react-popper';
import Popover from 'material-ui/Popover';

const styles = theme => ({
    paper: {
        padding: theme.spacing.unit,
    },
    popover: {
        pointerEvents: 'none',
    },
    popperClose: {
        pointerEvents: 'none',
    },
    button: {
        margin: theme.spacing.unit,
    },
});

class CustomRuleDetails extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handlePopoverOpen = this.handlePopoverOpen.bind(this);
        this.handlePopoverClose = this.handlePopoverClose.bind(this);
    }

    handleChange(name) {
        return (event) => {
            this.props.handleChangeChild(name, event.target.value);
        };
    }

    handlePopoverOpen(event) {
        this.props.handlePopoverOpen(event);
    }

    handlePopoverClose() {
        this.props.handlePopoverClose();
    }

    render() {
        const { classes } = this.props;
        const { anchorEl, popperOpen } = this.props.state;
        const open = !!anchorEl;
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
                        // TODO: bind popover for samples
                    >
                    </TextField>
                    <Popover
                        className={classes.popover}
                        classes={{
                            paper: classes.paper,
                        }}
                        open={open}
                        anchorEl={anchorEl}
                        anchorOrigin={{
                            vertical: 'bottom',
                            horizontal: 'left',
                        }}
                        transformOrigin={{
                            vertical: 'top',
                            horizontal: 'left',
                        }}
                        onClose={this.handlePopoverClose}
                        disableRestoreFocus
                    >
                        <Typography>$userId:$apiContext:$apiVersion</Typography>
                    </Popover>
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

export default withStyles(styles)(CustomRuleDetails);
