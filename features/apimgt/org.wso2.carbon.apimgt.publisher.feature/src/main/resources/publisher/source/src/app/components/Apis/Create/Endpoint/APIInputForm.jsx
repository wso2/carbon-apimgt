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
import { ScopeValidation, resourceMethod, resourcePath } from 'AppData/ScopeValidation';
import API from 'AppData/api';
import Policies from '../../Details/LifeCycle/Policies';
import { FormattedMessage } from 'react-intl';
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
        const { api, handleInputChange } = this.props;
        const policiesProps = { handleInputChange, api, policies };
        const endpoints = api.getProductionEndpoint().endpointConfig.list;
        const endpoint = endpoints && endpoints[0];
        return (
            <React.Fragment>
                <TextField
                    fullWidth
                    id='name'
                    label={<FormattedMessage id="name" defaultMessage="Name"/>}
                    required
                    type='text'
                    name='name'
                    margin='normal'
                    value={api.name || ''}
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
                    label={<FormattedMessage id="version" defaultMessage="Version"/>}
                    required
                    id='version'
                    helperText={<FormattedMessage id="version.helper.text" defaultMessage="**Version input is not support in this release"/>}
                    type='text'
                    name='version'
                    margin='normal'
                    disabled
                />
                <TextField
                    fullWidth
                    id='context'
                    required
                    label={ <FormattedMessage id="context" defaultMessage="Context"/>}
                    type='text'
                    name='context'
                    margin='normal'
                    value={api.context}
                    onChange={handleInputChange}
                />
                <TextField
                    fullWidth
                    id='endpoint'
                    label={ <FormattedMessage id="endpoint" defaultMessage="Endpoint"/>}
                    type='text'
                    name='endpoint'
                    margin='normal'
                    value={endpoint && endpoint.url}
                    onChange={handleInputChange}
                />
                <ScopeValidation resourcePath={resourcePath.API_CHANGE_LC} resourceMethod={resourceMethod.POST}>
                    {policies && <Policies {...policiesProps} />}
                </ScopeValidation>
            </React.Fragment>
        );
    }
}

APIInputForm.defaultProps = {
    validate: false,
};

APIInputForm.propTypes = {
    validate: PropTypes.bool,
    api: PropTypes.shape({}).isRequired,
    handleInputChange: PropTypes.func.isRequired,
};
