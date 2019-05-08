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
import Api from 'AppData/api';
import { Grid, Paper, Typography, Divider } from '@material-ui/core';
import PropTypes from 'prop-types';
import InputLabel from '@material-ui/core/InputLabel';
import { withStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import { FormattedMessage } from 'react-intl';
import EndpointForm from './EndpointForm.jsx';
import AdvanceEndpointDetail from './AdvanceEndpointDetail.jsx';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import MenuItem from '@material-ui/core/MenuItem';
import Select from '@material-ui/core/Select';
import IconButton from '@material-ui/core/IconButton';
import AddIcon from '@material-ui/icons/Add';
import DeleteIcon from '@material-ui/icons/Delete';
import SettingsIcon from '@material-ui/icons/Settings';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import LaunchIcon from '@material-ui/icons/Launch';
import { Link } from 'react-router-dom';
import Grow from '@material-ui/core/Grow';
import Slide from '@material-ui/core/Slide';

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
        color: theme.palette.text.primary,
    },
    textField: {
        minWidth: '75%',
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
    },
    selectEmpty: {
        marginTop: theme.spacing.unit * 2,
        textAlign: 'left',
    },
    bootstrapRoot: {
        padding: 0,
        'label + &': {
            marginTop: theme.spacing.unit * 3,
        },
        flexDirection: 'row',
    },
    bootstrapInput: {
        borderRadius: 4,
        backgroundColor: theme.palette.common.white,
        border: '1px solid #ced4da',
        fontSize: 16,
        padding: '10px 12px',
        width: 'calc(100% - 24px)',
        flexDirection: 'row',
        transition: theme.transitions.create(['border-color', 'box-shadow']),
        fontFamily: ['-apple-system', 'BlinkMacSystemFont', '"Segoe UI"', 'Roboto', '"Helvetica Neue"', 'Arial', 'sans-serif', '"Apple Color Emoji"', '"Segoe UI Emoji"', '"Segoe UI Symbol"'].join(','),
        '&:focus': {
            borderColor: '#80bdff',
            boxShadow: '0 0 0 0.2rem rgba(0,123,255,.25)',
            margin: `${theme.spacing.unit}px 0`,
            flexDirection: 'row',
        },
    },
    bootstrapFormLabel: {
        fontSize: 18,
    },
    contentWrapper: {
        flexDirection: 'column',
    },
});

/**
 *
 *
 * @class EndpointDetail
 * @extends {Component}
 */
class EndpointDetail extends Component {
    /**
     *Creates an instance of EndpointDetail.
     * @param {*} props properies passed by the parent element
     * @memberof EndpointDetail
     */
    constructor(props) {
        super(props);
        const epTypeNumber = this.props.endpoint.endpointConfig.endpointType === 'LOAD_BALANCED' ? '1' : this.props.endpoint.endpointConfig.endpointType === 'FAIL_OVER' ? '2' : '1';
        const urlList = [];
        urlList.push(this.props.endpoint.endpointConfig.list[0].url);
        this.state = {
            isInline: this.props.isInline,
            selectedEndpointConfig: this.props.endpoint.endpointConfig.list[0],
            selectedEndpointIndex: '0',
            selectedGlobalEndpoint: this.props.isInline ? null : this.props.endpoint,
            backUpEndpointValue: this.props.initialValue,
            urlList,
            readOnly: this.props.readOnly,
            globalEndpoints: null,
            additonalEndpoints: false,
            endpointType: epTypeNumber,
            extraEndpoint: '',
            showEpConfig: false,
            showEpConfigSlide: false,
        };
        this.handleEpClick = this.handleEpClick.bind(this);
        this.handleRadioButtonChange = this.handleRadioButtonChange.bind(this);
        this.handleSelectChange = this.handleSelectChange.bind(this);
        this.getGlobalEndpoints = this.getGlobalEndpoints.bind(this);
        this.getSelectedEndpoint = this.getSelectedEndpoint.bind(this);
        this.handleAddAdditionalEndpoints = this.handleAddAdditionalEndpoints.bind(this);
        this.handleEPTextField = this.handleEPTextField.bind(this);
        this.setAdditionalEndpoint = this.setAdditionalEndpoint.bind(this);
        this.handleRemoveEndpointURL = this.handleRemoveEndpointURL.bind(this);
        this.handleEpTypeRadioButtonChange = this.handleEpTypeRadioButtonChange.bind(this);
        this.handleUrlEdit = this.handleUrlEdit.bind(this);
        this.retrieveFromBackUpValue = this.retrieveFromBackUpValue.bind(this);
    }

    /**
     *
     *
     * @memberof EndpointDetail
     */
    componentDidMount() {
        if (!this.props.isInline) {
            const api = new Api();
            const promisedEndpoints = api.getEndpoints();
            promisedEndpoints.then((response) => {
                if (response.body.list && response.body.list.length > 0) {
                    this.state.globalEndpoints = response.body.list;
                } else {
                    this.state.globalEndpoints = null;
                }
            });
        }
    }

    handleEpClick(e) {
        const growValue = this.state.showEpConfig;
        const slidevalue = this.state.showEpConfigSlide;
        if ((growValue || slidevalue) && e.currentTarget.id === this.state.selectedEndpointIndex) {
            this.setState({
                selectedEndpointConfig: this.props.endpoint.endpointConfig.list[e.currentTarget.id],
                showEpConfig: false,
                showEpConfigSlide: false,
            });
        } else if (growValue !== slidevalue) {
            this.setState({
                selectedEndpointConfig: this.props.endpoint.endpointConfig.list[e.currentTarget.id],
                selectedEndpointIndex: e.currentTarget.id,
                showEpConfig: !growValue,
                showEpConfigSlide: !slidevalue,
            });
        } else {
            this.setState({
                selectedEndpointConfig: this.props.endpoint.endpointConfig.list[e.currentTarget.id],
                selectedEndpointIndex: e.currentTarget.id,
                showEpConfig: true,
                showEpConfigSlide: false,
            });
        }
    }

    /**
     *  Set the edited URL value to the state
     *
     * @param {*} e Event that fired the function call
     * @memberof EndpointDetail
     */
    handleUrlEdit(e) {
        const id = e.target.id;
        this.props.endpoint.endpointConfig.list[id].url = e.target.value;
        const urls = this.state.urlList;
        urls[id] = e.target.value;
        this.setState({ urlList: urls });
    }

    /**
     * Handles the endpoint type(inline. global) radio button event change
     *
     * @param {*} event Event that fired the function call
     * @memberof EndpointDetail
     */
    handleRadioButtonChange(event) {
        const selectedVal = event.target.value === 'inline';
        this.props.endpoint.inline = selectedVal;
        if (!selectedVal) {
            this.getGlobalEndpoints();
            this.setState({ isInline: selectedVal });
        } else {
            this.retrieveFromBackUpValue();
            this.setState({
                isInline: selectedVal,
                selectedEndpointConfig: this.props.endpoint.endpointConfig.list[0],
                selectedGlobalEndpoint: null,
            });
        }
    }

    /**
     * Handles the endpoint type(fail over, load balance) radio button event change
     *
     * @param {*} event Event that fired the function call
     * @memberof EndpointDetail
     */
    handleEpTypeRadioButtonChange(event) {
        const value = event.target.value;
        if (this.state.endpointType === value) {
            this.props.endpoint.endpointConfig.endpointType = 'SINGLE';
            this.setState({ endpointType: '3' });
            return;
        }
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

    /**
     * Set the added endpoint to the state
     *
     * @param {*} event Event that fired the function call
     * @memberof EndpointDetail
     */
    handleAddAdditionalEndpoints(event) {
        this.props.endpoint.endpointConfig.list.push({ url: '' });
        this.setState({ additonalEndpoints: true });
    }

    /**
     * Set the state value from the text field.
     *
     * @param {*} event Event that fired the function call
     * @memberof EndpointDetail
     */
    handleEPTextField(event) {
        this.setState({ extraEndpoint: event.target.value });
    }

    /**
     * Add additional fail over or load balnced endpoints.
     *
     * @param {*} event Event that fired the function call
     * @memberof EndpointDetail
     */
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
        this.setState({
            selectedEndpointConfig,
            additonalEndpoints: false,
            extraEndpoint: '',
        });
    }

    /**
     * Removes the failover or load balance endpoint urls
     *
     * @param {*} event Event that fired the function call.
     * @memberof EndpointDetail
     */
    handleRemoveEndpointURL(event) {
        this.props.endpoint.endpointConfig.list.splice(event.currentTarget.id, 1);
        if (this.props.endpoint.endpointConfig.list.length < 2) {
            this.props.endpoint.endpointConfig.endpointType = 'SINGLE';
            this.setState({
                selectedEndpointConfig: this.props.endpoint.endpointConfig.list[0],
            });
        } else {
            this.setState({
                selectedEndpointConfig: this.props.endpoint.endpointConfig.list[0],
            });
        }
    }

    /**
     * Get the list of all global endpoints defined.
     *
     * @memberof EndpointDetail
     */
    getGlobalEndpoints() {
        const api = new Api();
        const promisedEndpoints = api.getEndpoints();
        promisedEndpoints.then((response) => {
            if (response.body.list && response.body.list.length > 0) {
                this.setState({ globalEndpoints: response.body.list });
            } else {
                this.setState({ globalEndpoints: null });
            }
        });
    }

    /**
     * Get the detals of the selected global endpoint and set it to the state.
     *
     * @param {*} id Id of the endpoint
     * @memberof EndpointDetail
     */
    getSelectedEndpoint(id) {
        const api = new Api();
        const promisedEndpoint = api.getEndpoint(id);
        promisedEndpoint.then((response) => {
            const ep = response.body;
            this.props.endpoint.endpointConfig = ep.endpointConfig;
            this.props.endpoint.id = ep.id;
            this.props.endpoint.type = ep.type;
            this.props.endpoint.inline = false;
            this.props.endpoint.maxTps = ep.maxTps;
            this.props.endpoint.endpointSecurity = ep.endpointSecurity;
            this.setState({
                selectedEndpointConfig: ep.endpointConfig.list[0],
                selectedGlobalEndpoint: ep,
                maxTps: ep.maxTps,
                type: ep.type,
                readOnly: false,
            });
        });
    }

    /**
     * Changes to back up value when switched from inline to golbal and vice versa.
     *
     * @memberof EndpointDetail
     */
    retrieveFromBackUpValue() {
        this.props.endpoint.endpointConfig = this.props.initialValue.endpointConfig;
        this.props.endpoint.endpointSecurity = this.props.initialValue.endpointSecurity;
        this.props.endpoint.maxTps = this.props.initialValue.maxTps;
        this.props.endpoint.type = this.props.initialValue.type;
    }

    /**
     *
     *  Render method of the component
     * @returns {React.Component} endpoint detail html component
     * @memberof EndpointDetail
     */
    render() {
        const { classes } = this.props;
        const flexContainer = {
            display: 'flex',
            flexDirection: 'row',
        };
        const epUrls = [];
        const globalEndpointMenu = [];
        const numOfEndpoints = this.props.endpoint.endpointConfig.list.length;
        if (!this.state.isInline && !this.state.globalEndpoints && !this.state.selectedGlobalEndpoint) {
            return (
                <div className={classes.root}>
                    <Grid container>
                        <Grid item xs={12} className={classes.contentWrapper}>
                            <Typography variant='title' gutterBottom>
                                {this.props.type} <FormattedMessage id='endpoint' defaultMessage='Endpoint' />
                            </Typography>
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
                                    </Grid>
                                </div>
                                {!this.props.readOnly && (
                                    <Grid container>
                                        <Card className={classes.card}>
                                            <CardContent>
                                                <Typography gutterBottom noWrap>
                                                    Global Endpoints are not defined.
                                                </Typography>
                                                <Button
                                                    component='a'
                                                    target='_blank'
                                                    href='/publisher-new/endpoints'
                                                    size='small'
                                                    className={classes.viewInStoreLauncher}>
                                                    <LaunchIcon />
                                                    <FormattedMessage
                                                        id='define.global.endpoint'
                                                        defaultMessage='Define Global Endpoint'
                                                    />
                                                </Button>
                                            </CardContent>
                                        </Card>
                                    </Grid>
                                )}
                            </Paper>
                        </Grid>
                    </Grid>
                </div>
            );
        }
        if (!(!this.state.isInline && !this.state.selectedGlobalEndpoint)) {
            for (name in this.props.endpoint.endpointConfig.list) {
                let epUrlContent;
                if (name > 0) {
                    epUrlContent = (
                        <Grid container>
                            <TextField
                                value={this.props.endpoint.endpointConfig.list[name].url}
                                id={name}
                                InputProps={{
                                    disableUnderline: true,
                                    classes: {
                                        root: classes.bootstrapRoot,
                                        input: classes.bootstrapInput,
                                    },
                                }}
                                InputLabelProps={{
                                    shrink: true,
                                    className: classes.bootstrapFormLabel,
                                }}
                                onChange={this.handleUrlEdit}
                                disabled={this.props.readOnly || (!this.props.readOnly && !this.state.isInline)}
                                className={classes.textField}
                            />
                            <IconButton id={name} className={classes.button} aria-label='Settings' onClick={this.handleEpClick}>
                                <SettingsIcon />
                            </IconButton>
                            <IconButton id={name} className={classes.button} aria-label='Remove' onClick={this.handleRemoveEndpointURL} disabled={this.props.readOnly}>
                                <DeleteIcon />
                            </IconButton>
                        </Grid>
                    );
                } else {
                    epUrlContent = (
                        <Grid container>
                            <TextField
                                value={this.props.endpoint.endpointConfig.list[name].url}
                                id={name}
                                InputProps={{
                                    disableUnderline: true,
                                    classes: {
                                        root: classes.bootstrapRoot,
                                        input: classes.bootstrapInput,
                                    },
                                }}
                                InputLabelProps={{
                                    shrink: true,
                                    className: classes.bootstrapFormLabel,
                                }}
                                onChange={this.handleUrlEdit}
                                disabled={this.props.readOnly || (!this.props.readOnly && !this.state.isInline)}
                                className={classes.textField}
                            />
                            <IconButton id={name} className={classes.button} aria-label='Settings' onClick={this.handleEpClick}>
                                <SettingsIcon id='1' />
                            </IconButton>
                        </Grid>
                    );
                }
                epUrls.push(epUrlContent);
            }
        }

        for (const globalEp in this.state.globalEndpoints) {
            const ep = this.state.globalEndpoints[globalEp];
            globalEndpointMenu.push(<MenuItem value={ep.id}>{ep.name}</MenuItem>);
        }
        return (
            <div className={classes.root}>
                <Grid container spacing={8} justify='center'>
                    <Grid item md={6}>
                        <Typography variant='title' gutterBottom>
                            {this.props.type} <FormattedMessage id='endpoint' defaultMessage='Endpoint' />
                        </Typography>

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
                                </Grid>
                                {!this.props.readOnly && (
                                    <Typography variant='caption' gutterBottom align='center'>
                                        <FormattedMessage
                                            id='you.can.define.endpoint.inline.or.pick.from.a.list.of.global.endpoints'
                                            defaultMessage='You can define endpoint inline or pick from a list of global endpoints'
                                        />
                                    </Typography>
                                )}
                                {!this.props.readOnly &&
                                    !this.state.isInline &&
                                    this.state.selectedGlobalEndpoint && (
                                    <Typography variant='caption' gutterBottom align='center'>
                                        <FormattedMessage
                                            id='global.endpoints.can.be.only.edited.from.global.endpoint.page'
                                            defaultMessage='Global endpoints can be only edited from Global Endpoint Page'
                                        />
                                        <Link to='/endpoints'>
                                            <Button aria-label='Back'>
                                                <LaunchIcon />
                                            </Button>
                                        </Link>
                                        <FormattedMessage
                                            id='here.you.can.only.attach.a.global.endpoint.to.api'
                                            defaultMessage='Here you can only attach a global endpoint to API.'
                                        />
                                    </Typography>
                                )}
                                {!this.props.readOnly &&
                                    !this.state.isInline &&
                                    this.state.globalEndpoints && (
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
                                {!this.props.readOnly &&
                                    !this.state.isInline &&
                                    !this.state.globalEndpoints && (
                                    <Grid container>
                                        <Card className={classes.card}>
                                            <CardContent>
                                                <Typography gutterBottom noWrap>
                                                        Global Endpoints are not defined.
                                                </Typography>
                                                <Link to='/endpoints'>
                                                    <Button aria-label='Back'>
                                                        <LaunchIcon />
                                                            Define Global endpoint
                                                    </Button>
                                                </Link>
                                            </CardContent>
                                        </Card>
                                    </Grid>
                                )}
                                <Divider />
                                <Grid container>
                                    <Grid container>
                                        <Typography variant='subheading' gutterBottom>
                                            <FormattedMessage id='endpoint.url' defaultMessage='Endpoint URL' />
                                            {!this.props.readOnly &&
                                                this.state.isInline && (
                                                <IconButton
                                                    id={name}
                                                    className={classes.button}
                                                    aria-label='Add'
                                                    onClick={this.handleAddAdditionalEndpoints}>
                                                    <AddIcon id='1' />
                                                </IconButton>
                                            )}
                                        </Typography>
                                    </Grid>
                                    {epUrls}
                                </Grid>
                                {!this.props.readOnly &&
                                    this.state.isInline &&
                                    numOfEndpoints > 1 && (
                                    <Grid container>
                                        <Grid container>
                                            <RadioGroup
                                                aria-label='Endpoint type'
                                                name='endpointpType'
                                                id='endpointpType'
                                                className={classes.group}
                                                value={this.state.endpointType}
                                                onClick={this.handleEpTypeRadioButtonChange}
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
                                        </Grid>
                                        <Grid container>
                                            <Typography variant='caption' gutterBottom align='center'>
                                                    Select either of these to make the endpoints behave as fail over or load balanced.
                                            </Typography>
                                        </Grid>
                                    </Grid>
                                )}
                            </div>
                        </Paper>
                        {!(!this.state.isInline && !this.state.selectedGlobalEndpoint) &&
                            <AdvanceEndpointDetail
                                endpointSecurity={this.props.endpoint.endpointSecurity}
                                endpoint={this.props.endpoint}
                                readOnly={this.props.readOnly}
                                isInline={this.state.isInline}
                            />
                        }
                    </Grid>
                    <Grid item xs={6}>
                        <Grid container>
                            {this.state.showEpConfig ? (
                                <Grow in={this.state.showEpConfig}>
                                    <EndpointForm
                                        selectedEndpointConfig={this.state.selectedEndpointConfig}
                                        endpoint={this.props.endpoint}
                                        isInline={this.state.isInline}
                                        readOnly={this.props.readOnly}
                                        showConfig={this.state.showEpConfig}
                                        showEpConfigSlide={this.state.showEpConfigSlide}
                                    />
                                </Grow>
                            ) : (
                                <Slide
                                    direction='up'
                                    in={this.state.showEpConfigSlide}
                                    mountOnEnter
                                    unmountOnExit
                                >
                                    <EndpointForm
                                        selectedEndpointConfig={this.state.selectedEndpointConfig}
                                        endpoint={this.props.endpoint}
                                        isInline={this.state.isInline}
                                        readOnly={this.props.readOnly}
                                        showConfig={this.state.showEpConfig}
                                        showEpConfigSlide={this.state.showEpConfigSlide}
                                    />
                                </Slide>
                            )}
                        </Grid>
                    </Grid>
                </Grid>
            </div>
        );
    }
}
EndpointDetail.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(EndpointDetail);
