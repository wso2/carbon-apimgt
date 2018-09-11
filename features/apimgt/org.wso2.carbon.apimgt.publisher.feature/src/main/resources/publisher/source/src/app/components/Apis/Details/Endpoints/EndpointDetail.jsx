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

import React, { Component } from 'react';
import Api from 'AppData/api';
import { Grid, Paper, Typography, Divider } from '@material-ui/core';
import PropTypes from 'prop-types';
import InputLabel from '@material-ui/core/InputLabel';
import { withStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import { FormattedMessage } from 'react-intl';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import MenuItem from '@material-ui/core/MenuItem';
import Select from '@material-ui/core/Select';
import IconButton from '@material-ui/core/IconButton';
import AddIcon from '@material-ui/icons/Add';
import RemoveIcon from '@material-ui/icons/RemoveRounded';

import EndpointForm from './EndpointForm.jsx';
import AdvanceEndpointDetail from './AdvanceEndpointDetail.jsx';

/**
 * API Details Endpoint page component
 * @class EndpointDetail
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
    button: {
        margin: theme.spacing.unit,
    },
    endpointButtons: {
        margin: theme.spacing.unit,
        maxHeight: '25px',
        backgroundColor: theme.palette.grey[300],
    },
    group: {
        margin: `${theme.spacing.unit}px 0`,
        flexDirection: 'row',
        justifyContent: 'space-around',
    },
    selectEmpty: {
        marginTop: theme.spacing.unit * 2,
        textAlign: 'left',
    },
});
class EndpointDetail extends Component {
    constructor(props) {
        super(props);
        const epTypeNumber =
            this.props.endpoint.endpointConfig.endpointType === 'LOAD_BALANCED'
                ? '1'
                : this.props.endpoint.endpointConfig.endpointType === 'FAIL_OVER'
                    ? '2'
                    : '0';
        this.state = {
            isInline: this.props.isInline,
            selectedEndpointConfig: this.props.endpoint.endpointConfig.list[0],
            maxTps: this.props.endpoint.maxTps,
            type: this.props.endpoint.type,
            readOnly: this.props.readOnly,
            globalEndpoints: null,
            additonalEndpoints: false,
            endpointType: epTypeNumber,
            extraEndpoint: '',
        };
        this.handleEpClick = this.handleEpClick.bind(this);
        this.handleTextFieldChange = this.handleTextFieldChange.bind(this);
        this.handleRadioButtonChange = this.handleRadioButtonChange.bind(this);
        this.handleSelectChange = this.handleSelectChange.bind(this);
        this.getGlobalEndpoints = this.getGlobalEndpoints.bind(this);
        this.getSelectedEndpoint = this.getSelectedEndpoint.bind(this);
        this.handleAddAdditionalEndpoints = this.handleAddAdditionalEndpoints.bind(this);
        this.handleEPTextField = this.handleEPTextField.bind(this);
        this.setAdditionalEndpoint = this.setAdditionalEndpoint.bind(this);
        this.handleRemoveEndpointURL = this.handleRemoveEndpointURL.bind(this);
        this.handleEpTypeRadioButtonChange = this.handleEpTypeRadioButtonChange.bind(this);
    }

    handleEpClick(e) {
        this.setState({
            selectedEndpointConfig: this.props.endpoint.endpointConfig.list[e.currentTarget.getAttribute('name')],
        });
    }

    handleTextFieldChange(event) {
        this.props.endpoint[event.target.id] = event.currentTarget.value;
        this.setState({ [event.target.id]: event.currentTarget.value });
    }

    handleRadioButtonChange(event) {
        const selectedVal = event.target.value === 'inline';
        this.props.endpoint.inline = selectedVal;
        this.setState({ isInline: selectedVal });
        if (!selectedVal) {
            this.getGlobalEndpoints();
        }
    }

    handleEpTypeRadioButtonChange(event) {
        const value = event.target.value;
        this.setState({ endpointType: value });
        if (value === '1') {
            // load balanced type
            this.props.endpoint.endpointConfig.endpointType = 'LOAD_BALANCED';
        } else if (value === '2') {
            // fail over type
            this.props.endpoint.endpointConfig.endpointType = 'FAIL_OVER';
        } else {
            this.props.endpoint.endpointConfig.endpointType = 'SINGLE';
        }
    }

    handleChange(event) {
        this.setState({ [event.target.name]: event.target.value });
    }

    handleSelectChange(event) {
        this.getSelectedEndpoint(event.target.value);
    }

    handleAddAdditionalEndpoints(event) {
        this.setState({ additonalEndpoints: true, endpointType: event.target.id });
    }

    handleEPTextField(event) {
        this.setState({ extraEndpoint: event.target.value });
    }

    setAdditionalEndpoint(event) {
        const selectedEndpointConfig = { url: this.state.extraEndpoint };
        if (this.state.endpointType === '1') {
            // load balanced type
            this.props.endpoint.endpointConfig.endpointType = 'LOAD_BALANCED';
        } else if (this.state.endpointType === '2') {
            // fail over type
            this.props.endpoint.endpointConfig.endpointType = 'FAIL_OVER';
        } else {
            this.props.endpoint.endpointConfig.endpointType = 'SINGLE';
        }
        this.props.endpoint.endpointConfig.list.push(selectedEndpointConfig);
        this.setState({ selectedEndpointConfig, additonalEndpoints: false, extraEndpoint: '' });
    }

    handleRemoveEndpointURL(event) {
        this.props.endpoint.endpointConfig.list.splice(event.target.id, 1);
        if (this.props.endpoint.endpointConfig.list.length < 2) {
            this.props.endpoint.endpointConfig.endpointType = 'SINGLE';
            this.setState({ selectedEndpointConfig: this.props.endpoint.endpointConfig.list[0], endpointType: '0' });
        } else {
            this.setState({ selectedEndpointConfig: this.props.endpoint.endpointConfig.list[0] });
        }
    }

    getGlobalEndpoints() {
        const api = new Api();
        const promisedEndpoints = api.getEndpoints();
        promisedEndpoints.then((response) => {
            this.setState({ globalEndpoints: response.body.list });
        });
    }

    getSelectedEndpoint(id) {
        const api = new Api();
        const promisedEndpoint = api.getEndpoint(id);
        promisedEndpoint.then((response) => {
            const ep = response.body;
            this.props.endpoint.endpointConfig = ep.endpointConfig;
            this.props.endpoint.id = ep.id;
            this.props.endpoint.type = ep.type;
            this.props.endpoint.inline = false;
            this.setState({
                selectedEndpointConfig: ep.endpointConfig.list[0],
                maxTps: ep.maxTps,
                type: ep.type,
                readOnly: false,
            });
        });
    }

    render() {
        const { classes } = this.props;
        const flexContainer = {
            display: 'flex',
            flexDirection: 'row',
        };
        const epUrls = [];
        const globalEndpointMenu = [];
        const numOfEndpoints = this.props.endpoint.endpointConfig.list.length;
        for (name in this.props.endpoint.endpointConfig.list) {
            epUrls.push(<Button variant='outlined' className={classes.endpointButtons} onClick={this.handleEpClick} name={name}>
                {this.props.endpoint.endpointConfig.list[name].url}
                        </Button>);
            if (name > 0 && (!this.props.readOnly && this.state.isInline)) {
                epUrls.push(<IconButton
                    id={name}
                    className={classes.button}
                    aria-label='Remove'
                    onClick={this.handleRemoveEndpointURL}
                >
                    <RemoveIcon id={name} />
                </IconButton>);
            }
        }
        for (const globalEp in this.state.globalEndpoints) {
            const ep = this.state.globalEndpoints[globalEp];
            globalEndpointMenu.push(<MenuItem value={ep.id}>{ep.name}</MenuItem>);
        }
        return (
            <div className={classes.root}>
                <Grid container spacing={8} justify='center'>
                    <Grid item md={12}>
                        <Typography variant='title' gutterBottom>
                            {this.props.type} <FormattedMessage id='endpoint' defaultMessage='Endpoint' />
                        </Typography>
                    </Grid>
                    <Grid item xs={6}>
                        <Paper className={classes.paper}>
                            <div className={classes.container}>
                                <Grid container>
                                    <Grid item xs={6}>
                                        <RadioGroup
                                            aria-label='Endpoint type'
                                            name='epType'
                                            id='epType'
                                            className={classes.group}
                                            value={this.state.isInline ? 'inline' : 'global'}
                                            onChange={this.handleRadioButtonChange}
                                            disabled
                                        >
                                            <FormControlLabel
                                                value='inline'
                                                control={<Radio />}
                                                label='Inline'
                                                disabled={this.props.readOnly}
                                            />
                                            <FormControlLabel
                                                value='global'
                                                control={<Radio />}
                                                label='Global'
                                                disabled={this.props.readOnly}
                                            />
                                        </RadioGroup>
                                    </Grid>
                                    {!this.props.readOnly &&
                                        !this.state.isInline && (
                                        <Grid item xs={6}>
                                            <InputLabel htmlFor='age-simple'>
                                                <FormattedMessage
                                                    id='select.a.global.endpoint'
                                                    defaultMessage='Select a Global Endpoint'
                                                />
                                            </InputLabel>
                                            <Select
                                                onChange={this.handleSelectChange}
                                                className={classes.selectEmpty}
                                                inputProps={{
                                                    name: 'globalEndpointList',
                                                    id: 'globalEndpointList',
                                                }}
                                            >
                                                {globalEndpointMenu}
                                            </Select>
                                        </Grid>
                                    )}
                                </Grid>
                                <Divider />
                                <Grid container>
                                    {numOfEndpoints > 1 &&
                                        this.state.endpointType === '1' && (
                                        <Grid container>
                                            <Typography variant='subheading' gutterBottom align='right'>
                                                <FormattedMessage
                                                    id='load.balanced.endpints'
                                                    defaultMessage='Load balanced endpints'
                                                />
                                            </Typography>
                                        </Grid>
                                    )}
                                    {numOfEndpoints > 1 &&
                                        this.state.endpointType === '2' && (
                                        <Grid container>
                                            <Typography variant='subheading' gutterBottom align='right'>
                                                <FormattedMessage
                                                    id='fail.over.endpints'
                                                    defaultMessage='Fail over endpints'
                                                />
                                            </Typography>
                                        </Grid>
                                    )}
                                    {epUrls}
                                </Grid>
                                {!this.props.readOnly &&
                                    this.state.isInline && (
                                    <Grid container>
                                        <RadioGroup
                                            aria-label='Endpoint type'
                                            name='endpointpType'
                                            id='endpointpType'
                                            className={classes.group}
                                            value={this.state.endpointType}
                                            onChange={this.handleEpTypeRadioButtonChange}
                                            disabled
                                        >
                                            <FormControlLabel
                                                value='1'
                                                control={<Radio />}
                                                label='Load Balance'
                                                disabled={this.props.readOnly}
                                            />
                                            <FormControlLabel
                                                value='2'
                                                control={<Radio />}
                                                label='Fail Over'
                                                disabled={this.props.readOnly}
                                            />
                                        </RadioGroup>
                                        {this.state.endpointType === '1' && (
                                            <Typography variant='body2' gutterBottom align='right'>
                                                <FormattedMessage
                                                    id='add.another.load.balanced.endpoint'
                                                    defaultMessage='Add another load balanced endpoint'
                                                />
                                                <IconButton
                                                    id={name}
                                                    className={classes.button}
                                                    aria-label='Add'
                                                    onClick={this.handleAddAdditionalEndpoints}
                                                >
                                                    <AddIcon id='1' />
                                                </IconButton>
                                            </Typography>
                                        )}
                                        {this.state.endpointType === '2' && (
                                            <Typography variant='body2' gutterBottom align='right'>
                                                <FormattedMessage
                                                    id='add.another.fail.over.endpoint'
                                                    defaultMessage='Add another fail over endpoint'
                                                />
                                                <IconButton
                                                    id={name}
                                                    className={classes.button}
                                                    aria-label='Add'
                                                    onClick={this.handleAddAdditionalEndpoints}
                                                >
                                                    <AddIcon id='2' />
                                                </IconButton>
                                            </Typography>
                                        )}
                                    </Grid>
                                )}
                                {!this.props.readOnly &&
                                    this.state.isInline &&
                                    this.state.additonalEndpoints && (
                                    <Grid container>
                                        <TextField
                                            id='extraEndpoint'
                                            label={
                                                <FormattedMessage id='service.url' defaultMessage='Service URL' />
                                            }
                                            className={classes.textField}
                                            value={this.state.extraEndpoint}
                                            margin='normal'
                                            onChange={this.handleEPTextField}
                                            disabled={
                                                this.props.readOnly ||
                                                    (!this.props.readOnly && !this.state.isInline)
                                            }
                                        />
                                        <Button
                                            mini
                                            variant='fab'
                                            color='primary'
                                            aria-label='Add'
                                            className={classes.button}
                                            onClick={this.setAdditionalEndpoint}
                                        >
                                            <AddIcon />
                                        </Button>
                                    </Grid>
                                )}
                            </div>
                        </Paper>
                        <Paper className={classes.paper}>
                            <Grid item xs={6}>
                                <TextField
                                    id='maxTps'
                                    label={<FormattedMessage id='max.tps' defaultMessage='Max TPS' />}
                                    className={classes.textField}
                                    value={this.state.maxTps}
                                    fullWidth
                                    margin='normal'
                                    onChange={this.handleTextFieldChange}
                                    disabled={this.props.readOnly || (!this.props.readOnly && !this.state.isInline)}
                                />
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    id='type'
                                    label={<FormattedMessage id='endpoint.type' defaultMessage='Endpoint Type' />}
                                    className={classes.textField}
                                    value={this.state.type}
                                    fullWidth
                                    margin='normal'
                                    onChange={this.handleTextFieldChange}
                                    disabled={this.props.readOnly || (!this.props.readOnly && !this.state.isInline)}
                                />
                            </Grid>
                        </Paper>
                        <AdvanceEndpointDetail
                            endpointSecurity={this.props.endpoint.endpointSecurity}
                            readOnly={this.props.readOnly}
                            isInline={this.state.isInline}
                        />
                    </Grid>
                    <EndpointForm
                        selectedEndpointConfig={this.state.selectedEndpointConfig}
                        endpoint={this.props.endpoint}
                        isInline={this.state.isInline}
                        readOnly={this.props.readOnly}
                    />
                </Grid>
            </div>
        );
    }
}
EndpointDetail.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(EndpointDetail);
