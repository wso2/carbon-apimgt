import React from 'react';
import { SettingsConsumer } from './SettingsContext';

const withSettings = (WrappedComponent) => {
    /**
     * Higher order component with settings
     * @param {*} props properties
     * @returns {Context.Consumer}
     */
    function HOCWithSettings(props) {
        return (
            <SettingsConsumer>
                {
                    context => <WrappedComponent {...context} {...props} />
                }
            </SettingsConsumer>
        );
    }
    return HOCWithSettings;
};

export default withSettings;
