/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';
import Grid from '@material-ui/core/Grid';
import { Link } from 'react-router-dom';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import Slide from '@material-ui/core/Slide';
import Dialog from '@material-ui/core/Dialog';
import AppBar from '@material-ui/core/AppBar';
import CloseIcon from '@material-ui/icons/Close';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import Select from '@material-ui/core/Select';
import FormControl from '@material-ui/core/FormControl';
import MenuItem from '@material-ui/core/MenuItem';
import Application from '../../../data/Application';
import API from '../../../data/api';
import Alert from '../../Shared/Alert';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';

/**
 * @param {*} theme theme details
 * @returns {Object}
 */
const styles = theme => ({
    root: {
        padding: theme.spacing.unit * 3,
    },
    appBar: {
        position: 'relative',
        backgroundColor: theme.palette.background.appBar,
        color: theme.palette.getContrastText(theme.palette.background.appBar),
    },
    title: {
        display: 'inline-block',
        marginLeft: 20,
    },
    buttonsWrapper: {
        marginTop: 40,
    },
    inputText: {
        marginTop: 20,
    },
    buttonRight: {
        textDecoration: 'none',
        color: 'white',
    },
    button: {
        marginRight: theme.spacing.unit * 2,
    },
    FormControl: {
        width: '100%',
    },
    FormControlOdd: {
        backgroundColor: theme.palette.background.paper,
        width: '100%',
    },
    flex: {
        flex: 1,
    },
    createFormWrapper: {
        paddingLeft: theme.spacing.unit * 5,
    },
    quotaHelp: {
        position: 'relative',
    },
});

/**
 * @param {*} props props
 * @returns {Slide}
 */
function Transition(props) {
    return <Slide direction='up' {...props} />;
}

/**
 * @class ApplicationEdit
 * @extends {Component}
 */
class ApplicationEdit extends Component {
    /**
     * @param {Object} props props passed from above
     */
    constructor(props) {
        super(props);
        this.state = {
            appName: null,
            open: true,
            quota: 'Unlimited',
            appDescription: null,
            id: null,
            appTiers: [],
            notFound: false,
            appLifeCycleStatus: null,
            appAttributes: null,
            allAppAttributes: null,
        };
        this.handleChange = this.handleChange.bind(this);
        this.handleAttributesChange = this.handleAttributesChange.bind(this);
        this.isRequiredAttribute = this.isRequiredAttribute.bind(this);
    }

    /**
     * @memberof ApplicationEdit
     */
    componentDidMount() {
        const { match } = this.props;
        const api = new API();
        const promisedApplication = Application.get(match.params.application_id);
        const promisedTiers = api.getAllTiers('application');
        const promisedAttributes = api.getAllApplicationAttributes();
        Promise.all([promisedApplication, promisedTiers, promisedAttributes])
            .then((response) => {
                const [application, tierResponse, allAttributes] = response;
                this.setState({
                    quota: application.throttlingPolicy,
                    appName: application.name,
                    appDescription: application.description,
                    id: application.applicationId,
                    appLifeCycleStatus: application.lifeCycleStatus,
                    appAttributes: application.attributes,
                });
                const appTiers = [];
                tierResponse.body.list.map(item => appTiers.push(item.name));
                const allAppAttributes = [];
                allAttributes.body.list.map(item => allAppAttributes.push(item));
                this.setState({ appTiers, allAppAttributes });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    /**
     * @param {object} name state key
     * @returns {void}
     * @memberof ApplicationEdit
     */
    handleChange = name => (event) => {
        this.setState({ [name]: event.target.value });
    };

    /**
     * @param {object} name application attribute name
     * @returns {void}
     * @memberof ApplicationEdit
     */
    handleAttributesChange = name => (event) => {
        const { appAttributes } = this.state;
        appAttributes[name.key] = event.target.value;
        this.setState({ appAttributes });
    };

    /**
     * @param {object} name application attribute name
     * @returns {void}
     * @memberof ApplicationEdit
     */
    isRequiredAttribute = (name) => {
        const { allAppAttributes } = this.state;
        if (allAppAttributes) {
            for (let i = 0; i < allAppAttributes.length; i++) {
                if (allAppAttributes[i].attribute === name.key) {
                    return allAppAttributes[i].required;
                }
            }
        }
        return false;
    };

    /**
     * @memberof ApplicationEdit
     */
    handleClose = () => {
        this.setState({ open: false });
    };

    /**
     * @param {Object} event the event object
     * @memberof ApplicationEdit
     */
    handleSubmit = (event) => {
        const {
            appName, id, quota, appDescription, appLifeCycleStatus, appAttributes,
        } = this.state;
        const {
            history, intl,
        } = this.props;
        event.preventDefault();
        if (!appName) {
            Alert.error(intl.formatMessage({
                id: 'Applications.Edit.app.name.required',
                defaultMessage: 'Application name is required',
            }));
        } else {
            const updatedApplication = {
                applicationId: id,
                name: appName,
                throttlingPolicy: quota,
                description: appDescription,
                lifeCycleStatus: appLifeCycleStatus,
                attributes: appAttributes,
            };
            const api = new API();
            const promisedUpdate = api.updateApplication(updatedApplication, null);
            promisedUpdate
                .then((response) => {
                    const appId = response.body.applicationId;
                    const redirectUrl = '/applications/' + appId;
                    history.push(redirectUrl);
                    Alert.info(intl.formatMessage({
                        id: 'Applications.Edit.app.updated.success',
                        defaultMessage: 'Application updated successfully',
                    }));
                    console.log('Application updated successfully.');
                })
                .catch((error) => {
                    Alert.error(intl.formatMessage({
                        id: 'Applications.Edit.error.update.app',
                        defaultMessage: 'Error while updating application',
                    }));
                    console.log('Error while updating application ' + error);
                });
        }
    };

    /**
     * @memberof ApplicationEdit
     * @returns {React.Fragment}
     */
    render() {
        const { classes, intl } = this.props;
        const {
            notFound, appDescription, open, appName, appTiers, quota, appAttributes, allAppAttributes,
        } = this.state;
        if (notFound) {
            return <ResourceNotFound />;
        }
        return (
            <React.Fragment>
                <Dialog fullScreen open={open} onClose={this.handleClose} TransitionComponent={Transition}>
                    <AppBar className={classes.appBar}>
                        <Toolbar>
                            <Link to='/applications' className={classes.buttonRight}>
                                <IconButton color='inherit' onClick={this.handleClose} aria-label='Close'>
                                    <CloseIcon />
                                </IconButton>
                            </Link>
                            <Typography variant='title' color='inherit' className={classes.flex}>
                                <FormattedMessage id='Applications.Edit.edit.app' defaultMessage='Edit Application' />
                            </Typography>
                        </Toolbar>
                    </AppBar>
                    <div className={classes.createFormWrapper}>
                        <form className={classes.container} noValidate autoComplete='off'>
                            <Grid container spacing={24} className={classes.root}>
                                <Grid item xs={12} md={6}>
                                    <FormControl margin='normal' className={classes.FormControl}>
                                        <TextField
                                            required
                                            label={(
                                                <FormattedMessage
                                                    id='Applications.Edit.app.name'
                                                    defaultMessage='Application Name'
                                                />
                                            )}
                                            value={appName || ''}
                                            InputLabelProps={{
                                                shrink: true,
                                            }}
                                            helperText={(
                                                <FormattedMessage
                                                    id='Applications.Edit.app.name.helper'
                                                    defaultMessage={'Enter a name to identify the'
                                                    + ' Application. You will be able to pick this application when'
                                                    + ' subscribing to APIs'}
                                                />
                                            )}
                                            fullWidth
                                            name='appName'
                                            onChange={this.handleChange('appName')}
                                            placeholder={
                                                intl.formatMessage({
                                                    id: 'Applications.Edit.app.name.placeholder',
                                                    defaultMessage: 'My Mobile Application',
                                                })
                                            }
                                            autoFocus
                                            className={classes.inputText}
                                        />
                                    </FormControl>
                                    {appTiers && (
                                        <FormControl margin='normal' className={classes.FormControlOdd}>
                                            <InputLabel htmlFor='quota-helper' className={classes.quotaHelp}>
                                                <FormattedMessage
                                                    id='Applications.Edit.token.quota'
                                                    defaultMessage='Per Token Quota'
                                                />
                                            </InputLabel>
                                            <Select
                                                value={quota}
                                                onChange={this.handleChange('quota')}
                                                input={<Input name='quota' id='quota-helper' />}
                                            >
                                                {appTiers.map(tier => (
                                                    <MenuItem key={tier} value={tier}>
                                                        {tier}
                                                    </MenuItem>
                                                ))}
                                            </Select>
                                            <Typography variant='caption'>
                                                <FormattedMessage
                                                    id='Applications.Edit.token.quota.caption'
                                                    defaultMessage={'Assign API request quota per access token.'
                                                    + ' Allocated quota will be shared among all the subscribed APIs'
                                                    + ' of the application.'}
                                                />
                                            </Typography>
                                        </FormControl>
                                    )}
                                    <FormControl margin='normal' className={classes.FormControl}>
                                        <TextField
                                            label={(
                                                <FormattedMessage
                                                    id='Applications.Edit.app.desc'
                                                    defaultMessage='Application Description'
                                                />
                                            )}
                                            InputLabelProps={{
                                                shrink: true,
                                            }}
                                            value={appDescription || ''}
                                            helperText={(
                                                <FormattedMessage
                                                    id='Applications.Edit.app.desc.helper'
                                                    defaultMessage='Describe the application'
                                                />
                                            )}
                                            fullWidth
                                            multiline
                                            rowsMax='4'
                                            name='appDescription'
                                            onChange={this.handleChange('appDescription')}
                                            placeholder={
                                                intl.formatMessage({
                                                    id: 'Applications.Edit.app.desc.placeholder',
                                                    defaultMessage: 'This application is grouping'
                                                        + ' apis for my mobile application',
                                                })
                                            }
                                            className={classes.inputText}
                                        />
                                    </FormControl>
                                    {appAttributes && (
                                        Object.entries(appAttributes).map(([key, value]) => (
                                            <FormControl margin='normal' className={classes.FormControl} key={key}>
                                                <TextField
                                                    required={this.isRequiredAttribute({ key })}
                                                    label={key}
                                                    InputLabelProps={{
                                                        shrink: true,
                                                    }}
                                                    value={value}
                                                    helperText={allAppAttributes
                                                        && (Object.entries(allAppAttributes).map((item) => {
                                                            if (item[1].attribute === key) {
                                                                return item[1].description;
                                                            }
                                                            return '';
                                                        }))
                                                    }
                                                    fullWidth
                                                    name={key}
                                                    onChange={this.handleAttributesChange({ key })}
                                                    placeholder={'Enter ' + key}
                                                    className={classes.inputText}
                                                />
                                            </FormControl>
                                        ))
                                    )}
                                    <div className={classes.buttonsWrapper}>
                                        <Link to='/applications' className={classes.buttonRight}>
                                            <Button
                                                variant='outlined'
                                                className={classes.button}
                                            >
                                                <FormattedMessage
                                                    id='Applications.Edit.cancel'
                                                    defaultMessage='CANCEL'
                                                />
                                            </Button>
                                        </Link>
                                        <Button
                                            variant='contained'
                                            className={classes.button}
                                            color='primary'
                                            onClick={this.handleSubmit}
                                        >
                                            <FormattedMessage
                                                id='Applications.Edit.update.app'
                                                defaultMessage='UPDATE APPLICATION'
                                            />
                                        </Button>
                                    </div>
                                </Grid>
                            </Grid>
                        </form>
                    </div>
                </Dialog>
            </React.Fragment>
        );
    }
}

ApplicationEdit.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(ApplicationEdit));
