import React, { Component } from 'react';
import PropTypes from 'prop-types';

/**
 * Error boundary for the application.catch JavaScript errors anywhere in their child component tree,
 * log those errors, and display a fallback UI instead of the component tree that crashed.
 * Error boundaries catch errors during rendering, in lifecycle methods,
 * and in constructors of the whole tree below them.
 * @class AppErrorBoundary
 * @extends {Component}
 */
class AppErrorBoundary extends Component {
    /**
     * Creates an instance of AppErrorBoundary.
     * @param {any} props @inheritDoc
     * @memberof AppErrorBoundary
     */
    constructor(props) {
        super(props);
        this.state = {
            hasError: false,
        };
    }

    /**
     * The componentDidCatch() method works like a JavaScript catch {} block, but for components.
     * @param {Error} error is an error that has been thrown
     * @param {Object} info info is an object with componentStack key. The property has information about component
     * stack during thrown error.
     * @memberof AppErrorBoundary
     */
    componentDidCatch(error, info) {
        this.setState({ hasError: true, error, info });
    }

    /**
     * Return error handled UI
     * @returns {React.Component} return react component
     * @memberof AppErrorBoundary
     */
    render() {
        const { hasError, error, info } = this.state;
        const { children, appName } = this.props;
        const errorStackStyle = {
            background: '#fff8dc',
            width: '50%',
            fontFamily: 'monospace',
        };
        if (hasError) {
            return (
                <div>
                    <h2>Something went wrong while rendering the</h2> <b>{appName}</b>
                    <hr />
                    <h3 style={{ color: 'red' }}>{error.message}</h3>
                    <pre style={errorStackStyle}>
                        <u>{error.stack}</u>
                    </pre>
                    <pre style={errorStackStyle}>
                        <u>{info.componentStack}</u>
                    </pre>
                    <span>You may refresh the page now or try again later</span>
                    <button
                        onClick={() => {
                            window.location.reload(true);
                        }}
                        aria-label='Refresh'
                    >
                        Refresh
                    </button>
                </div>
            );
        } else {
            return children;
        }
    }
}

AppErrorBoundary.defaultProps = {
    appName: 'Application',
};

AppErrorBoundary.propTypes = {
    children: PropTypes.node.isRequired,
    appName: PropTypes.string,
};

export default AppErrorBoundary;
