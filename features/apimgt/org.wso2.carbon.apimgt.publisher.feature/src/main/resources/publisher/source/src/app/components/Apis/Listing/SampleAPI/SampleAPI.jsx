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

import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Redirect from 'react-router-dom/Redirect';
import CircularProgress from '@material-ui/core/CircularProgress';
import { withStyles } from '@material-ui/core/styles';
import green from '@material-ui/core/colors/green';
import Create from '@material-ui/icons/Create';
import GetApp from '@material-ui/icons/GetApp';
import { PropTypes } from 'prop-types';
import { FormattedMessage } from 'react-intl';

import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import APICreateMenu from '../components/APICreateMenu';

const styles = theme => ({
    buttonProgress: {
        color: green[500],
        position: 'relative',
    },
    headline: { paddingTop: theme.spacing.unit * 1.25, paddingLeft: theme.spacing.unit * 2.5 },
    head: {
        paddingBottom: theme.spacing.unit,
    },
    content: {
        paddingBottom: theme.spacing.unit,
    },
    buttonLeft: {
        marginRight: theme.spacing.unit,
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
        this.setState({ deploying: true });
        const promisedSampleAPI = this.createSampleAPI();
        promisedSampleAPI.then((sampleAPI) => {
            sampleAPI
                .publish()
                .then(() => {
                    const message = 'Pet-Store API Published successfully';
                    this.setState({ published: true, api: sampleAPI });
                    Alert.info(message);
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
            name: 'Swagger Petstore',
            context: '/v2',
            version: '1.0.0',
            description: 'This is a sample server Petstore server.  You can find out more about Swagger at' +
            '[http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).' +
            '  For this sample, you can use the api key `special-key` to test the authorization filters.',
            isDefaultVersion: true,
            responseCaching: 'Disabled',
            cacheTimeout: 300,
            destinationStatsEnabled: 'Disabled',
            businessInformation: {
                businessOwner: 'APIM Team',
                businessOwnerEmail: 'hello@wso2.com',
                technicalOwner: 'WSO2 APIM Team',
                technicalOwnerEmail: 'dev@wso2.org',
            },
            tags: ['Pet', 'Store', 'Cat', 'Hippopotamus'],
            transport: ['http', 'https'],
            visibility: 'PUBLIC',
            securityScheme: ['OAuth'],
            scopes: ['read:pets', 'write:pets'],
            policies: ['Bronze', 'Unlimited', 'Gold'],
        };
        const serviceUrl = 'https://localhost:9443/publisher/public/app/petstore/pet/1.json';
        const production = {
            type: 'Production',
            inline: {
                name: data.name.replace(/ /g, '_') + data.version.replace(/\./g, '_'),
                // TODO: It's better to add this name property from the REST api itself,
                // making sure no name conflicts with other inline endpoint definitions ~tmkb
                endpointConfig: { list: [{ url: serviceUrl, timeout: '1000' }], endpointType: 'SINGLE' },
                endpointSecurity: { enabled: false },
                type: 'http',
            },
        };
        const sandbox = JSON.parse(JSON.stringify(production)); // deep coping the object
        sandbox.type = 'Sandbox';
        sandbox.inline.name += '_sandbox';
        data.endpoint = [production, sandbox];
        Alert.info('Creating sample Pet-Store API . . .');
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
                    <Typography variant='headline' component='h3' className={classes.head}>
                        <FormattedMessage id='welcome.to.wso2.api.manager' defaultMessage='Welcome to WSO2 API Manager' />
                    </Typography>
                    <Typography component='p' className={classes.content}>
                        <FormattedMessage id={'wso2.api.publisher.enables.api.providers.to.publish.apis,.share.' +
                        'documentation,.provision.api.keys.and.gather.feedback.on.features,.quality' +
                        '.and.usage..to.get.started,.create.an.api.or.publish.a.sample.api..'}
                        />
                    </Typography>
                    <div className={classes.actions}>
                        <APICreateMenu buttonProps={{
                            size: 'small', color: 'primary', variant: 'outlined', className: classes.buttonLeft,
                        }}
                        >
                            <Create />
                            <FormattedMessage id='create.new.api' defaultMessage='Create New API' />
                        </APICreateMenu>
                        <Button size='small' color='primary' disabled={deploying} variant='outlined' onClick={this.handleDeploySample}>
                            <GetApp />
                            <FormattedMessage id='deploy.sample.api' defaultMessage='Deploy Sample API' />
                        </Button>
                        {deploying && <CircularProgress size={24} className={classes.buttonProgress} />}
                    </div>
                </div>
            </InlineMessage>
        );
    }
}

SampleAPI.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(SampleAPI);
