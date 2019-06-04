import React from 'react';
import PropTypes from 'prop-types';
import 'swagger-ui/dist/swagger-ui.css';
import SwaggerUILib from './PatchedSwaggerUIReact';

const disableAuthorizeAndInfoPlugin = function () {
    return {
        wrapComponents: {
            authorizeBtn: () => () => null,
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
    const { spec, accessTokenProvider } = props;

    const componentProps = {
        spec,
        validatorUrl: null,
        docExpansion: 'list',
        defaultModelsExpandDepth: 0,
        requestInterceptor: (req) => {
            req.headers.Authorization = 'Bearer ' + accessTokenProvider();
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
