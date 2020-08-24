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
// import { unwrap } from '@material-ui/core/test-utils';
import MuiThemeProvider from '@material-ui/core/styles/MuiThemeProvider';
import createMuiTheme from '@material-ui/core/styles/createMuiTheme';

import Themes from 'AppData/defaultTheme';
import LeftMenuItem from './LeftMenuItem';

describe('<LeftMenuItem/> tests', () => {
    test.todo('should render <LeftMenuItem/> component with dark theme styles');
    test.todo('should render <LeftMenuItem/> component without themes (unwrapped)');
    test.todo('should render <LeftMenuItem/> contains the property text in wrapped instance');
    test('should render the <LeftMenuItem/> component with light theme styles', () => {
        const { light } = Themes;
        const TestComponent = (
            <MuiThemeProvider theme={createMuiTheme(light)}>
                <LeftMenuItem />
            </MuiThemeProvider>
        );
        // mount(TestComponent);
    });
});
