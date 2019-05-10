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
    headline: {
        paddingTop: theme.spacing.unit * 1.25,
        paddingLeft: theme.spacing.unit * 2.5,
    },
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
          "name": "CalculatorAPI",
          "description": "A calculator API that supports basic operations",
          "context": "CalculatorAPI",
          "version": "1.0.0",
          "provider": "admin",
          "lifeCycleStatus": "CREATED",
          "responseCaching": "Disabled",
          "cacheTimeout": 300,
          "destinationStatsEnabled": "Disabled",
          "isDefaultVersion": false,
          "type": "HTTP",
          "transport": [
            "http",
            "https"
          ],
          "tags": [
            "substract",
            "add"
          ],
          "policies": [
            "Unlimited"
          ],
          "apiPolicy": "Unlimited",
          "authorizationHeader": "string",
          "securityScheme": [
            "string"
          ],
          "maxTps": {
            "production": 1000,
            "sandbox": 1000
          },
          "visibility": "PUBLIC",
          "visibleRoles": [],
          "visibleTenants": [
            "string"
          ],
          "workflowStatus": "APPROVED",
          "endpoint":[
            {
               "inline":{
                     "id":"id",
                     "name":"name",
                     "endpointConfig":"{https://localhost:9443/am/sample/pizzashack/v1/api/, timeout: 1000}",
                     "endpointSecurity":{
                        "enabled":false,
                        "type":"basic",
                        "username":"basic",
                        "password":"basic"
                     },
                     "maxTps":1000,
                     "type":"http"
               },
               "type":"production_endpoints",
               "key":"01234567-0123-0123-0123-012345678903"
            },
            {
               "inline":{
                     "id":"id",
                     "name":"name",
                     "endpointConfig":"{https://localhost:9443/am/sample/pizzashack/v1/api/, timeout: 1000}",
                     "endpointSecurity":{
                        "enabled":false,
                        "type":"basic",
                        "username":"basic",
                        "password":"basic"
                    },
                    "maxTps":1000,
                    "type":"http"
               },
               "type":"sandbox_endpoints",
               "key":"01234567-0123-0123-0123-012345678904"
            }
         ],
          "scopes":[{
              "name": "newScopeForGET5",
              "description" : "This Scope can be used to create Apis",
              "bindings" : {
                  "type":"newRole",
                  "values":["newRoleVal"]
            }
          }],
          "operations":[
              {
                "id":"postapiresource1",
                "uritemplate":"/*",
                "httpVerb":"GET",
                "authType":"Any",
                "scopes":["newScopeForGET"]
              }
          ]
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
                    <Typography variant='headline' component='h3' className={classes.head}>
                        <FormattedMessage
                            id='welcome.to.wso2.api.manager'
                            defaultMessage='Welcome to WSO2 API Manager'
                        />
                    </Typography>
                    <Typography component='p' className={classes.content}>
                        <FormattedMessage
                            id={
                                'wso2.api.publisher.enables.api.providers.to.publish.apis,.share.' +
                                'documentation,.provision.api.keys.and.gather.feedback.on.features,.quality' +
                                '.and.usage..to.get.started,.create.an.api.or.publish.a.sample.api..'
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
                        <Button
                            size='small'
                            color='primary'
                            disabled={deploying}
                            variant='outlined'
                            onClick={this.handleDeploySample}
                        >
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
