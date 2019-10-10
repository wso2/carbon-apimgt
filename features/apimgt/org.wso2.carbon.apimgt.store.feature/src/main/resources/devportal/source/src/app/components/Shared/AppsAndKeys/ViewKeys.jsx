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
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import InputAdornment from '@material-ui/core/InputAdornment';
import FormHelperText from '@material-ui/core/FormHelperText';
import FormControl from '@material-ui/core/FormControl';
import Icon from '@material-ui/core/Icon';
import Grid from '@material-ui/core/Grid';
import CopyToClipboard from 'react-copy-to-clipboard';
import Tooltip from '@material-ui/core/Tooltip';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl } from 'react-intl';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import Loading from '../../Base/Loading/Loading';
import Application from '../../../data/Application';
import Tokens from './Tokens';
import ViewToken from './ViewToken';
import ViewCurl from './ViewCurl';
import TextField from '@material-ui/core/TextField';

const styles = theme => ({
    button: {
        margin: theme.spacing.unit,
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
    },
    copyWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    gridWrapper: {
        paddingTop: theme.spacing.unit * 2,
    },
    iconStyle: {
        cursor: 'grab',
    },
    tokenSection: {
        marginTop: theme.spacing.unit * 2,
        marginBottom: theme.spacing.unit * 2,
    },
    margin: {
        marginRight: theme.spacing.unit * 2,
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
            accessTokenRequest: {
                timeout: 3600,
                scopesSelected: [],
                keyType: '',
            },
            subscriptionScopes: [],
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
                        .map((scope) => { return scope.scopeKey; });
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
     * Handle onClick of get curl
     * */
    handleClickOpenCurl = () => {
        this.setState({ open: true, showCurl: true });
    };

    /**
     * Handle on close of dialog for generating access token and get curl
     * */
    handleClose = () => {
        this.setState({ open: false, showCurl: false, isKeyJWT: false });
    };

    /**
     * Generate access token
     * */
    generateAccessToken = () => {
        const { accessTokenRequest } = this.state;
        this.applicationPromise
            .then(application => application.generateToken(
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
     * @inheritdoc
     */
    render() {
        const {
            notFound, showCS, showToken, showCurl, secretCopied, tokenCopied, keyCopied, open,
            token, tokenScopes, tokenValidityTime, accessTokenRequest, subscriptionScopes, isKeyJWT, tokenResponse,
        } = this.state;
        const {
            intl, keyType, classes, fullScreen, keys, selectedApp: { tokenType },
        } = this.props;

        if (notFound) {
            return <ResourceNotFound />;
        }
        if (!keys) {
            return <Loading />;
        }

        const csCkKeys = keys.get(keyType);
        const consumerKey = csCkKeys && csCkKeys.consumerKey;
        const consumerSecret = csCkKeys && csCkKeys.consumerSecret;
        let accessToken;
        let accessTokenScopes;
        let validityPeriod;
        let tokenDetails;

        if (token) {
            accessToken = token;
            accessTokenScopes = tokenScopes;
            validityPeriod = tokenValidityTime;
        } else if (keys.get(keyType) && keys.get(keyType).token) {
            ({ accessToken } = keys.get(keyType).token);
            accessTokenScopes = keys.get(keyType).token.tokenScopes;
            validityPeriod = keys.get(keyType).token.validityTime;
            tokenDetails = keys.get(keyType).token;
        }

        return consumerKey ? (
            <React.Fragment>
                <div className={classes.inputWrapper}>
                    <Grid container spacing={3} className={classes.gridWrapper}>
                        <Grid item xs={6}>
                            <div className={classes.copyWrapper}>
                                <TextField
                                    id='consumer-key'
                                    value={consumerKey}
                                    margin='normal'
                                    label={
                                        <FormattedMessage
                                            id='Shared.AppsAndKeys.ViewKeys.consumer.key'
                                            defaultMessage='Consumer Key'
                                        />
                                    }
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
                                                                id: 'Shared.AppsAndKeys.ViewKeys.copied',
                                                            })
                                                    }
                                                    placement='right'
                                                    className={classes.iconStyle}
                                                >
                                                    <CopyToClipboard
                                                        text={consumerKey}
                                                        onCopy={() => this.onCopy('keyCopied')}
                                                    >
                                                        <Icon
                                                            color='secondary'
                                                        >description
                                                        </Icon>
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
                                <TextField
                                    id='consumer-secret'
                                    label={
                                        <FormattedMessage
                                            id='Shared.AppsAndKeys.ViewKeys.consumer.secret'
                                            defaultMessage='Consumer Secret'
                                        />
                                    }
                                    type={showCS || !consumerSecret ? 'text' : 'password'}
                                    value={consumerSecret}
                                    margin='normal'
                                    fullWidth
                                    variant='outlined'
                                    InputProps={{
                                        readOnly: true,
                                        endAdornment: (
                                            <InputAdornment position='end'>
                                                <IconButton
                                                    classes=''
                                                    onClick={() => this.handleShowHidden('showCS')}
                                                    onMouseDown={this.handleMouseDownGeneric}
                                                >
                                                    {showCS ? <Icon>visibility_off</Icon> : <Icon>visibility</Icon>}
                                                </IconButton>
                                                <Tooltip
                                                    title={secretCopied ? 'Copied' : 'Copy to clipboard'}
                                                    placement='right'
                                                    className={classes.iconStyle}
                                                >
                                                    <CopyToClipboard
                                                        text={consumerSecret}
                                                        onCopy={() => this.onCopy('secretCopied')}
                                                    >
                                                        <Icon
                                                            color='secondary'
                                                        >description
                                                        </Icon>
                                                    </CopyToClipboard>
                                                </Tooltip>
                                            </InputAdornment>
                                        ),
                                    }}
                                />
                            </div>
                            <FormControl>
                                <FormHelperText id='consumer-secret-helper-text'>
                                    <FormattedMessage
                                        id='Shared.AppsAndKeys.ViewKeys.consumer.secret.of.application'
                                        defaultMessage='Consumer Secret of the application'
                                    />
                                </FormHelperText>
                            </FormControl>
                        </Grid>
                        {(accessToken && tokenType !== 'JWT') && (
                            <Grid item xs={6}>
                                <InputLabel htmlFor='adornment-amount'>
                                    <FormattedMessage
                                        id='Shared.AppsAndKeys.ViewKeys.access.token'
                                        defaultMessage='Access Token'
                                    />
                                </InputLabel>
                                <div className={classes.copyWrapper}>
                                    <Input
                                        inputProps={{ readOnly: true }}
                                        id='access-token'
                                        value={accessToken}
                                        margin='normal'
                                        fullWidth
                                    />
                                    <Tooltip title={tokenCopied ? 'Copied' : 'Copy to clipboard'} placement='right'>
                                        <CopyToClipboard text={accessToken} onCopy={() => this.onCopy('tokenCopied')}>
                                            <Icon color='secondary'>file_copy</Icon>
                                        </CopyToClipboard>
                                    </Tooltip>
                                </div>
                                <FormControl>
                                    <FormHelperText id='access-token-helper-text'>
                                        {`Above token has a validity period of ${validityPeriod} seconds.
                                            And the token has (${accessTokenScopes.join(', ')}) scopes.`}
                                    </FormHelperText>
                                </FormControl>
                            </Grid>
                        )}
                        <Grid item xs={12}>
                            <Dialog
                                fullScreen={fullScreen}
                                open={open || isKeyJWT}
                                onClose={this.handleClose}
                                aria-labelledby='responsive-dialog-title'
                            >
                                <DialogTitle id='responsive-dialog-title'>
                                    {showCurl ? 'Get CURL to Generate Access Token' : 'Generate Access Token'}
                                </DialogTitle>
                                <DialogContent>
                                    {!showCurl && !isKeyJWT && (
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
                                            <ViewCurl keys={{ consumerKey, consumerSecret }} />
                                        </DialogContentText>
                                    )}
                                    {(isKeyJWT && tokenDetails) && (
                                        <DialogContentText>
                                            <ViewToken token={{ ...tokenDetails, isOauth: true }} />
                                        </DialogContentText>
                                    )}
                                </DialogContent>
                                <DialogActions>
                                    {!showToken && !showCurl && !isKeyJWT && (
                                        <Button onClick={this.generateAccessToken} color='primary'>
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
                            <div className={classes.tokenSection}>
                                <Button
                                    variant='outlined'
                                    size='small'
                                    color='primary'
                                    className={classes.margin}
                                    onClick={this.handleClickOpen}
                                >
                                    <FormattedMessage
                                        id='Shared.AppsAndKeys.ViewKeys.generate.access.token'
                                        defaultMessage='Generate Access Token'
                                    />
                                </Button>
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
                        </Grid>
                    </Grid>
                </div>
            </React.Fragment>
        ) : (
            <React.Fragment>
                <Typography variant='caption' gutterBottom >
                    {keyType === 'PRODUCTION' ? 'Production ' : 'Sandbox '}
                    <FormattedMessage
                        id='Shared.AppsAndKeys.ViewKeys.key.secret.title'
                        defaultMessage='Key and Secret is not generated for this application'
                    />
                </Typography>
            </React.Fragment>
        );
    }
}

ViewKeys.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    fullScreen: PropTypes.bool.isRequired,
    isKeyJWT: PropTypes.bool.isRequired,
};

export default injectIntl(withStyles(styles)(ViewKeys));
