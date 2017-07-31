import React, {Component} from 'react'
import {Card, Button} from 'antd'

import GenericEndpointInputs from './GenericEndpointInputs'
import Api from '../../../../data/api'
import Loading from '../../../Base/Loading/Loading'


class Endpoint extends Component {

    constructor(props){
        super(props);
        this.state = {
            endpoints : {

            },
            productionEndpoint : {
            },
            sandboxEndpoint : {
            }
        };
        this.api_uuid = props.match.params.api_uuid;
        this.endpoint_type = props.endpoint_type;
        this.handleProductionInputs = this.handleProductionInputs.bind(this);
        this.handleSandboxInputs = this.handleSandboxInputs.bind(this);
        this.updateEndpoints = this.updateEndpoints.bind(this);
    }

    componentDidMount(){

        // Populate Defined endpoints dropdowns
        const api = new Api();
        const promised_endpoints = api.getEndpoints();
        /* TODO: Handle catch case , auth errors and ect ~tmkb*/
        promised_endpoints.then(
            response => {
                this.setState({endpoints: response.obj.list});
            }
        );

        // Populate endpoint details
        let promised_api = api.get(this.api_uuid);
        promised_api.then(
            response => {
                let api_response = response.obj;
                let default_prod_ep = null;
                let default_sandbox_ep = null;

                for(var i in api_response.endpoint) {

                    if(api_response.endpoint[i].inline != undefined) {
                        let endpoint_element = api_response.endpoint[i].inline.endpointConfig;
                        if(api_response.endpoint[i].type == 'production') {
                            default_prod_ep = JSON.parse(endpoint_element).serviceUrl;
                        }else if(api_response.endpoint[i].type == 'sandbox') {
                            default_sandbox_ep = JSON.parse(endpoint_element).serviceUrl;
                        }
                    }
                }

                this.setState({api: response.obj,
                    productionEndpoint : {url : default_prod_ep, username : "my-prod-username"},
                    sandboxEndpoint : {url : default_sandbox_ep, username : "my-sandbx-username"} });
            }
        ).catch(
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

    handleProductionInputs(e){
        let prod = this.state.productionEndpoint;
        prod[e.target.name]= e.target.value;
        this.setState({productionEndpoint: prod});
    }

    handleSandboxInputs(e){
        let sandbox = this.state.sandboxEndpoint;
        sandbox[e.target.name]= e.target.value;
        this.setState({sandboxEndpoint: sandbox});
    }

    updateEndpoints(e){

        //this.setState({loading: true});
        let prod = this.state.productionEndpoint;
        let sandbox = this.state.sandboxEndpoint;
        let prodJSON = null;
        let sandboxJSON = null;

        if(!prod.url == "") {
            prodJSON = {
                inline: {
                    endpointConfig: JSON.stringify({serviceUrl: prod.url}),
                    endpointSecurity: {enabled: false},
                    type: "http",
                    maxTps: 1000
                },
                type : "production"
            }
        }

        if(!sandbox.url == "") {
            sandboxJSON = {
                inline: {
                    endpointConfig: JSON.stringify({serviceUrl: sandbox.url}),
                    endpointSecurity: {enabled: false},
                    type: "http",
                    maxTps: 1000
                },
                type : "sandbox"
            }
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
        });
    }


    render() {

        if (!this.state.api) {
            return <Loading/>
        }

        return (
            <div>
                <Card title="Production Endpoint" bordered={false} style={{width: '90%', marginBottom: '10px'}}>
                    <GenericEndpointInputs handleInputs={this.handleProductionInputs} epList={this.state.endpoints} endpoint={this.state.productionEndpoint} match={this.props.match}/>
                </Card>
                <Card title="Sandbox Endpoint" bordered={false} style={{width: '90%'}}>
                    <GenericEndpointInputs handleInputs={this.handleSandboxInputs} epList={this.state.endpoints} endpoint={this.state.sandboxEndpoint} match={this.props.match}/>
                </Card>
                <Button style={{margin: "5px"}} type="primary" onClick={() => this.updateEndpoints()}>Save</Button>
            </div>
        );
    }
};

export default Endpoint