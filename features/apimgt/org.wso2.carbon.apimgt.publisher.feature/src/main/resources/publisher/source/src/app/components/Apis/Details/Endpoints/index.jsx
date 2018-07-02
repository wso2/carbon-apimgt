/**
 * Copyright (c), WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import React, { Component } from 'react';
import { Grid, Paper, Typography, Divider } from 'material-ui';
import PropTypes from 'prop-types';

import EndpointForm from '../../../Endpoints/Create/EndpointForm';
import Api from '../../../../data/api';
import ApiPermissionValidation from '../../../../data/ApiPermissionValidation';
import { resourceMethod, resourcePath, ScopeValidation } from '../../../../data/ScopeValidation';
import ResourceNotFound from '../../../Base/Errors/ResourceNotFound';
import { Alert, InteractiveButton, Progress } from '../../../Shared';
import EndpointDAO from '../../../../data/Endpoint';
import EndpointsSelector from './EndpointsSelector';
/**
 * API Details Endpoint page component
 * @class Endpoint
 * @extends {Component}
 */
class Endpoint extends Component {
    /**
     * Creates an instance of Endpoint.
     * @param {any} props @inheritDoc
     * @memberof Endpoint
     */
    constructor(props) {
        super(props);
        this.state = {
            endpointsMap: new Map(),
            productionEndpoint: new EndpointDAO('', 'https', 10),
            sandboxEndpoint: new EndpointDAO('', 'https', 10),
            isInline: true,
            notFound: false,
        };
        this.handleProductionInputs = this.handleProductionInputs.bind(this);
        this.handleSandboxInputs = this.handleSandboxInputs.bind(this);
        this.updateEndpoints = this.updateEndpoints.bind(this);
        this.switchEndpoint = this.switchEndpoint.bind(this);
    }

    /**
     * @inheritDoc
     * @memberof Endpoint
     */
    componentDidMount() {
        const { apiUUID } = this.props.match.params;
        // Populate Defined endpoints dropdowns
        const api = new Api();
        const promisedEndpoints = EndpointDAO.all();

        // Populate endpoint details
        const promisedAPI = api.get(apiUUID);

        const setSelectedEp = Promise.all([promisedEndpoints, promisedAPI]);
        setSelectedEp
            .then((responses) => {
                const epMap = new Map();
                for (const endpoint of responses[0]) {
                    epMap.set(endpoint.id, endpoint);
                }

                // this.setState({ endpointsMap: epMap });
                let inline = false;
                let currentProdEP = null;
                let currentSandboxEP = null;
                // let selectedProdEP = null;
                // let selectedSandboxEP = null;
                // let isGlobalEPSelectedSand = false;
                // let isGlobalEPSelectedProd = false;

                const endpointInAPI = responses[1].body.endpoint;
                for (const i in endpointInAPI) {
                    if (endpointInAPI[i].inline !== undefined) {
                        inline = true;
                        const endpointJSON = endpointInAPI[i].inline;
                        if (endpointInAPI[i].type === 'production') {
                            currentProdEP = new EndpointDAO(endpointJSON); // JSON.parse(endpointElement).serviceUrl;
                        } else if (endpointInAPI[i].type === 'sandbox') {
                            currentSandboxEP = new EndpointDAO(endpointJSON); // JSON.parse(endpoint).serviceUrl;
                        }
                    } else {
                        // global endpoint with key
                        const endpointKey = endpointInAPI[i].key;
                        if (endpointInAPI[i].type === 'production') {
                            currentProdEP = epMap[endpointKey].name;
                            // currentProdEP = JSON.parse(epMap[endpointKey].endpointConfig).serviceUrl;
                            // isGlobalEPSelectedProd = true;
                        } else if (endpointInAPI[i].type === 'sandbox') {
                            currentSandboxEP = epMap[endpointKey].name;
                            // currentSandboxEP = JSON.parse(epMap[endpointKey].endpointConfig).serviceUrl;
                            // isGlobalEPSelectedSand = true;
                        }
                    }
                }

                this.setState({
                    api: responses[1].data,
                    productionEndpoint: currentSandboxEP,
                    sandboxEndpoint: currentProdEP,
                    isInline: inline,
                    endpointsMap: epMap,
                });
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

    getURLType(serviceUrl) {
        // remove last : character
        return new URL(serviceUrl).protocol.replace(/:$/, '');
    }

    handleProductionInputs(e) {
        const prod = this.state.productionEndpoint;
        const eventName = e.target.name;
        if (eventName === 'uuid') {
            this.setState({ productionEndpoint: e.target.value });
        } else {
            prod[eventName] = e.target.value;
            this.setState({ productionEndpoint: prod });
        }
    }

    handleSandboxInputs(e) {
        const sandbox = this.state.sandboxEndpoint;
        const eventName = e.target.name;
        if (eventName === 'uuid') {
            this.setState({ sandboxEndpoint: e.target.value });
        } else {
            sandbox[eventName] = e.target.value;
            this.setState({ sandboxEndpoint: sandbox });
        }
    }

    updateEndpoints() {
        // this.setState({loading: true});
        const prod = this.state.productionEndpoint;
        const sandbox = this.state.sandboxEndpoint;
        const prodJSON = { type: 'production' };
        const sandboxJSON = { type: 'sandbox' };

        if (prod.url === undefined) {
            prodJSON.key = prod;
        } else if (prod.url != null) {
            const inline = {};
            inline.endpointConfig = JSON.stringify({ serviceUrl: prod.url });
            inline.endpointSecurity = { enabled: false };
            inline.type = this.getURLType(prod.url);
            inline.maxTps = 1000;
            prodJSON.inline = inline;
        }

        if (sandbox.url === undefined) {
            sandboxJSON.key = sandbox;
        } else if (sandbox.url != null) {
            const inline = {};
            inline.endpointConfig = JSON.stringify({ serviceUrl: sandbox.url });
            inline.endpointSecurity = { enabled: false };
            inline.type = this.getURLType(sandbox.url);
            inline.maxTps = 1000;
            sandboxJSON.inline = inline;
        }

        const api = new Api();
        const promisedAPI = api.get(this.apiUUID);

        const endpointArray = [];
        endpointArray.push(prodJSON);
        endpointArray.push(sandboxJSON);

        promisedAPI
            .then((response) => {
                const apiData = JSON.parse(response.data);
                apiData.endpoint = endpointArray;
                const promisedUpdate = api.update(apiData);
                promisedUpdate.then(() => {
                    Alert.info('Endpoints updated successfully');
                });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                Alert.error('Error occurred when updating endpoints');
            });
    }

    /**
     * Event handler for endpoints selector
     * @param {React.SyntheticEvent} event user select event
     * @memberof Endpoint
     */
    switchEndpoint(event) {
        const { name, value } = event.target;
        const { endpointsMap } = this.state;
        const endpoint = endpointsMap.get(value);
        if (name === 'production') {
            this.setState({ productionEndpoint: endpoint });
        } else if (name === 'sandbox') {
            this.setState({ sandboxEndpoint: endpoint });
        }
    }

    /**
     * @inheritDoc
     * @returns {React.Component} Render endpoints UI
     * @memberof Endpoint
     */
    render() {
        const {
            api, endpointsMap, productionEndpoint, sandboxEndpoint, isInline,
        } = this.state;
        const globalEndpoints = endpointsMap.values();
        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFountMessage} />;
        }
        if (!api) {
            return <Progress />;
        }

        return (
            <Paper>
                <Grid container spacing={16} justify='center'>
                    <Grid item md={6}>
                        <Typography variant='subheading' gutterBottom>
                            Production Endpoint
                        </Typography>
                        <EndpointsSelector
                            type='production'
                            onChange={this.switchEndpoint}
                            currentValue={isInline ? 'inline' : productionEndpoint.id}
                            endpoints={globalEndpoints}
                        />
                        <EndpointForm
                            endpoint={productionEndpoint}
                            handleInputs={isInline && this.handleProductionInputs}
                        />
                    </Grid>
                    <Grid item md={6}>
                        <Typography variant='subheading' gutterBottom>
                            Sandbox Endpoint
                        </Typography>
                        <EndpointsSelector
                            type='sandbox'
                            onChange={this.switchEndpoint}
                            currentValue={isInline ? 'inline' : sandboxEndpoint.id}
                            endpoints={globalEndpoints}
                        />
                        <EndpointForm endpoint={sandboxEndpoint} handleInputs={isInline && this.handleSandboxInputs} />
                    </Grid>
                </Grid>
                <Divider />
                <Grid container justify='flex-end'>
                    <Grid item md={5}>
                        {/* Allowing create endpoints based on scopes */}
                        <ScopeValidation resourcePath={resourcePath.ENDPOINTS} resourceMethod={resourceMethod.POST}>
                            <ApiPermissionValidation userPermissions={JSON.parse(this.state.api).userPermissionsForApi}>
                                <InteractiveButton variant='raised' color='primary' onClick={this.updateEndpoints}>
                                    Save
                                </InteractiveButton>
                            </ApiPermissionValidation>
                        </ScopeValidation>
                    </Grid>
                </Grid>
            </Paper>
        );
    }
}

Endpoint.defaultProps = {
    resourceNotFountMessage: {
        title: 'Resource Not Found Error',
        body: 'Resource Not Found',
    },
};

Endpoint.propTypes = {
    resourceNotFountMessage: PropTypes.shape({
        title: PropTypes.string,
        body: PropTypes.string,
    }),
    match: PropTypes.shape({
        params: PropTypes.shape({
            apiUUID: PropTypes.string,
        }),
    }).isRequired,
};
export default Endpoint;
