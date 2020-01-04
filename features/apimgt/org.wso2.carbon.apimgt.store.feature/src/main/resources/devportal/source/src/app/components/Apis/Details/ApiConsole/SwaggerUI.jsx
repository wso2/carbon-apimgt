import React from 'react';
import PropTypes from 'prop-types';
import 'swagger-ui/dist/swagger-ui.css';
import SwaggerUILib from './PatchedSwaggerUIReact';

const disableAuthorizeAndInfoPlugin = function () {
    return {
        wrapComponents: {
            info: () => () => null,
        },
    };
};
/**
 *
 * @class SwaggerUI
 * @extends {Component}
 */
const SwaggerUI = (props) => {
    const {
        spec, accessTokenProvider, authorizationHeader, api,
    } = props;

    const componentProps = {
        spec,
        validatorUrl: null,
        docExpansion: 'list',
        defaultModelsExpandDepth: 0,
        requestInterceptor: (req) => {
            const { url } = req;
            const { context } = api;
            const patternToCheck = `${context}/*`;
            if (authorizationHeader === 'apikey') {
                req.headers[authorizationHeader] = accessTokenProvider();
            } else {
                req.headers[authorizationHeader] = 'Bearer ' + accessTokenProvider();
            }
            if (url.endsWith(patternToCheck)) {
                req.url = url.substring(0, url.length - 2);
            } else if (url.includes(patternToCheck + '?')) { // Check for query parameters.
                const splitTokens = url.split('/*?');
                req.url = splitTokens.length > 1 ? splitTokens[0] + '?' + splitTokens[1] : splitTokens[0];
            }
            return req;
        },

        presets: [disableAuthorizeAndInfoPlugin],
        plugins: null,
    };
    return <SwaggerUILib {...componentProps} />;
};

SwaggerUI.propTypes = {
    accessTokenProvider: PropTypes.func.isRequired,
    authorizationHeader: PropTypes.string.isRequired,
    api: PropTypes.shape({
        context: PropTypes.string.isRequired,
    }).isRequired,
    spec: PropTypes.string.isRequired,
};
export default SwaggerUI;
