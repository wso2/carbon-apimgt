import React, { Component } from 'react';
import { Grid, Paper, Typography, Divider } from 'material-ui';
import PropTypes from 'prop-types';

import GenericEndpointInputs from './GenericEndpointInputs';
import Api from '../../../../data/api';
import ApiPermissionValidation from '../../../../data/ApiPermissionValidation';
import { resourceMethod, resourcePath, ScopeValidation } from '../../../../data/ScopeValidation';
import ResourceNotFound from '../../../Base/Errors/ResourceNotFound';
import { Alert, InteractiveButton, Progress } from '../../../Shared';
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
            productionEndpoint: {},
            sandboxEndpoint: {},
            notFound: false,
        };
        this.apiUUID = props.match.params.apiUUID;
        this.handleProductionInputs = this.handleProductionInputs.bind(this);
        this.handleSandboxInputs = this.handleSandboxInputs.bind(this);
        this.updateEndpoints = this.updateEndpoints.bind(this);
    }

    /**
     * @inheritDoc
     * @memberof Endpoint
     */
    componentDidMount() {
        // Populate Defined endpoints dropdowns
        const api = new Api();
        const promisedEndpoints = api.getEndpoints();

        // Populate endpoint details
        const promisedAPI = api.get(this.apiUUID);

        const setSelectedEp = Promise.all([promisedEndpoints, promisedAPI]);
        setSelectedEp
            .then((responses) => {
                const epMap = new Map();
                for (const ep of responses[0].body.list) {
                    epMap.set(ep.id, ep);
                }

                this.setState({ endpointsMap: epMap });

                let defaultProdEP = null;
                let defaultSandboxEP = null;
                let selectedProdEP = null;
                let selectedSandboxEP = null;
                let isGlobalEPSelectedSand = false;
                let isGlobalEPSelectedProd = false;

                const endpointInAPI = responses[1].body.endpoint;
                for (const i in endpointInAPI) {
                    if (endpointInAPI[i].inline !== undefined) {
                        const endpointElement = endpointInAPI[i].inline.endpointConfig;
                        if (endpointInAPI[i].type === 'production') {
                            defaultProdEP = JSON.parse(endpointElement).serviceUrl;
                        } else if (endpointInAPI[i].type === 'sandbox') {
                            defaultSandboxEP = JSON.parse(endpointElement).serviceUrl;
                        }
                    } else {
                        // global endpoint with key
                        const endpointKey = endpointInAPI[i].key;
                        if (endpointInAPI[i].type === 'production') {
                            selectedProdEP = epMap[endpointKey].name;
                            defaultProdEP = JSON.parse(epMap[endpointKey].endpointConfig).serviceUrl;
                            isGlobalEPSelectedProd = true;
                        } else if (endpointInAPI[i].type === 'sandbox') {
                            selectedSandboxEP = epMap[endpointKey].name;
                            defaultSandboxEP = JSON.parse(epMap[endpointKey].endpointConfig).serviceUrl;
                            isGlobalEPSelectedSand = true;
                        }
                    }
                }

                this.setState({
                    api: responses[1].data,
                    productionEndpoint: {
                        url: defaultProdEP,
                        username: '',
                        selectedEP: selectedProdEP,
                        isGlobalEPSelected: isGlobalEPSelectedProd,
                    },
                    sandboxEndpoint: {
                        url: defaultSandboxEP,
                        username: '',
                        selectedEP: selectedSandboxEP,
                        isGlobalEPSelected: isGlobalEPSelectedSand,
                    },
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
     * @inheritDoc
     * @returns {React.Component} Render endpoints UI
     * @memberof Endpoint
     */
    render() {
        const { api, endpointsMap } = this.state;
        const { match } = this.props;

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
                        <GenericEndpointInputs
                            endpointsMap={endpointsMap}
                            match={match}
                            endpoint={this.state.productionEndpoint}
                            handleInputs={this.handleProductionInputs}
                        />
                    </Grid>
                    <Grid item md={6}>
                        <Typography variant='subheading' gutterBottom>
                            Sandbox Endpoint
                        </Typography>
                        <GenericEndpointInputs
                            endpointsMap={endpointsMap}
                            match={match}
                            endpoint={this.state.sandboxEndpoint}
                            handleInputs={this.handleSandboxInputs}
                        />
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
