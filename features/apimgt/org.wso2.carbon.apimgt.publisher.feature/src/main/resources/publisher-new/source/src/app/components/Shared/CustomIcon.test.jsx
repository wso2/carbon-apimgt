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
