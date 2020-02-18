/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
import React from 'react';
import PropTypes from 'prop-types';
import 'swagger-ui/dist/swagger-ui.css';
import PatchedSwaggerUI from './PatchedSwaggerUIReact';

const disableAuthorizeAndInfoPlugin = function () {
    return {
        wrapComponents: {
            info: () => () => null,
        },
    };
};
/**
 *
 * @class SwaggerUI
 * @extends {Component}
 */
const SwaggerUI = (props) => {
    const {
        spec,
        url,
    } = props;

    const componentProps = {
        spec,
        url,
        validatorUrl: null,
        docExpansion: 'list',
        defaultModelsExpandDepth: 0,
        presets: [disableAuthorizeAndInfoPlugin],
        plugins: null,
    };
    return <PatchedSwaggerUI {...componentProps} />;
};

SwaggerUI.propTypes = {
    accessTokenProvider: PropTypes.func.isRequired,
    authorizationHeader: PropTypes.string.isRequired,
    api: PropTypes.shape({
        context: PropTypes.string.isRequired,
    }).isRequired,
    url: PropTypes.string.isRequired,
};
export default SwaggerUI;
