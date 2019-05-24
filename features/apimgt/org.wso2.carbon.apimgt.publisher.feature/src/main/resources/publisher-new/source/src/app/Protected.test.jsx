import React from 'react';
import Protected from './ProtectedApp';

describe('Protected component tests', () => {
    test('should render the Protected app component', () => {
        shallow(<Protected />);
    });
});
