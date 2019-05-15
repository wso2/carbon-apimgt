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
import { Divider } from '@material-ui/core';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';
import SaveIcon from '@material-ui/icons/Save';
import EditIcon from '@material-ui/icons/Edit';
import CancelIcon from '@material-ui/icons/Cancel';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';

import EndpointDAO from 'AppData/Endpoint';
import Api from 'AppData/api';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import { Alert, Progress } from 'AppComponents/Shared';
import ApiPermissionValidation from 'AppData/ApiPermissionValidation';

import EndpointDetail from './EndpointDetail.jsx';

const styles = theme => ({
    button: {
        margin: theme.spacing.unit,
        float: 'right',
    },
    input: {
        display: 'none',
    },
});
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
            readOnly: true,
            disableSave: true,
        };
        this.handleProductionInputs = this.handleProductionInputs.bind(this);
        this.handleSandboxInputs = this.handleSandboxInputs.bind(this);
        this.updateEndpoints = this.updateEndpoints.bind(this);
        this.switchEndpoint = this.switchEndpoint.bind(this);
        this.makeEditable = this.makeEditable.bind(this);
        this.makeNonEditable = this.makeNonEditable.bind(this);
    }

    /**
     * @inheritDoc
     * @memberof Endpoint
     */
    componentDidMount() {
        const { api } = this.props;
        const promisedEndpoints = EndpointDAO.all();

        promisedEndpoints
            .then((response) => {
                const epMap = new Map();
                for (const endpoint of response) {
                    epMap.set(endpoint.id, endpoint);
                }

                let inline = false;
                let currentProdEP = null;
                let currentSandboxEP = null;
                const endpointInAPI = api.endpoint;
                for (const i in endpointInAPI) {
                    if (endpointInAPI[i].inline !== undefined) {
                        inline = true;
                        const endpointJSON = endpointInAPI[i].inline;
                        if (endpointInAPI[i].type === 'Production') {
                            currentProdEP = new EndpointDAO(endpointJSON); // JSON.parse(endpointElement).serviceUrl;
                            currentProdEP.inline = true;
                        } else if (endpointInAPI[i].type === 'Sandbox') {
                            currentSandboxEP = new EndpointDAO(endpointJSON); // JSON.parse(endpoint).serviceUrl;
                            currentSandboxEP.inline = true;
                        }
                    } else {
                        // global endpoint with key
                        const endpointKey = endpointInAPI[i].key;
                        if (endpointInAPI[i].type === 'Production') {
                            currentProdEP = epMap.get(endpointKey);
                            currentProdEP.inline = false;
                        } else if (endpointInAPI[i].type === 'Sandbox') {
                            currentSandboxEP = epMap.get(endpointKey);
                            currentSandboxEP.inline = false;
                        }
                    }
                }

                this.setState({
                    api,
                    productionEndpoint: currentProdEP,
                    sandboxEndpoint: currentSandboxEP,
                    isInline: inline,
                    endpointsMap: epMap,
                    disableSave: true,
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

    /**
     * Update the endpoint related data of the API.
     *
     * @memberof EndpointForm
     */
    updateEndpoints() {
        // this.setState({loading: true});

        const prod = this.state.productionEndpoint;
        const sandbox = this.state.sandboxEndpoint;
        const prodJSON = { type: 'Production' };
        const sandboxJSON = { type: 'Sandbox' };

        if (prod && !prod.inline) {
            prodJSON.key = prod.id;
        } else if (prod.id != null) {
            const inline = {};
            inline.endpointConfig = prod.endpointConfig;
            inline.endpointSecurity = prod.endpointSecurity;
            inline.type = prod.type;
            inline.maxTps = prod.maxTps;
            prodJSON.inline = inline;
        }

        if (sandbox && !sandbox.inline) {
            sandboxJSON.key = sandbox.id;
        } else if (sandbox.id != null) {
            const inline = {};
            inline.endpointConfig = sandbox.endpointConfig;
            inline.endpointSecurity = sandbox.endpointSecurity;
            inline.type = sandbox.type;
            inline.maxTps = sandbox.maxTps;
            sandboxJSON.inline = inline;
        }

        const api = new Api();
        const { id } = this.props.api;
        const promisedAPI = api.get(id);

        const endpointArray = [];
        endpointArray.push(prodJSON);
        endpointArray.push(sandboxJSON);

        promisedAPI.then((response) => {
            const apiData = JSON.parse(response.data);
            apiData.endpoint = endpointArray;
            const promisedUpdate = api.update(apiData);
            promisedUpdate
                .then(() => {
                    Alert.info('Endpoints updated successfully');
                })
                .catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.log(error);
                    }
                    Alert.error('Error occurred when updating endpoints');
                });
        });
    }

    makeEditable(disableSave) {
        this.state.readOnly = false;
        this.state.disableSave = disableSave;
        this.setState(this.state);
    }

    makeNonEditable(disableSave) {
        this.state.readOnly = true;
        this.state.disableSave = disableSave;
        this.setState(this.state);
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
        const { api, productionEndpoint, sandboxEndpoint } = this.state;
        const copyOfProdEndpoint = JSON.parse(JSON.stringify(productionEndpoint));
        const copyOfSandboxEndpoint = JSON.parse(JSON.stringify(sandboxEndpoint));
        const { classes } = this.props;
        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFountMessage} />;
        }
        if (!api) {
            return <Progress />;
        }
        return (
            <React.Fragment>
                {this.state.readOnly && (
                    <IconButton className={classes.button} aria-label='Edit' color='primary' onClick={() => this.makeEditable(false)}>
                        <EditIcon />
                    </IconButton>
                )}
                {!this.state.readOnly && (
                    <IconButton className={classes.button} aria-label='Cancel' onClick={() => this.makeNonEditable(true)}>
                        <CancelIcon />
                    </IconButton>
                )}
                {productionEndpoint && <EndpointDetail type={<FormattedMessage id='production' defaultMessage='Production' />} endpoint={productionEndpoint} initialValue={copyOfProdEndpoint} isInline={productionEndpoint.inline} readOnly={this.state.readOnly} makeNonEditable={this.makeNonEditable} />}
                {sandboxEndpoint && <EndpointDetail type={<FormattedMessage id='sandbox' defaultMessage='Sandbox' />} endpoint={sandboxEndpoint} initialValue={copyOfSandboxEndpoint} isInline={sandboxEndpoint.inline} readOnly={this.state.readOnly} makeNonEditable={this.makeNonEditable} />}
                <Divider />
                <ApiPermissionValidation userPermissions={api.userPermissionsForApi}>
                    <Button color='primary' onClick={this.updateEndpoints} disabled={this.state.disableSave} >
                        Save
                    </Button>
                </ApiPermissionValidation>
            </React.Fragment>
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
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(Endpoint);
