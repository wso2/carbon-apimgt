/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ReactDOM from 'react-dom'
import React from 'react'
import Publisher from "./src/App.js"
import 'typeface-roboto'
import 'material-design-icons'
import 'material-ui-icons'
import {MuiThemeProvider, createMuiTheme} from 'material-ui/styles';

const theme = createMuiTheme({
    palette: {
        type: 'light', // Switching the dark mode on is a single property value change.
    },
});

ReactDOM.render(<MuiThemeProvider theme={theme}>
    <Publisher/>
</MuiThemeProvider>, document.getElementById("react-root"));