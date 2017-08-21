import React, {Component} from 'react'
import {Table} from 'antd';
import API from '../../../../data/api'
import Loading from '../../../Base/Loading/Loading'
import ApiPermissionValidation from '../../../../data/ApiPermissionValidation'

class Subscriptions extends Component {
    constructor(props)   {
        super(props);
        this.api_uuid = props.match.params.api_uuid;
        this.state = {
            subscriptions: null
        };
        this.updateProductionSubscription = this.updateProductionSubscription.bind(this);
    }

    componentDidMount() {
        const api = new API();
        let promised_api = api.get(this.api_uuid);
        promised_api.then(
            response => {
                this.setState({api: response.obj});
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
       this.fetchData();
    }

    fetchData(){
        const api = new API();
        const promised_endpoints = api.subscriptions(this.api_uuid);
        /* TODO: Handle catch case , auth errors and ect ~tmkb*/
        promised_endpoints.then(
                response => {
                    this.setState({subscriptions: response.obj.list});
                }
        );
    }

    updateProductionSubscription(event)    {
        const currentState = event.target.dataset.currentState;
        const subscriptionId = event.target.dataset.sample;
        const api = new API();
        let setState = "";
        const eventId = event.target.id;

        if(eventId === "PROD_ONLY_BLOCKED" ){
            if (event.target.checked)   {
                if(currentState === "SANDBOX_ONLY_BLOCKED") {
                    setState = "BLOCKED"
                } else {
                    setState = "PROD_ONLY_BLOCKED";
                }

            } else {
                if (currentState === "BLOCKED") {
                    setState = "SANDBOX_ONLY_BLOCKED";
                } else {
                    setState = "ACTIVE";
                }

            }

        } else if(eventId === "SANDBOX_ONLY_BLOCKED") {

            if (event.target.checked)   {
                if(currentState === "PROD_ONLY_BLOCKED") {
                    setState = "BLOCKED"
                } else {
                    setState = "SANDBOX_ONLY_BLOCKED";
                }

            } else {
                if (currentState === "BLOCKED") {
                    setState = "PROD_ONLY_BLOCKED";
                } else {
                    setState = "ACTIVE";
                }

            }
        }
        const promised_subs = api.blockSubscriptions(subscriptionId, setState);
        promised_subs.then(
                response => {
                    this.fetchData()
                }
        )
    }



    render()    {
        if (!this.state.api) {
            return <Loading/>
        }
        const {subscriptions} = this.state;
        const columns = [{
            title: 'Application',
            render: (a,b,c) => <div>{a.applicationInfo.name}</div>
        },  {
            title: 'Access Control for',
            render: (a,b,c) => (
                    <div>
                    <ApiPermissionValidation userPermissions={this.state.api.userPermissionsForApi}>
                        <ApiPermissionValidation checkingPermissionType={ApiPermissionValidation.permissionType.MANAGE_SUBSCRIPTION}
                                                                 userPermissions={this.state.api.userPermissionsForApi}>
                            <label htmlFor="production">Production </label>
                            <input type="checkbox" checked={a.subscriptionStatus === "PROD_ONLY_BLOCKED" ||
                                                                                    a.subscriptionStatus === "BLOCKED"}
                                data-sample={a.subscriptionId} id="PROD_ONLY_BLOCKED"
                                data-current={a.subscriptionStatus}
                                style={{marginRight: "100px"}}
                                onChange={this.updateProductionSubscription}
                            />
                            <label htmlFor="sandbox">Sandbox</label>
                            <input checked={a.subscriptionStatus === "SANDBOX_ONLY_BLOCKED" ||
                                                                                    a.subscriptionStatus === "BLOCKED"}
                                type="checkbox" id="SANDBOX_ONLY_BLOCKED" data-sample={a.subscriptionId}
                                data-current={a.subscriptionStatus}
                                onChange={this.updateProductionSubscription}/>
                        </ApiPermissionValidation>
                     </ApiPermissionValidation>
                   </div>)
        }];
        return (
                <div>
                    <h1>API Subscriptions</h1>

                    <Table columns={columns} dataSource={subscriptions}/>

                </div>
        )
    }
}

export default Subscriptions
