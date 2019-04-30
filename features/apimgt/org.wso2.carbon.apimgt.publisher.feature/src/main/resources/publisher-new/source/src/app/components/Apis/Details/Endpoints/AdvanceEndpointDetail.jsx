/**
 * Copyright (c)  WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React from 'react';
import { Component } from 'react';
import { Grid, Typography } from '@material-ui/core';
import InputLabel from '@material-ui/core/InputLabel';
import FormControl from '@material-ui/core/FormControl';
import { withStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import { FormattedMessage } from 'react-intl';

import MenuItem from '@material-ui/core/MenuItem';
import Select from '@material-ui/core/Select';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';

/**
 * API Endpoint Advance details component
 * @class AdvanceEndpointDetail
 * @extends {Component}
 */

const styles = theme => ({
    root: {
        flexGrow: 1,
    },
    container: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    margin: {
        margin: theme.spacing.unit,
    },
    paper: {
        padding: theme.spacing.unit * 2,
        textAlign: 'center',
        color: theme.palette.text.primary,
    },
    selectEmpty: {
        marginTop: theme.spacing.unit * 2,
        textAlign: 'left',
    },
    widthControl: {
        minWidth: '100%',
    },
    grid: {
        paddingLeft: '10px',
        paddingRight: '10px',
        minWidth: 0,
    },
});

/**
 *
 *
 * @class AdvanceEndpointDetail
 * @extends {Component}
 */
class AdvanceEndpointDetail extends Component {
    /**
     *Creates an instance of AdvanceEndpointDetail.
     * @param {*} props
     * @memberof AdvanceEndpointDetail
     */
    constructor(props) {
        super(props);
        this.state = {
            maxTps: this.props.endpoint.maxTps,
            type: this.props.endpoint.type,
            securityEnabled: this.props.endpointSecurity.enabled,
            securityType: 'basic',
            username: '',
            password: '',
        };

        this.handleScurityChange = this.handleScurityChange.bind(this);
        this.handleScurityTypeChange = this.handleScurityTypeChange.bind(this);
        this.handleTextFieldChange = this.handleTextFieldChange.bind(this);
        this.handleAdvanceTextFieldChange = this.handleAdvanceTextFieldChange.bind(this);
    }

    /**
     * Triggered when security type is changed. Update the state with selected value.
     *
     * @param {*} event Event that fired the function call
     * @memberof AdvanceEndpointDetail
     */
    handleScurityChange(event) {
        this.props.endpointSecurity.enabled = event.target.value;
        this.setState({ securityEnabled: event.target.value });
    }

    /**
     * Set the security related values to the state.
     *
     * @param {*} event Event that fired the function call
     * @memberof AdvanceEndpointDetail
     */
    handleScurityTypeChange(event) {
        this.props.endpointSecurity.type = event.target.value;
        this.setState({ securityType: event.target.value });
    }

    /**
     * Set the advance configuration related values to the state.
     *
     * @param {*} event Event that fired the function call
     * @memberof AdvanceEndpointDetail
     */
    handleAdvanceTextFieldChange(event) {
        this.props.endpoint[event.target.id] = event.currentTarget.value;
        this.setState({ [event.target.id]: event.currentTarget.value });
    }


    /**
     * Set the text field value to the state.
     *
     * @param {*} event Event that fired the function call
     * @memberof AdvanceEndpointDetail
     */
    handleTextFieldChange(event) {
        const inputField = event.target.id;
        const value = event.target.value;
        this.props.endpointSecurity[inputField] = value;
        this.setState({ inputField: value });
    }

    /**
     *
     *
     * @returns {React.Component} HTML content
     * @memberof AdvanceEndpointDetail
     */
    render() {
        const { classes } = this.props;

        return (
            <Grid container>
                <Grid item xs={12}>
                    <ExpansionPanel>
                        <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                            <Typography className={classes.heading}>
                                <FormattedMessage
                                    id='endpoint.advance.configuration'
                                    defaultMessage='Endpoint Advance Configuration'
                                />
                            </Typography>
                        </ExpansionPanelSummary>
                        <ExpansionPanelDetails>
                            <Grid item xs={12} className={classes.grid}>
                                <TextField
                                    id='maxTps'
                                    label={<FormattedMessage id='max.tps' defaultMessage='Max TPS' />}
                                    value={this.props.endpoint.maxTps}
                                    fullWidth
                                    margin='normal'
                                    onChange={this.handleAdvanceTextFieldChange}
                                    disabled={this.props.readOnly || (!this.props.readOnly && !this.props.isInline)}
                                />
                            </Grid>
                            <Grid item xs={12} className={classes.grid}>
                                <TextField
                                    id='type'
                                    label={<FormattedMessage id='endpoint.type' defaultMessage='Endpoint Type' />}
                                    value={this.props.endpoint.type}
                                    fullWidth
                                    margin='normal'
                                    onChange={this.handleAdvanceTextFieldChange}
                                    disabled={this.props.readOnly || (!this.props.readOnly && !this.props.isInline)}
                                />
                            </Grid>
                        </ExpansionPanelDetails>
                    </ExpansionPanel>
                </Grid>
                <Grid item xs={12}>
                    <ExpansionPanel>
                        <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                            <Typography className={classes.heading}>
                                <FormattedMessage
                                    id='security'
                                    defaultMessage='Security'
                                />
                            </Typography>
                        </ExpansionPanelSummary>
                        <ExpansionPanelDetails>
                            <Grid container className={classes.grid}>
                                <FormControl
                                    variant='subheading'
                                    disabled={this.props.readOnly || (!this.props.readOnly && !this.props.isInline)}
                                    className={classes.widthControl}
                                >
                                    <InputLabel htmlFor='security'>
                                        <FormattedMessage
                                            id='endpoint.security.scheme'
                                            defaultMessage='Endpoint Security Scheme'
                                        />
                                    </InputLabel>
                                    <Select
                                        value={this.props.endpointSecurity.enabled}
                                        onChange={this.handleScurityChange}
                                        fullwidth
                                        inputProps={{
                                            name: 'security',
                                            id: 'security',
                                        }}
                                    >
                                        <MenuItem value={false}>Unsecured</MenuItem>
                                        <MenuItem value>Secured</MenuItem>
                                    </Select>
                                </FormControl>
                            </Grid>
                            {this.props.endpointSecurity.enabled && (
                                <Grid container className={classes.grid}>
                                    <FormControl
                                        className={classes.widthControl}
                                        disabled={this.props.readOnly || (!this.props.readOnly && !this.props.isInline)}
                                    >
                                        <InputLabel htmlFor='securityType'>
                                            <FormattedMessage
                                                id='endpoint.auth.type'
                                                defaultMessage='Endpoint Auth Type'
                                            />
                                        </InputLabel>
                                        <Select
                                            value={this.props.endpointSecurity.type}
                                            onChange={this.handleScurityTypeChange}
                                            inputProps={{
                                                name: 'securityType',
                                                id: 'securityType',
                                            }}
                                        >
                                            <MenuItem value='basic'>Basic Auth</MenuItem>
                                            <MenuItem value='digest'>Digest Auth</MenuItem>
                                        </Select>
                                    </FormControl>
                                    <Grid container >
                                        <TextField
                                            id='username'
                                            label={<FormattedMessage id='username' defaultMessage='Username' />}
                                            value={this.props.endpointSecurity.username}
                                            margin='normal'
                                            onChange={this.handleTextFieldChange}
                                            disabled={
                                                this.props.readOnly || (!this.props.readOnly && !this.props.isInline)
                                            }
                                            className={classes.widthControl}
                                        />
                                        <TextField
                                            id='password'
                                            label={<FormattedMessage id='password' defaultMessage='Password' />}
                                            value={this.props.endpointSecurity.password}
                                            margin='normal'
                                            onChange={this.handleTextFieldChange}
                                            disabled={
                                                this.props.readOnly || (!this.props.readOnly && !this.props.isInline)
                                            }
                                            className={classes.widthControl}
                                        />
                                    </Grid>
                                </Grid>
                            )}
                        </ExpansionPanelDetails>
                    </ExpansionPanel>
                </Grid>
            </Grid>
        );
    }
}

export default withStyles(styles)(AdvanceEndpointDetail);
