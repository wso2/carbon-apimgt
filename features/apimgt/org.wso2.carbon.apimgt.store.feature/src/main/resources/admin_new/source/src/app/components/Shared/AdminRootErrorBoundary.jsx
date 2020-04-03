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

import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';

/**
 * Error boundary for the application.catch JavaScript errors anywhere in their child component tree,
 * log those errors, and display a fallback UI instead of the component tree that crashed.
 * Error boundaries catch errors during rendering, in lifecycle methods,
 * and in constructors of the whole tree below them.
 * @class AdminRootErrorBoundary
 * @extends {Component}
 */
class AdminRootErrorBoundary extends Component {
    /**
     * Creates an instance of AdminRootErrorBoundary.
     * @param {any} props @inheritDoc
     * @memberof AdminRootErrorBoundary
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
     * @memberof AdminRootErrorBoundary
     */
    componentDidCatch(error, info) {
        this.setState({ hasError: true, error, info });
    }

    /**
     * Return error handled UI
     * @returns {React.Component} return react component
     * @memberof AdminRootErrorBoundary
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
                    <h2>
                        <FormattedMessage
                            id='Apis.Shared.AdminRootErrorBoundary.something.went.wrong.while.rendering.heading'
                            defaultMessage='Something went wrong while rendering the'
                        />
                    </h2>
                    {' '}
                    <b>{appName}</b>
                    <hr />
                    <h3 style={{ color: 'red' }}>{error.message}</h3>
                    <pre style={errorStackStyle}>
                        <u>{error.stack}</u>
                    </pre>
                    <pre style={errorStackStyle}>
                        <u>{info.componentStack}</u>
                    </pre>
                    <span>
                        <FormattedMessage
                            id='Apis.Shared.AdminRootErrorBoundary.refresh.or.try.again.message'
                            defaultMessage='You may refresh the page now or try again later'
                        />
                    </span>
                    <button
                        type='button'
                        onClick={() => {
                            window.location.reload(true);
                        }}
                        aria-label={(
                            <FormattedMessage
                                id='Apis.Shared.AdminRootErrorBoundary.something.went.wrong.while.rendering.button'
                                defaultMessage='Something went wrong while rendering the'
                            />
                        )}
                    >
                        <FormattedMessage
                            id='Apis.Shared.AdminRootErrorBoundary.refresh'
                            defaultMessage='Refresh'
                        />
                    </button>
                </div>
            );
        } else {
            return children;
        }
    }
}

AdminRootErrorBoundary.defaultProps = {
    appName: 'Application',
};

AdminRootErrorBoundary.propTypes = {
    children: PropTypes.node.isRequired,
    appName: PropTypes.string,
};

export default AdminRootErrorBoundary;
