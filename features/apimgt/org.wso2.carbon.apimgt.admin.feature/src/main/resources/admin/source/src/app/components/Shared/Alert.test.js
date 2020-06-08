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
import Alert from './Alert';

describe('Shared Alerts', () => {
    beforeEach(() => {
        jest.resetModules();
    });

    it('Should render information alert with given message', () => {
        const messageText = 'Test Message';
        const infoMessage = Alert.info(messageText);
        expect(infoMessage.message).toEqual(messageText);
        expect(infoMessage.type).toEqual(Alert.CONSTS.INFO);
    });

    it('Should render warning alert with given message', () => {
        const messageText = 'Test Warning Message';
        const warningMessage = Alert.warning(messageText);
        expect(warningMessage.message).toEqual(messageText);
        expect(warningMessage.type).toEqual(Alert.CONSTS.WARN);
    });

    it('Should render success alert with given message', () => {
        const messageText = 'Test success Message';
        const successMessage = Alert.success(messageText);
        expect(successMessage.message).toEqual(messageText);
        expect(successMessage.type).toEqual(Alert.CONSTS.SUCCESS);
    });

    it('Should render error alert with given message', () => {
        const messageText = 'Test error Message';
        const errorMessage = Alert.error(messageText);
        expect(errorMessage.message).toEqual(messageText);
        expect(errorMessage.type).toEqual(Alert.CONSTS.ERROR);
    });

    it('Should render loading alert with given message', () => {
        const messageText = 'Test loading Message';
        const loadingMessage = Alert.loading(messageText);
        expect(loadingMessage.message).toEqual(messageText);
        expect(loadingMessage.type).toEqual(Alert.CONSTS.LOADING);
    });

    it('Should render info alert with custom duration', () => {
        const messageText = 'Test info Message';
        const duration = 1000;
        const infoMessage = Alert.info(messageText, duration);
        expect(infoMessage.duration).toEqual(duration);
    });

    it('Should return alert object with custom configurations', () => {
        const customConfig = { top: 842, duration: 213 };
        Alert.configs(customConfig);
        const messageText = 'Test info Message';
        const afterConfig = Alert.info(messageText);
        expect(afterConfig.defaultTop).toEqual(customConfig.top);
        expect(afterConfig.duration).toEqual(customConfig.duration);
        Alert.configs({});
        expect(afterConfig.defaultTop).toEqual(customConfig.top);
        expect(afterConfig.duration).toEqual(customConfig.duration);
    });
});
