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
// These must be the first lines in source/index.js
import 'react-app-polyfill/ie11';
import 'react-app-polyfill/stable';
import 'fastestsmallesttextencoderdecoder'; // Added to fix TextEncoding issue in edge <79

import { hot } from 'react-hot-loader/root'; // Added to do the Hot Module Replacement(HMR)
import ReactDOM from 'react-dom';
import React from 'react';
import Admin from './src/Admin';

// What about production? # https://github.com/gaearon/react-hot-loader#what-about-production
const HotApp = hot(Admin);
ReactDOM.render(<HotApp />, document.getElementById('react-root'));
