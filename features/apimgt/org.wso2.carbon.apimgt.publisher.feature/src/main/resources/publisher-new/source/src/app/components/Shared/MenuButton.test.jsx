import React from 'react';
import Button from '@material-ui/core/Button';
import { unwrap } from '@material-ui/core/test-utils';
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
        expect(wrapper.state().open).toBeTruthy();
        wrapper
            .find('ClickAwayListener')
            .props()
            .onClickAway(mockedEvent);
        expect(wrapper.state().open).toBeTruthy();
    });

    test('should close the menu when clicked away', () => {
        const dropDownButton = wrapper.find(Button);
        dropDownButton.simulate('click');
        const mockedEvent = { target: null };
        expect(wrapper.state().open).toBeTruthy();
        wrapper
            .find('ClickAwayListener')
            .props()
            .onClickAway(mockedEvent);
        expect(wrapper.state().open).toBeFalsy();
        // TODO: Need to check whether TestingMenu is closed
    });
});
