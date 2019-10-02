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

import React, { Component } from 'react';
import { Checkbox, Table, Col, Row, Card, Button, message, Form, Badge } from 'antd';
import MenuItem from '@material-ui/core/MenuItem';
import Select from '@material-ui/core/Select';
import Input from '@material-ui/core/Input';
import { FormattedMessage, injectIntl } from 'react-intl';
import Api from 'AppData/api';
import { Progress } from 'AppComponents/Shared';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import ApiPermissionValidation from 'AppData/ApiPermissionValidation';
import { ScopeValidation, resourceMethod, resourcePath } from 'AppData/ScopeValidation';
import { app } from 'Config';

/**
 * @inheritdoc
 */
class Permission extends Component {
    /**
     * @inheritdoc
     * @param {*} props properties
     */
    constructor(props) {
        super(props);
        this.state = {
            api: null,
            notFound: false,
            visibility: 'PUBLIC',
            visibleRoles: [],
            visibleRolesDisplay: 'none',
            roleField: '',
            readField: false,
            updateField: false,
            deleteField: false,
            apikeyField: false,
            oauthField: false,
            manageSubField: false,
            permissionData: [],
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
        const promised_api = api.get(this.api_uuid);
        promised_api
            .then((response) => {
                this.setState({ api: response.obj });
                this.getExistingPermissions(this);
                this.getExistingSecuritySchemes(this);
                this.getExistingVisibility(this);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    handleChangeVisibilityField(event) {
        this.setState({ visibility: event.target.value });
        if (event.target.value == 'RESTRICTED') {
            this.setState({ visibleRolesDisplay: 'inline' });
        } else {
            this.setState({ visibleRolesDisplay: 'none' });
        }
    }

    handleChangeVisibilityRolesField(event) {
        const roleList = event.target.value.split(',');
        const roleList2 = roleList.map(e => String(e).trim());
        this.setState({ visibleRoles: roleList2 });
    }

    handleSwitch(secured) {
        this.setState({ secured });
    }

    handleInputs(e) {
        this.setState({ [e.target.name]: e.target.value });
    }

    handleMaxTPS(maxTPS) {
        this.setState({ maxTPS });
    }

    handleSubmit = (e) => {
        const api = new Api();
        const promised_api = api.get(this.api_uuid);
        promised_api
            .then((response) => {
                const api_data = JSON.parse(response.data);
                api_data.visibility = this.state.visibility;
                api_data.visibleRoles = this.state.visibleRoles;
                const permissionString = this.createPermissionJsonString(this);
                api_data.permission = permissionString;
                const securitySchemes = this.createSecuritySchemeArray(this);
                api_data.securityScheme = securitySchemes;
                const promised_update = api.update(api_data);
                const { intl } = this.props;
                promised_update
                    .then((response) => {
                        message.success(intl.formatMessage({ id: 'Apis.Details.Permissions.Permission.permissions.updated.successfully', defaultMessage: 'Permissions updated successfully' }));
                    })
                    .catch((error) => {
                        console.error(error);
                        message.error(intl.formatMessage({ id: 'Apis.Details.Permissions.Permission.error.occurred.while.updating.permissions', defaultMessage: 'Error occurred while updating permissions!' }));
                        this.setState({ loading: false });
                    });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    };

    handleChangeRoleField(event) {
        this.setState({ roleField: event.target.value });
    }

    handleChangeReadField(event) {
        this.setState({ readField: event.target.checked });
    }

    handleChangeUpdateField(event) {
        this.setState({ updateField: event.target.checked });
    }

    handleChangeDeleteField(event) {
        this.setState({ deleteField: event.target.checked });
    }

    handleChangeManageSubField(event) {
        this.setState({ manageSubField: event.target.checked });
    }

    toggleOauthSelect(event) {
        this.setState({ oauthField: event.target.checked });
    }

    toggleApiKeySelect(event) {
        this.setState({ apikeyField: event.target.checked });
    }

    handleAddRole(permissionData) {
        const groupPermissions = {};
        groupPermissions.key = this.state.roleField.trim();
        groupPermissions.isRead = this.state.readField;
        groupPermissions.isUpdate = this.state.updateField;
        groupPermissions.isDelete = this.state.deleteField;
        groupPermissions.isManageSubscription = this.state.manageSubField;
        permissionData.push(groupPermissions);
        this.setState({
            permissionData,
            roleField: null,
            readField: false,
            updateField: false,
            deleteField: false,
            manageSubField: false,
        });
    }

    handleRemoveRole(value) {
        const permissionData = this.state.permissionData.slice();
        let index = -1;
        for (let i = 0; i < permissionData.length; i++) {
            if (permissionData[i].key === value) {
                index = i;
            }
        }
        permissionData.splice(index, 1);
        this.setState({ permissionData });
    }

    createPermissionJsonString() {
        const apiPermissionArray = [];
        const permissionData = this.state.permissionData;
        Object.keys(permissionData).map((key) => {
            const groupPermissions = {};
            const permissionSubJson = permissionData[key];
            const roleName = permissionSubJson.key;
            const isRead = permissionSubJson.isRead;
            const isUpdate = permissionSubJson.isUpdate;
            const isDelete = permissionSubJson.isDelete;
            const isManageSubscription = permissionSubJson.isManageSubscription;
            const permissionArray = [];
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
            groupPermissions.groupId = roleName;
            groupPermissions.permission = permissionArray;
            apiPermissionArray.push(groupPermissions);
        });
        const updatedPermissionString = JSON.stringify(apiPermissionArray);
        if (updatedPermissionString == '[]') {
            return '';
        } else {
            return updatedPermissionString;
        }
    }

    createSecuritySchemeArray() {
        const securitySchemes = [];
        if (this.state.oauthField) {
            securitySchemes.push('Oauth');
        }
        if (this.state.apikeyField) {
            securitySchemes.push('apikey');
        }

        return securitySchemes;
    }

    getExistingSecuritySchemes() {
        const securitySchemes = this.state.api.securityScheme;
        if (securitySchemes.includes('Oauth')) {
            this.setState({ oauthField: true });
        } else {
            this.setState({ oauthField: false });
        }
        if (securitySchemes.includes('apikey')) {
            this.setState({ apikeyField: true });
        } else {
            this.setState({ apikeyField: false });
        }
    }

    getExistingVisibility() {
        const visibility = this.state.api.visibility;
        this.setState({ visibility });

        if (visibility == 'RESTRICTED') {
            this.setState({ visibleRolesDisplay: 'inline' });
        } else {
            this.setState({ visibleRolesDisplay: 'none' });
        }

        const visibleRoles = this.state.api.visibleRoles;
        console.log(visibleRoles);
        this.setState({ visibleRoles });
    }

    getExistingPermissions() {
        if (!this.state.api) {
            return <Progress />;
        }
        const permissionData = [];
        const permissionString = this.state.api.permission;
        if (permissionString != '') {
            const permissionJson = JSON.parse(permissionString);
            Object.keys(permissionJson).map((key) => {
                /* permissionSubJson contains permissions for a single role */
                const permissionSubJson = permissionJson[key];
                const roleName = permissionSubJson.groupId;
                const permissionArray = permissionSubJson.permission;
                let isRead = false;
                let isUpdate = false;
                let isDelete = false;
                let isManageSubscription = false;

                const groupPermissions = {};

                if (permissionArray.includes(ApiPermissionValidation.permissionType.READ)) {
                    isRead = true;
                }
                if (permissionArray.includes(ApiPermissionValidation.permissionType.UPDATE)) {
                    isUpdate = true;
                }
                if (permissionArray.includes(ApiPermissionValidation.permissionType.DELETE)) {
                    isDelete = true;
                }
                if (permissionArray.includes(ApiPermissionValidation.permissionType.MANAGE_SUBSCRIPTION)) {
                    isManageSubscription = true;
                }
                groupPermissions.key = roleName;
                groupPermissions.isRead = isRead;
                groupPermissions.isUpdate = isUpdate;
                groupPermissions.isDelete = isDelete;
                groupPermissions.isManageSubscription = isManageSubscription;
                permissionData.push(groupPermissions);
            });
        }
        this.setState({ permissionData });
    }

    /**
     * @inheritdoc
     */
    render() {
        const { intl, resourceNotFountMessage } = this.props;
        const columns = [
            {
                title: intl.formatMessage({ id: 'Apis.Details.Permissions.Permission.role.col', defaultMessage: 'Role' }),
                dataIndex: 'key',
                key: 'key',
                render: text => <a href='#'>{text}</a>,
            },
            {
                title: intl.formatMessage({ id: 'Apis.Details.Permissions.Permission.read', defaultMessage: 'Read' }),
                dataIndex: 'isRead',
                key: 'isRead',
                render: isChecked => (
                    <Checkbox checked={isChecked} name='read' value={ApiPermissionValidation.permissionType.READ} />
                ),
            },
            {
                title: intl.formatMessage({ id: 'Apis.Details.Permissions.Permission.update', defaultMessage: 'Update' }),
                dataIndex: 'isUpdate',
                key: 'isUpdate',
                render: isChecked => (
                    <Checkbox checked={isChecked} name='update' value={ApiPermissionValidation.permissionType.UPDATE} />
                ),
            },
            {
                title: intl.formatMessage({ id: 'Apis.Details.Permissions.Permission.delete', defaultMessage: 'Delete' }),
                dataIndex: 'isDelete',
                key: 'isDelete',
                render: isChecked => (
                    <Checkbox checked={isChecked} name='delete' value={ApiPermissionValidation.permissionType.DELETE} />
                ),
            },
            {
                title: intl.formatMessage({ id: 'Apis.Details.Permissions.Permission.manages.subscriptions', defaultMessage: 'Manage Subscriptions' }),
                dataIndex: 'isManageSubscription',
                key: 'isManageSubscription',
                render: isChecked => (
                    <Checkbox
                        checked={isChecked}
                        name='manage_subscription'
                        value={ApiPermissionValidation.permissionType.MANAGE_SUBSCRIPTION}
                    />
                ),
            },
            {
                title: intl.formatMessage({ id: 'Apis.Details.Permissions.Permission.action', defaultMessage: 'Action' }),
                key: 'action',
                render: (text, record) => (
                    <span>
                        <a href='#' onClick={this.handleRemoveRole.bind(this, record.key)}>
                            Delete
                        </a>
                    </span>
                ),
            },
        ];

        const { permissionData, api } = this.state;

        if (this.state.notFound) {
            return <ResourceNotFound message={resourceNotFountMessage} />;
        }
        if (!api) {
            return <Progress />;
        }

        return (
            <div>
                <Row type='flex' justify='left'>
                    <Col span={4}>
                        <Card bodyStyle={{ padding: 5 }}>
                            <div className='custom-image'>
                                <img
                                    alt={intl.formatMessage({ id: 'Apis.Details.Permissions.Permission.api.thumb', defaultMessage: 'API thumb' })}
                                    width='100%'
                                    src={app.context + '/site/public/images/api/api-default.png'}
                                />
                            </div>
                            <div className='custom-card'>
                                <Badge status='processing' text={api.lifeCycleStatus} />
                                <p><FormattedMessage id='Apis.Details.Permissions.Permission.11.Apps' defaultMessage='11 Apps' /></p>
                                <a href={'/store/apis/' + this.api_uuid} target='_blank' title='Developer Portal'>
                                    <FormattedMessage id='Apis.Details.Permissions.Permission.View.In.Store' defaultMessage='View in developer portal' />
                                </a>
                            </div>
                        </Card>
                    </Col>
                    <Col span={19} offset={1}>
                        <form onSubmit={this.handleSubmit}>
                            <Card bodyStyle={{ padding: 5 }}>
                                <Row style={{ marginBottom: '10px' }} type='flex' justify='left'>
                                    <Col span={8}><FormattedMessage id='Apis.Details.Permissions.Permission.Visibility' defaultMessage='Visibility' /></Col>

                                    <Select value={this.state.visibility} onChange={this.handleChangeVisibilityField}>
                                        <MenuItem value='PUBLIC'><FormattedMessage id='Apis.Details.Permissions.Permission.Select.Visibility' defaultMessage='Visibility' /></MenuItem>
                                        <MenuItem value='RESTRICTED'><FormattedMessage id='Apis.Details.Permissions.Permission.Restricted.By.Roles' defaultMessage='Restricted by Roles' /></MenuItem>
                                    </Select>
                                </Row>

                                <Row
                                    style={{ marginBottom: '10px', display: `${this.state.visibleRolesDisplay}` }}
                                    type='flex'
                                    justify='left'
                                >
                                    <Col span={8}><FormattedMessage id='Apis.Details.Permissions.Permission.Visible.To.Roles' defaultMessage='Visible to Roles' /></Col>
                                    <Col span={16}>
                                        <Input
                                            name='roles'
                                            placeholder={intl.formatMessage({ id: 'Apis.Details.Permissions.Permission.sales.group.engineering', defaultMessage: 'Sales-group,Engineering' })}
                                            value={this.state.visibleRoles}
                                            onChange={this.handleChangeVisibilityRolesField}
                                        />
                                    </Col>
                                </Row>
                            </Card>

                            <Card bodyStyle={{ padding: 5 }}>
                                <Row style={{ marginBottom: '10px' }} type='flex' justify='center'>
                                    <Col span={8}>
                                        <FormattedMessage
                                            id='Apis.Details.Permissions.Permission.API.Permissions'
                                            defaultMessage='API Permissions'
                                        />
                                    </Col>
                                    <Col span={16}>
                                        <Row>
                                            <Col span={4} style={{ margin: '10px' }}>
                                                <Input
                                                    name='roleField'
                                                    placeholder={intl.formatMessage({
                                                        id: 'Apis.Details.Permissions.Permission.role.input',
                                                        defaultMessage: 'role',
                                                    })}
                                                    value={this.state.roleField}
                                                    onChange={this.handleChangeRoleField}
                                                />
                                            </Col>
                                            <Col span={2} style={{ margin: '10px' }}>
                                                <Checkbox
                                                    name='readField'
                                                    value={ApiPermissionValidation.permissionType.READ}
                                                    checked={this.state.readField}
                                                    onChange={this.handleChangeReadField}
                                                >
                                                    <FormattedMessage
                                                        id='Apis.Details.Permissions.Permission.Read'
                                                        defaultMessage='Read'
                                                    />
                                                </Checkbox>
                                            </Col>
                                            <Col span={2} style={{ margin: '10px' }}>
                                                <Checkbox
                                                    name='updateField'
                                                    value={ApiPermissionValidation.permissionType.UPDATE}
                                                    checked={this.state.updateField}
                                                    onChange={this.handleChangeUpdateField}
                                                >
                                                    <FormattedMessage
                                                        id='Apis.Details.Permissions.Permission.Update'
                                                        defaultMessage='Update'
                                                    />
                                                </Checkbox>
                                            </Col>
                                            <Col span={2} style={{ margin: '10px' }}>
                                                <Checkbox
                                                    name='deleteField'
                                                    value={ApiPermissionValidation.permissionType.DELETE}
                                                    checked={this.state.deleteField}
                                                    onChange={this.handleChangeDeleteField}
                                                >
                                                    <FormattedMessage
                                                        id='Apis.Details.Permissions.Permission.Delete'
                                                        defaultMessage='Delete'
                                                    />
                                                </Checkbox>
                                            </Col>
                                            <Col span={5} style={{ margin: '10px' }}>
                                                <Checkbox
                                                    name='manageSubField'
                                                    value={ApiPermissionValidation.permissionType.MANAGE_SUBSCRIPTION}
                                                    checked={this.state.manageSubField}
                                                    onChange={this.handleChangeManageSubField}
                                                >
                                                    <FormattedMessage
                                                        id='Apis.Details.Permissions.Permission.Manage.Subscriptions'
                                                        defaultMessage='Manage Subscriptions'
                                                    />
                                                </Checkbox>
                                            </Col>
                                            <Col span={1} style={{ margin: '10px' }}>
                                                <Button
                                                    name='add'
                                                    onClick={this.handleAddRole.bind(this, permissionData)}
                                                >
                                                    <FormattedMessage
                                                        id='Apis.Details.Permissions.Permission.Add'
                                                        defaultMessage='Add'
                                                    />
                                                </Button>
                                            </Col>
                                        </Row>
                                    </Col>
                                </Row>
                                <Row style={{ marginBottom: '10px' }} type='flex' justify='center'>
                                    <Col span={8} />
                                    <Col span={16}>
                                        <Table columns={columns} dataSource={permissionData} />
                                    </Col>
                                </Row>
                            </Card>
                            <Card bodyStyle={{ padding: 5 }}>
                                <Row style={{ marginBottom: '10px' }} type='flex' justify='center'>
                                    <Col span={8}>
                                        <FormattedMessage
                                            id='Apis.Details.Permissions.Permission.API.Security.Schemes'
                                            defaultMessage='API Security Schemes'
                                        />
                                    </Col>
                                    <Col span={16}>
                                        <Row>
                                            <Col span={8} style={{ margin: '10px' }}>
                                                <Checkbox
                                                    name='oauthField'
                                                    checked={this.state.oauthField}
                                                    onChange={this.toggleOauthSelect}
                                                >
                                                    <FormattedMessage
                                                        id='Apis.Details.Permissions.Permission.Oauth'
                                                        defaultMessage='Oauth'
                                                    />
                                                </Checkbox>
                                            </Col>
                                            <Col span={8} style={{ margin: '10px' }}>
                                                <Checkbox
                                                    name='apikeyField'
                                                    checked={this.state.apikeyField}
                                                    onChange={this.toggleApiKeySelect}
                                                >
                                                    <FormattedMessage
                                                        id='Apis.Details.Permissions.Permission.API.Key'
                                                        defaultMessage='API Key'
                                                    />
                                                </Checkbox>
                                            </Col>
                                        </Row>
                                    </Col>
                                </Row>
                            </Card>
                            {/* Allowing update API with scopes */}
                            <ScopeValidation resourcePath={resourcePath.SINGLE_API} resourceMethod={resourceMethod.PUT}>
                                <ApiPermissionValidation userPermissions={this.state.api.userPermissionsForApi}>
                                    <Button loading={this.state.creating} type='primary' onClick={this.handleSubmit}>
                                        <FormattedMessage
                                            id='Apis.Details.Permissions.Permission.Update'
                                            defaultMessage='Update'
                                        />
                                    </Button>
                                </ApiPermissionValidation>
                            </ScopeValidation>
                        </form>
                    </Col>
                    <Col span={4} />
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
            notFound: false,
        };
        // this.api_uuid = this.props.match.params.api_uuid;
    }

    render = () => {
        const { match } = this.props;
        return (
            <PermissionFormGenerated
                match={match}
                history={this.props.history}
                resourceNotFountMessage={this.props.resourceNotFountMessage}
            />
        );
    };
}

export default injectIntl(PermissionFormWrapper);
