import React, { Component } from 'react';
import PropTypes from 'prop-types';
import SwaggerUILib, { SwaggerUIStandalonePreset } from 'swagger-ui';

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
            presets: [SwaggerUILib.presets.apis, disableAuthorizeAndInfoPlugin, SwaggerUIStandalonePreset],
            plugins: [SwaggerUILib.plugins.DownloadUrl],
            layout: 'StandaloneLayout',
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
