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
import getMockedModel from 'AppTests/Utils/MockAPIModel.js';
import { unwrap } from '@material-ui/core/test-utils';
import MuiThemeProvider from '@material-ui/core/styles/MuiThemeProvider';
import createMuiTheme from '@material-ui/core/styles/createMuiTheme';
import Configurations from 'Config';
import { MemoryRouter } from 'react-router-dom';
import AuthManager from 'AppData/AuthManager';
import SampleAPI from './SampleAPI/SampleAPI';
import Listing from './Listing';

jest.mock('AppData/AuthManager');
jest.mock('AppData/api.js', () => {
    return function () {
        return {
            getAPIThumbnail: () => {
                return Promise.resolve({});
            },
        };
    };
});

const mockedHasScopes = jest.fn();
const mockedAll = jest.fn();

describe('APIs <Listing/> component tests', () => {
    beforeAll(() => {
        API.all = mockedAll.bind(API);
        AuthManager.hasScopes = mockedHasScopes.bind(AuthManager);
    });

    afterEach(() => {
        mockedAll.mockReset();
    });
    /** TODO temporarily disabled tests.enable these tests */
    /*
    test('should shallow render the listing page', async () => {
        mockedAll.mockReturnValue(Promise.resolve({ obj: { list: [] } }));
        const UnWrappedListing = unwrap(Listing);
        const { light } = Configurations.themes;
        const wrapper = await shallow(<UnWrappedListing classes={{}} theme={createMuiTheme(light)} />);
        expect(wrapper.contains(<SampleAPI />)).toBeTruthy();
    });

    test('should mount and render the listing page with given APIs list', async () => {
        const { light } = Configurations.themes;
        const ThemedListing = (
            <MuiThemeProvider theme={createMuiTheme(light)}>
                <MemoryRouter>
                    <Listing />
                </MemoryRouter>
            </MuiThemeProvider>
        );
        const mockedModel = await getMockedModel('APIList');
        mockedAll.mockReturnValue(Promise.resolve({ obj: mockedModel }));
        mockedHasScopes.mockReturnValue(Promise.resolve(true));

        let wrapper = await mountWithIntl(ThemedListing);
        wrapper = await wrapper.update();
        // Calling children() because Listing component has exported with withstyle wrapper
        expect(wrapper
            .find(Listing)
            .children()
            .state().apis).toEqual(mockedModel);

        expect(wrapper.contains(mockedModel.list[0].name)).toBeTruthy();
        expect(wrapper.contains(mockedModel.list[0].version)).toBeTruthy();
        expect(wrapper.contains(mockedModel.list[0].context)).toBeTruthy();

        expect(wrapper.contains('Create API')).toBeTruthy();
    });
    */

    test.todo('should remove the API from listing when clicked on delete button');
    test.todo('should navigate to API overview page when clicked on API thumb');
    test.todo('should show table view when clicked on toggle button');
});
