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
import API from 'AppData/api';
import AuthManager from 'AppData/AuthManager';
import Settings from 'AppComponents/Shared/SettingsContext';
import KeyConfiguration from './KeyConfiguration';
import ViewKeys from './ViewKeys';
import ViewToken from './ViewToken';
import WaitingForApproval from './WaitingForApproval';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import ApiKey from './../ApiKey'
import FormLabel from '@material-ui/core/FormLabel';
import FormGroup from '@material-ui/core/FormGroup';
import Grid from '@material-ui/core/Grid';

const styles = theme => ({
    root: {
        padding: theme.spacing.unit * 3,
    },
    button: {
        marginLeft: theme.spacing.unit * 5,
        padding: '10px',
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
        padding: '10px',
        'margin-inline-end': 'auto',
    },
    paper: {
        display: 'flex',
        marginTop: theme.spacing.unit * 2,
        marginBottom: theme.spacing.unit * 2,
        marginLeft: theme.spacing.unit * 10,
    },
    formLabel: {
        'align-self': 'center',
        'font-size': 'large',
    },
    dialogTitle: {
        padding: '24px 24px 0px',
    },
    dialogContent: {
        padding: '0 24px 0px',
    },
    formGroup: {
        padding: '20px',
    },
    gridWrapper: {
        'align-self': 'center',
    }
});

class ApiKeyManager extends React.Component {

    constructor(props) {
        super(props);
        const {classes, selectedApp, keyType } = this.props;
        this.state = {
            apikey: null,
            open: false,
            showToken: false,
            accessTokenRequest: {
                timeout: -1,
            },
        };
    }

    handleClose = () => {
        this.setState(() => ({open: false, accessTokenRequest: {timeout:-1}}))
    }

    handleClickOpen =() => {
        this.setState(() => ({ open: true, showToken: false }));
    }

    updateAccessTokenRequest = (accessTokenRequest) => {
        this.setState(() => ({ accessTokenRequest }));
    }

    generateKeys = (selectedApp, keyType) => {
        const client = new API();
        const promisedKey = client.generateApiKey(selectedApp.appId, keyType, this.state.accessTokenRequest.timeout);
        promisedKey
            .then((response) => {
                console.log('Non empty response received');
                const apikey = {accessToken: response.body.apikey, validityTime: response.body.validityTime, isOauth:false};
                this.setState(() => ({apikey: apikey, open: true, showToken: true}));
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

    render() {
        const {classes, selectedApp, keyType } = this.props;
        const {showToken, accessTokenRequest, open, apikey} = this.state;
        return (
            <div className={classes.root}>
                    <Typography variant='h5' className={classes.keyTitle}>
                        {keyType + ' '}
                        <FormattedMessage
                            defaultMessage='Api Key'
                            id='Shared.AppsAndKeys.TokenManager.ApiKey'
                        />
                    </Typography>
                    <FormGroup row className={classes.formGroup}>
                        <Grid container spacing={3}>
                            <Grid item xs={4}>
                            </Grid>
                            <Grid item xs={2} className={classes.gridWrapper}>
                                <FormLabel className={classes.formLabel}>Generate Api Key</FormLabel>
                            </Grid>
                            <Grid item xs={3}>
                                <Button
                                    variant='contained'
                                    color='primary'
                                    className={classes.button}
                                    onClick= {this.handleClickOpen}
                                >
                                    {'Generate Key'}
                                </Button>
                            </Grid>
                        </Grid>
                    </FormGroup>
                    <Dialog open={open} onClose={this.handleClose} aria-labelledby="form-dialog-title">
                        <DialogTitle id='responsive-dialog-title' className={classes.dialogTitle}>
                            {'Generate Api Key'}
                        </DialogTitle>
                        <DialogContent className={classes.dialogContent}>
                            <DialogContentText>
                                {!showToken && (
                                    <ApiKey
                                        updateAccessTokenRequest={this.updateAccessTokenRequest}
                                        accessTokenRequest={accessTokenRequest}
                                    />
                                )}
                                {showToken && <ViewToken token={apikey} />}
                            </DialogContentText>
                        </DialogContent>
                        <DialogActions>
                            {!showToken && (
                                <Button onClick={() => this.generateKeys(selectedApp,keyType)} disabled={!accessTokenRequest.timeout} color='primary'>
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
            </div>
        );
    }
}

ApiKeyManager.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    selectedApp: PropTypes.shape({
        tokenType: PropTypes.string.isRequired,
    }).isRequired,
    keyType: PropTypes.string.isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

export default injectIntl(withStyles(styles)(ApiKeyManager));
