/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
// TODO: Move to `User` class , drawback is updating new property require JSON parse, Stringify cycle ~tmkb
import Utils from 'AppData/Utils';
import User from 'AppData/User';

const environmentName = Utils.getCurrentEnvironment().label;
const userStorageKey = `${User.CONST.LOCAL_STORAGE_USER}_${environmentName}`;

export const updateUserLocalStorage = (key, newValue) => {
    const userData = JSON.parse(localStorage.getItem(userStorageKey));
    userData[key] = newValue;
    localStorage.setItem(userStorageKey, JSON.stringify(userData));
};

export const getUserLocalStorage = (key) => {
    const userData = JSON.parse(localStorage.getItem(userStorageKey));
    return userData[key];
};
