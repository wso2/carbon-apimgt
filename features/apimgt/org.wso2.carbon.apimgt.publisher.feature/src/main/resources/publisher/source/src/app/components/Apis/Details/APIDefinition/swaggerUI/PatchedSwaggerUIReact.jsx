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
import swaggerUIConstructor, { presets } from 'swagger-ui';

/**
 *
 *
 * @export
 * @class SwaggerUI
 * @extends {React.Component}
 */
export default class PatchedSwaggerUI extends React.Component {
    constructor(props) {
        super(props);
        this.SwaggerUIComponent = null;
        this.system = null;
    }

    /**
     *
     *
     * @memberof SwaggerUI
     */
    componentDidMount() {
        const ui = swaggerUIConstructor({
            spec: this.props.spec,
            url: this.props.url,
            defaultModelsExpandDepth: this.props.defaultModelsExpandDepth,
            presets: [presets.apis, ...this.props.presets],
            requestInterceptor: this.requestInterceptor,
            responseInterceptor: this.responseInterceptor,
            onComplete: this.onComplete,
            docExpansion: this.props.docExpansion,
            supportedSubmitMethods: [],
        });

        this.system = ui;
        this.SwaggerUIComponent = ui.getComponent('App', 'root');

        this.forceUpdate();
    }

    /**
     *
     *
     * @param {*} prevProps
     * @memberof SwaggerUI
     */
    componentDidUpdate(prevProps) {
        if (this.props.url !== prevProps.url) {
            // flush current content
            this.system.specActions.updateSpec('');

            if (this.props.url) {
                // update the internal URL
                this.system.specActions.updateUrl(this.props.url);
                // trigger remote definition fetch
                this.system.specActions.download(this.props.url);
            }
        }

        if (this.props.spec !== prevProps.spec && this.props.spec) {
            if (typeof this.props.spec === 'object') {
                this.system.specActions.updateSpec(JSON.stringify(this.props.spec));
            } else {
                this.system.specActions.updateSpec(this.props.spec);
            }
        }
    }

    render() {
        return this.SwaggerUIComponent ? <this.SwaggerUIComponent /> : null;
    }
}
PatchedSwaggerUI.defaultProps = {
    docExpansion: 'list',
    defaultModelsExpandDepth: 1,
    presets: [],
    spec: '',
};

PatchedSwaggerUI.propTypes = {
    spec: PropTypes.shape({}),
    url: PropTypes.string.isRequired,
    defaultModelsExpandDepth: PropTypes.number,
    docExpansion: PropTypes.oneOf(['list', 'full', 'none']),
    presets: PropTypes.arrayOf(PropTypes.func),
};
