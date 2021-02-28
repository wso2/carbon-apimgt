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
import Autosuggest from 'react-autosuggest';
import { MemoryRouter } from 'react-router-dom';
import MuiThemeProvider from '@material-ui/core/styles/MuiThemeProvider';
import createMuiTheme from '@material-ui/core/styles/createMuiTheme';
import Themes from 'AppData/defaultTheme';
import { mountWithIntl } from 'AppTests/Utils/IntlHelper';
import API from 'AppData/api.js';
import AuthManager from 'AppData/AuthManager';
import { getExampleBodyById } from 'AppTests/Utils/MockAPIModel';
import { resourceMethod } from 'AppData/ScopeValidation';
import HeaderSearch from './HeaderSearch';

const mockedHasScopes = jest.fn();
const mockedSearch = jest.fn();
jest.mock('history', () => {
    const originalHistory = jest.requireActual('history');
    const { createMemoryHistory } = originalHistory;
    const mockedPush = createMemoryHistory('/apis/');
    mockedPush.push = jest.fn();
    return {
        ...originalHistory,
        createMemoryHistory: jest.fn(() => mockedPush),
    };
});

describe('Publisher <HeaderSearch> component tests', () => {
    beforeAll(async () => {
        AuthManager.hasScopes = mockedHasScopes.bind(AuthManager);
        API.search = mockedSearch.bind(API);
        const searchResponse = await getExampleBodyById('/search', resourceMethod.GET, 'searchApis');
        mockedSearch.mockReturnValue(Promise.resolve({ obj: searchResponse }));
    });
    /**
     * Mounts the Header Search component
     * @returns
     * wrapper: mounted HeaderSearch component
     */
    async function mountHeaderSearchComponent() {
        const { light } = Themes;
        const headerSearchComponent = (
            <MuiThemeProvider theme={createMuiTheme(light)}>
                <MemoryRouter>
                    <HeaderSearch classes={{}} />
                </MemoryRouter>
            </MuiThemeProvider>
        );
        const wrapper = await mountWithIntl(headerSearchComponent);
        return wrapper;
    }

    test.skip('should render the Autosuggest component', async () => {
        const wrapper = await mountHeaderSearchComponent();
        expect(wrapper.find(Autosuggest).exists()).toBeTruthy();
    });
    test.skip('user entered search query should be passed to the search api call', async () => {
        const wrapper = await mountHeaderSearchComponent();
        await wrapper.find('#searchQuery input').simulate('change', { target: { value: 'test' } });
        expect(wrapper.find('#searchQuery input').props().value).toEqual('test');
        await wrapper.update();
        expect(mockedSearch).toHaveBeenCalledWith({ limit: 8, query: 'content:test' });
    });
    test.skip('search results should be displayed for the user provided search query', async () => {
        const wrapper = await mountHeaderSearchComponent();
        await wrapper.find('#searchQuery input').simulate('focus');
        await wrapper.find('#searchQuery input').simulate('change', { target: { value: 'test' } });
        await wrapper.update();
        // Assuming the mocked api response have atleast one api as search result
        expect(wrapper.find(Autosuggest).props().suggestions.length).toBeGreaterThan(0);
    });
    test.skip('When a search result is clicked,name of the clicked one should be returned', async () => {
        const wrapper = await mountHeaderSearchComponent();
        const searchQueryInput = wrapper.find('#searchQuery input');
        await searchQueryInput.simulate('focus');
        await searchQueryInput.simulate('change', { target: { value: 'test' } });
        await wrapper.update();
        const autoSuggestProps = wrapper.find(Autosuggest).props();
        const firstSuggestion = autoSuggestProps.suggestions[0];
        const suggestionName = autoSuggestProps.getSuggestionValue(firstSuggestion);
        expect(suggestionName).toEqual(firstSuggestion.name);
    });
    test.skip('Pressing Enter key on a search result takes to the relevant artifact page', async () => {
        const wrapper = await mountHeaderSearchComponent();
        const searchQueryInput = wrapper.find('#searchQuery input');
        await searchQueryInput.simulate('focus');
        await searchQueryInput.simulate('change', { target: { value: 'test' } });
        expect(wrapper.find('HeaderSearch').state().searchText).toEqual('test');
        await wrapper.update();
        const autoSuggestProps = wrapper.find(Autosuggest).props();
        const firstSuggestion = autoSuggestProps.suggestions[0];
        autoSuggestProps.onSuggestionSelected({ key: 'Enter' }, { suggestion: firstSuggestion });
        const expectedPath = firstSuggestion.type === 'API' ? `/apis/${firstSuggestion.id}/overview`
            : `/apis/${firstSuggestion.apiUUID}/documents/${firstSuggestion.id}/details`;
        expect(wrapper.find('HeaderSearch').props().history.push.mock.calls[0][0]).toEqual(expectedPath);
    });
    test.skip('Search results needs to be wiped out when search query is erased', async () => {
        const wrapper = await mountHeaderSearchComponent();
        const autoSuggestProps = wrapper.find(Autosuggest).props();
        autoSuggestProps.onSuggestionsClearRequested();
        expect(wrapper.find(Autosuggest).props().suggestions.length).toEqual(0);
    });
    test.skip('Search query needs to be cleared when search input is focus out', async () => {
        const wrapper = await mountHeaderSearchComponent();
        const searchQueryInput = wrapper.find('#searchQuery input');
        await searchQueryInput.simulate('change', { target: { value: 'test' } });
        expect(wrapper.find('#searchQuery input').props().value).toEqual('test');
        await wrapper.find('#searchQuery input').props().onBlur();
        expect(wrapper.find('HeaderSearch').state().searchText).toEqual('');
    });
});
