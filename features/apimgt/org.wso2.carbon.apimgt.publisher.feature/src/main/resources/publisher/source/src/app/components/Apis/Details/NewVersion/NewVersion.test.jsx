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
import { getExampleBodyById, getExampleResponseById } from 'AppTests/Utils/MockAPIModel.js';
import { mountWithIntl } from 'AppTests/Utils/IntlHelper';
import Themes from 'AppData/defaultTheme';
import { MemoryRouter, Redirect } from 'react-router-dom';
import { resourceMethod, resourcePath } from 'AppData/ScopeValidation';
import createMuiTheme from '@material-ui/core/styles/createMuiTheme';
import RadioGroup from '@material-ui/core/RadioGroup';
import { APIProvider } from 'AppComponents/Apis/Details/components/ApiContext';
import NewVersion from './NewVersion';

const FIELD_EMPTY = 'This field cannot be empty';

describe('Unit test for CreateNewVersion component', () => {
    /**
     * Mounts the CreateNewVersion component
     *
     * @returns {Promise<{api: *, mountedNewVersion: *}>} api: api object used to render the component
     * mountedNewVersion: mounted CreateNewVersion component
     */
    async function mountNewVersionComponent() {
        const { light } = Themes;
        const api = await getExampleBodyById(resourcePath.SINGLE_API, resourceMethod.GET, 'getAPI');
        const newVersion = (
            <APIProvider value={{ api }}>
                <MemoryRouter>
                    <NewVersion classes={{}} theme={createMuiTheme(light)} />
                </MemoryRouter>
            </APIProvider>
        );
        const mountedNewVersion = await mountWithIntl(newVersion);
        return { api, mountedNewVersion };
    }

    test('should render the create new version page with given API', async () => {
        const { api, mountedNewVersion } = await mountNewVersionComponent();
        expect(mountedNewVersion.contains('Create New Version')).toBeTruthy();
        expect(mountedNewVersion.contains('Create')).toBeTruthy();
        expect(mountedNewVersion.contains('Cancel')).toBeTruthy();
        expect(mountedNewVersion.find('Link').props().to).toEqual('/apis/' + api.id + '/overview');
        expect(mountedNewVersion.contains(FIELD_EMPTY)).toBeFalsy();
    });

    test('should avoid create new version without specifying version', async () => {
        const { mountedNewVersion } = await mountNewVersionComponent();
        expect(mountedNewVersion.contains(FIELD_EMPTY)).toBeFalsy();
        // simulate a Create Button click without specifying version
        await mountedNewVersion.find('#createBtn button').simulate('click');
        expect(mountedNewVersion.find('CreateNewVersion').state().valid.version.empty).toBeTruthy();
        expect(mountedNewVersion.contains(FIELD_EMPTY)).toBeTruthy();
    });

    test('should create version properly after specifying version', async () => {
        const { mountedNewVersion, api } = await mountNewVersionComponent();
        const newVersionResponse = await getExampleBodyById(
            resourcePath.API_COPY, resourceMethod.POST,
            'copyAPIWithoutDefaultVersion',
        );
        const createNewAPIVersion = jest.fn();
        createNewAPIVersion.mockReturnValue(Promise.resolve({ obj: newVersionResponse }));
        api.createNewAPIVersion = createNewAPIVersion;

        // set new version as v3.0.1 and simulate Create button click
        await mountedNewVersion.find('#newVersion input')
            .simulate('change', { target: { value: 'v3.0.1' } });
        expect(mountedNewVersion.find('#newVersion input').props().value).toEqual('v3.0.1');

        await mountedNewVersion.find('#createBtn button').simulate('click');
        await mountedNewVersion.update();
        expect(createNewAPIVersion).toHaveBeenCalledWith('v3.0.1', false, null);

        expect(mountedNewVersion.find('CreateNewVersion').state().redirectToReferrer).toBeTruthy();
        expect(mountedNewVersion.find(Redirect).props().to).toEqual('/apis/' + newVersionResponse.id + '/overview');
    });

    test('should create version properly after specifying version and default version as true', async () => {
        const { mountedNewVersion, api } = await mountNewVersionComponent();
        const newVersionResponse = await getExampleBodyById(
            resourcePath.API_COPY, resourceMethod.POST,
            'copyAPIWithDefaultVersion',
        );
        const createNewAPIVersion = jest.fn();
        createNewAPIVersion.mockReturnValue(Promise.resolve({ obj: newVersionResponse }));
        api.createNewAPIVersion = createNewAPIVersion;

        // set new version as v5.0.1 and simulate Create button click
        await mountedNewVersion.find('#newVersion input').simulate('change', { target: { value: 'v5.0.1' } });
        expect(mountedNewVersion.find('#newVersion input').props().value).toEqual('v5.0.1');

        mountedNewVersion.find(RadioGroup).at(0).props().onChange({ target: { value: 'yes' } });

        await mountedNewVersion.find('#createBtn button').simulate('click');
        await mountedNewVersion.update();
        expect(createNewAPIVersion).toHaveBeenCalledWith('v5.0.1', true, null);

        expect(mountedNewVersion.find('CreateNewVersion').state().redirectToReferrer).toBeTruthy();
        expect(mountedNewVersion.find(Redirect).props().to).toEqual('/apis/' + newVersionResponse.id + '/overview');
    });

    test('should notify user when conflicting API version created', async () => {
        const { mountedNewVersion, api } = await mountNewVersionComponent();
        const newVersionResponse = await getExampleResponseById(
            resourcePath.API_COPY, resourceMethod.POST,
            'copyWithConflictingVersion',
        );
        const createNewAPIVersion = jest.fn();
        const err = new Error(newVersionResponse.status.msg);
        err.response = newVersionResponse.body;
        err.status = newVersionResponse.status.code;
        createNewAPIVersion.mockReturnValue(Promise.reject(err));
        api.createNewAPIVersion = createNewAPIVersion;

        // set new version as v1.0.1 and simulate Create button click
        await mountedNewVersion.find('#newVersion input').simulate('change', { target: { value: 'v5.0.1' } });
        expect(mountedNewVersion.find('#newVersion input').props().value).toEqual('v5.0.1');

        await mountedNewVersion.find('#createBtn button').simulate('click');
        await mountedNewVersion.update();
        expect(createNewAPIVersion).toHaveBeenCalledWith('v5.0.1', false, null);
        expect(mountedNewVersion.find('CreateNewVersion').state().valid.version.alreadyExists).toBeTruthy();
        expect(mountedNewVersion.find('CreateNewVersion').state().redirectToReferrer).toBeFalsy();
        expect(mountedNewVersion.text().indexOf('already exists')).toBeGreaterThanOrEqual(0);
    });
});
