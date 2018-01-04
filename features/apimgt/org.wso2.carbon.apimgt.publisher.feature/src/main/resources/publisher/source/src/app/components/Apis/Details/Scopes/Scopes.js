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

import React from 'react'
import Api from '../../../../data/api'
import Loading from '../../../Base/Loading/Loading'
import Scope from "./Scope";
import {  Icon, Checkbox, Button, Card, Tag, Form} from 'antd';
import Input, {InputLabel} from 'material-ui/Input';
import TagsInput from 'react-tagsinput'
import { Row, Col } from 'antd';
import 'react-tagsinput/react-tagsinput.css'
import {message} from "antd/lib/index";
class Scopes extends React.Component {
    constructor(props) {
        super(props);
        this.api = new Api();
        this.api_uuid = props.match.params.api_uuid;
        this.state = {
            apiScopes: null,
            apiScope:{
                name: "",
                description: "",
                bindings: {
                    type: "role",
                    values: []
                }
            }
        };
        this.deleteScope = this.deleteScope.bind(this);
        this.updateScope = this.updateScope.bind(this);
        this.onChangeScopeName = this.onChangeScopeName.bind(this);
        this.onChangeScopeDescription = this.onChangeScopeDescription.bind(this);
        this.onChangeScopeRoleAddition = this.onChangeScopeRoleAddition.bind(this);
        this.addScope = this.addScope.bind(this);
    }

    deleteScope(scope_name) {
        let apiScopes = this.state.apiScopes;
        for (let apiScope in apiScopes) {
            if (apiScopes.hasOwnProperty(apiScope) && apiScopes[apiScope].name === scope_name) {
                apiScopes.splice(apiScope, 1);
                break;
            }
        }
        this.setState({active: false, apiScopes: apiScopes});
    }
    updateScope(scope_name,scopeObj) {
        let apiScopes = this.state.apiScopes;
        for (let apiScope in apiScopes) {
            if (apiScopes.hasOwnProperty(apiScope) && apiScopes[apiScope].name === scope_name) {
                apiScopes[apiScope].description = scopeObj.description;
                break;
            }
        }
        this.setState({active: false, apiScopes: apiScopes});
    }
    addScope(){
        const hideMessage = message.loading("Adding the Scope ...", 0);
        const api = new Api();
        let scope = this.state.apiScope;
        let promised_scope_add = api.addScope(this.props.match.params.api_uuid, scope);
        promised_scope_add.then(
            response => {
                if (response.status !== 201) {
                    console.log(response);
                    message.error("Something went wrong while updating the " + scope.name + " Scope!");
                    hideMessage();
                    return;
                }
                message.success(scope.name + " Scope added successfully!");
                let apiScopes = this.state.apiScopes;
                apiScopes[apiScopes.length] = this.state.apiScope;
                this.setState({active: false, apiScopes: apiScopes,apiScope:{}});
                hideMessage();
            }
        );
    }

    onChangeScopeName(e) {
        let {apiScope} = this.state;
        apiScope.name = e.target.value;
        this.setState({apiScope: apiScope});
    }
    onChangeScopeDescription(e) {
        let {apiScope} = this.state;
        apiScope.description = e.target.value;
        this.setState({apiScope: apiScope});
    }
    onChangeScopeRoleAddition(roles) {
        let apiScope = this.state.apiScope;
        apiScope.bindings.values = roles;
        this.setState({apiScope:apiScope})
    }
    componentDidMount() {

        const api = new Api();
        let promised_scopes_object = api.getScopes(this.api_uuid);
        promised_scopes_object.then(
            response => {
                this.setState({apiScopes: response.obj.list});
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

        let api_scopes = this.state.apiScopes;

        if (!api_scopes) {
            return <Loading/>
        }
        const {apiScope} = this.state;
        const values = apiScope.bindings ? apiScope.bindings.values : [];
         return (
             <div>
                 <Card title="Add Scope" style={{ width: "100%",marginBottom:20 }}>
                     <Row type="flex" justify="start">
                         <Col span={4}>Scope Name</Col>
                         <Col span={10}>
                             <Input onChange={this.onChangeScopeName} value={apiScope.name} />
                         </Col>
                     </Row>
                     <br/>
                     <Row type="flex" justify="start">
                         <Col span={4}>Description</Col>
                         <Col span={10}>
                             <Input onChange={this.onChangeScopeDescription} value={apiScope.description} />
                         </Col>
                     </Row>
                     <br/>
                     <Row type="flex" justify="start">
                         <Col span={4}>Roles</Col>
                         <Col span={10}>
                             <TagsInput value={values} onChange={this.onChangeScopeRoleAddition} onlyUnique={true} inputProps={{ placeholder: 'add role' }}/>
                         </Col>
                     </Row>
                     <br/>
                     <Row type="flex" justify="start">
                         <Col span={5}/>
                         <Col span={10}>
                             <Button type="primary"  onClick={this.addScope}>Add Scope to API</Button>
                         </Col>
                         <Col span={5}/>
                     </Row>

                 </Card>
             {
                    Object.keys(api_scopes).map(
                    (key) => {
                    let scope = api_scopes[key];
                        return (<Scope name={scope.name} description={scope.description} api_uuid={this.api_uuid} deleteScope={this.deleteScope} key={key} updateScope = {this.updateScope}/>);
                }
                    )}
             </div>
                    );

    }
}



export default Scopes