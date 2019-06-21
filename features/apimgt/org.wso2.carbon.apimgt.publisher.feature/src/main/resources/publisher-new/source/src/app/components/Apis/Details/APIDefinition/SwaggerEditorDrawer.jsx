/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import React from "react";
import PropTypes from "prop-types";
import {Progress} from "AppComponents/Shared";
import SwaggerEditor, {plugins, presets} from "swagger-editor";
import "swagger-editor/dist/swagger-editor.css";

class SwaggerEditorDrawer extends React.Component {
    constructor(props) {
        super(props);
        this.state = {};

    }
    componentDidMount() {
        const editor = SwaggerEditor({
            dom_id: '#swagger-editor',
            layout: 'EditorLayout',
            plugins: Object.values(plugins),
            supportedSubmitMethods: [],
            debounce: 10,
            components: {},
            showExtensions: false,
            swagger2GeneratorUrl: "https://generator.swagger.io/api/swagger.json",
            oas3GeneratorUrl: "https://generator3.swagger.io/openapi.json",
            swagger2ConverterUrl: "https://converter.swagger.io/api/convert",
        });
    }

    render() {
        if (!this.props.swagger) {
            <Progress />
        }

        return (
            <div id="swagger-editor">
            </div>
        )
    }
}
SwaggerEditorDrawer.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    state: PropTypes.shape({}).isRequired,
};
export default SwaggerEditorDrawer;
