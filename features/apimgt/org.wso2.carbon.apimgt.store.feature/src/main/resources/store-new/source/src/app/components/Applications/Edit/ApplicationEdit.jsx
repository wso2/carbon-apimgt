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
        };
        this.handleChange = this.handleChange.bind(this);
    }

    /**
     * @memberof ApplicationEdit
     */
    componentDidMount() {
        const { match } = this.props;
        const api = new API();
        const promisedApplication = Application.get(match.params.application_id);
        const promisedTiers = api.getAllTiers('application');
        Promise.all([promisedApplication, promisedTiers])
            .then((response) => {
                const [application, tierResponse] = response;
                this.setState({
                    quota: application.throttlingPolicy,
                    appName: application.name,
                    appDescription: application.description,
                    id: application.applicationId,
                    appLifeCycleStatus: application.lifeCycleStatus,
                });
                const appTiers = [];
                tierResponse.body.list.map(item => appTiers.push(item.name));
                this.setState({ appTiers });
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
            appName, id, quota, appDescription, appLifeCycleStatus,
        } = this.state;
        const {
            history,
        } = this.props;
        event.preventDefault();
        if (!appName) {
            Alert.error('Application name is required');
        } else {
            const updatedApplication = {
                applicationId: id,
                name: appName,
                throttlingPolicy: quota,
                description: appDescription,
                lifeCycleStatus: appLifeCycleStatus,
            };
            const api = new API();
            const promisedUpdate = api.updateApplication(updatedApplication, null);
            promisedUpdate
                .then((response) => {
                    const appId = response.body.applicationId;
                    const redirectUrl = '/applications/' + appId;
                    history.push(redirectUrl);
                    Alert.info('Application updated successfully');
                    console.log('Application updated successfully.');
                })
                .catch((error) => {
                    Alert.error('Error while updating application');
                    console.log('Error while updating application ' + error);
                });
        }
    };

    /**
     * @memberof ApplicationEdit
     * @returns {React.Fragment}
     */
    render() {
        const { classes } = this.props;
        const {
            notFound, appDescription, open, appName, appTiers, quota,
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
                                Edit Application
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
                                            label='Application Name'
                                            value={appName || ''}
                                            InputLabelProps={{
                                                shrink: true,
                                            }}
                                            helperText='Enter a name to identify the Application. You will be able to
                                            pick this application when subscribing to APIs '
                                            fullWidth
                                            name='appName'
                                            onChange={this.handleChange('appName')}
                                            placeholder='My Mobile Application'
                                            autoFocus
                                            className={classes.inputText}
                                        />
                                    </FormControl>
                                    {appTiers && (
                                        <FormControl margin='normal' className={classes.FormControlOdd}>
                                            <InputLabel htmlFor='quota-helper' className={classes.quotaHelp}>
                                                Per Token Quota
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
                                                Assign API request quota per access token. Allocated quota will be
                                                shared among all the subscribed APIs of the application.
                                            </Typography>
                                        </FormControl>
                                    )}
                                    <FormControl margin='normal' className={classes.FormControl}>
                                        <TextField
                                            label='Application Description'
                                            InputLabelProps={{
                                                shrink: true,
                                            }}
                                            value={appDescription || ''}
                                            helperText='Describe the application'
                                            fullWidth
                                            multiline
                                            rowsMax='4'
                                            name='appDescription'
                                            onChange={this.handleChange('appDescription')}
                                            placeholder='This application is grouping apis for my mobile application'
                                            className={classes.inputText}
                                        />
                                    </FormControl>
                                    <div className={classes.buttonsWrapper}>
                                        <Link to='/applications' className={classes.buttonRight}>
                                            <Button
                                                variant='outlined'
                                                className={classes.button}
                                            >
                                                Cancel
                                            </Button>
                                        </Link>
                                        <Button
                                            variant='contained'
                                            className={classes.button}
                                            color='primary'
                                            onClick={this.handleSubmit}
                                        >
                                            UPDATE APPLICATION
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
};

export default withStyles(styles)(ApplicationEdit);
