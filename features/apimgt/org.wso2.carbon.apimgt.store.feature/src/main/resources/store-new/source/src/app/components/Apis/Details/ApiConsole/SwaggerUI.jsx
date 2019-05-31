import React from 'react';
import PropTypes from 'prop-types';
import SwaggerUILib from 'swagger-ui-react';

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
 *
 * @class SwaggerUI
 * @extends {Component}
 */
const SwaggerUI = (props) => {
    const { spec, accessTokenProvider } = props;

    const componentProps = {
        spec,
        validatorUrl: null,
        requestInterceptor: (req) => {
            req.headers.Authorization = 'Bearer ' + accessTokenProvider();
            return req;
        },
        presets: [disableAuthorizeAndInfoPlugin],
    };
    return <SwaggerUILib {...componentProps} />;
};

SwaggerUI.propTypes = {
    spec: PropTypes.shape({}).isRequired,
};

export default SwaggerUI;
