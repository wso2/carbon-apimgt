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
import { MemoryRouter } from 'react-router-dom';

import DefaultAPIForm from './DefaultAPIForm';

describe('<DefaultAPIForm/> tests', () => {
    test('Should have default input fields', () => {
        const wrappedComponent = (
            <MemoryRouter>
                <DefaultAPIForm />
            </MemoryRouter>
        );
        const wrapper = mountWithIntl(wrappedComponent);
        // Should have 2 default input fields
        const formLabels = wrapper.find('label');
        expect(formLabels).toHaveLength(4); // 4 inputs
        const name = formLabels.at(0).find('FormattedMessage').text();
        expect(name).toEqual('Name');
    });
});
