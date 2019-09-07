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
import { MemoryRouter, Route } from 'react-router-dom';
import MuiThemeProvider from '@material-ui/core/styles/MuiThemeProvider';
import createMuiTheme from '@material-ui/core/styles/createMuiTheme';
import Configurations from 'Config';
import { mountWithIntl } from 'AppTests/Utils/IntlHelper';
import APIs from './Apis';
import ApiCreate from './Create/ApiCreate';

import { PageNotFound } from '../Base/Errors';

jest.mock('./Listing/Listing', () => () => {
    return <div>Testing Listing page</div>;
});

const { light } = Configurations.themes;

describe('Test APIs main routing component', () => {
    test('Should render the APIs routing component in smoke test', () => {
        shallow(<APIs />);
    });

    test('should return API Listing component when request path match with /apis', () => {
        const exactPath = '/apis';
        const exactApisPath = (
            <MemoryRouter initialEntries={[exactPath]}>
                <APIs />
            </MemoryRouter>
        );
        const wrapper = mountWithIntl(exactApisPath);
        expect(wrapper.find(Route).prop('path')).toEqual(exactPath);
        expect(wrapper.contains('Testing Listing page')).toBeTruthy();
    });
    test('should return API product Listing component when request path match with /api-products', () => {
        const exactPath = '/api-products';
        const exactApisPath = (
            <MemoryRouter initialEntries={[exactPath]}>
                <APIs />
            </MemoryRouter>
        );
        const wrapper = mountWithIntl(exactApisPath);
        expect(wrapper.find(Route).prop('path')).toEqual(exactPath);
        expect(wrapper.contains('Testing Listing page')).toBeTruthy();
    });
    test('should return ApiCreate component when request path match with /apis/create', () => {
        const apiCreatePath = '/apis/create';
        const createAPI = (
            <MemoryRouter initialEntries={[apiCreatePath]}>
                <APIs />
            </MemoryRouter>
        );

        const wrapper = mountWithIntl(createAPI);
        expect(wrapper.find(ApiCreate)).toHaveLength(1);

        // Page not found is expected here, Because we are navigating to exact /apis/create path
        const pageNotFoundWrapper = wrapper.find(PageNotFound);
        expect(pageNotFoundWrapper).toHaveLength(1);
    });

    test.todo('should return API Details component when request path match with /apis/:apiUUID/');
    test('should return PageNotFound component if there is no matching path', () => {
        const url = '/apis/chuck/norris';
        const noneExistingPath = (
            <MemoryRouter initialEntries={[url]}>
                <MuiThemeProvider theme={createMuiTheme(light)}>
                    <APIs />
                </MuiThemeProvider>
            </MemoryRouter>
        );
        const wrapper = mountWithIntl(noneExistingPath);
        const pageNotFoundWrapper = wrapper.find(PageNotFound);
        expect(pageNotFoundWrapper).toHaveLength(1);
        expect(pageNotFoundWrapper.contains('404 Page Not Found!')).toBeTruthy();
        expect(pageNotFoundWrapper.contains(url)).toBeTruthy();
    });
});
