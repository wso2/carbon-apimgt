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
import SwaggerUILib from 'swagger-ui-react';
import 'swagger-ui-react/swagger-ui.css';


const disableAuthorizeAndInfoPlugin = function () {
    return {
        wrapComponents: {
            info: () => () => null,
            authorizeBtn: () => () => null,
        },
    };
};
const disableTryItOutPlugin = function () {
    return {
        statePlugins: {
            spec: {
                wrapSelectors: {
                    allowTryItOutFor: () => () => false,
                },
            },
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
        defaultModelsExpandDepth: -1,
        plugins: [disableAuthorizeAndInfoPlugin, disableTryItOutPlugin],
    };
    return <SwaggerUILib {...componentProps} />;
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
