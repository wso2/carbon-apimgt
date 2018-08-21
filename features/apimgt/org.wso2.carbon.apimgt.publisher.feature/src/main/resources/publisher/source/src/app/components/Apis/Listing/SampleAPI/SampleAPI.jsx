/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Redirect from 'react-router-dom/Redirect';
import CircularProgress from '@material-ui/core/CircularProgress';
import { withStyles } from '@material-ui/core/styles';
import green from '@material-ui/core/colors/green';
import { Divider, Card, CardContent, CardActions } from '@material-ui/core/';
import { Create, GetApp } from '@material-ui/icons';
import { PropTypes } from 'prop-types';

import API from '../../../../data/api';
import Alert from '../../../Shared/Alert';
import APICreateMenu from '../components/APICreateMenu';

const styles = {
    buttonProgress: {
        color: green[500],
        position: 'relative',
    },
};

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
        this.publishSampleAPI = this.publishSampleAPI.bind(this);
        this.updatePolicies = this.updatePolicies.bind(this);
    }

    /**
     *Handle onClick event for `Deploy Sample API` Button
     * @memberof SampleAPI
     */
    handleDeploySample() {
        this.setState({ deploying: true });
        const sampleAPI = this.createSampleAPI();
        sampleAPI.then(this.updatePolicies).then(this.publishSampleAPI);
    }

    /**
     * Construct the sample API date and invoke API.create method to create API
     * @returns {Promise} SwaggerJs client promise appending an error handler and mapping response.obj as resolved value
     * @memberof SampleAPI
     */
    createSampleAPI() {
        const data = {
            name: 'Swagger Petstore',
            context: '/v2',
            version: '1.0.0',
        };
        const serviceUrl = 'https://localhost:9443/publisher/public/app/petstore/pet/1.json';
        const production = {
            type: 'production',
            inline: {
                name: data.name.replace(/ /g, '_') + data.version.replace(/\./g, '_'), // TODO: It's better to add this name property from the REST api itself, making sure no name conflicts with other inline endpoint definitions ~tmkb
                endpointConfig: JSON.stringify({ serviceUrl }),
                endpointSecurity: { enabled: false },
                type: 'http',
            },
        };
        const sandbox = JSON.parse(JSON.stringify(production)); // deep coping the object
        sandbox.type = 'sandbox';
        sandbox.inline.name += '_sandbox';
        data.endpoint = [production, sandbox];
        Alert.info('Creating sample Pet-Store API . . .');
        return this.sampleApi
            .create(data)
            .then(response => response.obj)
            .catch((error) => {
                console.error(error);
                this.setState({ deploying: false });
                Alert.error(error);
                const errorData = JSON.parse(error.data);
                const messageTxt =
                    'Error[' + errorData.code + ']: ' + errorData.description + ' | ' + errorData.message + '.';
                Alert.info(messageTxt);
            });
    }

    /**
     * Update the policies of the sample API
     * @param {Object} api Response object from REST API
     * @returns {Promise} SwaggerJS-client returned promise maping resolve value to response.obj
     */
    updatePolicies(api) {
        const uuid = api.id;
        const promisedApi = this.sampleApi.get(uuid);
        this.setState({ api });
        Alert.info('API Created with UUID :' + uuid);
        return promisedApi.then((response) => {
            Alert.info('Updating API policies with Bronze, Unlimited & Gold. . .');
            const fetchedApi = response.obj;
            fetchedApi.policies = ['Bronze', 'Unlimited', 'Gold'];
            fetchedApi.securityScheme = ['Oauth'];

            const promisedUpdate = this.sampleApi.update(fetchedApi);
            return promisedUpdate.then((updateResponse) => {
                Alert.info('Policies updated successfully!');
                return updateResponse.obj;
            });
        });
    }

    /**
     * Change the life cycle state of the sample API
     *
     * @param {Object} api API response object returned from previous promise
     * @memberof SampleAPI
     */
    publishSampleAPI(api) {
        const newState = 'Published';
        const apiUUID = api.id;
        const promisedUpdate = this.sampleApi.updateLcState(apiUUID, newState);
        promisedUpdate
            .then(() => {
                const message = 'Pet-Store API Published successfully';
                this.setState({ published: true });
                Alert.info(message);
            })
            .catch((error) => {
                console.error(error);
                this.setState({ deploying: false });
                Alert.error(error);
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
            <Grid container justify='center'>
                <Grid item sm={4}>
                    <Card className={classes.card}>
                        <Typography
                            style={{ paddingTop: '5px', paddingLeft: '10px' }}
                            gutterBottom
                            variant='headline'
                            component='h2'
                        >
                            Welcome to WSO2 API Manager
                        </Typography>
                        <Divider />
                        <CardContent>
                            <Typography align='justify' component='p'>
                                WSO2 API Publisher enables API providers to publish APIs, share documentation, provision
                                API keys and gather feedback on features, quality and usage. To get started, Create an
                                API or Publish a sample API.
                            </Typography>
                        </CardContent>
                        <CardActions>
                            <APICreateMenu buttonProps={{ size: 'small', color: 'primary', variant: 'outlined' }}>
                                <Create />
                                Create New API
                            </APICreateMenu>
                            <Button
                                size='small'
                                color='primary'
                                disabled={deploying}
                                variant='outlined'
                                onClick={this.handleDeploySample}
                            >
                                <GetApp />
                                Deploy Sample API
                            </Button>
                            {deploying && <CircularProgress size={24} className={classes.buttonProgress} />}
                        </CardActions>
                    </Card>
                </Grid>
            </Grid>
        );
    }
}

SampleAPI.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(SampleAPI);
