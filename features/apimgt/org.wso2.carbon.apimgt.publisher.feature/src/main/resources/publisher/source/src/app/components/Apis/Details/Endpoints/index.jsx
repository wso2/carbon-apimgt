import React, { Component } from 'react';
import { Grid, Button } from 'material-ui';
import PropTypes from 'prop-types';

import GenericEndpointInputs from './GenericEndpointInputs';
import Api from '../../../../data/api';
import Loading from '../../../Base/Loading/Loading';
import ApiPermissionValidation from '../../../../data/ApiPermissionValidation';
import { resourceMethod, resourcePath, ScopeValidation } from '../../../../data/ScopeValidation';
import ResourceNotFound from '../../../Base/Errors/ResourceNotFound';
import Alert from '../../../Shared/Alert';

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
            endpoints: {},
            productionEndpoint: {},
            sandboxEndpoint: {},
            notFound: false,
        };
        this.apiUUID = props.match.params.apiUUID;
        this.handleProductionInputs = this.handleProductionInputs.bind(this);
        this.handleSandboxInputs = this.handleSandboxInputs.bind(this);
        this.updateEndpoints = this.updateEndpoints.bind(this);
        this.dropdownItems = null;
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
            .then((response) => {
                const epMap = {};
                this.dropdownItems = [<option key='custom'>Custom...</option>];
                for (const ep of JSON.parse(response[0].data).list) {
                    epMap[ep.id] = ep;
                    // construct dropdown
                    this.dropdownItems.push(<option key={ep.id}>{ep.name}</option>);
                }

                this.setState({ endpoints: epMap });

                let defaultProdEP = null;
                let defaultSandboxEP = null;
                let selectedProdEP = null;
                let selectedSandboxEP = null;
                let isGlobalEPSelectedSand = false;
                let isGlobalEPSelectedProd = false;

                const endpointInAPI = JSON.parse(response[1].data).endpoint;
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
                    api: response[1].data,
                    productionEndpoint: {
                        url: defaultProdEP,
                        username: '',
                        selectedep: selectedProdEP,
                        isGlobalEPSelected: isGlobalEPSelectedProd,
                    },
                    sandboxEndpoint: {
                        url: defaultSandboxEP,
                        username: '',
                        selectedep: selectedSandboxEP,
                        isGlobalEPSelected: isGlobalEPSelectedSand,
                    },
                });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
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
        const { api } = this.state;

        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFountMessage} />;
        }
        if (!api) {
            return <Loading />;
        }

        return (
            <div>
                <Grid container spacing={16} justify='center'>
                    <Grid item md={5} title='Production Endpoint'>
                        <GenericEndpointInputs
                            handleInputs={this.handleProductionInputs}
                            epList={this.state.endpoints}
                            endpoint={this.state.productionEndpoint}
                            dropdownItems={this.dropdownItems}
                            match={this.props.match}
                        />
                    </Grid>
                    <Grid item md={5} title='Sandbox Endpoint'>
                        <GenericEndpointInputs
                            handleInputs={this.handleSandboxInputs}
                            epList={this.state.endpoints}
                            dropdownItems={this.dropdownItems}
                            endpoint={this.state.sandboxEndpoint}
                            match={this.props.match}
                        />
                    </Grid>
                </Grid>
                <Grid container justify='flex-end'>
                    <Grid item md={4}>
                        {/* Allowing create endpoints based on scopes */}
                        <ScopeValidation resourcePath={resourcePath.ENDPOINTS} resourceMethod={resourceMethod.POST}>
                            <ApiPermissionValidation userPermissions={JSON.parse(this.state.api).userPermissionsForApi}>
                                <Button variant='raised' color='primary' onClick={this.updateEndpoints}>
                                    Save
                                </Button>
                            </ApiPermissionValidation>
                        </ScopeValidation>
                    </Grid>
                </Grid>
            </div>
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
