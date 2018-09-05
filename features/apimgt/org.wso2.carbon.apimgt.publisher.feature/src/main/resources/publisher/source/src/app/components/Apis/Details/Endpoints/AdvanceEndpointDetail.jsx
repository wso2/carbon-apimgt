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
import Api from '../../../../data/api';
import { Grid, Paper, Typography, Divider } from '@material-ui/core';
import EndpointsSelector from './EndpointsSelector';
import PropTypes from 'prop-types';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import FormHelperText from '@material-ui/core/FormHelperText';
import FormControl from '@material-ui/core/FormControl';
import { withStyles, MuiThemeProvider, createMuiTheme } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import { FormattedMessage } from 'react-intl';

import FormControlLabel from '@material-ui/core/FormControlLabel';
import MenuItem from '@material-ui/core/MenuItem';
import Select from '@material-ui/core/Select';
import IconButton from '@material-ui/core/IconButton';
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
    textField: {
        marginLeft: theme.spacing.unit,
        marginRight: theme.spacing.unit,

    },
    selectEmpty: {
        marginTop: theme.spacing.unit * 2,
        textAlign: 'left',
    },
});

class AdvanceEndpointDetail extends Component {

    constructor(props) {
        super(props);
        this.state = {
            securityEnabled: this.props.endpointSecurity.enabled,
            securityType: 'basic',
            username: '',
            password: '',
        }

        this.handleScurityChange = this.handleScurityChange.bind(this);
        this.handleScurityTypeChange = this.handleScurityTypeChange.bind(this);
        this.handleTextFieldChange = this.handleTextFieldChange.bind(this);
    }

    handleScurityChange(event) {
        this.props.endpointSecurity.enabled = event.target.value;
        this.setState({ securityEnabled: event.target.value })
    }

    handleScurityTypeChange(event) {
        this.props.endpointSecurity.type = event.target.value;
        this.setState({ securityType: event.target.value })
    }

    handleTextFieldChange(event) {
        const inputField = event.target.id;
        const value = event.target.value;
        this.props.endpointSecurity[inputField] = value;
        this.setState({inputField : value})
    }

    render() {
        const { classes } = this.props;

        return (
            <ExpansionPanel>
                <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography className={classes.heading}>Security</Typography>
                </ExpansionPanelSummary>
                <ExpansionPanelDetails>
                    <Grid container>
                        <FormControl variant="subheading" disabled={this.props.readOnly || (!this.props.readOnly && !this.props.isInline)}>
                            <InputLabel htmlFor="security"><FormattedMessage id="endpoint.security.scheme" defaultMessage="Endpoint Security Scheme" /></InputLabel>
                            <Select
                                value={this.props.endpointSecurity.enabled}
                                onChange={this.handleScurityChange}
                                fullwidth
                                inputProps={{
                                    name: 'security',
                                    id: 'security',
                                }}
                            >

                                <MenuItem value={false}>Un secured</MenuItem>
                                <MenuItem value={true}>Secured</MenuItem>
                            </Select>
                        </FormControl>
                    </Grid>
                    {this.state.securityEnabled &&
                        <Grid container>
                            <FormControl className={classes.formControl} disabled={this.props.readOnly || (!this.props.readOnly && !this.props.isInline)}>
                                <InputLabel htmlFor="securityType"><FormattedMessage id="endpoint.auth.type" defaultMessage="Endpoint Auth Type" /></InputLabel>
                                <Select
                                    value={this.props.endpointSecurity.type}
                                    onChange={this.handleScurityTypeChange}
                                    inputProps={{
                                        name: 'securityType',
                                        id: 'securityType',
                                    }}
                                >
                                    <MenuItem value="basic">Basic Auth</MenuItem>
                                    <MenuItem value="digest">Digest Auth</MenuItem>
                                </Select>
                            </FormControl>
                            <Grid container>
                                <TextField
                                    id="username"
                                    label={<FormattedMessage id="username" defaultMessage="Username" />}
                                    className={classes.textField}
                                    value={this.props.endpointSecurity.username}
                                    margin="normal"
                                    onChange={this.handleTextFieldChange}
                                    disabled={this.props.readOnly || (!this.props.readOnly && !this.props.isInline)}
                                />
                                <TextField
                                    id="password"
                                    label={<FormattedMessage id="password" defaultMessage="Password" />}
                                    className={classes.textField}
                                    value={this.props.endpointSecurity.password}
                                    margin="normal"
                                    onChange={this.handleTextFieldChange}
                                    disabled={this.props.readOnly || (!this.props.readOnly && !this.props.isInline)}
                                />
                            </Grid>
                        </Grid>
                    }
                </ExpansionPanelDetails>
            </ExpansionPanel>
        )
    }
}

export default withStyles(styles)(AdvanceEndpointDetail);
