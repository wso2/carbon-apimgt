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
import qs from 'qs';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import API from 'AppData/api.js';
import { Progress } from 'AppComponents/Shared';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import SampleAPI from './SampleAPI/SampleAPI';
import CardView from './CardView/CardView';
import TableView from './TableView/TableView';
import TopMenu from './components/TopMenu';
import Listing from './Listing';
import { mountWithIntl } from 'AppTests/Utils/IntlHelper.js';
import { unwrap } from '@material-ui/core/test-utils';
import MuiThemeProvider from '@material-ui/core/styles/MuiThemeProvider';
import createMuiTheme from '@material-ui/core/styles/createMuiTheme';

import Configurations from 'Config';

jest.mock('AppData/api.js');
const mockedAll = jest.fn();

describe('APIs <Listing/> component tests', () => {
    beforeAll(() => {
        API.all = mockedAll.bind(API);
    });

    afterEach(() => {
        mockedAll.mockReset();
    });

    test('should shallow render the listing page', () => {
        const { light } = Configurations.themes;
        const ThemedListing = (
            <MuiThemeProvider theme={createMuiTheme(light)}>
                <Listing />
            </MuiThemeProvider>
        );
        mockedAll.mockReturnValue(Promise.resolve({ obj: { list: [] } }));
        const wrapper = mountWithIntl(ThemedListing);
        expect(wrapper.contains('Create an API')).toBeTruthy();
    });

    test('should shallow render the listing page 2', async () => {
        mockedAll.mockReturnValue(Promise.resolve({ obj: { list: [] } }));
        const UnWrappedListing = unwrap(Listing);
        const { light } = Configurations.themes;
        const wrapper = await shallow(<UnWrappedListing classes={{}} theme={createMuiTheme(light)} />);
        expect(wrapper.contains(<SampleAPI />)).toBeTruthy();
    });
});
