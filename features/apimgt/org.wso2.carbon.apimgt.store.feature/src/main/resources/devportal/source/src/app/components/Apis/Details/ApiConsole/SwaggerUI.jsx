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
    const { spec, accessTokenProvider, authorizationHeader, api } = props;

    const componentProps = {
        spec,
        validatorUrl: null,
        docExpansion: 'list',
        defaultModelsExpandDepth: 0,
        requestInterceptor: (req) => {
            const { url } = req;
            const patternToCheck = api.context + '/*';
            req.headers[authorizationHeader] = 'Bearer ' + accessTokenProvider();
            if (url.endsWith(patternToCheck)) {
                req.url = url.substring(url.length - 1, 0);
            }
            return req;
        },

        presets: [disableAuthorizeAndInfoPlugin],
        plugins: null,
    };
    return <SwaggerUILib {...componentProps} />;
};

SwaggerUI.propTypes = {
    spec: PropTypes.shape({}).isRequired,
};

export default SwaggerUI;
