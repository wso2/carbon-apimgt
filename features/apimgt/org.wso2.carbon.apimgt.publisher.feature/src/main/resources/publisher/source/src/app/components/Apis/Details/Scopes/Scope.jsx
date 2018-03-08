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

import React from 'react';
import { Input, Icon, Checkbox, Button, Card, Tag, Form } from 'antd';
import { Row, Col } from 'antd';
import Api from '../../../../data/api';
import { message } from 'antd/lib/index';
import TagsInput from 'react-tagsinput';
import 'react-tagsinput/react-tagsinput.css';
import Loading from '../../../Base/Loading/Loading';

class Scope extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            visible: false,
            tags: [],
        };
        this.toggleScopeData = this.toggleScopeData.bind(this);
        this.removeScope = this.removeScope.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleChangeDescription = this.handleChangeDescription.bind(this);
    }
    toggleScopeData() {
        const api = new Api();
        const promised_scopes_object = api.getScopeDetail(this.props.api_uuid, this.props.name);
        promised_scopes_object
            .then((response) => {
                this.setState({ apiScope: response.obj });
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
        this.setState({ visible: !this.state.visible });
    }
    removeScope() {
        const scope_name = this.props.name;
        const hideMessage = message.loading('Deleting the Scope ...', 0);
        const api = new Api();
        const promised_scope_delete = api.deleteScope(this.props.api_uuid, scope_name);
        promised_scope_delete.then((response) => {
            if (response.status !== 200) {
                console.log(response);
                message.error('Something went wrong while deleting the ' + scope_name + ' Scope!');
                hideMessage();
                return;
            }
            message.success(scope_name + ' Scope deleted successfully!');
            this.props.deleteScope(scope_name);
            hideMessage();
        });
    }
    handleChange(roles) {
        const apiScope = this.state.apiScope;
        apiScope.bindings.values = roles;
        this.setState({ apiScope });
    }
    handleChangeDescription(e) {
        const { apiScope } = this.state;
        apiScope.description = e.target.value;
        this.setState({ apiScope });
    }
    handleSubmit() {
        const scope_name = this.props.name;
        const hideMessage = message.loading('Updating the Scope ...', 0);
        const api = new Api();
        const promised_scope_update = api.updateScope(this.props.api_uuid, scope_name, this.state.apiScope);
        promised_scope_update.then((response) => {
            if (response.status !== 200) {
                console.log(response);
                message.error('Something went wrong while updating the ' + scope_name + ' Scope!');
                hideMessage();
                return;
            }
            message.success(scope_name + ' Scope updated successfully!');
            this.props.updateScope(scope_name, this.state.apiScope);
            hideMessage();
        });
    }
    render() {
        return (
            <div id={this.props.name}>
                <Row type='flex' justify='start' className='resource-head'>
                    <Col span={8}>
                        <a onClick={this.toggleScopeData}>{this.props.name}</a>
                    </Col>
                    <Col span={8}>{this.props.description}</Col>
                    <Col span={8} style={{ textAlign: 'right', cursor: 'pointer' }} onClick={this.removeScope}>
                        <Icon type='delete' />
                    </Col>
                </Row>
                {this.state.visible ? (
                    <div>
                        {this.state.apiScope ? (
                            <Row type='flex' justify='start' className='resource-body'>
                                <Col span={20}>
                                    <Input
                                        name='descriptions'
                                        placeholder='descriptions'
                                        type='text'
                                        onChange={this.handleChangeDescription}
                                        defaultValue={this.state.apiScope.description}
                                    />
                                </Col>
                                <Col span={20}>
                                    <TagsInput
                                        value={this.state.apiScope.bindings.values}
                                        onChange={this.handleChange}
                                        onlyUnique
                                        inputProps={{ placeholder: 'add a valid role' }}
                                    />
                                </Col>
                                <Col span={20}>
                                    <Button loading={this.state.creating} type='primary' onClick={this.handleSubmit}>
                                        Update
                                    </Button>
                                </Col>
                            </Row>
                        ) : (
                            <Loading />
                        )}
                    </div>
                ) : null}
            </div>
        );
    }
}

export default Scope;
