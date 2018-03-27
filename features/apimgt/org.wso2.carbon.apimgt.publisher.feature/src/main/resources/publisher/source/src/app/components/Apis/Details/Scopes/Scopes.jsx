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

import 'react-tagsinput/react-tagsinput.css';
import { message } from 'antd/lib/index';
import PropTypes from 'prop-types';
import React from 'react';
import Api from '../../../../data/api';
import { Progress } from '../../../Shared';
import ResourceNotFound from '../../../Base/Errors/ResourceNotFound';

/**
 * Generate the scopes UI in API details page.
 * @class Scopes
 * @extends {React.Component}
 */
class Scopes extends React.Component {
    /**
     * Creates an instance of Scopes.
     * @param {any} props Generic props
     * @memberof Scopes
     */
    constructor(props) {
        super(props);
        this.api = new Api();
        this.api_uuid = props.match.params.api_uuid;
        this.state = {
            apiScopes: null,
            apiScope: {},
            roles: [],
            notFound: false,
        };
        this.deleteScope = this.deleteScope.bind(this);
        this.updateScope = this.updateScope.bind(this);
        this.handleInputs = this.handleInputs.bind(this);
        this.addScope = this.addScope.bind(this);
    }

    /**
     * Fetch API resource when component get mounted
     * @memberof Scopes
     */
    componentDidMount() {
        const api = new Api();
        const promisedScopesObject = api.getScopes(this.api_uuid);
        promisedScopesObject
            .then((response) => {
                this.setState({
                    apiScopes: response.obj.list,
                });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({
                        notFound: true,
                    });
                }
            });
    }
    /**
     * Delete scope
     * @param {any} scopeName Name of the scope need to be deleted
     * @memberof Scopes
     */
    deleteScope(scopeName) {
        const { apiScopes } = this.state;
        for (const apiScope in apiScopes) {
            if (Object.prototype.hasOwnProperty.call(apiScopes, apiScope) && apiScopes[apiScope].name === scopeName) {
                apiScopes.splice(apiScope, 1);
                break;
            }
        }
        this.setState({
            apiScopes,
        });
    }

    /**
     * Update scope
     * @param {any} scopeName Scope name to be updated
     * @param {any} scopeObj New Scope object
     * @memberof Scopes
     */
    updateScope(scopeName, scopeObj) {
        const { apiScopes } = this.state;
        for (const apiScope in apiScopes) {
            if (Object.prototype.hasOwnProperty.call(apiScopes, apiScope) && apiScopes[apiScope].name === scopeName) {
                apiScopes[apiScope].description = scopeObj.description;
                break;
            }
        }
        this.setState({
            apiScopes,
        });
    }

    /**
     * Add new scope
     * @memberof Scopes
     */
    addScope() {
        const hideMessage = message.loading('Adding the Scope ...', 0);
        const api = new Api();
        const scope = this.state.apiScope;
        scope.bindings = {
            type: 'role',
            values: this.state.roles,
        };
        const promisedScopeAdd = api.addScope(this.props.match.params.api_uuid, scope);
        promisedScopeAdd.then((response) => {
            if (response.status !== 201) {
                console.log(response);
                message.error('Something went wrong while updating the ' + scope.name + ' Scope!');
                hideMessage();
                return;
            }
            message.success(scope.name + ' Scope added successfully!');
            const { apiScopes } = this.state;
            apiScopes[apiScopes.length] = this.state.apiScope;
            this.setState({
                apiScopes,
                apiScope: {},
                roles: [],
            });
            hideMessage();
        });
    }
    /**
     * Handle api scope addition event
     * @param {any} event Button Click event
     * @memberof Scopes
     */
    handleInputs(event) {
        if (Array.isArray(event)) {
            this.setState({
                roles: event,
            });
        } else {
            const input = event.target;
            const { apiScope } = this.state;
            apiScope[input.id] = input.value;
            this.setState({
                apiScope,
            });
        }
    }

    /**
     * Render Scopes section
     * @returns {React.Component} React Component
     * @memberof Scopes
     */
    render() {
        const { apiScopes } = this.state;

        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFountMessage} />;
        }

        if (!apiScopes) {
            return <Progress />;
        }

        return (
            <div>
                {/* <Card
                    title='Add Scope'
                    style={{
                        width: '100%',
                        marginBottom: 20,
                    }}
                >
                    <Row type='flex' justify='start'>
                        <Col span={4}> Scope Name </Col>
                        <Col span={10}>
                            <Input id='name' onChange={this.handleInputs} value={apiScope.name || ''} />
                        </Col>
                    </Row>
                    <br />
                    <Row type='flex' justify='start'>
                        <Col span={4}> Description </Col>
                        <Col span={10}>
                            <Input id='description' onChange={this.handleInputs} value={apiScope.description || ''} />
                        </Col>
                    </Row>
                    <br />
                    <Row type='flex' justify='start'>
                        <Col span={4}> Roles </Col>
                        <Col span={10}>
                            <TagsInput
                                value={roles}
                                onChange={this.handleInputs}
                                onlyUnique
                                inputProps={{
                                    placeholder: 'add a role',
                                }}
                            />
                        </Col>
                    </Row>
                    <br />
                    <Row type='flex' justify='start'>
                        <Col span={5} />
                        <Col span={10}>
                            <button onClick={this.addScope}> Add Scope to API </button>
                        </Col>
                        <Col span={5} />
                    </Row>
                </Card>
                {Object.keys(apiScopes).map((key) => {
                    const scope = apiScopes[key];
                    return (
                        <Scope
                            name={scope.name}
                            description={scope.description}
                            api_uuid={this.api_uuid}
                            deleteScope={this.deleteScope}
                            key={key}
                            updateScope={this.updateScope}
                        />
                    );
                })} */}
            </div>
        );
    }
}

Scopes.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.object,
    }),
    resourceNotFountMessage: PropTypes.string.isRequired,
};

Scopes.defaultProps = {
    match: { params: {} },
};

export default Scopes;
