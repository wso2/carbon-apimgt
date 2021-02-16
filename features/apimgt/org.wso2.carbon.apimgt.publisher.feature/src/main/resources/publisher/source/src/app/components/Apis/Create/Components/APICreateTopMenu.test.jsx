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
import { mountWithIntl } from 'AppTests/Utils/IntlHelper';
import { MemoryRouter, Link } from 'react-router-dom';

import APICreateTopMenu from './APICreateTopMenu';

describe('<APICreateTopMenu/> tests', () => {
    test('should render APICreateTopMenu without errors', () => {
        const wrappedComponent = (
            <MemoryRouter>
                <APICreateTopMenu />
            </MemoryRouter>
        );
        const wrapper = mountWithIntl(wrappedComponent);
        // Show have a back link to home page
        const backLink = wrapper.find(Link);
        expect(backLink).toHaveLength(1);
        expect(backLink.props().to).toEqual('/apis');
    });
});
