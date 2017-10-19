
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

import React, {Component} from 'react'
import {Link} from 'react-router-dom'
import {
    Col,
    Row,
    Card,
    Radio,
    InputNumber,
    Switch,
    Button,
    message,
    Form,
    Dropdown,
    Tag,
    Menu,
    Badge
} from 'antd';

const FormItem = Form.Item;
import Api from '../../../data/api'
import Loading from '../../Base/Loading/Loading'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import ApiPermissionValidation from '../../../data/ApiPermissionValidation'
import {ScopeValidation, resourceMethod, resourcePath} from '../../../data/ScopeValidation'
import {Checkbox} from 'antd';
import {Table, Icon} from 'antd';
import { MenuItem } from 'material-ui/Menu';
import Select from 'material-ui/Select'
import Input from 'material-ui/Input'

const RadioButton = Radio.Button;
const RadioGroup = Radio.Group;


class Permission extends Component {
    constructor(props) {
        super(props);
        this.state = {
            api: null,
            notFound: false,
            visibility: "PUBLIC",
            visibleRoles:[],
            visibleRolesDisplay:"none",
            roleField: "",
            readField: false,
            updateField: false,
            deleteField: false,
            apikeyField: false,
            oauthField: false,
            manageSubField: false,
            permissionData: []
        };
        this.api_uuid = this.props.match.params.api_uuid;
        this.handleChangeVisibilityField = this.handleChangeVisibilityField.bind(this);
        this.handleChangeVisibilityRolesField = this.handleChangeVisibilityRolesField.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleInputs = this.handleInputs.bind(this);
        this.handleSwitch = this.handleSwitch.bind(this);
        this.handleMaxTPS = this.handleMaxTPS.bind(this);
        this.handleChangeRoleField = this.handleChangeRoleField.bind(this);
        this.handleChangeReadField = this.handleChangeReadField.bind(this);
        this.handleChangeUpdateField = this.handleChangeUpdateField.bind(this);
        this.handleChangeDeleteField = this.handleChangeDeleteField.bind(this);
        this.toggleOauthSelect = this.toggleOauthSelect.bind(this);
        this.toggleApiKeySelect = this.toggleApiKeySelect.bind(this);
        this.handleChangeManageSubField = this.handleChangeManageSubField.bind(this);
    }

    componentDidMount() {
        const api = new Api();
        let promised_api = api.get(this.api_uuid);
        promised_api.then(
            response => {
                this.setState({api: response.obj});
                this.getExistingPermissions(this);
                this.getExistingSecuritySchemes(this);
                this.getExistingVisibility(this);
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

    handleChangeVisibilityField(event) {
        this.setState({visibility: event.target.value});
        if(event.target.value == 'RESTRICTED'){
            this.setState({visibleRolesDisplay: "inline"});
        } else {
            this.setState({visibleRolesDisplay: "none"});
        }

    }

    handleChangeVisibilityRolesField(event){
        let roleList = event.target.value.split(',');
        let roleList2 = roleList.map(e => String(e).trim());
        this.setState({visibleRoles: roleList2})
    }

    handleSwitch(secured) {
        this.setState({secured: secured})
    }

    handleInputs(e) {
        this.setState({[e.target.name]: e.target.value});
    }

    handleMaxTPS(maxTPS) {
        this.setState({maxTPS: maxTPS});
    }

    handleSubmit = (e) => {
        const api = new Api();
        let promised_api = api.get(this.api_uuid);
        promised_api.then(
            response => {
                var api_data = JSON.parse(response.data);
                api_data.visibility = this.state.visibility;
                api_data.visibleRoles = this.state.visibleRoles;
                var permissionString = this.createPermissionJsonString(this);
                api_data.permission = permissionString;
                var securitySchemes = this.createSecuritySchemeArray(this);
                api_data.securityScheme = securitySchemes;
                let promised_update = api.update(api_data);
                promised_update.then(
                    response => {
                        message.success("Permissions updated successfully");
                    }
                ).catch (
                    error => {
                        console.error(error);
                        message.error("Error occurred while updating permissions!");
                        this.setState({loading: false});
                    }
                );
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



    handleChangeRoleField(event) {
        this.setState({roleField: event.target.value});
    }

    handleChangeReadField(event) {
        this.setState({readField: event.target.checked});
    }

    handleChangeUpdateField(event) {
        this.setState({updateField: event.target.checked});
    }

    handleChangeDeleteField(event) {
        this.setState({deleteField: event.target.checked});
    }

    handleChangeManageSubField(event) {
        this.setState({manageSubField: event.target.checked});
    }

    toggleOauthSelect(event) {
        this.setState({oauthField: event.target.checked});
    }

    toggleApiKeySelect(event) {
        this.setState({apikeyField: event.target.checked});
    }

    handleAddRole(permissionData) {
        var groupPermissions = {};
        groupPermissions['key'] = this.state.roleField.trim();
        groupPermissions['isRead'] = this.state.readField;
        groupPermissions['isUpdate'] = this.state.updateField;
        groupPermissions['isDelete'] = this.state.deleteField;
        groupPermissions['isManageSubscription'] = this.state.manageSubField;
        permissionData.push(groupPermissions);
        this.setState({permissionData: permissionData,
            roleField: null,
            readField: false,
            updateField: false,
            deleteField: false,
            manageSubField: false});
    }

    handleRemoveRole(value) {
        var permissionData = this.state.permissionData.slice();
        var index = -1;
        for(var i = 0; i < permissionData.length; i++) {
            if(permissionData[i]["key"] === value) {
                index = i;
            }
        }
        permissionData.splice(index, 1);
        this.setState({permissionData: permissionData});
    }

    createPermissionJsonString() {
        var apiPermissionArray = [];
        var permissionData = this.state.permissionData;
        Object.keys(permissionData).map(function(key) {
            var groupPermissions = {};
            var permissionSubJson = permissionData[key];
            var roleName = permissionSubJson['key'];
            var isRead = permissionSubJson['isRead'];;
            var isUpdate = permissionSubJson['isUpdate'];
            var isDelete = permissionSubJson['isDelete'];
            var isManageSubscription = permissionSubJson['isManageSubscription'];
            var permissionArray = [];
            if (isRead) {
                permissionArray.push(ApiPermissionValidation.permissionType.READ);
            }
            if (isUpdate) {
                permissionArray.push(ApiPermissionValidation.permissionType.UPDATE);
            }
            if (isDelete) {
                permissionArray.push(ApiPermissionValidation.permissionType.DELETE);
            }
            if (isManageSubscription) {
                permissionArray.push(ApiPermissionValidation.permissionType.MANAGE_SUBSCRIPTION);
            }
            groupPermissions['groupId'] = roleName;
            groupPermissions['permission'] = permissionArray;
            apiPermissionArray.push(groupPermissions);
        });
        var updatedPermissionString = JSON.stringify(apiPermissionArray);
        if (updatedPermissionString == "[]") {
            return "";
        } else {
            return updatedPermissionString;
        }
    }

    createSecuritySchemeArray() {
        var securitySchemes = [];
        if(this.state.oauthField) {
            securitySchemes.push("Oauth");
        }
        if(this.state.apikeyField) {
            securitySchemes.push("apikey");
        }

        return securitySchemes;
    }

    getExistingSecuritySchemes() {
        var securitySchemes = this.state.api.securityScheme;
        if (securitySchemes.includes("Oauth")) {
            this.setState({oauthField: true});
        } else {
            this.setState({oauthField: false});
        }
        if (securitySchemes.includes("apikey")) {
            this.setState({apikeyField: true});
        } else {
            this.setState({apikeyField: false});
        }
    }

    getExistingVisibility() {
        var visibility = this.state.api.visibility;
        this.setState({visibility: visibility});

        if(visibility == 'RESTRICTED'){
            this.setState({visibleRolesDisplay: "inline"});
        } else {
            this.setState({visibleRolesDisplay: "none"});
        }

        var visibleRoles = this.state.api.visibleRoles;
        console.log(visibleRoles);
        this.setState({visibleRoles: visibleRoles})
    }

    getExistingPermissions() {
        if (!this.state.api) {
            return <Loading/>
        }
        const permissionData = [];
        var permissionString = this.state.api.permission;
        if (permissionString != "") {
            const permissionJson = JSON.parse(permissionString);
            Object.keys(permissionJson).map(function(key) {
                /* permissionSubJson contains permissions for a single role*/
                var permissionSubJson = permissionJson[key];
                var roleName = permissionSubJson['groupId'];
                var permissionArray = permissionSubJson['permission'];
                var isRead = false;
                var isUpdate = false;
                var isDelete = false;
                var isManageSubscription = false;

                var groupPermissions = {};

                if(permissionArray.includes(ApiPermissionValidation.permissionType.READ)) {
                    isRead = true;
                }
                if (permissionArray.includes(ApiPermissionValidation.permissionType.UPDATE)){
                    isUpdate = true;
                }
                if (permissionArray.includes(ApiPermissionValidation.permissionType.DELETE)) {
                    isDelete = true;
                }
                if (permissionArray.includes(ApiPermissionValidation.permissionType.MANAGE_SUBSCRIPTION)) {
                    isManageSubscription = true;
                }
                groupPermissions['key'] = roleName;
                groupPermissions['isRead'] = isRead;
                groupPermissions['isUpdate'] = isUpdate;
                groupPermissions['isDelete'] = isDelete;
                groupPermissions['isManageSubscription'] = isManageSubscription;
                permissionData.push(groupPermissions);
            });
        }
        this.setState({permissionData: permissionData});
    }


    render() {
        const {getFieldDecorator} = this.props.form;
        const formItemLayout = {
            labelCol: {span: 6},
            wrapperCol: {span: 18}
        };

        const columns = [{
            title: 'Role',
            dataIndex: 'key',
            key: 'key',
            render: text => <a href="#">{text}</a>,
        }, {
            title: 'Read',
            dataIndex: 'isRead',
            key: 'isRead',
            render: isChecked => <Checkbox checked={isChecked} name="read"
                                           value={ApiPermissionValidation.permissionType.READ}></Checkbox>
        }, {
            title: 'Update',
            dataIndex: 'isUpdate',
            key: 'isUpdate',
            render: isChecked => <Checkbox checked={isChecked} name="update"
                                           value={ApiPermissionValidation.permissionType.UPDATE}></Checkbox>
        }, {
            title: 'Delete',
            dataIndex: 'isDelete',
            key: 'isDelete',
            render: isChecked => <Checkbox checked={isChecked} name="delete"
                                           value={ApiPermissionValidation.permissionType.DELETE}></Checkbox>
        }, {
            title: 'Manage Subscriptions',
            dataIndex: 'isManageSubscription',
            key: 'isManageSubscription',
            render: isChecked => <Checkbox checked={isChecked} name="manage_subscription"
                                           value={ApiPermissionValidation.permissionType.MANAGE_SUBSCRIPTION}></Checkbox>
        }, {
            title: 'Action',
            key: 'action',
            render: (text, record) => (
                <span>
                    <a href="#" onClick={this.handleRemoveRole.bind(this, record.key)}>Delete</a>
                </span>
            ),
        }];

        const columnsOfScopeTable = [{
            title: 'Scopes',
            dataIndex: 'name',
            key: 'name',
            render: text => <a href="#">{text}</a>,
        }, {
            title: '',
            key: 'delete',
            render: (text, record) => (
                <span>
                    <a href="#">Delete</a>
                </span>
            ),
        }];

        const dataOfScopes = [];

        const permissionData = this.state.permissionData;

        const api = this.state.api;

        if (this.state.notFound) {
            return <ResourceNotFound/>
        }
        if (!this.state.api) {
            return <Loading/>
        }

        return (

            <div>
                <Row type="flex" justify="left">
                    <Col span={4}>
                        <Card bodyStyle={{padding: 5}}>
                            <div className="custom-image">
                                <img alt="API thumb" width="100%" src="/publisher/public/images/api/api-default.png"/>
                            </div>
                            <div className="custom-card">
                                <Badge status="processing" text={api.lifeCycleStatus}/>
                                <p>11 Apps</p>
                                <a href={"/store/apis/" + this.api_uuid} target="_blank" title="Store">View in store</a>
                            </div>
                        </Card>
                    </Col>
                    <Col span={19} offset={1}>
                        <form onSubmit={this.handleSubmit}>
                            <Card bodyStyle={{padding: 5}}>
                                <Row style={{marginBottom: "10px"}} type="flex" justify="left">
                                    <Col span={8}>Visibility</Col>

                                    <Select value = {this.state.visibility} onChange={this.handleChangeVisibilityField}>
                                        <MenuItem value={"PUBLIC"}>Public</MenuItem>
                                        <MenuItem value={"RESTRICTED"}>Restricted by Roles</MenuItem>
                                    </Select>

                                </Row>

                                <Row style={{marginBottom: "10px", display:`${ this.state.visibleRolesDisplay }`}} type="flex" justify="left">
                                    <Col span={8}>Visible to Roles</Col>
                                    <Col span={16}>
                                        <Input name="roles" placeholder="Sales-group,Engineering" value= {this.state.visibleRoles}  onChange={this.handleChangeVisibilityRolesField}/>
                                    </Col>
                                </Row>
                            </Card>

                            <Card bodyStyle={{padding: 5}}>
                                <Row style={{marginBottom: "10px"}} type="flex" justify="center">
                                    <Col span={8}>API Permissions</Col>
                                    <Col span={16}>
                                        <Row>
                                            <Col span={4} style={{margin: "10px"}}>
                                                <Input name="roleField" placeholder="role" value={this.state.roleField}
                                                       onChange={this.handleChangeRoleField}/>
                                            </Col>
                                            <Col span={2} style={{margin: "10px"}}>
                                                <Checkbox name="readField" value={ApiPermissionValidation.permissionType.READ}
                                                          checked={this.state.readField} onChange={this.handleChangeReadField}>
                                                    Read </Checkbox>
                                            </Col>
                                            <Col span={2} style={{margin: "10px"}}>
                                                <Checkbox name="updateField" value={ApiPermissionValidation.permissionType.UPDATE}
                                                          checked={this.state.updateField} onChange={this.handleChangeUpdateField}>
                                                    Update </Checkbox>
                                            </Col>
                                            <Col span={2} style={{margin: "10px"}}>
                                                <Checkbox name="deleteField" value={ApiPermissionValidation.permissionType.DELETE}
                                                          checked={this.state.deleteField} onChange={this.handleChangeDeleteField}>
                                                    Delete </Checkbox>
                                            </Col>
                                            <Col span={5} style={{margin: "10px"}}>
                                                <Checkbox name="manageSubField"
                                                          value={ApiPermissionValidation.permissionType.MANAGE_SUBSCRIPTION}
                                                          checked={this.state.manageSubField} onChange={this.handleChangeManageSubField}>
                                                    Manage Subscriptions </Checkbox>
                                            </Col>
                                            <Col span={1} style={{margin: "10px"}}>
                                                <Button name="add" onClick=
                                                    {this.handleAddRole.bind(this, permissionData)}> Add </Button>
                                            </Col>
                                        </Row>
                                    </Col>

                                </Row>
                                <Row style={{marginBottom: "10px"}} type="flex" justify="center">
                                    <Col span={8}></Col>
                                    <Col span={16}>
                                        <Table columns={columns} dataSource={permissionData}/>
                                    </Col>
                                </Row>
                            </Card>
                            <Card bodyStyle={{padding: 5}}>
                                <Row style={{marginBottom: "10px"}} type="flex" justify="center">
                                    <Col span={8}>API Security Schemes</Col>
                                    <Col span={16}>
                                        <Row>
                                            <Col span={8} style={{margin: "10px"}}>
                                                <Checkbox name="oauthField" checked={this.state.oauthField}
                                                          onChange={this.toggleOauthSelect}>Oauth</Checkbox>
                                            </Col>
                                            <Col span={8} style={{margin: "10px"}}>
                                                <Checkbox name="apikeyField" checked={this.state.apikeyField}
                                                          onChange={this.toggleApiKeySelect}>API Key</Checkbox>
                                            </Col>
                                        </Row>
                                    </Col>
                                </Row>
                            </Card>
                            {this.state.oauthField ?
                                (<Card bodyStyle={{padding: 5}}>
                                    <Row style={{marginBottom: "10px"}} type="flex" justify="center">
                                        <Col span={8}>API Scopes</Col>
                                        <Col span={16}>
                                            <Row>
                                                <Col span={8} style={{margin: "10px"}}>
                                                    <Input name="scopeKey" placeholder="scopeKey"/>
                                                </Col>
                                                <Col span={8} style={{margin: "10px"}}>
                                                    <Input name="scopeName" placeholder="scopeName"/>
                                                </Col>
                                            </Row>
                                            <Row>
                                                <Col span={16} style={{margin: "10px"}}>
                                                    <Input name="roles" placeholder="roles"/>
                                                </Col>
                                            </Row>
                                            <Row>
                                                <Col span={16} style={{margin: "10px"}}>
                                                    <Input name="descriptions" placeholder="descriptions"/>
                                                </Col>
                                            </Row>
                                            <Row>
                                                <Col span={16} style={{margin: "10px"}}>
                                                    <Table columns={columnsOfScopeTable} dataSource={dataOfScopes}/>
                                                </Col>
                                            </Row>
                                        </Col>
                                    </Row>
                                </Card>) : <p/>
                            }
                            {/* Allowing update API with scopes */}
                            <ScopeValidation resourcePath={resourcePath.SINGLE_API} resourceMethod={resourceMethod.PUT}>
                                <ApiPermissionValidation userPermissions={this.state.api.userPermissionsForApi}>
                                    <Button loading={this.state.creating} type="primary"
                                            onClick={this.handleSubmit}>Update</Button>
                                </ApiPermissionValidation>
                            </ScopeValidation>
                        </form>
                    </Col>
                </Row>
            </div>


        );
    }
}

const PermissionFormGenerated = Form.create()(Permission);

class PermissionFormWrapper extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            api: null,
            notFound: false
        };
        // this.api_uuid = this.props.match.params.api_uuid;
    }


    render = () => {
        const {match} = this.props;
        return <PermissionFormGenerated match={match} history={this.props.history}/>
    }
}

export default PermissionFormWrapper

