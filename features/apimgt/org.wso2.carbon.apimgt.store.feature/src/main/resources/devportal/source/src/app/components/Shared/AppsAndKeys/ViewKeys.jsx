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
import IconButton from '@material-ui/core/IconButton';
import Button from '@material-ui/core/Button';
import Alert1 from 'AppComponents/Shared/Alert';
import InputAdornment from '@material-ui/core/InputAdornment';
import FormHelperText from '@material-ui/core/FormHelperText';
import FormControl from '@material-ui/core/FormControl';
import Icon from '@material-ui/core/Icon';
import Grid from '@material-ui/core/Grid';
import CopyToClipboard from 'react-copy-to-clipboard';
import CircularProgress from '@material-ui/core/CircularProgress';
import Tooltip from '@material-ui/core/Tooltip';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import Alert from 'AppComponents/Shared/Alert';
import { FormattedMessage, injectIntl } from 'react-intl';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import Loading from '../../Base/Loading/Loading';
import Application from '../../../data/Application';
import Tokens from './Tokens';
import ViewToken from './ViewToken';
import ViewSecret from './ViewSecret';
import ViewCurl from './ViewCurl';

const styles = (theme) => ({
    button: {
        margin: theme.spacing(3),
        color: theme.palette.getContrastText(theme.palette.background.default),
        display: 'flex',
        alignItems: 'center',
        fontSize: '11px',
        cursor: 'pointer',
        '& span': {
            paddingLeft: 6,
            display: 'inline-block',
        },
    },
    inputWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        '& span, & h5, & label, & td, & li, & div, & input': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        },
    },
    copyWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    tokenSection: {
        marginTop: 0,
        marginBottom: theme.spacing(0.5),
    },
    margin: {
        marginRight: theme.spacing(2),
    },
    dialogWrapper: {
        '& label,& h5, & label, & td, & li, & input, & h2, & p.MuiTypography-root,& p.MuiFormHelperText-root': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        },
    },
    iconButton: {
        padding: '0 0 0 10px',
        '& .material-icons': {
            fontSize: 16,
        },
    },
});

/**
 * Class used to displays in key generation UI
 */
class ViewKeys extends React.Component {
    /**
     * @param {*} props properties
     */
    constructor(props) {
        super(props);
        const { selectedApp } = this.props;
        let appId;
        if (selectedApp) {
            appId = selectedApp.appId || selectedApp.value;
        }
        this.applicationPromise = Application.get(appId);
        this.state = {
            showCS: false,
            open: false,
            showToken: false,
            showCurl: false,
            showSecretGen: false,
            accessTokenRequest: {
                timeout: 3600,
                scopesSelected: [],
                keyType: '',
            },
            subscriptionScopes: [],
            isUpdating: false,
        };
    }

    /**
     * Fetch Application object by ID coming from URL path params and fetch related keys to display
     */
    componentDidMount() {
        const { accessTokenRequest } = this.state;
        const { keyType } = this.props;
        this.applicationPromise
            .then((application) => {
                application.getKeys().then(() => {
                    const newRequest = { ...accessTokenRequest, keyType };
                    const subscriptionScopes = application.subscriptionScopes
                        .map((scope) => { return scope.key; });
                    this.setState({ accessTokenRequest: newRequest, subscriptionScopes });
                });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    /**
     * Adding this here becasue it is not possible to add in the render method becasue isKeyJWT in state is used
     * to close the dialog box and render method will casue this to be always true and cannot close the box.
     * Rule is ignored becasue according to react docs its ok to setstate as long as we are checking a condition
     * This is an ani pattern to be fixed later.
     *  wso2/product-apim#5293
     * https://reactjs.org/docs/react-component.html#componentdidupdate
     * @param {*} prevProps previous props
     * @memberof ViewKeys
     */
    componentDidUpdate(prevProps) {
        const { isKeyJWT } = this.props;
        if (isKeyJWT && !prevProps.isKeyJWT) {
            // eslint-disable-next-line react/no-did-update-set-state
            this.setState({ isKeyJWT: true });
        }
    }

    /**
     * Set accesstoken request in state
     * @param {*} accessTokenRequest access token request object
     * @memberof ViewKeys
     */
    updateAccessTokenRequest = (accessTokenRequest) => {
        this.setState({ accessTokenRequest });
    }

    /**
     * Handle onClick of the copy icon
     * @param {*} name name of what is copied
     * */
    onCopy = (name) => {
        this.setState({
            [name]: true,
        });
        const that = this;
        const elementName = name;
        const caller = () => {
            that.setState({
                [elementName]: false,
            });
        };
        setTimeout(caller, 4000);
    };

    /**
     * Handle onClick of the show consumer secret icon
     * @param data
     * */
    handleShowHidden = (data) => {
        this.setState({ [data]: !this.state[data] });
    };

    /**
     * Avoid conflict with `onClick`
     * @param event
     */
    handleMouseDownGeneric = (event) => {
        event.preventDefault();
    };

    /**
     * Handle onCLick of generate access token
     * */
    handleClickOpen = () => {
        this.setState({ open: true, showToken: false });
    };

    /**
     * Handle onCLick of regenerate consumer secret
     * */
    handleSecretRegenerate = (consumerKey, keyType, keyMappingId, selectedTab) => {
        this.applicationPromise
            .then((application) => application.regenerateSecret(consumerKey, keyType, keyMappingId, selectedTab))
            .then((response) => {
                console.log('consumer secret regenerated successfully ' + response);
                this.setState({
                    open: true,
                    showSecretGen: true,
                    secretGenResponse: response,
                });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    };

    /**
     * Handle onClick of get curl
     * */
    handleClickOpenCurl = () => {
        this.setState({ open: true, showCurl: true });
    };

    /**
     * Handle on close of dialog for generating access token and get curl
     * */
    handleClose = () => {
        this.setState({
            open: false, showCurl: false, isKeyJWT: false, showSecretGen: false,
        });
    };

    /**
     * Generate access token
     * */
    generateAccessToken = () => {
        const { accessTokenRequest, isUpdating } = this.state;
        const { selectedTab, intl } = this.props;
        this.setState({ isUpdating: true });
        this.applicationPromise
            .then((application) => application.generateToken(
                selectedTab,
                accessTokenRequest.keyType,
                accessTokenRequest.timeout,
                accessTokenRequest.scopesSelected,
            ))
            .then((response) => {
                console.log('token generated successfully ' + response);
                this.setState({
                    showToken: true,
                    tokenResponse: response,
                    token: response.accessToken,
                    tokenScopes: response.tokenScopes,
                    tokenValidityTime: response.validityTime,
                });
                this.setState({ isUpdating: false });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 400) {
                     Alert1.error(error.description ||
                         intl.formatMessage({
                             id: 'Shared.AppsAndKeys.TokenManager.key.generate.bad.request.error',
                              defaultMessage: 'Error occurred when generating Access Token',
                         })
                     );
                }
                this.setState({ isUpdating: false });
                const { response } = error;
                if (response && response.body) {
                    Alert.error(response.body.message);
                }
            });
    };

    viewKeyAndSecret = (consumerKey, consumerSecret, keyMappingId, selectedTab, isUserOwner) => {
        const { classes, intl, selectedApp: { hashEnabled }, keyType } = this.props;
        const { keyCopied, secretCopied, showCS } = this.state;
        return (
            <>
                <Grid item xs={6}>
                    <div className={classes.copyWrapper}>
                        <TextField
                            id='consumer-key'
                            value={consumerKey}
                            margin='dense'
                            label={(
                                <FormattedMessage
                                    id='Shared.AppsAndKeys.ViewKeys.consumer.key'
                                    defaultMessage='Consumer Key'
                                />
                            )}
                            fullWidth
                            variant='outlined'
                            InputProps={{
                                readOnly: true,
                                endAdornment: (
                                    <InputAdornment position='end'>
                                        <Tooltip
                                            title={
                                                keyCopied
                                                    ? intl.formatMessage({
                                                        defaultMessage: 'Copied',
                                                        id: 'Shared.AppsAndKeys.ViewKeys.copied',
                                                    })
                                                    : intl.formatMessage({
                                                        defaultMessage: 'Copy to clipboard',
                                                        id: 'Shared.AppsAndKeys.ViewKeys.copy.to',
                                                    })
                                            }
                                            placement='right'
                                        >
                                            <CopyToClipboard
                                                text={consumerKey}
                                                onCopy={() => this.onCopy('keyCopied')}
                                                classes={{ root: classes.iconButton }}
                                            >
                                                <IconButton aria-label='Copy to clipboard' classes={{ root: classes.iconButton }}>
                                                    <Icon color='secondary'>
                                                        file_copy
                                                    </Icon>
                                                </IconButton>
                                            </CopyToClipboard>
                                        </Tooltip>
                                    </InputAdornment>
                                ),
                            }}
                        />
                    </div>
                    <FormControl>
                        <FormHelperText id='consumer-key-helper-text'>
                            <FormattedMessage
                                id='Shared.AppsAndKeys.ViewKeys.consumer.key.title'
                                defaultMessage='Consumer Key of the application'
                            />
                        </FormHelperText>
                    </FormControl>
                </Grid>
                <Grid item xs={6}>
                    <div className={classes.copyWrapper}>
                        {!hashEnabled ? (
                            <TextField
                                id='consumer-secret'
                                label={(
                                    <FormattedMessage
                                        id='Shared.AppsAndKeys.ViewKeys.consumer.secret'
                                        defaultMessage='Consumer Secret'
                                    />
                                )}
                                type={showCS || !consumerSecret ? 'text' : 'password'}
                                value={consumerSecret}
                                margin='dense'
                                fullWidth
                                variant='outlined'
                                InputProps={{
                                    readOnly: true,
                                    endAdornment: (
                                        <InputAdornment position='end'>
                                            <IconButton
                                                classes={{ root: classes.iconButton }}
                                                onClick={() => this.handleShowHidden('showCS')}
                                                onMouseDown={this.handleMouseDownGeneric}
                                            >
                                                {showCS ? <Icon>visibility_off</Icon> : <Icon>visibility</Icon>}
                                            </IconButton>
                                            <Tooltip
                                                title={secretCopied ? 'Copied' : 'Copy to clipboard'}
                                                placement='right'
                                            >
                                                <CopyToClipboard
                                                    text={consumerSecret}
                                                    onCopy={() => this.onCopy('secretCopied')}
                                                    classes={{ root: classes.iconButton }}
                                                >
                                                    <IconButton aria-label='Copy to clipboard' classes={{ root: classes.iconButton }}>
                                                        <Icon color='secondary'>file_copy</Icon>
                                                    </IconButton>
                                                </CopyToClipboard>
                                            </Tooltip>
                                        </InputAdornment>
                                    ),
                                }}
                            />
                        ) : (
                                <Button
                                    variant='contained'
                                    color='primary'
                                    className={classes.button}
                                    onClick={() => this.handleSecretRegenerate(consumerKey, keyType, keyMappingId, selectedTab)}
                                    disabled={!isUserOwner}
                                >
                                    <FormattedMessage
                                        defaultMessage='Regenerate Consumer Secret'
                                        id='Shared.AppsAndKeys.ViewKeys.consumer.secret.button.regenerate'
                                    />
                                </Button>
                            )}
                    </div>
                    {!hashEnabled && (
                        <FormControl>
                            <FormHelperText id='consumer-secret-helper-text'>
                                <FormattedMessage
                                    id='Shared.AppsAndKeys.ViewKeys.consumer.secret.of.application'
                                    defaultMessage='Consumer Secret of the application'
                                />
                            </FormHelperText>
                        </FormControl>
                    )}
                </Grid>
            </>
        );
    }

    /**
     * @inheritdoc
     */
    render() {
        const {
            notFound, showToken, showCurl, showSecretGen, tokenCopied, open,
            token, tokenScopes, tokenValidityTime, accessTokenRequest, subscriptionScopes,
            isKeyJWT, tokenResponse, secretGenResponse, isUpdating,
        } = this.state;
        const {
            intl, keyType, classes, fullScreen, keys, selectedApp: { tokenType }, selectedGrantTypes, isUserOwner, summary,
            selectedTab, hashEnabled, keyManagerConfig
        } = this.props;

        if (notFound) {
            return <ResourceNotFound />;
        }
        if (!keys) {
            return <Loading />;
        }

        const csCkKeys = keys.size > 0 && keys.get(selectedTab) && (keys.get(selectedTab).keyType === keyType) && keys.get(selectedTab);
        const consumerKey = csCkKeys && csCkKeys.consumerKey;
        const consumerSecret = csCkKeys && csCkKeys.consumerSecret;
        const keyMappingId = csCkKeys && csCkKeys.keyMappingId;

        let accessToken;
        let accessTokenScopes;
        let validityPeriod;
        let tokenDetails;

        if (token) {
            accessToken = token;
            accessTokenScopes = tokenScopes;
            validityPeriod = tokenValidityTime;
        } else if (keys.get(selectedTab) && keys.get(selectedTab).keyType === keyType && keys.get(selectedTab).token) {
            ({ accessToken } = keys.get(selectedTab).token);
            accessTokenScopes = keys.get(selectedTab).token.tokenScopes;
            validityPeriod = keys.get(selectedTab).token.validityTime;
            tokenDetails = keys.get(selectedTab).token;
        }

        let dialogHead = 'Undefined';
        if (showCurl) {
            dialogHead = 'Get CURL to Generate Access Token';
        } else if (showSecretGen) {
            dialogHead = 'Generate Consumer Secret';
        } else {
            dialogHead = 'Generate Access Token';
        }
        if (!consumerKey) {
            return (
                <Typography variant='caption' gutterBottom>
                    {keyType === 'PRODUCTION' ? 'Production ' : 'Sandbox '}
                    <FormattedMessage
                        id='Shared.AppsAndKeys.ViewKeys.key.secret.title'
                        defaultMessage='Key and Secret is not generated for this application'
                    />
                </Typography>
            );
        }
        if (summary) {
            return (
                <Grid container spacing={3}>
                    {this.viewKeyAndSecret(consumerKey, consumerSecret, keyMappingId, selectedTab, isUserOwner)}
                </Grid>
            );
        }

        // Get the grant types for the generated keys
        const { supportedGrantTypes: supportedGrantTypesUnchanged } = keys.get(selectedTab);

        return consumerKey && (
            <div className={classes.inputWrapper}>
                <Grid container spacing={3}>
                    {this.viewKeyAndSecret(consumerKey, consumerSecret, keyMappingId, selectedTab, isUserOwner)}
                    <Grid item xs={12}>
                        <Dialog
                            fullScreen={fullScreen}
                            open={(open || (isKeyJWT && tokenDetails))}
                            onClose={this.handleClose}
                            aria-labelledby='responsive-dialog-title'
                            className={classes.dialogWrapper}
                        >
                            <DialogTitle id='responsive-dialog-title'>
                                {dialogHead}
                            </DialogTitle>
                            <DialogContent>
                                {!showCurl && !isKeyJWT && !showSecretGen && (
                                    <DialogContentText>
                                        {!showToken && (
                                            <Tokens
                                                updateAccessTokenRequest={this.updateAccessTokenRequest}
                                                accessTokenRequest={accessTokenRequest}
                                                subscriptionScopes={subscriptionScopes}
                                            />
                                        )}
                                        {showToken && <ViewToken token={{ ...tokenResponse, isOauth: true }} />}
                                    </DialogContentText>
                                )}
                                {showCurl && (
                                    <DialogContentText>
                                        <ViewCurl 
                                            keys={{ consumerKey, consumerSecret }} 
                                            keyType={keyType} 
                                            keyManagerConfig={keyManagerConfig} 
                                        />
                                    </DialogContentText>
                                )}
                                {showSecretGen && (
                                    <DialogContentText>
                                        <ViewSecret secret={{ ...secretGenResponse }} />
                                    </DialogContentText>
                                )}
                                {(isKeyJWT && tokenDetails && hashEnabled) && (
                                    <DialogContentText>
                                        <ViewToken token={{ ...tokenDetails, isOauth: true }} consumerSecret={consumerSecret} />
                                    </DialogContentText>
                                )}
                                {(isKeyJWT && tokenDetails && !hashEnabled) && (
                                    <DialogContentText>
                                        <ViewToken token={{ ...tokenDetails, isOauth: true }} />
                                    </DialogContentText>
                                )}
                            </DialogContent>
                            <DialogActions>
                                {isUpdating && <CircularProgress size={24} />}
                                {!showToken && !showCurl && !isKeyJWT && !showSecretGen && (
                                    <Button onClick={this.generateAccessToken} color='primary' disabled={isUpdating}>
                                        <FormattedMessage
                                            id='Shared.AppsAndKeys.ViewKeys.consumer.generate.btn'
                                            defaultMessage='Generate'
                                        />
                                    </Button>
                                )}
                                <Button onClick={this.handleClose} color='primary' autoFocus>
                                    <FormattedMessage
                                        id='Shared.AppsAndKeys.ViewKeys.consumer.close.btn'
                                        defaultMessage='Close'
                                    />
                                </Button>
                            </DialogActions>
                        </Dialog>
                        {!hashEnabled && (
                            <div className={classes.tokenSection}>
                                {keyManagerConfig.enableTokenGeneration &&
                                    supportedGrantTypesUnchanged.find(a => a.includes('client_credentials')) &&
                                    (<Button
                                        variant='outlined'
                                        size='small'
                                        color='primary'
                                        className={classes.margin}
                                        onClick={this.handleClickOpen}
                                        disabled={!supportedGrantTypesUnchanged.includes('client_credentials')}
                                    >
                                        <FormattedMessage
                                            id='Shared.AppsAndKeys.ViewKeys.generate.access.token'
                                            defaultMessage='Generate Access Token'
                                        />
                                    </Button>)}
                                <Button
                                    variant='outlined'
                                    size='small'
                                    color='primary'
                                    className={classes.margin}
                                    onClick={this.handleClickOpenCurl}
                                >
                                    <FormattedMessage
                                        id='Shared.AppsAndKeys.ViewKeys.curl.to.generate'
                                        defaultMessage='CURL to Generate Access Token'
                                    />
                                </Button>
                            </div>
                        )}
                        {!supportedGrantTypesUnchanged.includes('client_credentials') && !hashEnabled && (
                            <Typography variant='caption' gutterBottom>
                                <FormattedMessage
                                    id='Shared.AppsAndKeys.ViewKeys.client.enable.client.credentials'
                                    defaultMessage={'Enable Client Credentials grant '
                                        + 'type to generate test access tokens'}
                                />
                            </Typography>
                        )}
                    </Grid>
                </Grid>
            </div>
        )
    }
}
ViewKeys.defaultProps = {
    fullScreen: false,
    summary: false,
};
ViewKeys.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    fullScreen: PropTypes.bool,
    isKeyJWT: PropTypes.bool.isRequired,
    isUserOwner: PropTypes.bool.isRequired,
    summary: PropTypes.bool,
};

export default injectIntl(withStyles(styles)(ViewKeys));
