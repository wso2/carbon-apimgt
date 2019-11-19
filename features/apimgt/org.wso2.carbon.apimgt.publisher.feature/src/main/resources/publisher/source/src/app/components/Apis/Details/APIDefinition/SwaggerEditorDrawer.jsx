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
import React from 'react';
import SwaggerEditor, { plugins } from 'swagger-editor';
import 'swagger-editor/dist/swagger-editor.css';

/**
 * This component hosts the Swagger Editor component.
 * Known Issue: The cursor jumps back to the start of the first line when updating the swagger-ui based on the
 * modification done via the editor.
 * https://github.com/wso2/product-apim/issues/5071
 * */
class SwaggerEditorDrawer extends React.Component {
    /**
     * @inheritDoc
     */
    componentDidMount() {
        window.editor = SwaggerEditor({
            dom_id: '#swagger-editor',
            layout: 'EditorLayout',
            plugins: Object.values(plugins),
            supportedSubmitMethods: [],
            components: {},
            swagger2GeneratorUrl: 'https://generator.swagger.io/api/swagger.json',
            oas3GeneratorUrl: 'https://generator3.swagger.io/openapi.json',
            swagger2ConverterUrl: 'https://converter.swagger.io/api/convert',
        });
    }

    /**
     * @inheritDoc
     */
    render() {
        return (
            <div id='swagger-editor' />
        );
    }
}

export default SwaggerEditorDrawer;
