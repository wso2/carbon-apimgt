import React from 'react';
import Footer from './Footer';

describe('Test scenarios for <Footer/> component', () => {
    test.skip('should match with the existing snapshot', () => {
        const wrapper = renderer.create(<Footer />).toJSON();
        expect(wrapper).toMatchSnapshot();
    });
});
