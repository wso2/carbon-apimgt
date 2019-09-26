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
import Button from '@material-ui/core/Button';
import { unwrap } from '@material-ui/core/test-utils';
import ClickAwayListener from '@material-ui/core/ClickAwayListener';

import MenuButton from './MenuButton';

const UnwrappedMenuButton = unwrap(MenuButton);

describe('<MenuButton/> tests', () => {
    let wrapper;
    const TestingMenu = () => <div>My test menu</div>;
    beforeEach(() => {
        wrapper = mount(<UnwrappedMenuButton menuList={<TestingMenu />} classes={{}} />);
    });

    test('should just render the Menu button', () => {
        const dropDown = wrapper.find(Button);
        expect(dropDown).toHaveLength(1);
    });

    test('should return the menus when clicked', () => {
        let dropDownMenu = wrapper.find(TestingMenu);
        expect(dropDownMenu).toHaveLength(0);
        const dropDownButton = wrapper.find(Button);
        dropDownButton.simulate('click');
        dropDownMenu = wrapper.find(TestingMenu);
        expect(dropDownMenu).toHaveLength(1);
    });

    test('should not close menu by ClickAwayListener if clicked on the menu button', () => {
        const dropDownButton = wrapper.find(Button);
        dropDownButton.simulate('click');
        const mockedEvent = { target: wrapper.getDOMNode()[0] };
        // Menu should be opened when clicked on it
        expect(wrapper.state().open).toBeTruthy();
        wrapper
            .find(ClickAwayListener)
            .props()
            .onClickAway(mockedEvent);
        // Menu should be closed when clicked away from it
        expect(wrapper.state().open).toBeFalsy();
    });

    test('should close the menu when clicked away', () => {
        const dropDownButton = wrapper.find(Button);
        dropDownButton.simulate('click');
        const mockedEvent = { target: null };
        expect(wrapper.state().open).toBeTruthy();
        wrapper
            .find(ClickAwayListener)
            .props()
            .onClickAway(mockedEvent);
        expect(wrapper.state().open).toBeFalsy();
        // TODO: Need to check whether TestingMenu is closed
    });
});
