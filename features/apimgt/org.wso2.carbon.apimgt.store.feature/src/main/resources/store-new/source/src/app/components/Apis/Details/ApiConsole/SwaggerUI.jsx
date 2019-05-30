import React, { Component } from 'react';
import PropTypes from 'prop-types';
import SwaggerUILib from 'swagger-ui';

/**
 *
 *
 * @class SwaggerUI
 * @extends {Component}
 */
class SwaggerUI extends Component {
    state = {};

    /**
     *
     *
     * @memberof SwaggerUI
     */
    componentDidMount() {
        const { spec, accessTokenProvider } = this.props;

        const disableAuthorizeAndInfoPlugin = function () {
            return {
                wrapComponents: {
                    authorizeBtn: () => () => null,
                    info: () => () => null,
                },
            };
        };
        SwaggerUILib({
            dom_id: '#swagger-ui-root',
            spec,
            validatorUrl: null,
            requestInterceptor: (req) => {
                req.headers.Authorization = 'Bearer ' + accessTokenProvider();
                return req;
            },
            presets: [SwaggerUILib.presets.apis, disableAuthorizeAndInfoPlugin],
            plugins: [SwaggerUILib.plugins.DownloadUrl],
        });
    }

    /**
     *
     *
     * @returns
     * @memberof SwaggerUI
     */
    render() {
        return <div id='swagger-ui-root' />;
    }
}

SwaggerUI.propTypes = {
    spec: PropTypes.shape({}).isRequired,
};

export default SwaggerUI;
