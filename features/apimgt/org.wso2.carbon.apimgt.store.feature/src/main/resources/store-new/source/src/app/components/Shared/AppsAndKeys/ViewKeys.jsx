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
import withMobileDialog from '@material-ui/core/withMobileDialog';
import Typography from '@material-ui/core/Typography';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import Loading from '../../Base/Loading/Loading';
import Application from '../../../data/Application';
import Tokens from '../../Shared/AppsAndKeys/Tokens';
import ViewToken from '../../Shared/AppsAndKeys/ViewToken';
import ViewCurl from '../../Shared/AppsAndKeys/ViewCurl';

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

class ViewKeys extends React.Component {
    state = {
        showCS: false,
        open: false,
        showToken: false,
        showCurl: false,
    };

    handleClickToken() {
        const { application } = this.state;
        const keys = application.keys.get(this.key_type) || {
            supportedGrantTypes: ['client_credentials'],
        };
        if (!keys.callbackUrl) {
            keys.callbackUrl = 'https://wso2.am.com';
        }
        application
            .generateKeys(this.key_type, keys.supportedGrantTypes, keys.callbackUrl)
            .then(() => application.generateToken(this.key_type).then(() => this.setState({ application })))
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    handleUpdateToken() {
        const { application } = this.state;
        const keys = application.keys.get(this.key_type);
        application
            .updateKeys(keys.tokenType, this.key_type, keys.supportedGrantTypes, keys.callbackUrl, keys.consumerKey, keys.consumerSecret)
            .then(() => this.setState({ application }))
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    /**
     * Because application access tokens are not coming with /keys or /application API calls,
     * Fetch access token value upon user request
     * @returns {boolean} If no application object found in state object
     */
    handleShowToken() {
        if (!this.state.application) {
            console.warn('No Application found!');
            return false;
        }
        const promised_tokens = this.state.application.generateToken(this.props.key_type);
        promised_tokens.then(token => this.setState({ showAT: true }));
    }

    handleTextChange(event) {
        const { application, key } = this.state;
        const { currentTarget } = event;
        const keys = application.keys.get(this.props.key_type) || {
            supportedGrantTypes: ['client_credentials'],
            keyType: this.props.key_type,
        };
        keys.callbackUrl = currentTarget.value;
        application.keys.set(this.key_type, keys);
        this.setState({ application });
    }

    handleCheckboxChange(event) {
        const { application } = this.state;
        const { currentTarget } = event;
        const keys = application.keys.get(this.props.key_type) || {
            supportedGrantTypes: ['client_credentials'],
            keyType: this.key_type,
        };
        let index;

        if (currentTarget.checked) {
            keys.supportedGrantTypes.push(currentTarget.id);
        } else {
            index = keys.supportedGrantTypes.indexOf(currentTarget.id);
            keys.supportedGrantTypes.splice(index, 1);
        }
        application.keys.set(this.key_type, keys);
        // update the state with the new array of options
        this.setState({ application });
    }

    handleShowCS = () => {
        this.setState({ showCS: !this.state.showCS });
    };

    /**
     * Avoid conflict with `onClick`
     * @param event
     */
    handleMouseDownGeneric = (event) => {
        event.preventDefault();
    };
    updateUI = () => {
        const promised_app = Application.get(this.props.selectedApp.appId);
        promised_app
            .then((application) => {
                application.getKeys().then(() => {
                    this.setState({ application });
                });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }
    /**
     * Fetch Application object by ID coming from URL path params and fetch related keys to display
     */
    componentDidMount() {
        this.updateUI();    
    }

    handleShowCS = () => {
        this.setState({ showCS: !this.state.showCS });
    };

    onCopy = name => (event) => {
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

    handleClickOpen = () => {
        this.setState({ open: true });
        this.setState({
            showToken: false,
        });
    };

    handleClickOpenCurl = () => {
        this.setState({ open: true });
        this.setState({
            showCurl: true,
        });
    };

    handleClose = () => {
        this.setState({ open: false, showCurl: false });
    };

    generateAccessToken = () => {
        const that = this;
        const promisseTokens = this.tokens.generateToken();
        promisseTokens
            .then(
                (response) => {
                    console.log('token generated successfully : ', response);
                    that.token = response;
                    that.setState({
                        showToken: true,
                    });
                },
                // () => application.generateToken(this.key_type).then(() => this.setState({ application: application }))
            )
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    };

    render() {
        const {
            notFound, showCS, showToken, showCurl,
        } = this.state;
        const {
            keyType, classes, fullScreen, selectedApp,
        } = this.props;
        if (notFound) {
            return <ResourceNotFound />;
        }
        if (!this.state.application) {
            return <Loading />;
        }
        const cs_ck_keys = this.state.application.keys.get(keyType);
        const consumerKey = cs_ck_keys && cs_ck_keys.consumerKey;
        const consumerSecret = cs_ck_keys && cs_ck_keys.consumerSecret;
        return consumerKey ? (
            <React.Fragment>
                <div className={classes.inputWrapper}>
                    <Grid container spacing={24}>
                        <Grid item xs={6}>
                            <InputLabel htmlFor='adornment-amount'>Consumer Key</InputLabel>
                            <div className={classes.copyWrapper}>
                                <Input inputProps={{ readonly: true }} id='consumerKey' value={consumerKey || 'Keys are not generated yet. Click the Generate token button to generate the keys.'} helperText='Consumer Key of the application' margin='normal' fullWidth />
                                <Tooltip title={this.state.keyCopied ? 'Copied' : 'Copy to clipboard'} placement='right'>
                                    <CopyToClipboard text={consumerKey || 'Keys are not generated yet.'} onCopy={this.onCopy('keyCopied')}>
                                        <FileCopy color='secondary' />
                                    </CopyToClipboard>
                                </Tooltip>
                            </div>
                        </Grid>
                        <Grid item xs={6}>
                            <InputLabel htmlFor='adornment-amount'>Consumer Secret</InputLabel>
                            <div className={classes.copyWrapper}>
                                <Input
                                    inputProps={{ readonly: true }}
                                    id='consumerSecret'
                                    label='Consumer Secret'
                                    type={showCS || !consumerSecret ? 'text' : 'password'}
                                    value={consumerSecret || 'Keys are not generated yet. Click the Generate token button to generate the keys.'}
                                    fullWidth
                                    endAdornment={(
                                        <InputAdornment position='end'>
                                            <IconButton classes='' onClick={this.handleShowCS} onMouseDown={this.handleMouseDownGeneric}>
                                                {showCS ? <VisibilityOff /> : <Visibility />}
                                            </IconButton>
                                        </InputAdornment>
                                    )}
                                />
                                <Tooltip title={this.state.secretCopied ? 'Copied' : 'Copy to clipboard'} placement='right'>
                                    <CopyToClipboard text={consumerSecret || 'Keys are not generated yet.'} onCopy={this.onCopy('secretCopied')}>
                                        <FileCopy color='secondary' />
                                    </CopyToClipboard>
                                </Tooltip>
                            </div>
                        </Grid>
                        <Grid item xs={12}>
                            <Dialog fullScreen={fullScreen} open={this.state.open} onClose={this.handleClose} aria-labelledby='responsive-dialog-title'>
                                <DialogTitle id='responsive-dialog-title'>{showCurl ? 'Get CURL to Generate Access Token' : 'Generate Access Token'}</DialogTitle>
                                <DialogContent>
                                    {!showCurl && (
                                        <DialogContentText>
                                            {!showToken && <Tokens innerRef={node => (this.tokens = node)} selectedApp={selectedApp} keyType={keyType} />}
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
                                            Generate
                                        </Button>
                                    )}
                                    <Button onClick={this.handleClose} color='primary' autoFocus>
                                        Close
                                    </Button>
                                </DialogActions>
                            </Dialog>
                            <div className={classes.tokenSection}>
                                <Button variant='outlined' size='small' color='primary' className={classes.margin} onClick={this.handleClickOpen}>
                                    Generate Access Token
                                </Button>
                                <Button variant='outlined' size='small' color='primary' className={classes.margin} onClick={this.handleClickOpenCurl}>
                                    CURL to Generate Access Token
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
                    {' '}
Key and Secret is not generated for this application
                </Typography>
            </React.Fragment>
        );
    }
}

ViewKeys.propTypes = {
    classes: PropTypes.object,
    fullScreen: PropTypes.bool.isRequired,
};

export default withStyles(styles)(ViewKeys);
