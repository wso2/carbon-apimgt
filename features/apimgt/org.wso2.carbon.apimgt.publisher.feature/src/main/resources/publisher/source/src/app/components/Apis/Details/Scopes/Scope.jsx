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
import {
    Input, Icon, Button, Row, Col,
} from 'antd';
import 'react-tagsinput/react-tagsinput.css';
import TagsInput from 'react-tagsinput';
import PropTypes from 'prop-types';
import Alert from 'AppComponents/Shared/Alert';
import { FormattedMessage, injectIntl } from 'react-intl';
import Api from 'AppData/api';
import Loading from '../../../Base/Loading/Loading';

/**
 * Renders an individual Scope record in table
 * @class Scope
 * @extends {React.Component}
 */
class Scope extends React.Component {
    /**
     * Creates an instance of Scope.
     * @param {any} props @inheritDoc
     * @memberof Scope
     */
    constructor(props) {
        super(props);
        this.state = {
            visible: false,
        };
        this.toggleScopeData = this.toggleScopeData.bind(this);
        this.removeScope = this.removeScope.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleChangeDescription = this.handleChangeDescription.bind(this);
    }

    /**
     * Update scope data
     * @memberof Scope
     */
    toggleScopeData() {
        const api = new Api();
        const promisedScopesObject = api.getScopeDetail(this.props.api_uuid, this.props.name);
        promisedScopesObject
            .then((response) => {
                this.setState({ apiScope: response.obj });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    console.warn('Page not found');
                }
            });
        this.setState((cState) => ({ visible: !cState.visible }));
    }

    /**
     * Handle remove scope action
     * @memberof Scope
     */
    removeScope() {
        const scopeName = this.props.name;
        const { intl } = this.props;
        Alert.info(intl.formatMessage({
            id: 'Apis.Details.Scopes.Scope.scope.deleting.the.scope',
            defaultMessage: 'Deleting the Scope ...',
        }));
        const api = new Api();
        const promisedScopeDelete = api.deleteScope(this.props.api_uuid, scopeName);
        promisedScopeDelete.then((response) => {
            if (response.status !== 200) {
                console.log(response);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.Scopes.Scope.something.went.wrong.while.deleting.the.scope',
                    defaultMessage: 'Something went wrong while deleting the {scopeName} Scope!',
                }, { scopeName }));
                return;
            }
            Alert.success(intl.formatMessage({
                id: 'Apis.Details.Scopes.Scope.scope.deleted.successfully',
                defaultMessage: '{scopeName} Scope deleted successfully!',
            }, { scopeName }));
            this.props.deleteScope(scopeName);
        });
    }

    /**
     *
     * @param {any} roles Associate roles for the group
     * @memberof Scope
     */
    handleChange(roles) {
        const { apiScope } = this.state;
        apiScope.bindings.values = roles;
        this.setState({ apiScope });
    }

    /**
     *
     * @param {any} e
     * @memberof Scope
     */
    handleChangeDescription(e) {
        const { apiScope } = this.state;
        apiScope.description = e.target.value;
        this.setState({ apiScope });
    }

    /**
     *
     * @memberof Scope
     */
    handleSubmit() {
        const scopeName = this.props.name;
        const { intl } = this.props;
        Alert.info(intl.formatMessage({
            id: 'Apis.Details.Scopes.Scope.scope.deleted.updating.the.scope',
            defaultMessage: 'Updating the Scope ...',
        }));

        const api = new Api();
        const promisedScopeUpdate = api.updateScope(this.props.api_uuid, scopeName, this.state.apiScope);
        promisedScopeUpdate.then((response) => {
            if (response.status !== 200) {
                console.log(response);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.Scopes.Scope.something.went.wrong.while.updating.the.scope',
                    defaultMessage: 'Something went wrong while updating the {scopeName} Scope!',
                }, { scopeName }));
                return;
            }
            Alert.success(intl.formatMessage({
                id: 'Apis.Details.Scopes.Scope.scope.updated.successfully',
                defaultMessage: '{scopeName} Scope updated successfully!',
            }, { scopeName }));
            this.props.updateScope(scopeName, this.state.apiScope);
        });
    }

    /**
     * @returns {React.Component} @inheritDoc
     * @memberof Scope
     */
    render() {
        const { intl } = this.props;
        return (
            <div id={this.props.name}>
                <Row type='flex' justify='start' className='resource-head'>
                    <Col span={8}>
                        <a onKeyPress={this.toggleScopeData} onClick={this.toggleScopeData}>
                            {this.props.name}
                        </a>
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
                                        placeholder={intl.formatMessage({
                                            id: 'Apis.Details.Scopes.Scope.scope.descriptions',
                                            defaultMessage: 'descriptions',
                                        })}
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
                                        inputProps={{
                                            placeholder: intl.formatMessage({
                                                id: 'Apis.Details.Scopes.Scope.scope.add.a.valid.role',
                                                defaultMessage: 'Add a valid role',
                                            }),
                                        }}
                                    />
                                </Col>
                                <Col span={20}>
                                    <Button loading={this.state.creating} type='primary' onClick={this.handleSubmit}>
                                        <FormattedMessage
                                            id='Apis.Details.Scopes.Scope.scope.update'
                                            defaultMessage='Update'
                                        />
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

Scope.propTypes = {
    api_uuid: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    description: PropTypes.string.isRequired,
    deleteScope: PropTypes.func.isRequired,
    updateScope: PropTypes.func.isRequired,
};

export default injectIntl(Scope);
