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
import Enzyme, { shallow, render, mount } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import renderer from 'react-test-renderer';
// React 16 Enzyme adapter
Enzyme.configure({ adapter: new Adapter() });

// Make Enzyme functions available in all test files without importing
global.React = React;
global.shallow = shallow;
global.render = render;
global.mount = mount;
global.renderer = renderer;
if (global.document) {
    // To resolve createRange not defined issue https://github.com/airbnb/enzyme/issues/1626#issuecomment-398588616
    document.createRange = () => ({
        setStart: () => {},
        setEnd: () => {},
        commonAncestorContainer: {
            nodeName: 'BODY',
            ownerDocument: document,
        },
    });
}
