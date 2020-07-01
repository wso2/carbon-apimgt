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
import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Box from '@material-ui/core/Box';
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Icon from '@material-ui/core/Icon';
import { FormattedMessage, injectIntl } from 'react-intl';
import Loading from 'AppComponents/Base/Loading/Loading';
import CircularProgress from '@material-ui/core/CircularProgress';

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
import TokenMangerSummary from './TokenManagerSummary';
import Progress from '../Progress';

const styles = (theme) => ({
    root: {
        padding: theme.spacing(3),
        '& span, & h5, & label, & input': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        },
    },
    button: {
        marginLeft: 0,
        '& span': {
            color: theme.palette.getContrastText(theme.palette.primary.main),
        }
    },
    cleanUpButton: {
        marginLeft: 15,
    },
    cleanUpInfoText: {
        padding: '10px 0px 10px 15px',
    },
    tokenSection: {
        marginTop: theme.spacing(2),
        marginBottom: theme.spacing(2),
    },
    margin: {
        marginRight: theme.spacing(2),
    },
    keyConfigWrapper: {
        flexDirection: 'column',
        marginBottom: 0,
    },
    generateWrapper: {
        padding: '10px 0px',
        marginLeft: theme.spacing(1.25),
    },
    paper: {
        background: 'none',
        marginBottom: theme.spacing(2),
        marginTop: theme.spacing(2),
    },
});

function TabPanel(props) {
    const { children, value, index, ...other } = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`nav-tabpanel-${index}`}
            aria-labelledby={`nav-tab-${index}`}
            {...other}
        >
            {value === index && (
                <Box p={3}>
                    <Typography>{children}</Typography>
                </Box>
            )}
        </div>
    );
};

TabPanel.propTypes = {
  children: PropTypes.node,
  index: PropTypes.any.isRequired,
  value: PropTypes.any.isRequired,
};

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
            isLoading: false,
            keys: null,
            isKeyJWT: false,
            keyRequest: {
                keyType,
                serverSupportedGrantTypes: [],
                supportedGrantTypes: [],
                callbackUrl: '',
                validityTime: 3600,
                additionalProperties: {},
                keyManager: '',
            },
            keyManagers: [],
            selectedTab: 'Default',
            providedConsumerKey: '',
            providedConsumerSecret: '',
            generateEnabled: true,
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

    setGenerateEnabled = (state) => {
        this.setState({ generateEnabled: state });
    }

    /**
     * get supported grant types from the settings api
     * deprecated
     */
    getserverSupportedGrantTypes = () => {
        const api = new API();
        const promisedSettings = api.getSettings();
        promisedSettings
            .then((response) => {
                const { keyRequest } = this.state;
                const newKeyRequest = { ...keyRequest };
                newKeyRequest.serverSupportedGrantTypes = response.obj.grantTypes;
                newKeyRequest.supportedGrantTypes = response.obj.grantTypes.filter((item) => item !== 'authorization_code'
                    && item !== 'implicit');
                this.setState({ keyRequest: newKeyRequest });
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
     * get registered key managers from the key-managers store rest api
     */
    getRegisteredKeyManagers = () => {
        const api = new API();
        const promisedKeyManagers = api.getKeyManagers();
        promisedKeyManagers
            .then((response) => {
                const responseKeyManagerList = [];
                response.body.list.map((item) => responseKeyManagerList.push(item));
                this.setState({ keyManagers: responseKeyManagerList });
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

    handleTabChange = (event, newValue) => {
        this.setState({ selectedTab: newValue });
    };

    /**
     * load application key generation ui
     */
    loadApplication = () => {
        this.getserverSupportedGrantTypes();
        this.getRegisteredKeyManagers();
        const { keyType } = this.props;
        if (this.appId) {
            this.application
                .then((application) => application.getKeys(keyType))
                .then((keys) => {
                    const { keyRequest, selectedTab } = this.state;
                    if (keys.size > 0 && keys.get(selectedTab) && keys.get(selectedTab).keyType === keyType) {
                        const { callbackUrl, supportedGrantTypes } = keys.get(selectedTab);
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
        const { keyRequest, keys, selectedTab } = this.state;
        this.setState({ isLoading: true });
        const {
            keyType, updateSubscriptionData, selectedApp: { tokenType, hashEnabled }, intl,
        } = this.props;
        this.application
            .then((application) => {
                return application.generateKeys(
                    keyType, keyRequest.supportedGrantTypes,
                    keyRequest.callbackUrl, keyRequest.validityTime, 
                    keyRequest.additionalProperties, selectedTab
                );
            })
            .then((response) => {
                if (updateSubscriptionData) {
                    updateSubscriptionData();
                }
                const newKeys = new Map([...keys]);
                // in case token hashing is enabled, isKeyJWT is set to true even if the token type is JWT.
                // This is to mimic the behavior of JWT tokens (by showing the token in a dialog)
                const isKeyJWT = (tokenType === 'JWT') || hashEnabled;
                newKeys.set(selectedTab, response);
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
            }).finally(() => this.setState({ isLoading: false }));
    }

    /**
     *
     * @memberof KeyConfiguration
     */
    updateKeys() {
        this.setState({ isLoading: true });
        const { keys, keyRequest, selectedTab } = this.state;
        const { keyType, intl } = this.props;
        const applicationKey = (keys.get(selectedTab).keyType === keyType) && keys.get(selectedTab);
        this.application
            .then((application) => {
                return application.updateKeys(
                    applicationKey.tokenType,
                    keyType,
                    keyRequest.supportedGrantTypes,
                    keyRequest.callbackUrl,
                    applicationKey.consumerKey,
                    applicationKey.consumerSecret,
                    applicationKey.additionalProperties,
                    selectedTab,
                    applicationKey.keyMappingId
                );
            })
            .then((response) => {
                const newKeys = new Map([...keys]);
                newKeys.set(selectedTab, response);
                this.setState({ keys: newKeys });
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
            }).finally(() => this.setState({ isLoading: false }));
    }

    /**
     * Cleanup application keys
     */
    cleanUpKeys(selectedTab, keyMappingId) {
        const { keyType, intl } = this.props;
        this.application
            .then((application) => {
                return application.cleanUpKeys(keyType, selectedTab, keyMappingId);
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
    provideOAuthKeySecret(selectedTab, keyMappingId) {
        const { providedConsumerKey, providedConsumerSecret } = this.state;
        const { keyType, intl } = this.props;

        this.application
            .then((application) => {
                return application.provideKeys(keyType, providedConsumerKey, providedConsumerSecret, selectedTab, keyMappingId);
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
            classes, selectedApp, keyType, summary, selectedApp: { hashEnabled },
        } = this.props;
        const {
            keys, keyRequest, isLoading, isKeyJWT, providedConsumerKey,
            providedConsumerSecret, generateEnabled, selectedTab, keyManagers
        } = this.state;
        if (!keys) {
            return <Loading />;
        }
        const username = AuthManager.getUser().name;
        let isUserOwner = false;

        if (selectedApp.owner && username.toLowerCase() === selectedApp.owner.toLowerCase()) {
            isUserOwner = true;
        }
        const key = keys.size > 0 && keys.get(selectedTab) && (keys.get(selectedTab).keyType === keyType) && keys.get(selectedTab);

        if (key && key.token) {
            keyRequest.validityTime = key.token.validityTime;
        }
        if (summary) {
            if (keys) {
                return (
                    <TokenMangerSummary
                        keys={keys}
                        key={key}
                        keyStates={this.keyStates}
                        selectedApp={selectedApp}
                        selectedTab={selectedTab}
                        keyType={keyType}
                        isKeyJWT={isKeyJWT}
                        isUserOwner={isUserOwner}
                    />
                );
            } else {
                return (<Progress />);
            }
        }
        if (keys.size > 0 && keys.get(selectedTab) && keys.get(selectedTab).keyType === keyType && key && key.keyState === 'APPROVED' && !key.consumerKey) {
            return (
                <>
                    <Typography className={classes.cleanUpInfoText} variant='subtitle1'>
                        <FormattedMessage
                            id='Shared.AppsAndKeys.TokenManager.cleanup.text'
                            defaultMessage='Error! You have partially-created keys.
                            Please click `Clean Up` button and try again.'
                        />
                    </Typography>
                    <Button
                        variant='contained'
                        color='primary'
                        className={classes.cleanUpButton}
                        onClick={this.cleanUpKeys(selectedTab, keys.get(selectedTab).keyMappingId)}
                    >
                        <FormattedMessage
                            defaultMessage='Clean up'
                            id='Shared.AppsAndKeys.TokenManager.cleanup'
                        />
                    </Button>
                </>
            );
        }
        if (key && (key.keyState === this.keyStates.CREATED || key.keyState === this.keyStates.REJECTED)) {
            return <WaitingForApproval keyState={key.keyState} states={this.keyStates} />;
        }
        const keyGrantTypes = key ? key.supportedGrantTypes : [];
        const settingsContext = this.context;
        const { mapExistingAuthApps } = settingsContext.settings;

        return (
            <div className={classes.root}>
                <Paper className={classes.paper}>
                    <Tabs
                        value={selectedTab}
                        indicatorColor="primary"
                        textColor="primary"
                        onChange={this.handleTabChange}
                        aria-label="key manager tabs"
                    >
                        {keyManagers.map(keymanager => (
                            <Tab label={keymanager.displayName || keymanager.name} value={keymanager.name} disabled={!keymanager.enabled}/>
                        ))}
                        
                    </Tabs>
                    {keyManagers.map(keymanager => (
                        <TabPanel value={selectedTab} index={keymanager.name}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary expandIcon={<Icon>expand_more</Icon>}>
                                    <Typography className={classes.heading} variant='subtitle1'>
                                        {
                                            keys.size > 0 && keys.get(selectedTab) && keys.get(selectedTab).keyType === keyType
                                                ? (
                                                    <FormattedMessage
                                                        defaultMessage='Key Configurations'
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
                                    <div>
                                        <Typography className={classes.heading} variant='subtitle1'>
                                            {
                                                keyType === 'PRODUCTION' ? 'Production ' : 'Sandbox '}
                                                    <FormattedMessage
                                                        defaultMessage='Key and Secret'
                                                            id='Shared.AppsAndKeys.TokenManager.key.and.secret'
                                                    />
                                        </Typography>
                                        <ViewKeys
                                            selectedApp={selectedApp}
                                            selectedTab={selectedTab}
                                            keyType={keyType}
                                            keys={keys}
                                            isKeyJWT={isKeyJWT}
                                            selectedGrantTypes={keyGrantTypes}
                                            isUserOwner={isUserOwner}
                                            hashEnabled={keymanager.enableTokenHashing || hashEnabled}
                                        />
                                    </div>
                                    <KeyConfiguration
                                        keys={keys}
                                        key={key}
                                        selectedApp={selectedApp}
                                        selectedTab={selectedTab}
                                        keyType={keyType}
                                        updateKeyRequest={this.updateKeyRequest}
                                        keyRequest={keyRequest}
                                        isUserOwner={isUserOwner}
                                        isKeysAvailable={keys.size > 0 && keys.get(selectedTab) && keys.get(selectedTab).keyType === keyType}
                                        setGenerateEnabled={this.setGenerateEnabled}
                                        keyManagerConfig={keymanager}
                                    />
                                    <div className={classes.generateWrapper}>
                                        <ScopeValidation
                                            resourcePath={resourcePaths.APPLICATION_GENERATE_KEYS}
                                            resourceMethod={resourceMethods.POST}
                                        >
                                            {!isUserOwner ? (
                                                <>
                                                    <Button
                                                        variant='contained'
                                                        color='primary'
                                                        className={classes.button}
                                                        onClick={
                                                            keys.size > 0 && keys.get(selectedTab) && keys.get(selectedTab).keyType === keyType ? this.updateKeys : this.generateKeys
                                                        }
                                                        disabled={!isUserOwner || isLoading || !keymanager.enableTokenGeneration}
                                                    >
                                                        {keys.size > 0 && keys.get(selectedTab) && keys.get(selectedTab).keyType === keyType ? 'Update keys' : 'Generate Keys'}
                                                        {isLoading && <CircularProgress size={20} />}
                                                    </Button>
                                                    <Typography variant='caption'>
                                                        <FormattedMessage
                                                            defaultMessage='Only owner can generate or update keys'
                                                            id='Shared.AppsAndKeys.TokenManager.key.and.user.owner'
                                                        />
                                                    </Typography>
                                                </>
                                            ) : (
                                                <Button
                                                    variant='contained'
                                                    color='primary'
                                                    className={classes.button}
                                                    onClick={keys.size > 0 && keys.get(selectedTab) && keys.get(selectedTab).keyType === keyType  ? this.updateKeys : this.generateKeys}
                                                    disabled={!generateEnabled || isLoading || !keymanager.enableTokenGeneration}
                                                >
                                                    {keys.size > 0 && keys.get(selectedTab) && keys.get(selectedTab).keyType === keyType ? 'Update' : 'Generate Keys'}
                                                    {isLoading && <CircularProgress size={20} />}
                                                </Button>
                                            )}
                                        </ScopeValidation>
                                    </div>
                                    {
                                        mapExistingAuthApps && !(keys.get(selectedTab).keyType === keyType) && (
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
                                                            <>
                                                                <Button
                                                                    variant='contained'
                                                                    color='primary'
                                                                    className={classes.button}
                                                                    onClick={this.provideOAuthKeySecret(selectedTab, keys.get(selectedTab).keyMappingId)}
                                                                    disabled={!isUserOwner}
                                                                >
                                                                    {
                                                                        keys.size > 0 && keys.get(selectedTab) && keys.get(selectedTab).keyType === keyType
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
                                                            </>
                                                        ) : (
                                                            <Button
                                                                variant='contained'
                                                                color='primary'
                                                                className={classes.button}
                                                                onClick={this.provideOAuthKeySecret(selectedTab, keys.get(selectedTab).keyMappingId)}
                                                            >
                                                                {
                                                                    keys.size > 0 && keys.get(selectedTab) && keys.get(selectedTab).keyType === keyType
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
                                </ExpansionPanelDetails>
                            </ExpansionPanel>
                        </TabPanel>
                    ))}
                </Paper>
            </div>
        );
    }
}
TokenManager.defaultProps = {
    updateSubscriptionData: () => {},
    summary: false,
};
TokenManager.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
    selectedApp: PropTypes.shape({
        tokenType: PropTypes.string.isRequired,
        appId: PropTypes.string,
        value: PropTypes.string,
        owner: PropTypes.string,
        hashEnabled: PropTypes.bool,
    }).isRequired,
    keyType: PropTypes.string.isRequired,
    updateSubscriptionData: PropTypes.func,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
    summary: PropTypes.bool,
};

export default injectIntl(withStyles(styles)(TokenManager));
