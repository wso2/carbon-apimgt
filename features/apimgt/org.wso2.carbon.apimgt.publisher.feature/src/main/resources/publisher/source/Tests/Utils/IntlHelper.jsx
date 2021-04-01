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

/**
 * Components using the react-intl module require access to the intl context.
 * This is not available when mounting single components in Enzyme.
 * These helper functions aim to address that and wrap a valid,
 * English-locale intl context around them.
 */

/**
 * WSO2 Addition : This pice of code is adapted from the suggested approach for enzyme mount
 * and shallow testing in official react-intl docs
 * For more info refer: https://github.com/formatjs/react-intl/blob/master/docs/Testing-with-React-Intl.md#enzyme
 */

import React from 'react';
import { IntlProvider, intlShape, useIntl } from 'react-intl';
import { mount, shallow } from 'enzyme';

// You can pass your messages to the IntlProvider. Optional: remove if not needed.
const messages = require('../../../site/public/locales/raw.en.json'); // en.json

// Create the IntlProvider to retrieve context for wrapping around.
const IntlProviderWithMessages = new IntlProvider({ locale: 'en', messages }, {});
// const { intl } = intlProvider.getChildContext();

/**
 * When using React-Intl `injectIntl` on components, props.intl is required.
 */
function nodeWithIntlProp(node) {
    return React.cloneElement(node, { intl });
}

/**
 *
 *
 * @export Shallow render method with injected Intl context
 * @param {*} node
 * @param {*} [{ context, ...additionalOptions }={}]
 * @returns
 */
export function shallowWithIntl(node, { context, ...additionalOptions } = {}) {
    return shallow(nodeWithIntlProp(node), {
        context: { ...context, intl },
        ...additionalOptions,
    });
}

/**
 *
 *
 * @export Mount method with injected Intl context
 * @param {*} node
 * @param {*} [{ context, childContextTypes, ...additionalOptions }={}]
 * @returns
 */
export function mountWithIntl(node) {
    const Wrapper = () => {
        return (
            <IntlProvider locale='en' messages={messages}>
                {node}
            </IntlProvider>
        );
    };
    return mount(<Wrapper />);
}
