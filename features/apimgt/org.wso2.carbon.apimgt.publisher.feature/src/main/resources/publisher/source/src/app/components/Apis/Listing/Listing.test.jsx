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
import API from 'AppData/api.js';
import { mountWithIntl } from 'AppTests/Utils/IntlHelper';
import getMockedModel, { getAllScopes } from 'AppTests/Utils/MockAPIModel.js';
import MuiThemeProvider from '@material-ui/core/styles/MuiThemeProvider';
import createMuiTheme from '@material-ui/core/styles/createMuiTheme';
import Themes from 'AppData/defaultTheme';
import { MemoryRouter } from 'react-router-dom';
import ScopeValidation from 'AppData/ScopeValidation';
import AuthManager from 'AppData/AuthManager';
import User from 'AppData/User';
import SampleAPI from './SampleAPI/SampleAPI';
import Listing from './Listing';
import TableView from './TableView/TableView';

const mockedGetUser = jest.fn();
AuthManager.getUser = mockedGetUser.bind(AuthManager);

jest.mock('AppData/ScopeValidation');
jest.mock('AppData/api.js', () => {
    const mockedAPI = function () {
        return {
            getAPIThumbnail: () => {
                return Promise.resolve({});
            },
        };
    };
    const OriginalAPI = jest.requireActual('AppData/api');
    Object.assign(mockedAPI, OriginalAPI.default);
    return mockedAPI;
});
const { light } = Themes;

const mockedHasScopes = jest.fn();
const mockedAll = jest.fn();

describe('APIs <Listing/> component tests', () => {
    beforeAll(async () => {
        API.all = mockedAll.bind(API);
        ScopeValidation.hasScopes = mockedHasScopes.bind(ScopeValidation);
        const mockedUser = new User('DEFAULT', 'admin');
        const allScopes = await getAllScopes();
        mockedUser.scopes = allScopes.filter((policy) => policy !== 'apim:api_publish');
        mockedGetUser.mockReturnValue(mockedUser);
    });

    afterEach(() => {
        mockedAll.mockReset();
    });

    test.skip('should shallow render the listing page', async () => {
        mockedAll.mockReturnValue(Promise.resolve({ body: { list: [], pagination: { total: 0 } } }));
        const WithStyleListing = (
            <MuiThemeProvider theme={createMuiTheme(light)}>
                <Listing classes={{}} theme={createMuiTheme(light)} />
            </MuiThemeProvider>
        );
        let wrapper = await mountWithIntl(WithStyleListing);
        // update the wrapper until the progress/loading icon disappears
        while (wrapper.find('#apim-loader').length > 0) {
            wrapper = await wrapper.update();
        }
        expect(wrapper.contains(<SampleAPI />)).toBeTruthy();
    });

    test.skip('should mount and render the listing page with given APIs list', async () => {
        const ThemedListing = (
            <MuiThemeProvider theme={createMuiTheme(light)}>
                <MemoryRouter>
                    <Listing />
                </MemoryRouter>
            </MuiThemeProvider>
        );
        const mockedModel = await getMockedModel('APIList');
        mockedAll.mockReturnValue(Promise.resolve({ body: mockedModel.right }));
        mockedHasScopes.mockReturnValue(Promise.resolve(true));

        let wrapper = await mountWithIntl(ThemedListing);
        wrapper = await wrapper.update();
        // Calling children() because Listing component has exported with withstyle wrapper
        // Instead of double .children() calls to unwrap intl and styles , We could use .Naked as well
        expect(wrapper
            .find(TableView)
            .children()
            .children()
            .state().apisAndApiProducts).toEqual(mockedModel.right.list);

        // update the wrapper until the progress/loading icon disappears
        while (wrapper.find('#apim-loader').length > 0) {
            wrapper = await wrapper.update();
        }

        expect(wrapper.contains(mockedModel.right.list[0].name)).toBeTruthy();
        expect(wrapper.contains(mockedModel.right.list[0].version)).toBeTruthy();
        expect(wrapper.contains(mockedModel.right.list[0].context)).toBeTruthy();

        expect(wrapper.contains('Create API')).toBeTruthy();
    });

    test.todo('should remove the API from listing when clicked on delete button');
    test.todo('should navigate to API overview page when clicked on API thumb');
    test.todo('should show table view when clicked on toggle button');
});
