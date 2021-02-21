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
import Grid from '@material-ui/core/Grid';
import Redirect from 'react-router-dom/Redirect';
import { PropTypes } from 'prop-types';
import { injectIntl } from 'react-intl';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import AuthManager from 'AppData/AuthManager';
import APICreateMenu from 'AppComponents/Apis/Listing/components/APICreateMenu';
import getSampleSwagger from './SamplePizzaShack.js';
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
    handleDeploySample(e) {
        e.preventDefault();
        const { intl } = this.props;
        const restApi = new API();
        let settings;
        restApi.getSettings().then((response) => {
            settings = response;
        });
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
        if (!AuthManager.isNotPublisher()) {
            swaggerUpdatePromise.then((sampleAPI) => {
                const body = {
                    description: 'Initial Revision',
                };
                restApi.createRevision(sampleAPI.id, body)
                    .then((api1) => {
                        const revisionId = api1.body.id;
                        const envList = settings.environment.map((env) => env.name);
                        const body1 = [];
                        const getFirstVhost = (envName) => {
                            const env = settings.environment.find(
                                (ev) => ev.name === envName && ev.vhosts.length > 0,
                            );
                            return env && env.vhosts[0].host;
                        };
                        if (envList && envList.length > 0) {
                            if (envList.includes('Default') && getFirstVhost('Default')) {
                                body1.push({
                                    name: 'Default',
                                    displayOnDevportal: true,
                                    vhost: getFirstVhost('Default'),
                                });
                            } else if (getFirstVhost(envList[0])) {
                                body1.push({
                                    name: envList[0],
                                    displayOnDevportal: true,
                                    vhost: getFirstVhost(envList[0]),
                                });
                            }
                        }
                        restApi.deployRevision(sampleAPI.id, revisionId, body1)
                            .then(() => {
                                Alert.info('API Revision Deployed Successfully');
                            })
                            .catch((error) => {
                                if (error.response) {
                                    Alert.error(error.response.body.description);
                                } else {
                                    const message = 'Something went wrong while deploying the API Revision';
                                    Alert.error(intl.formatMessage({
                                        id: 'Apis.Listing.SampleAPI.SampleAPI.error.errorMessage.deploy.revision',
                                        defaultMessage: message,
                                    }));
                                }
                            });
                    })
                    .catch((error) => {
                        if (error.response) {
                            Alert.error(error.response.body.description);
                        } else {
                            const message = 'Something went wrong while creating the API Revision';
                            Alert.error(intl.formatMessage({
                                id: 'Apis.Listing.SampleAPI.SampleAPI.error.errorMessage.create.revision',
                                defaultMessage: message,
                            }));
                        }
                    });
                sampleAPI.publish()
                    .then(() => {
                        this.setState({ published: true, api: sampleAPI });
                        Alert.info(intl.formatMessage({
                            id: 'Apis.Listing.SampleAPI.SampleAPI.created',
                            defaultMessage: 'Sample PizzaShackAPI API created successfully',
                        }));
                    })
                    .catch((error) => {
                        this.setState({ deploying: false });
                        Alert.error(error);
                    });
            });
        } else {
            swaggerUpdatePromise.then((sampleApi) => {
                this.setState({ published: true, api: sampleApi });
                Alert.info(intl.formatMessage({
                    id: 'Apis.Listing.SampleAPI.SampleAPI.created',
                    defaultMessage: 'Sample PizzaShackAPI API created successfully',
                }));
            })
                .catch((error) => {
                    Alert.error(error);
                });
        }
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

        if (published && api) {
            const url = '/apis/' + api.id + '/overview';
            return <Redirect to={url} />;
        }
        return (
            <Grid container spacing={3}>
                <Grid item xs={12} />
                {/*
            Following two grids control the placement of whole create page
            For centering the content better use `container` props, but instead used an empty grid item for flexibility
             */}
                <Grid item sm={0} md={2} />
                <Grid item sm={12} md={8}>
                    <APICreateMenu deploying={deploying} handleDeploySample={this.handleDeploySample} />
                </Grid>
            </Grid>
        );
    }
}

SampleAPI.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

export default injectIntl(SampleAPI);
