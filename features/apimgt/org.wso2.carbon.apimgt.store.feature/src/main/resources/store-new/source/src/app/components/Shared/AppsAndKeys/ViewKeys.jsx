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
import Visibility from '@material-ui/icons/Visibility';
import VisibilityOff from '@material-ui/icons/VisibilityOff';
import Grid from '@material-ui/core/Grid';
import CopyToClipboard from 'react-copy-to-clipboard';
import Tooltip from '@material-ui/core/Tooltip';
import FileCopy from '@material-ui/icons/FileCopy';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl, } from 'react-intl';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import Loading from '../../Base/Loading/Loading';
import Application from '../../../data/Application';
import Tokens from './Tokens';
import ViewToken from './ViewToken';
import ViewCurl from './ViewCurl';

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
    },
    copyWrapper: {
        display: 'flex',
        flexDirection: 'row',
    },
    gridWrapper: {
        paddingTop: theme.spacing.unit * 2,
    },
    tokenSection: {
        marginTop: theme.spacing.unit * 2,
        marginBottom: theme.spacing.unit * 2,
    },
    margin: {
        marginRight: theme.spacing.unit * 2,
    },
    noKeyMessageBox: {
        padding: theme.spacing.unit * 2,
    },
});

/**
 * Class used to displays in key generation UI
 */
class ViewKeys extends React.Component {
    state = {
        showCS: false,
        open: false,
        showToken: false,
        showCurl: false,
    };

    /**
     * Handle onClick of the copy icon
     * */
    onCopy = (name) => {
        this.setState({
            [name]: true,
        });
        const that = this;
        const elementName = name;
        const caller = function () {
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
        this.setState({ open: false, showCurl: false });
    };

    /**
     * Generate access token
     * */
    generateAccessToken = () => {
        const that = this;
        const promiseTokens = this.tokens.generateToken();
        const { intl } = this.props;
        promiseTokens
            .then((response) => {
                console.log(
                    intl.formatMessage({
                        defaultMessage: 'token generated successfully : ',
                        id: 'Shared.AppsAndKeys.ViewKeys.success',
                    }),
                    response,
                );
                that.token = response;
                that.setState({
                    showToken: true,
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
            notFound,
            showCS,
            showToken,
            showCurl,
            secretCopied,
            tokenCopied,
            keyCopied,
            open,
            token,
            tokenScopes,
            tokenValidityTime,
        } = this.state;
        const { intl } = this.props;
        const {
            keyType, classes, fullScreen, selectedApp, keys,
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

        if (token) {
            accessToken = token;
            accessTokenScopes = tokenScopes;
            validityPeriod = tokenValidityTime;
        } else if (keys.get(keyType) && keys.get(keyType).token) {
            ({ accessToken } = keys.get(keyType).token);
            accessTokenScopes = keys.get(keyType).token.tokenScopes;
            validityPeriod = keys.get(keyType).token.validityTime;
        }

        return consumerKey ? (
            <React.Fragment>
                <div className={classes.inputWrapper}>
                    <Grid container spacing={24} className={classes.gridWrapper}>
                        <Grid item xs={6}>
                            <InputLabel htmlFor='adornment-amount'>
                                <FormattedMessage
                                    id='Shared.AppsAndKeys.ViewKeys.consumer.key'
                                    defaultMessage='Consumer Key'
                                />
                            </InputLabel>
                            <div className={classes.copyWrapper}>
                                <Input
                                    inputProps={{ readOnly: true }}
                                    id='consumer-key'
                                    value={consumerKey}
                                    margin='normal'
                                    fullWidth
                                />
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
                                >
                                    <CopyToClipboard text={consumerKey} onCopy={() => this.onCopy('keyCopied')}>
                                        <FileCopy color='secondary' />
                                    </CopyToClipboard>
                                </Tooltip>
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
                            <InputLabel htmlFor='adornment-amount'>Consumer Secret</InputLabel>
                            <div className={classes.copyWrapper}>
                                <Input
                                    inputProps={{ readOnly: true }}
                                    id='consumer-secret'
                                    label='Consumer Secret'
                                    type={showCS || !consumerSecret ? 'text' : 'password'}
                                    value={consumerSecret}
                                    fullWidth
                                    endAdornment={(
                                        <InputAdornment position='end'>
                                            <IconButton
                                                classes=''
                                                onClick={() => this.handleShowHidden('showCS')}
                                                onMouseDown={this.handleMouseDownGeneric}
                                            >
                                                {showCS ? <VisibilityOff /> : <Visibility />}
                                            </IconButton>
                                        </InputAdornment>
                                    )}
                                />
                                <Tooltip title={secretCopied ? 'Copied' : 'Copy to clipboard'} placement='right'>
                                    <CopyToClipboard text={consumerSecret} onCopy={() => this.onCopy('secretCopied')}>
                                        <FileCopy color='secondary' />
                                    </CopyToClipboard>
                                </Tooltip>
                            </div>
                            <FormControl>
                                <FormHelperText id='consumer-secret-helper-text'>
                                    <FormattedMessage
                                        id='Shared.AppsAndKeys.ViewKeys.consumer.secret.title'
                                        defaultMessage='Consumer Secret of the application'
                                    />
                                </FormHelperText>
                            </FormControl>
                        </Grid>
                        {accessToken && (
                            <Grid item xs={6}>
                                <InputLabel htmlFor='adornment-amount'>
                                    <FormattedMessage
                                        id='Shared.AppsAndKeys.ViewKeys.consumer.secret.title'
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
                                            <FileCopy color='secondary' />
                                        </CopyToClipboard>
                                    </Tooltip>
                                </div>
                                <FormControl>
                                    <FormHelperText id='access-token-helper-text'>
                                        {`Above token has a validity period of ${validityPeriod}seconds.
                                            And the token has (${accessTokenScopes.join(', ')}) scopes.`}
                                    </FormHelperText>
                                </FormControl>
                            </Grid>
                        )}
                        <Grid item xs={12}>
                            <Dialog
                                fullScreen={fullScreen}
                                open={open}
                                onClose={this.handleClose}
                                aria-labelledby='responsive-dialog-title'
                            >
                                <DialogTitle id='responsive-dialog-title'>
                                    {showCurl ? 'Get CURL to Generate Access Token' : 'Generate Access Token'}
                                </DialogTitle>
                                <DialogContent>
                                    {!showCurl && (
                                        <DialogContentText>
                                            {!showToken && (
                                                <Tokens
                                                    innerRef={(node) => {
                                                        this.tokens = node;
                                                    }}
                                                    selectedApp={selectedApp}
                                                    keyType={keyType}
                                                />
                                            )}
                                            {showToken && <ViewToken token={this.token} />}
                                        </DialogContentText>
                                    )}
                                    {showCurl && (
                                        <DialogContentText>
                                            <ViewCurl keys={{ consumerKey, consumerSecret }} />
                                        </DialogContentText>
                                    )}
                                </DialogContent>
                                <DialogActions>
                                    {!showToken && !showCurl && (
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
                <Typography variant='caption' gutterBottom className={classes.noKeyMessageBox}>
                    {keyType}
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
    classes: PropTypes.object,
    fullScreen: PropTypes.bool.isRequired,
};

export default injectIntl(withStyles(styles)(ViewKeys));
