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
import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Icon from '@material-ui/core/Icon';
import { FormattedMessage, injectIntl } from 'react-intl';
import Loading from 'AppComponents/Base/Loading/Loading';
import Alert from 'AppComponents/Shared/Alert';
import ProvideOAuthKeys from 'AppComponents/Shared/AppsAndKeys/ProvideOAuthKeys';
import Application from 'AppData/Application';
import AuthManager from 'AppData/AuthManager';
import Settings from 'AppComponents/Shared/SettingsContext';
import API from 'AppData/api';
import KeyConfiguration from './KeyConfiguration';
import ViewKeys from './ViewKeys';
import WaitingForApproval from './WaitingForApproval';
import { ScopeValidation, resourceMethods, resourcePaths } from '../ScopeValidation';

const styles = theme => ({
    root: {
        padding: theme.spacing.unit * 3,
    },
    button: {
        marginLeft: 0,
    },
    cleanUpButton: {
        marginLeft: 15,
    },
    cleanUpInfoText: {
        padding: '10px 0px 10px 15px',
    },
    tokenSection: {
        marginTop: theme.spacing.unit * 2,
        marginBottom: theme.spacing.unit * 2,
    },
    margin: {
        marginRight: theme.spacing.unit * 2,
    },
    keyTitle: {
        textTransform: 'uppercase',
    },
    keyConfigWrapper: {
        flexDirection: 'column',
        marginBottom: 0,
    },
    generateWrapper: {
        padding: '10px 0px',
        marginLeft: theme.spacing.unit * 1.25,
    },
    paper: {
        background: 'none',
        marginBottom: theme.spacing.unit * 2,
    },
});

/**
 *  @param {event} event event
 *  @param {String} value description
 */
class TokenManager extends React.Component {
    static contextType = Settings;

    /**
     *
     * @param {*} props props
     */
    constructor(props) {
        super(props);
        const { selectedApp, keyType } = this.props;
        this.state = {
            keys: null,
            isKeyJWT: false,
            keyRequest: {
                keyType,
                serverSupportedGrantTypes: [],
                supportedGrantTypes: [],
                callbackUrl: '',
            },
            providedConsumerKey: '',
            providedConsumerSecret: '',
            isUserOwner: false,
        };
        this.keyStates = {
            COMPLETED: 'COMPLETED',
            APPROVED: 'APPROVED',
            CREATED: 'CREATED',
            REJECTED: 'REJECTED',
        };
        if (selectedApp) {
            this.appId = selectedApp.appId || selectedApp.value;
            this.application = Application.get(this.appId);
        }
        this.updateKeyRequest = this.updateKeyRequest.bind(this);
        this.generateKeys = this.generateKeys.bind(this);
        this.updateKeys = this.updateKeys.bind(this);
        this.cleanUpKeys = this.cleanUpKeys.bind(this);
        this.handleOnChangeProvidedOAuth = this.handleOnChangeProvidedOAuth.bind(this);
        this.provideOAuthKeySecret = this.provideOAuthKeySecret.bind(this);
    }

    /**
     *
     *
     * @memberof TokenManager
     */
    componentDidMount() {
        this.loadApplication();
    }

    /**
     * load application key generation ui
     */
    loadApplication = () => {
        this.getserverSupportedGrantTypes();
        this.checkOwner();
        if (this.appId) {
            this.application
                .then(application => application.getKeys())
                .then((keys) => {
                    const { keyType } = this.props;
                    const { keyRequest } = this.state;
                    if (keys.size > 0 && keys.get(keyType)) {
                        const { callbackUrl, supportedGrantTypes } = keys.get(keyType);
                        const newRequest = {
                            ...keyRequest, callbackUrl, supportedGrantTypes,
                        };
                        this.setState({ keys, keyRequest: newRequest });
                    } else {
                        this.setState({ keys });
                    }
                })
                .catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.error(error);
                    }
                    if (error.status === 404) {
                        this.setState({ notFound: true });
                    }
                });
        }
    }

    /**
     * get supported grant types from the settings api
     */
    getserverSupportedGrantTypes = () => {
        const api = new API();
        const promisedSettings = api.getSettings();
        promisedSettings
            .then((response) => {
                let { keyRequest } = this.state;
                keyRequest = { ...keyRequest };
                keyRequest.serverSupportedGrantTypes = response.obj.grantTypes;
                keyRequest.supportedGrantTypes = response.obj.grantTypes.filter(item => item !== 'authorization_code'
                    && item !== 'implicit');
                this.setState({ keyRequest });
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
    };

    /**
     * Check if the current user is the owner of the application
     * @param {*} owner required param
     */
    checkOwner() {
        const { selectedApp } = this.props;
        const username = AuthManager.getUser().name;
        this.setState({ isUserOwner: username.includes(selectedApp.owner) });
    }

    /**
     * Update keyRequest state
     * @param {Object} keyRequest parameters requried for key generation request
     */
    updateKeyRequest(keyRequest) {
        this.setState({ keyRequest });
    }

    /**
     * Generate keys for application,
     *
     * @memberof KeyConfiguration
     */
    generateKeys() {
        const { keyRequest, keys } = this.state;
        const {
            keyType, updateSubscriptionData, selectedApp: { tokenType }, intl,
        } = this.props;
        this.application
            .then((application) => {
                return application.generateKeys(keyType, keyRequest.supportedGrantTypes, keyRequest.callbackUrl);
            })
            .then((response) => {
                if (updateSubscriptionData) {
                    updateSubscriptionData();
                }
                const newKeys = new Map([...keys]);
                const isKeyJWT = tokenType === 'JWT';
                newKeys.set(keyType, response);
                this.setState({ keys: newKeys, isKeyJWT });
                Alert.info(intl.formatMessage({
                    id: 'Shared.AppsAndKeys.TokenManager.key.generate.success',
                    defaultMessage: 'Application keys generated successfully',
                }));
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
                Alert.error(intl.formatMessage({
                    id: 'Shared.AppsAndKeys.TokenManager.key.generate.error',
                    defaultMessage: 'Error occurred when generating application keys',
                }));
            });
    }

    /**
     *
     * @memberof KeyConfiguration
     */
    updateKeys() {
        const { keys, keyRequest } = this.state;
        const { keyType, intl } = this.props;
        const applicationKey = keys.get(keyType);
        this.application
            .then((application) => {
                return application.updateKeys(
                    applicationKey.tokenType,
                    keyType,
                    keyRequest.supportedGrantTypes,
                    keyRequest.callbackUrl,
                    applicationKey.consumerKey,
                    applicationKey.consumerSecret,
                );
            })
            .then(() => {
                Alert.info(intl.formatMessage({
                    id: 'Shared.AppsAndKeys.TokenManager.key.update.success',
                    defaultMessage: 'Application keys updated successfully',
                }));
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
                Alert.error(intl.formatMessage({
                    id: 'Shared.AppsAndKeys.TokenManager.key.update.error',
                    defaultMessage: 'Error occurred when updating application keys',
                }));
            });
    }

    /**
     * Cleanup application keys
     */
    cleanUpKeys() {
        const { keyType, intl } = this.props;
        this.application
            .then((application) => {
                return application.cleanUpKeys(keyType);
            })
            .then(() => {
                this.loadApplication();
                Alert.info(intl.formatMessage({
                    id: 'Shared.AppsAndKeys.TokenManager.key.cleanup.success',
                    defaultMessage: 'Application keys cleaned successfully',
                }));
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
                Alert.error(intl.formatMessage({
                    id: 'Shared.AppsAndKeys.TokenManager.key.cleanup.error',
                    defaultMessage: 'Error occurred while cleaning up application keys',
                }));
            });
    }

    /**
     * Handle on change of provided consumer key and consumer secret
     *
     * @param event onChange event
     */
    handleOnChangeProvidedOAuth(event) {
        this.setState({ [event.target.name]: event.target.value });
    }

    /**
     * Provide consumer key and secret of an existing OAuth app to an application
     */
    provideOAuthKeySecret() {
        const { providedConsumerKey, providedConsumerSecret } = this.state;
        const { keyType, intl } = this.props;

        this.application
            .then((application) => {
                return application.provideKeys(keyType, providedConsumerKey, providedConsumerSecret);
            })
            .then(() => {
                this.setState({ providedConsumerKey: '', providedConsumerSecret: '' });
                Alert.info(intl.formatMessage({
                    id: 'Shared.AppsAndKeys.TokenManager.key.provide.success',
                    defaultMessage: 'Application keys provided successfully',
                }));
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
                Alert.error(intl.formatMessage({
                    id: 'Shared.AppsAndKeys.TokenManager.key.provide.error',
                    defaultMessage: 'Error occurred when providing application keys',
                }));
            });
    }

    /**
     *  @returns {Component}
     * @memberof Tokenemanager
     */
    render() {
        const {
            classes, selectedApp, keyType,
        } = this.props;
        const {
            keys, keyRequest, notFound, isKeyJWT, providedConsumerKey, providedConsumerSecret, isUserOwner,
        } = this.state;
        if (!keys) {
            return <Loading />;
        }
        const key = keys.get(keyType);
        if (keys.size > 0 && key && key.keyState === 'APPROVED' && !key.consumerKey) {
            return (
                <Fragment>
                    <Typography className={classes.cleanUpInfoText} variant='subtitle1'>
                        <FormattedMessage
                            id='Shared.AppsAndKeys.TokenManager.cleanup.text'
                            defaultMessage='Error! You have partially-created keys.
                            Please click the Clean Up button and try again.'
                        />
                    </Typography>
                    <Button
                        variant='contained'
                        color='primary'
                        className={classes.cleanUpButton}
                        onClick={this.cleanUpKeys}
                    >
                        <FormattedMessage
                            defaultMessage='Clean up'
                            id='Shared.AppsAndKeys.TokenManager.cleanup'
                        />
                    </Button>
                </Fragment>
            );
        }
        if (key && (key.keyState === this.keyStates.CREATED || key.keyState === this.keyStates.REJECTED)) {
            return <WaitingForApproval keyState={key.keyState} states={this.keyStates} />;
        }
        // todo replace use of localStorage with useContext
        // const settingsData = localStorage.getItem('settings');
        // const { mapExistingAuthApps } = JSON.parse(settingsData);

        const settingsContext = this.context;
        const { mapExistingAuthApps } = settingsContext.settings;

        return (
            <div className={classes.root}>
                <Typography variant='headline' className={classes.keyTitle}>
                    {keyType + ' '}
                    <FormattedMessage
                        defaultMessage='Key and Secret'
                        id='Shared.AppsAndKeys.TokenManager.key.and.secret'
                    />
                </Typography>
                <ViewKeys
                    selectedApp={selectedApp}
                    keyType={keyType}
                    keys={keys}
                    isKeyJWT={isKeyJWT}
                />
                <Paper className={classes.paper}>
                    <ExpansionPanel defaultExpanded>
                        <ExpansionPanelSummary expandIcon={<Icon>expand_more</Icon>}>
                            <Typography className={classes.heading} variant='subtitle1'>
                                {
                                    keys.size > 0 && keys.get(keyType)
                                        ? (
                                            <FormattedMessage
                                                defaultMessage='Update Configuration'
                                                id='Shared.AppsAndKeys.TokenManager.update.configuration'
                                            />
                                        )
                                        : (
                                            <FormattedMessage
                                                defaultMessage='Key Configuration'
                                                id='Shared.AppsAndKeys.TokenManager.key.configuration'
                                            />
                                        )
                                }
                            </Typography>
                        </ExpansionPanelSummary>
                        <ExpansionPanelDetails className={classes.keyConfigWrapper}>
                            <KeyConfiguration
                                keys={keys}
                                selectedApp={selectedApp}
                                keyType={keyType}
                                updateKeyRequest={this.updateKeyRequest}
                                keyRequest={keyRequest}
                                isUserOwner={isUserOwner}
                            />
                        </ExpansionPanelDetails>
                    </ExpansionPanel>
                    <div className={classes.generateWrapper}>
                        <ScopeValidation
                            resourcePath={resourcePaths.APPLICATION_GENERATE_KEYS}
                            resourceMethod={resourceMethods.POST}
                        >
                            {!isUserOwner ? (
                                <Fragment>
                                    <Button
                                        variant='contained'
                                        color='primary'
                                        className={classes.button}
                                        onClick={
                                            keys.size > 0 && keys.get(keyType) ? this.updateKeys : this.generateKeys}
                                        noFound={notFound}
                                        disabled={!isUserOwner}
                                    >
                                        {keys.size > 0 && keys.get(keyType) ? 'Update keys' : 'Generate Keys'}
                                    </Button>
                                    <Typography variant='caption'>
                                        <FormattedMessage
                                            defaultMessage='Only owner can generate or update keys'
                                            id='Shared.AppsAndKeys.TokenManager.key.and.user.owner'
                                        />
                                    </Typography>
                                </Fragment>
                            ) : (
                                <Button
                                    variant='contained'
                                    color='primary'
                                    className={classes.button}
                                    onClick={keys.size > 0 && keys.get(keyType) ? this.updateKeys : this.generateKeys}
                                    noFound={notFound}
                                >
                                    {keys.size > 0 && keys.get(keyType) ? 'Update' : 'Generate Keys'}
                                </Button>
                            )}
                        </ScopeValidation>
                    </div>
                </Paper>
                {
                    mapExistingAuthApps && !keys.get(keyType) && (
                        <Paper className={classes.paper}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary expandIcon={<Icon>expand_more</Icon>}>
                                    <Typography className={classes.heading} variant='subtitle1'>
                                        <FormattedMessage
                                            defaultMessage='Provide Existing OAuth Keys'
                                            id='Shared.AppsAndKeys.TokenManager.provide.oauth'
                                        />
                                    </Typography>
                                </ExpansionPanelSummary>
                                <ExpansionPanelDetails className={classes.keyConfigWrapper}>
                                    <ProvideOAuthKeys
                                        onChange={this.handleOnChangeProvidedOAuth}
                                        consumerKey={providedConsumerKey}
                                        consumerSecret={providedConsumerSecret}
                                        isUserOwner={isUserOwner}
                                    />
                                </ExpansionPanelDetails>
                            </ExpansionPanel>
                            <div className={classes.generateWrapper}>
                                <ScopeValidation
                                    resourcePath={resourcePaths.APPLICATION_GENERATE_KEYS}
                                    resourceMethod={resourceMethods.POST}
                                >
                                    {!isUserOwner ? (
                                        <Fragment>
                                            <Button
                                                variant='contained'
                                                color='primary'
                                                className={classes.button}
                                                onClick={this.provideOAuthKeySecret}
                                                noFound={notFound}
                                                disabled={!isUserOwner}
                                            >
                                                {
                                                    keys.size > 0 && keys.get(keyType)
                                                        ? (
                                                            <FormattedMessage
                                                                defaultMessage='Update'
                                                                id='Shared.AppsAndKeys.TokenManager.provide.
                                                                oauth.button.update'
                                                            />
                                                        )
                                                        : (
                                                            <FormattedMessage
                                                                defaultMessage='Provide'
                                                                id='Shared.AppsAndKeys.TokenManager.
                                                                provide.oauth.button.provide'
                                                            />
                                                        )
                                                }
                                            </Button>
                                            <Typography variant='caption'>
                                                <FormattedMessage
                                                    defaultMessage='Only owner can provide keys'
                                                    id='Shared.AppsAndKeys.TokenManager.key.provide.user.owner'
                                                />
                                            </Typography>
                                        </Fragment>
                                    ) : (
                                        <Button
                                            variant='contained'
                                            color='primary'
                                            className={classes.button}
                                            onClick={this.provideOAuthKeySecret}
                                            noFound={notFound}
                                        >
                                            {
                                                keys.size > 0 && keys.get(keyType)
                                                    ? (
                                                        <FormattedMessage
                                                            defaultMessage='Update'
                                                            id='Shared.AppsAndKeys.TokenManager.
                                                            provide.oauth.button.update'
                                                        />
                                                    )
                                                    : (
                                                        <FormattedMessage
                                                            defaultMessage='Provide'
                                                            id='Shared.AppsAndKeys.
                                                            TokenManager.provide.oauth.button.provide'
                                                        />
                                                    )
                                            }
                                        </Button>
                                    )}
                                </ScopeValidation>
                            </div>
                        </Paper>
                    )
                }
            </div>
        );
    }
}

TokenManager.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
    selectedApp: PropTypes.shape({
        tokenType: PropTypes.string.isRequired,
        appId: PropTypes.string,
        value: PropTypes.string,
        owner: PropTypes.string,
    }).isRequired,
    keyType: PropTypes.string.isRequired,
    updateSubscriptionData: PropTypes.func.isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

export default injectIntl(withStyles(styles)(TokenManager));
