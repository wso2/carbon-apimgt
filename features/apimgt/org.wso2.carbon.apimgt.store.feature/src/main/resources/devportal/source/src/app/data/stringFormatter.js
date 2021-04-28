/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

/**
 * Capitalize the first letter of a string
 * @param {string} string string to be formatted
 * @return {string} formatted string
 */
function capitalizeFirstLetter(string) {
    return string && string.charAt(0).toUpperCase() + string.slice(1);
}

/**
 * Lowercase all letters in a string
 * @param {string} string string to be formatted
 * @return {string} formatted string
 */
function lowerCaseString(string) {
    return string.toLowerCase();
}

/**
 * Uppercase all letters in a string
 * @param {string} string string to be formatted
 * @return {string} formatted string
 */
function upperCaseString(string) {
    return string.toUpperCase();
}

export { capitalizeFirstLetter, lowerCaseString, upperCaseString };
