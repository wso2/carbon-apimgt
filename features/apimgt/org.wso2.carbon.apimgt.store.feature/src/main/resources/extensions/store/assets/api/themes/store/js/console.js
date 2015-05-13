/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

/*
 This js function is capable of printing warning, information and error messages directly to the console(terminal)
 */
(function () {

    var WARN_MSG = '[WARN]';
    var INFO_MSG = '[INFO]';
    var ERR_MSG = '[ERR]';

    var main = function () {

        if (checkConsoleObject()) {
            init(console);
            return;
        }

        initEmptyLogger(console);
    };

    var checkConsoleObject = function () {
        if (typeof console==undefined) {
            return false;
        }
        return true;
    };

    var init = function (console) {

        console.info = function (msg) {
            console.log(INFO_MSG + msg);
        };

        console.warn = function (msg) {
            console.log(WARN_MSG + msg);
        };

        console.err = function (msg) {
            console.log(ERR_MSG + msg);
        };
    }

    var initEmptyLogger = function (console) {
        console={};
        console.info = function (msg) {

        };

        console.debug = function (msg) {

        };

        console.warn = function (msg) {

        };

        console.log = function (msg) {

        };
    }

    main();
})();