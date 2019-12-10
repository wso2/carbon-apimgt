/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import PropTypes from 'prop-types';
import API from 'AppData/api.js';
import Alert from 'AppComponents/Shared/Alert';
import APICreateDefault from './APICreateDefault';

/**
 * Wrapper for API create component. this wrapper is used when creating
 * an API
 * @deprecated don't use `APICreateWrapper` to create new type of api create page,
 * instead use use `APICreateBase` and `DefaultAPIForm` components.
 */
class APICreateWrapper extends Component {
    constructor(props) {
        super(props);
        this.state = {
            api: new API(),
            oasVersion: 'v3',
            valid: {
                name: { empty: false, alreadyExists: false },
                context: { empty: false, alreadyExists: false },
                version: { empty: false },
            },
        };
        this.handleSubmit = this.handleSubmit.bind(this);
        this.inputChange = this.inputChange.bind(this);
        this.handleOASVersionChange = this.handleOASVersionChange.bind(this);
    }

    /**
     * Do create API from either swagger URL or swagger file upload.In case of URL pre fetch the swagger file and make
     * a blob
     * and the send it over REST API.
     * @param e {Event}
     */
    handleSubmit(e) {
        e.preventDefault();
        const { api: currentAPI } = this.state;
        const { type: apiType } = this.props;
        if (!currentAPI.name || !currentAPI.context || !currentAPI.version) {
            // Checking the api name,version,context undefined or empty states
            this.setState((oldState) => {
                const { valid, api } = oldState;
                const validUpdated = valid;
                validUpdated.name.empty = !api.name;
                validUpdated.context.empty = !api.context;
                validUpdated.version.empty = !api.version;
                return { valid: validUpdated };
            });
            return;
        }
        if (apiType === 'ws') {
            currentAPI.type = 'WS';
        }
        currentAPI
            .save(this.state.oasVersion)
            .then((newAPI) => {
                const redirectURL = '/apis/' + newAPI.id + '/overview';
                Alert.info(`${newAPI.name} created.`);
                this.props.history.push(redirectURL);
            })
            .catch((error) => {
                console.error(error);
                if (error.response) {
                    Alert.error(error.response.body.message);
                } else {
                    Alert.error(`Something went wrong while creating ${currentAPI.name}`);
                }
            });
    }

    /**
     * Change input
     * @param {any} e Synthetic React Event
     * @memberof APICreateForm
     */
    inputChange({ target }) {
        const { name, value } = target;
        this.setState(({ api, valid }) => {
            const changes = api;
            if (name === 'endpoint') {
                changes.endpointConfig = {
                    endpoint_type: 'http',
                    sandbox_endpoints: {
                        url: value,
                    },
                    production_endpoints: {
                        url: value,
                    },
                };
            } else {
                changes[name] = value;
            }
            // Checking validity.
            const validUpdated = valid;
            validUpdated.name.empty = !api.name;
            validUpdated.context.empty = !api.context;
            validUpdated.version.empty = !api.version;
            // TODO we need to add the already existing error for (context)
            // by doing an api call ( the swagger definition does not contain such api call)
            return { api: changes, valid: validUpdated };
        });
    }

    handleOASVersionChange(event) {
        this.setState({ oasVersion: event.target.value });
    }

    /**
     * @inheritDoc
     */
    render() {
        const { type } = this.props;
        return (
            <>
                <APICreateDefault
                    api={this.state.api}
                    oasVersion={this.state.oasVersion}
                    handleOASVersionChange={this.handleOASVersionChange}
                    handleSubmit={this.handleSubmit}
                    inputChange={this.inputChange}
                    isAPIProduct={false}
                    valid={this.state.valid}
                    type={type}
                />
            </>
        );
    }
}

APICreateWrapper.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({
        push: PropTypes.func.isRequired,
    }).isRequired,
    type: PropTypes.shape({}).isRequired,
    valid: PropTypes.shape({}).isRequired,
    location: PropTypes.shape({
        pathname: PropTypes.string,
    }).isRequired,
};
export default APICreateWrapper;
