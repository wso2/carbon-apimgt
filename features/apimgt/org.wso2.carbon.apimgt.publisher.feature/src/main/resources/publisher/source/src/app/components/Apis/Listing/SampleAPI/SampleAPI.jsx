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
import { Link } from 'react-router-dom';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import { Link as MUILink } from '@material-ui/core';
import Grid from '@material-ui/core/Grid';
import Redirect from 'react-router-dom/Redirect';
import CircularProgress from '@material-ui/core/CircularProgress';
import { withStyles } from '@material-ui/core/styles';
import green from '@material-ui/core/colors/green';
import { PropTypes } from 'prop-types';
import { FormattedMessage, injectIntl } from 'react-intl';
import Configurations from 'Config';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import AuthManager from 'AppData/AuthManager';
import getSampleSwagger from './SamplePetStore.js';

const styles = () => ({
    buttonProgress: {
        color: green[500],
        position: 'relative',
    },
    links: {
        cursor: 'pointer',
    },
    root: {
        '& a': {
            color: '#34679D',
        },
        '& a:visited': {
            color: '#34679D',
        },
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
                        if (envList.length > 0) {
                            body1.push({
                                name: envList[0],
                                displayOnDevportal: true,
                            });
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
        const { classes } = this.props;

        if (published && api) {
            const url = '/apis/' + api.id + '/overview';
            return <Redirect to={url} />;
        }
        return (
            <Grid container spacing={3} className={classes.root}>
                <Grid item xs={12} />
                {/*
            Following two grids control the placement of whole create page
            For centering the content better use `container` props, but instead used an empty grid item for flexibility
             */}
                <Grid item sm={0} md={3} />
                <Grid item sm={12} md={6}>
                    <Grid container spacing={5}>
                        <Grid item xl={3}>
                            <Box textAlign='center' mb={2}>
                                <Typography variant='h6'>
                                    <FormattedMessage
                                        id='Apis.Listing.SampleAPI.SampleAPI.create.new'
                                        defaultMessage='Create an API'
                                    />
                                </Typography>
                            </Box>
                            <Box textAlign='center'>
                                <Typography variant='body2'>
                                    <FormattedMessage
                                        id='Apis.Listing.SampleAPI.SampleAPI.create.new.description'
                                        defaultMessage={`API creation is the process of linking an existing 
                                        backend API backend API implementation to the API Publisher, 
                                        so that you can manage and monitor the API’s lifecycle, documentation, 
                                        security, community, and subscriptions Alternatively, you can provide 
                                        the API implementation in-line in the API Publisher itself.`}
                                    />
                                </Typography>
                            </Box>
                        </Grid>
                        <Grid item xs={12} md={12}>
                            <Grid container>
                                <Grid item xs={12} sm={6} md={6} lg={3}>
                                    <Box textAlign='center' mt={4}>
                                        <Typography variant='subtitle' component='div'>
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.SampleAPI.rest.api'
                                                defaultMessage='REST API'
                                            />
                                        </Typography>
                                        <img
                                            src={Configurations.app.context
                                                + '/site/public/images/landing-icons/restapi.svg'}
                                            alt='Rest API'
                                        />
                                        <Box mt={2}>
                                            <Link
                                                id='itest-id-createdefault'
                                                to='/apis/create/rest'
                                                className={classes.links}
                                            >
                                                <FormattedMessage
                                                    id='Apis.Listing.SampleAPI.SampleAPI.rest.api.scratch.title'
                                                    defaultMessage='Start From Scratch'
                                                />
                                            </Link>
                                            <Typography variant='body2'>
                                                <FormattedMessage
                                                    id='Apis.Listing.SampleAPI.SampleAPI.rest.api.scratch.content'
                                                    defaultMessage='Design and prototype a new REST API'
                                                />
                                            </Typography>
                                        </Box>
                                        <Box mt={2}>
                                            <Link
                                                id='itest-id-createdefault'
                                                to='/apis/create/openapi'
                                                className={classes.links}
                                            >
                                                <FormattedMessage
                                                    id='Apis.Listing.SampleAPI.SampleAPI.rest.api.import.open.title'
                                                    defaultMessage='Import Open API'
                                                />
                                            </Link>
                                            <Typography variant='body2'>
                                                <FormattedMessage
                                                    id='Apis.Listing.SampleAPI.SampleAPI.rest.api.import.open.content'
                                                    defaultMessage='Upload definition or provide the url'
                                                />
                                            </Typography>
                                        </Box>
                                        {!AuthManager.isNotCreator()
                                            && (
                                                <Box mt={2}>
                                                    {!deploying ? (
                                                        <MUILink
                                                            id='itest-id-createdefault'
                                                            onClick={this.handleDeploySample}
                                                            className={classes.links}
                                                        >
                                                            <FormattedMessage
                                                                id={'Apis.Listing.SampleAPI.SampleAPI.'
                                                                + 'rest.d.sample.title'}
                                                                defaultMessage='Deploy Sample API'
                                                            />
                                                        </MUILink>
                                                    )
                                                        : (
                                                            <CircularProgress
                                                                size={24}
                                                                className={classes.buttonProgress}
                                                            />
                                                        )}
                                                    <Typography variant='body2'>
                                                        <FormattedMessage
                                                            id='Apis.Listing.SampleAPI.SampleAPI.rest.d.sample.content'
                                                            defaultMessage={`This is a sample API for Pizza Shack 
                                                    online pizza delivery store`}
                                                        />
                                                    </Typography>
                                                </Box>

                                            )}

                                    </Box>

                                </Grid>
                                <Grid item xs={12} sm={6} md={6} lg={3}>
                                    <Box textAlign='center' mt={4}>
                                        <Typography variant='subtitle' component='div'>
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.SampleAPI.soap.api'
                                                defaultMessage='SOAP API'
                                            />
                                        </Typography>
                                        <img
                                            src={Configurations.app.context
                                                + '/site/public/images/landing-icons/soapapi.svg'}
                                            alt='SOAP API'
                                        />
                                        <Box mt={2}>
                                            <Link
                                                id='itest-id-createdefault'
                                                to='/apis/create/wsdl'
                                                className={classes.links}
                                            >
                                                <FormattedMessage
                                                    id='Apis.Listing.SampleAPI.SampleAPI.soap.import.wsdl.title'
                                                    defaultMessage='Import WSDL'
                                                />
                                            </Link>
                                            <Typography variant='body2'>
                                                <FormattedMessage
                                                    id='Apis.Listing.SampleAPI.SampleAPI.soap.import.wsdl.content'
                                                    defaultMessage='Use an existing WSDL'
                                                />
                                            </Typography>
                                        </Box>
                                    </Box>
                                </Grid>
                                <Grid item xs={12} sm={6} md={6} lg={3}>
                                    <Box textAlign='center' mt={4}>
                                        <Typography variant='subtitle' component='div'>
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.SampleAPI.graphql.api'
                                                defaultMessage='GraphQL'
                                            />
                                        </Typography>
                                        <img
                                            src={Configurations.app.context
                                                + '/site/public/images/landing-icons/graphqlapi.svg'}
                                            alt='GraphQL'
                                        />
                                        <Box mt={2}>
                                            <Link
                                                id='itest-id-createdefault'
                                                to='/apis/create/graphQL'
                                                className={classes.links}
                                            >
                                                <FormattedMessage
                                                    id='Apis.Listing.SampleAPI.SampleAPI.graphql.import.sdl.title'
                                                    defaultMessage='Import GraphQL SDL'
                                                />
                                            </Link>
                                            <Typography variant='body2'>
                                                <FormattedMessage
                                                    id='Apis.Listing.SampleAPI.SampleAPI.graphql.import.sdl.content'
                                                    defaultMessage='Use an existing definition'
                                                />
                                            </Typography>
                                        </Box>
                                    </Box>
                                </Grid>
                                <Grid item xs={12} sm={6} md={6} lg={3}>
                                    <Box textAlign='center' mt={4}>
                                        <Typography variant='subtitle' component='div'>
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.SampleAPI.websocket.api'
                                                defaultMessage='WebSocket API'
                                            />
                                        </Typography>
                                        <img
                                            src={Configurations.app.context
                                                + '/site/public/images/landing-icons/websocketapi.svg'}
                                            alt='WebSocket API'
                                        />
                                        <Box mt={2}>
                                            <Link
                                                id='itest-id-createdefault'
                                                to='/apis/create/ws'
                                                className={classes.links}
                                            >
                                                <FormattedMessage
                                                    id='Apis.Listing.SampleAPI.SampleAPI.websocket.design.new.title'
                                                    defaultMessage='Design New WebSocket API'
                                                />
                                            </Link>
                                            <Typography variant='body2'>
                                                <FormattedMessage
                                                    id='Apis.Listing.SampleAPI.SampleAPI.websocket.design.new.content'
                                                    defaultMessage='Design and prototype a new WebSocket API'
                                                />
                                            </Typography>
                                        </Box>
                                    </Box>
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        );
    }
}

SampleAPI.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

export default injectIntl(withStyles(styles)(SampleAPI));
