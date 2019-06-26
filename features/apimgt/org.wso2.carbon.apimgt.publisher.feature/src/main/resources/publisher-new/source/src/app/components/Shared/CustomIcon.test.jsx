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
import CustomIcon from './CustomIcon';

describe('<CustomIcon> Tests', () => {
    test('should render with default props', () => {
        const wrapper = mount(<CustomIcon />);
        const { defaultProps } = CustomIcon;
        expect(wrapper.props()).not.toBeUndefined();
        expect(wrapper.props()).toMatchObject(defaultProps);
        expect(wrapper.find('svg')).toHaveLength(1);
    });

    test('Should return svg icon for overview', () => {
        const wrapper = mount(<CustomIcon />);
        const { defaultProps } = CustomIcon;
        // Check overview prop
        wrapper.setProps({ icon: 'overview' });
        expect(wrapper.props()).not.toMatchObject(defaultProps);
        expect(wrapper.find('svg')).toHaveLength(1);
    });

    test('should return null if not one of supported icon types', () => {
        const wrapper = mount(<CustomIcon />);

        // If not a supported icon type should return null
        wrapper.setProps({ icon: 'noExist' });
        expect(wrapper.html()).toBeNull();
    });
});
