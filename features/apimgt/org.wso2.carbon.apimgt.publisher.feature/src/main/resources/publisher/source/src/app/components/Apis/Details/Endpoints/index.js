import React, {Component} from 'react'
import {Card, Button, message} from 'antd'

import GenericEndpointInputs from './GenericEndpointInputs'
import Api from '../../../../data/api'
import Loading from '../../../Base/Loading/Loading'
import ApiPermissionValidation from '../../../../data/ApiPermissionValidation'


class Endpoint extends Component {

    constructor(props) {
        super(props);
        this.state = {
            endpoints: {},
            productionEndpoint: {},
            sandboxEndpoint: {},
            dropDownItems : {}
        };
        this.api_uuid = props.match.params.api_uuid;
        this.endpoint_type = props.endpoint_type;
        this.handleProductionInputs = this.handleProductionInputs.bind(this);
        this.handleSandboxInputs = this.handleSandboxInputs.bind(this);
        this.updateEndpoints = this.updateEndpoints.bind(this);
        this.dropdownItems =  null;
    }

    componentDidMount() {

        // Populate Defined endpoints dropdowns
        const api = new Api();
        let promised_endpoints = api.getEndpoints();

        // Populate endpoint details
        let promised_api = api.get(this.api_uuid);

        let setSelectedEp = Promise.all([promised_endpoints, promised_api]).then(
            (response) => {
                let epMap = {};
                this.dropdownItems = [<Option key="custom">Custom...</Option>];
                for (let ep of JSON.parse(response[0].data).list) {
                    epMap[ep.id] = ep;
                    // construct dropdown
                    this.dropdownItems.push(<Option key={ep.id}>{ep.name}</Option>);
                }

                this.setState({endpoints: epMap});

                let default_prod_ep = null;
                let default_sandbox_ep = null;
                let selected_prod_ep = null;
                let selected_sandbox_ep = null;
                let isGlobalEPSelectedSand = false;
                let isGlobalEPSelectedProd = false;

                let endpointInAPI = JSON.parse(response[1].data).endpoint;
                for (var i in endpointInAPI) {

                    if (endpointInAPI[i].inline != undefined) {
                        let endpoint_element = endpointInAPI[i].inline.endpointConfig;
                        if (endpointInAPI[i].type == 'production') {
                            default_prod_ep = JSON.parse(endpoint_element).serviceUrl;
                        } else if (endpointInAPI[i].type == 'sandbox') {
                            default_sandbox_ep = JSON.parse(endpoint_element).serviceUrl;
                        }
                    } else { // global endpoint with key
                        let endpoint_key = endpointInAPI[i].key;
                        if (endpointInAPI[i].type == 'production') {
                            selected_prod_ep = epMap[endpoint_key].name;
                            default_prod_ep = JSON.parse(epMap[endpoint_key].endpointConfig).serviceUrl;
                            isGlobalEPSelectedProd = true;
                        } else if (endpointInAPI[i].type == 'sandbox') {
                            selected_sandbox_ep = epMap[endpoint_key].name;
                            default_sandbox_ep = JSON.parse(epMap[endpoint_key].endpointConfig).serviceUrl;
                            isGlobalEPSelectedSand = true;
                        }
                    }
                }

                this.setState({
                    api: response[1].data,
                    productionEndpoint: {
                        url: default_prod_ep,
                        username: "",
                        selectedep: selected_prod_ep,
                        isGlobalEPSelected: isGlobalEPSelectedProd
                    },
                    sandboxEndpoint: {
                        url: default_sandbox_ep,
                        username: "",
                        selectedep: selected_sandbox_ep,
                        isGlobalEPSelected: isGlobalEPSelectedSand
                    }
                });


            }).catch(
            error => {
                if (process.env.NODE_ENV !== "production") {
                    console.log(error);
                }
                let status = error.status;
                if (status === 404) {
                    this.setState({notFound: true});
                }
            }
        );
    }


    handleProductionInputs(e) {
        let prod = this.state.productionEndpoint;
        const eventName = e.target.name;
        if (eventName === "uuid") {
            this.setState({productionEndpoint: e.target.value})
        } else {
            prod[eventName] = e.target.value;
            this.setState({productionEndpoint: prod});

        }
    }

    handleSandboxInputs(e) {
        let sandbox = this.state.sandboxEndpoint;
        const eventName = e.target.name;
        if (eventName === "uuid") {
            this.setState({sandboxEndpoint: e.target.value})
        } else {
            sandbox[eventName] = e.target.value;
            this.setState({sandboxEndpoint: sandbox});
        }
    }


    getURLType(serviceUrl) {
        // remove last : character
        return new URL(serviceUrl).protocol.replace(/\:$/, '');;
    }

    updateEndpoints(e) {

        //this.setState({loading: true});
        let prod = this.state.productionEndpoint;
        let sandbox = this.state.sandboxEndpoint;
        let prodJSON = {type: "production"};
        let sandboxJSON = {type: "sandbox"};

        if (prod.url === undefined) {
            prodJSON.key = prod;
        } else if(prod.url != null) {
            let inline = {};
            inline.endpointConfig = JSON.stringify({serviceUrl: prod.url});
            inline.endpointSecurity = {enabled: false};
            inline.type = this.getURLType(prod.url);
            inline.maxTps = 1000;
            prodJSON.inline = inline;
        }

        if (sandbox.url === undefined) {
            sandboxJSON.key = sandbox;
        } else if (sandbox.url != null ) {
            let inline = {};
            inline.endpointConfig = JSON.stringify({serviceUrl: sandbox.url});
            inline.endpointSecurity = {enabled: false};
            inline.type = this.getURLType(sandbox.url);
            inline.maxTps = 1000;
            sandboxJSON.inline = inline;
        }

        const api = new Api();
        let promised_api = api.get(this.api_uuid);

        let endpointArray = new Array();
        endpointArray.push(prodJSON);
        endpointArray.push(sandboxJSON);

        promised_api.then(response => {
            let api_data = JSON.parse(response.data);
            api_data.endpoint = endpointArray;
            let promised_update = api.update(api_data);
            promised_update.then(response => {
                this.setState({loading: false});
                message.info("Endpoints updated successfully");
            })
        }).catch(
            error => {
                if (process.env.NODE_ENV !== "production") {
                    console.log(error);
                }
                message.error("Error occurred when updating endpoints");
            }
        );
    }



    render() {

        if (!this.state.api) {
            return <Loading/>
        }

        return (
            <div>
                <Card title="Production Endpoint" bordered={false} style={{width: '90%', marginBottom: '10px'}}>
                    <GenericEndpointInputs handleInputs={this.handleProductionInputs}
                                           epList={this.state.endpoints}
                                           endpoint={this.state.productionEndpoint}
                                           dropdownItems={this.dropdownItems}
                                           match={this.props.match}/>
                </Card>
                <Card title="Sandbox Endpoint" bordered={false} style={{width: '90%'}}>
                    <GenericEndpointInputs handleInputs={this.handleSandboxInputs}
                                           epList={this.state.endpoints}
                                           dropdownItems={this.dropdownItems}
                                           endpoint={this.state.sandboxEndpoint}
                                           match={this.props.match}/>
                </Card>
                <ApiPermissionValidation userPermissions={JSON.parse(this.state.api).userPermissionsForApi}>
                    <Button style={{margin: "5px"}} type="primary" onClick={() => this.updateEndpoints()}>Save</Button>
                </ApiPermissionValidation>
            </div>
        );
    }
}
;

export default Endpoint