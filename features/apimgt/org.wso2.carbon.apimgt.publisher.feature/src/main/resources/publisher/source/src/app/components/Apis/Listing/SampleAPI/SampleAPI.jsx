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

import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Redirect from 'react-router-dom/Redirect';
import CircularProgress from '@material-ui/core/CircularProgress';
import { withStyles } from '@material-ui/core/styles';
import green from '@material-ui/core/colors/green';
import Create from '@material-ui/icons/Create';
import GetApp from '@material-ui/icons/GetApp';
import { PropTypes } from 'prop-types';
import { FormattedMessage, injectIntl } from 'react-intl';

import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import AuthManager from 'AppData/AuthManager';
import APICreateMenu from '../components/APICreateMenu';
import getSampleSwagger from './SamplePetStore.js';

const styles = theme => ({
    buttonProgress: {
        color: green[500],
        position: 'relative',
    },
    headline: {
        paddingTop: theme.spacing(1.25),
        paddingLeft: theme.spacing(2.5),
    },
    head: {
        paddingBottom: theme.spacing(2),
        fontWeight: 200,
    },
    content: {
        paddingBottom: theme.spacing(2),
    },
    buttonLeft: {
        marginRight: theme.spacing(1),
    },
});

/**
 * Show Initial Welcome card if no APIs are available to list
 * Handle deploying a sample API (Create and Publish)
 *
 * @class SampleAPI
 * @extends {Component}
 */
class SampleAPI extends Component {
    /**
     *Creates an instance of SampleAPI.
     * @param {Object} props @inheritdoc
     * @memberof SampleAPI
     */
    constructor(props) {
        super(props);
        this.state = {
            published: false,
            api: null,
            deploying: false,
        };
        this.sampleApi = new API();
        this.handleDeploySample = this.handleDeploySample.bind(this);
        this.createSampleAPI = this.createSampleAPI.bind(this);
    }

    /**
     *Handle onClick event for `Deploy Sample API` Button
     * @memberof SampleAPI
     */
    handleDeploySample() {
        const { intl } = this.props;
        this.setState({ deploying: true });
        const promisedSampleAPI = this.createSampleAPI();
        const swaggerUpdatePromise = promisedSampleAPI.then((sampleAPI) => {
            sampleAPI.updateSwagger(getSampleSwagger('Unlimited'));
            return sampleAPI;
        });
        swaggerUpdatePromise.catch((error) => {
            console.error(error);
            Alert.error(error);
        });
        swaggerUpdatePromise.then((sampleAPI) => {
            sampleAPI.publish()
                .then(() => {
                    this.setState({ published: true, api: sampleAPI });
                    Alert.info(intl.formatMessage({
                        id: 'Apis.Listing.SampleAPI.SampleAPI.created',
                        defaultMessage: 'Sample PizzaShackAPI API created successfully',
                    }));
                })
                .catch((error) => {
                    console.error(error);
                    this.setState({ deploying: false });
                    Alert.error(error);
                });
        });
    }

    /**
     * Construct the sample API date and invoke API.create method to create API
     * @returns {Promise} SwaggerJs client promise appending an error handler and mapping response.obj as resolved value
     * @memberof SampleAPI
     */
    createSampleAPI() {
        const data = {
            name: 'PizzaShackAPI',
            description: 'This is a simple API for Pizza Shack online pizza delivery store.',
            context: '/pizzashack',
            version: '1.0.0',
            transport: ['http', 'https'],
            tags: ['pizza'],
            policies: ['Unlimited'],
            securityScheme: ['oauth2'],
            visibility: 'PUBLIC',
            gatewayEnvironments: ['Production and Sandbox'],
            businessInformation: {
                businessOwner: 'Jane Roe',
                businessOwnerEmail: 'marketing@pizzashack.com',
                technicalOwner: 'John Doe',
                technicalOwnerEmail: 'architecture@pizzashack.com',
            },
            endpointConfig: {
                endpoint_type: 'http',
                sandbox_endpoints: {
                    url: 'https://localhost:9443/am/sample/pizzashack/v1/api/',
                },
                production_endpoints: {
                    url: 'https://localhost:9443/am/sample/pizzashack/v1/api/',
                },
            },
            operations: [
                {
                    target: '/order/{orderId}',
                    verb: 'GET',
                    throttlingPolicy: 'Unlimited',
                    authType: 'Application & Application User',
                },
                {
                    target: '/order/{orderId}',
                    verb: 'DELETE',
                    throttlingPolicy: 'Unlimited',
                    authType: 'Application & Application User',
                },
                {
                    target: '/order/{orderId}',
                    verb: 'PUT',
                    throttlingPolicy: 'Unlimited',
                    authType: 'Application & Application User',
                },
                {
                    target: '/menu',
                    verb: 'GET',
                    throttlingPolicy: 'Unlimited',
                    authType: 'Application & Application User',
                },
                {
                    target: '/order',
                    verb: 'POST',
                    throttlingPolicy: 'Unlimited',
                    authType: 'Application & Application User',
                },
            ],
        };

        const sampleAPI = new API(data);
        return sampleAPI.save().catch((error) => {
            console.error(error);
            this.setState({ deploying: false });
            const { response } = error;
            if (response) {
                const { code, description, message } = response.body;
                Alert.error(`ERROR[${code}] : ${description} | ${message}`);
            } else {
                Alert.error(error);
            }
        });
    }

    /**
     *
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof SampleAPI
     */
    render() {
        const { published, api, deploying } = this.state;
        const { classes } = this.props;

        if (published && api) {
            const url = '/apis/' + api.id + '/overview';
            return <Redirect to={url} />;
        }
        return (
            <InlineMessage type='info' height={140}>
                <div className={classes.contentWrapper}>
                    <Typography variant='h5' component='h3' className={classes.head}>
                        <FormattedMessage
                            id='welcome.to.wso2.api.manager'
                            defaultMessage='Welcome to WSO2 API Manager'
                        />
                    </Typography>
                    <Typography component='p' className={classes.content}>
                        <FormattedMessage
                            id='Apis.Listing.SampleAPI.SampleAPI.description'
                            defaultMessage={
                                'WSO2 API Publisher enables API providers to ' +
                                ' publish APIs, share documentation, provision API keys and gather feedback' +
                                ' on features, quality and usage. To get started, Create an API ' +
                                'or Publish a sample API.'
                            }
                        />
                    </Typography>
                    <div className={classes.actions}>
                        <APICreateMenu
                            buttonProps={{
                                size: 'small',
                                color: 'primary',
                                variant: 'outlined',
                                className: classes.buttonLeft,
                            }}
                        >
                            <Create />
                            <FormattedMessage id='create.new.api' defaultMessage='Create New API' />
                        </APICreateMenu>
                        {!AuthManager.isNotCreator() &&
                            <Button
                                size='small'
                                color='primary'
                                disabled={deploying}
                                variant='outlined'
                                onClick={this.handleDeploySample}
                            >
                                <GetApp />
                                <FormattedMessage id='deploy.sample.api' defaultMessage='Deploy Sample API' />
                                {deploying && <CircularProgress size={24} className={classes.buttonProgress} />}
                            </Button>
                        }
                    </div>
                </div>
            </InlineMessage>
        );
    }
}

SampleAPI.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

export default injectIntl(withStyles(styles)(SampleAPI));
