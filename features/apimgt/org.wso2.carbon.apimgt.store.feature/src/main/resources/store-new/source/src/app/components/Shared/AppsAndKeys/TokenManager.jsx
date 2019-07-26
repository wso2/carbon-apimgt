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
import Typography from '@material-ui/core/Typography';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import { FormattedMessage, injectIntl, } from 'react-intl';
import Application from '../../../data/Application';
import Loading from '../../Base/Loading/Loading';
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
    },
});

/**
 *  @param {event} event event
 *  @param {String} value description
 */
class TokenManager extends React.Component {
    /**
     *
     * @param {*} props props
     */
    constructor(props) {
        super(props);
        const { selectedApp, keyType } = this.props;
        this.state = {
            keys: null,
            keyRequest: {
                keyType,
                supportedGrantTypes: ['client_credentials'],
                callbackUrl: 'https://wso2.am.com',
            },
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
    }

    /**
     *
     *
     * @memberof TokenManager
     */
    componentDidMount() {
        if (this.appId) {
            this.application
                .then(application => application.getKeys())
                .then((keys) => {
                    const { keyType } = this.props;
                    const { keyRequest } = this.state;
                    if (keys.size > 0 && keys.get(keyType)) {
                        const { callbackUrl, supportedGrantTypes } = keys.get(keyType);
                        const newRequest = { ...keyRequest, callbackUrl, supportedGrantTypes };
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
        const { keyRequest, keys } = this.state;
        const { keyType, updateSubscriptionData, intl } = this.props;
        this.application
            .then((application) => {
                return application.generateKeys(keyType, keyRequest.supportedGrantTypes, keyRequest.callbackUrl);
            })
            .then((response) => {
                console.log(
                    intl.formatMessage({
                        defaultMessage: 'Keys generated successfully with ID : ',
                        id: 'Shared.AppsAndKeys.TokenManager.keys.generated.success',
                    }) + response,
                );
                if (updateSubscriptionData) {
                    updateSubscriptionData();
                }
                const newKeys = new Map([...keys]);
                newKeys.set(keyType, response);
                this.setState({ keys: newKeys });
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
     *
     * @memberof KeyConfiguration
     */
    updateKeys() {
        const { keys, keyRequest } = this.state;
        const { keyType } = this.props;
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
            .then((response) => {
                console.log(
                    intl.formatMessage({
                        defaultMessage: 'Keys updated successfully : ',
                        id: 'Shared.AppsAndKeys.TokenManager.keys.generated.success',
                    }) + response,
                );
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
     *  @returns {Component}
     * @memberof Tokenemanager
     */
    render() {
        const { classes, selectedApp, keyType } = this.props;
        const { keys, keyRequest, notFound } = this.state;
        if (!keys) {
            return <Loading />;
        }
        const key = keys.get(keyType);
        if (key && (key.keyState === this.keyStates.CREATED || key.keyState === this.keyStates.REJECTED)) {
            return <WaitingForApproval keyState={key.keyState} states={this.keyStates} />;
        }
        return (
            <div className={classes.root}>
                <Typography variant='headline' className={classes.keyTitle}>
                    {keyType}
                    <FormattedMessage
                        defaultMessage='Key and Secret'
                        id='Shared.AppsAndKeys.TokenManager.key.and.secret'
                    />
                </Typography>
                <ViewKeys selectedApp={selectedApp} keyType={keyType} keys={keys} />

                <ExpansionPanel>
                    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography className={classes.heading} variant='subtitle1'>
                            <FormattedMessage
                                defaultMessage='Key Configuration'
                                id='Shared.AppsAndKeys.TokenManager.key.configuration'
                            />
                        </Typography>
                    </ExpansionPanelSummary>
                    <ExpansionPanelDetails className={classes.keyConfigWrapper}>
                        <KeyConfiguration
                            keys={keys}
                            selectedApp={selectedApp}
                            keyType={keyType}
                            updateKeyRequest={this.updateKeyRequest}
                            keyRequest={keyRequest}
                        />
                    </ExpansionPanelDetails>
                </ExpansionPanel>
                <div className={classes.generateWrapper}>
                    <ScopeValidation
                        resourcePath={resourcePaths.APPLICATION_GENERATE_KEYS}
                        resourceMethod={resourceMethods.POST}
                    >
                        <Button
                            variant='contained'
                            color='primary'
                            className={classes.button}
                            onClick={keys.size > 0 && keys.get(keyType) ? this.updateKeys : this.generateKeys}
                            noFound={notFound}
                        >
                            {keys.size > 0 && keys.get(keyType) ? 'Update keys' : 'Generate Keys'}
                        </Button>
                    </ScopeValidation>
                </div>
            </div>
        );
    }
}

TokenManager.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(TokenManager));
