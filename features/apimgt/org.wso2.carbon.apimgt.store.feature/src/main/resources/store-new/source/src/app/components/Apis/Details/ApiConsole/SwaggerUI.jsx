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
    /**
     *
     *
     * @memberof SwaggerUI
     */
    componentDidMount() {
        const { spec } = this.props;

        const disableAuthorizeAndInfoPlugin = function () {
            return {
                wrapComponents: {
                    authorizeBtn: () => () => null,
                    info: () => () => null,
                },
            };
        };
        const a = SwaggerUILib({
            dom_id: '#swagger-ui-root',
            spec,
            requestInterceptor: (req) => {
                return req;
            },
            presets: [SwaggerUILib.presets.apis, disableAuthorizeAndInfoPlugin],
            plugins: [SwaggerUILib.plugins.DownloadUrl],
        });
        console.log(a);
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
