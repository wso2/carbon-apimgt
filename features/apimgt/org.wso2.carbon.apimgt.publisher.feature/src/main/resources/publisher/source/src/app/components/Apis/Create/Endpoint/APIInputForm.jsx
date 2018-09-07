/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';

import Policies from '../../Details/LifeCycle/Policies';
import { ScopeValidation, resourceMethod, resourcePath } from '../../../../data/ScopeValidation';
import API from '../../../../data/api';

/**
 * @export @inheritDoc
 * @class InputForm
 * @extends {Component}
 */
export default class APIInputForm extends Component {
    /**
     * Creates an instance of InputForm.
     * @param {any} props @inheritDoc
     * @memberof InputForm
     */
    constructor(props) {
        super(props);
        this.api = new API();
        this.state = {
            policies: [],
        };
    }

    /**
     * @inheritDoc
     * @memberof InputForm
     */
    componentDidMount() {
        const promisedTier = API.policies('api');
        promisedTier.then((response) => {
            const tiers = response.obj;
            this.setState({ policies: tiers });
        });
    }

    /**
     * @inheritDoc
     * @returns {React.Component}
     * @memberof InputForm
     */
    render() {
        const { policies } = this.state;
        const { apiFields, validate, handleInputChange } = this.props;
        const props = {
            policies: this.state.policies,
            handlePolicies: handleInputChange,
            selectedPolicies: apiFields && apiFields.selectedPolicies,
        };
        return (
            <div>
                <TextField
                    fullWidth
                    error={!apiFields.apiName && validate}
                    id='apiName'
                    label='Name'
                    required
                    type='text'
                    name='apiName'
                    margin='normal'
                    value={apiFields.apiName || ''}
                    onChange={handleInputChange}
                />
                <TextField
                    // TODO: These lines were commented because they need to be there but currently
                    // REST API doesn't support API versioning.So when we implement the versioning
                    // support we could simply uncomment those 2 lines and allow the user to
                    // provide version numbers. ~tmkb
                    // InputLabelProps={inputLabelClass}
                    // value={this.state.apiFields.apiVersion}
                    fullWidth
                    label='Version'
                    required
                    id='apiVersion'
                    helperText='**Version input is not support in this release'
                    type='text'
                    name='apiVersion'
                    margin='normal'
                    disabled
                />
                <TextField
                    fullWidth
                    error={!apiFields.apiContext && validate}
                    id='apiContext'
                    required
                    label='Context'
                    type='text'
                    name='apiContext'
                    margin='normal'
                    value={apiFields.apiContext}
                    onChange={handleInputChange}
                />
                <TextField
                    fullWidth
                    id='apiEndpoint'
                    label='Endpoint'
                    type='text'
                    name='apiEndpoint'
                    margin='normal'
                    value={apiFields.apiEndpoint}
                    onChange={handleInputChange}
                />
                <ScopeValidation resourcePath={resourcePath.API_CHANGE_LC} resourceMethod={resourceMethod.POST}>
                    {policies && <Policies {...props} />}
                </ScopeValidation>
            </div>
        );
    }
}

APIInputForm.defaultProps = {
    validate: false,
};

APIInputForm.propTypes = {
    validate: PropTypes.bool,
    apiFields: PropTypes.shape({}).isRequired,
    handleInputChange: PropTypes.func.isRequired,
};
