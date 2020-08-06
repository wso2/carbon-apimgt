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
import AuthManager from 'AppData/AuthManager';
import User from 'AppData/User';
import API from 'AppData/api.js';
import { unwrap } from '@material-ui/core/test-utils';
import { createMemoryHistory } from 'history';
import SwaggerClient from 'swagger-client';
import createMuiTheme from '@material-ui/core/styles/createMuiTheme';
import MuiThemeProvider from '@material-ui/core/styles/MuiThemeProvider';

import getMockedModel, { getAllScopes } from 'AppTests/Utils/MockAPIModel.js';
import MenuItem from '@material-ui/core/MenuItem';
import Policies from 'AppComponents/Apis/Details/LifeCycle/Policies';
import Themes from 'AppData/defaultTheme';
import APICreateDefault from './APICreateDefault';

const mockedGetUser = jest.fn();
const mockedHasScopes = jest.fn();

AuthManager.getUser = mockedGetUser.bind(AuthManager);
const theme = createMuiTheme(Themes.light);

describe('<APICreateForm/> tests', () => {
    let spec;

    afterEach(() => {
        jest.restoreAllMocks();
        jest.clearAllMocks();
        jest.resetModules();
        jest.resetModuleRegistry();
        // eslint-disable-next-line no-underscore-dangle
        User._userMap.clear();
    });
    beforeAll(async () => {
        spec = await apiDef;
    });
    test.skip('should not show the policies dropdown if user dose not have required scopes', async () => {
        const mockedResolve = Promise.resolve({ spec });
        SwaggerClient.resolve = jest.fn(() => mockedResolve).bind(SwaggerClient);

        const mockedPolicies = jest.fn();
        API.policies = mockedPolicies.bind(API);

        const mockedPoliciesData = await getMockedModel('ThrottlingPolicyList');
        mockedPolicies.mockReturnValue(Promise.resolve({ obj: mockedPoliciesData }));

        const mockedUser = new User('DEFAULT', 'admin');
        const allScopes = await getAllScopes();
        mockedUser.scopes = allScopes.filter((policy) => policy !== 'apim:api_publish');
        mockedGetUser.mockReturnValue(mockedUser);
        const originalAuthManager = require.requireActual('AppData/AuthManager');

        AuthManager.prototype.hasScopes = originalAuthManager.default.hasScopes;
        jest.doMock('AppData/AuthManager', () => {
            const originalAuthManagers = require.requireActual('AppData/AuthManager');
            return {
                hasScopes: originalAuthManagers.default.hasScopes,
            };
        });
        const wrapper = await mountWithIntl(<MuiThemeProvider theme={theme}>
            <APICreateDefault
                api={new API('mockAPI', '1.1.1', '/sample')}
                handleSubmit={() => {}}
                inputChange={() => {}}
                isAPIProduct={false}
                valid={{ name: '', version: '', context: '' }}
            />
        </MuiThemeProvider>);
        await new Promise((resolve) => setImmediate(resolve));
        await wrapper.update();
        const policiesDropDown = await wrapper.find(Policies);
        expect(policiesDropDown.length).toBe(0);
    });

    test.skip('should render APICreateForm without any issues', async () => {
        AuthManager.hasScopes = mockedHasScopes.bind(AuthManager);
        SwaggerClient.resolve = jest.fn(() => Promise.resolve({ spec })).bind(SwaggerClient);

        // Create mock functions to be used in static methods mocking
        // These are just dump jest mock function, Will return `undefined` unless otherwise specify a return value
        const mockedPolicies = jest.fn();

        // Binding the mock functions to modules
        // Mock return values will be set in each individual test cases
        API.policies = mockedPolicies.bind(API);

        // Mocking ES6 class module
        jest.mock('AppData/APIClientFactory', () => {
            return function () {
                return {
                    getAPIClient: jest.fn(() => ({ client: {} })),
                };
            };
        });

        // Get generated mock data giving the models/definition name, that are used in the testing component
        // These derived testing data will be used for mocking API responses or building API request payloads
        const mockedPoliciesData = await getMockedModel('ThrottlingPolicyList');
        const sampleAPIData = await getMockedModel('API');

        // Setting up class static and instance method mocks and spies
        const APISaveSpy = jest.spyOn(API.prototype, 'save').mockImplementation(() => Promise.resolve({}));
        const history = createMemoryHistory('/apis/');
        // const historyPushSpy = jest.spyOn(history, 'push');

        mockedHasScopes.mockReturnValue(Promise.resolve(true));
        mockedPolicies.mockReturnValue(Promise.resolve({ obj: mockedPoliciesData }));

        // Create a mock user for testing
        const mockedUser = new User('DEFAULT', 'admin');
        mockedUser.scopes = await getAllScopes();
        mockedGetUser.mockReturnValueOnce(mockedUser);

        // The moment we wait for :) , Mounting the testing component
        const wrapper = mountWithIntl(<MuiThemeProvider theme={theme}>
            <APICreateDefault
                api={new API(sampleAPIData)}
                handleSubmit={() => {}}
                inputChange={() => {}}
                isAPIProduct={false}
                valid={{ name: '', version: '', context: '' }}
                history={history}
            />
        </MuiThemeProvider>);

        // Simulate typing values into input fields, Entering API name, version , context , endpoint
        // and selecting a policy
        await wrapper.find('#name input').simulate('change', { target: { name: 'name', value: sampleAPIData.name } });
        await wrapper
            .find('#version input')
            .simulate('change', { target: { name: 'version', value: sampleAPIData.version } });
        await wrapper
            .find('#context input')
            .simulate('change', { target: { name: 'context', value: sampleAPIData.context } });
        const { url } = sampleAPIData.endpointConfig.production_endpoints;
        await wrapper.find('#endpoint input').simulate('change', { target: { name: 'endpoint', value: url } });
        await wrapper
            .find(Policies)
            .find('[role="button"]')
            .simulate('click');

        // If our examples produce more than one policy
        if (wrapper.find(MenuItem).length > 1) {
            await wrapper
                .find(MenuItem)
                .at(1)
                .simulate('click');
        } else {
            // Else If there is only one policy in the `mockedPoliciesData`
            await wrapper.find(MenuItem).simulate('click');
        }

        // Wait till all the above changes get effected
        await wrapper.update();
        // Wait till all the mocked Promises get resource, Or in other words , wait till Promise queue get exhausted
        await new Promise((resolve) => setImmediate(resolve));

        // Simulate form submission
        await wrapper.find("button[type='submit']").simulate('submit');

        // Doing assertions for validating the expected behavior or output
        const { api } = await wrapper.find(unwrap(APICreateDefault)).props();

        // Expecting API.save instance method to be called at least once
        // expect(APISaveSpy).toHaveBeenCalled(); // This should be tested in APICreateWrapper component
        // expect(historyPushSpy).toHaveBeenCalled(); // This should be tested in APICreateWrapper component

        // Note: You must wrap the code in a function,
        // otherwise the error will not be caught and the assertion will fail.
        // expect(() => api.getPolicies()).not.toThrow();
        expect(api.name).toEqual(sampleAPIData.name);
        expect(api.version).toEqual(sampleAPIData.version);
        expect(api.context).toEqual(sampleAPIData.context);
        // expect(api.policies).toContain(mockedPoliciesData.list[0].name);

        expect(api.getProductionEndpoint()).toEqual(url);

        // Cleaning up the mock implementation
        APISaveSpy.mockRestore();
    });

    test('should not allow to submit new API with invalid inputs', () => {});
    test('should not allow to create new API without required inputs', () => {});

    test('should not show the API create submit button if user do not have required scopes', () => {});
});
