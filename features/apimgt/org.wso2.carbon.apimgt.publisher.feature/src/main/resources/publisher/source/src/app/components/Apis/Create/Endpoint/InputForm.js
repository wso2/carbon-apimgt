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

import React, {Component} from 'react'
import PropTypes from 'prop-types'
import 'react-toastify/dist/ReactToastify.min.css';
import Policies from '../../Details/LifeCycle/Policies.js'
import {ScopeValidation, resourceMethod, resourcePath} from '../../../../data/ScopeValidation';
import TextField from 'material-ui/TextField';
import API from '../../../../data/api.js'

export default class InputForm extends Component {
    constructor(props) {
        super(props);
        this.api = new API();
        this.state = {
            apiFields: '',
            validate: false,
            policies: []
        };
    }

    componentDidMount() {
        let promised_tier = this.api.policies('api');
        promised_tier.then(response => {
            let tiers = response.obj;
            this.setState({policies: tiers});
        })
    }

    render() {
        const {policies} = this.state;
        const {apiFields, validate, handleInputChange} = this.props;
        const SuperScriptAsterisk = _ => (<sup style={{color: "red"}}>*</sup>);
        const props = {
            policies: this.state.policies,
            handlePolicies: handleInputChange,
            selectedPolicies: apiFields && apiFields.selectedPolicies
        };
        return (
            <div>
                <TextField
                    error={!apiFields.apiName && validate}
                    id="apiName"
                    label={<div><span>Name </span><SuperScriptAsterisk/></div>}
                    type="text"
                    name="apiName"
                    margin="normal"
                    style={{width: "100%"}}
                    value={apiFields.apiName}
                    onChange={handleInputChange}
                />
                <TextField
                    // InputLabelProps={inputLabelClass}
                    label={<div><span>Version </span><SuperScriptAsterisk/></div>}
                    id="apiVersion"
                    helperText="**Version input not support in this release"
                    type="text"
                    name="apiVersion"
                    margin="normal"
                    style={{width: "100%"}}
                    disabled
                    // value={this.state.apiFields.apiVersion}
                />
                <TextField
                    error={!apiFields.apiContext && validate}
                    id="apiContext"
                    label={<div><span>Context </span><SuperScriptAsterisk/></div>}
                    type="text"
                    name="apiContext"
                    margin="normal"
                    style={{width: "100%"}}
                    value={apiFields.apiContext}
                    onChange={handleInputChange}
                />
                <TextField
                    id="apiEndpoint"
                    label="Endpoint"
                    type="text"
                    name="apiEndpoint"
                    margin="normal"
                    style={{width: "100%"}}
                    value={apiFields.apiEndpoint}
                    onChange={handleInputChange}
                />
                <ScopeValidation resourcePath={resourcePath.API_CHANGE_LC}
                                 resourceMethod={resourceMethod.POST}>

                    {policies ? <Policies {...props}/> : ''}
                </ScopeValidation>
            </div>
        );
    }
}

InputForm.defaultProps = {
    validate: false
}

InputForm.propTypes = {
    validate: PropTypes.bool,
    apiFields: PropTypes.object,
    handleInputChange: PropTypes.func.isRequired
};