import React from 'react';
import Footer from './Footer';

describe('Test scenarios for <Footer/> component', () => {
    test('should match with the existing snapshot', () => {
        const wrapper = renderer.create(<Footer />).toJSON();
        console.log(wrapper);
        expect(wrapper).toMatchSnapshot();
    });
});
