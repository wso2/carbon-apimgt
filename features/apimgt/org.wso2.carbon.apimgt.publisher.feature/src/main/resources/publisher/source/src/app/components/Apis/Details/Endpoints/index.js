import React, {Component} from 'react'
import {Card, Button} from 'antd'

import GenericEndpointInputs from './GenericEndpointInputs'
import Api from '../../../../data/api'
import Loading from '../../../Base/Loading/Loading'


class Endpoint extends Component {

    constructor(props){
        super(props);
        this.state = {
            api : null,
            productionEndpointUrl : null
        };
        this.api_uuid = props.match.params.api_uuid;
        this.endpoint_type = props.endpoint_type;
    }

    componentDidMount(){
        const api = new Api();
        let promised_api = api.get(this.api_uuid);
        promised_api.then(
            response => {
                let api_response = response.obj;
                let default_prod_ep = null;

                console.log(api_response);

                for(var i in api_response.endpoint) {
                    if(api_response.endpoint[i].inline != undefined && api_response.endpoint[i].type == 'production') {
                        let endpoint_element = api_response.endpoint[i].inline.endpointConfig;
                        default_prod_ep = JSON.parse(endpoint_element).serviceUrl;
                    }
                }

                this.setState({api: response.obj, productionEndpointUrl: default_prod_ep});
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

    render() {

        if (!this.state.api) {
            return <Loading/>
        }

        return (
            <div>
                <Card title="Production Endpoint" bordered={false} style={{width: '90%', marginBottom: '10px'}}>
                    <GenericEndpointInputs endpoint_url={this.state.productionEndpointUrl} endpoint_type="production" match={this.props.match}/>
                </Card>
                <Card title="Sandbox Endpoint" bordered={false} style={{width: '90%'}}>
                    <GenericEndpointInputs endpoint_url="test.sandbox.url" endpoint_type="sandbox" match={this.props.match}/>
                </Card>
                <Button style={{margin: "5px"}} type="primary">Save</Button>
            </div>
        );
    }
};

export default Endpoint