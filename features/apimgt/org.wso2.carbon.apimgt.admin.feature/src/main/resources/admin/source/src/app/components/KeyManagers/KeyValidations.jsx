import React from 'react';
import sortby from 'lodash.sortby';
import KeyValidation from './KeyValidation';


/**
 * Trottling Policies dropdown selector used in minimized API Create form
 * @export
 * @param {*} props
 * @returns {React.Component}
 */
export default function KeyValidations(props) {
    const { tokenValidations, setTokenValidations } = props;
    const setTokenValidation = (value) => {
        const newTokenValidations = tokenValidations.filter((tokenValidation) => tokenValidation.id !== value.id);
        newTokenValidations.push(value);
        setTokenValidations(newTokenValidations);
    };
    return (
        sortby(tokenValidations, ['id']).map(((tokenValidation) => (
            <KeyValidation
                tokenValidation={tokenValidation}
                setTokenValidation={setTokenValidation}
            />
        )
        ))
    );
}
KeyValidations.defaultProps = {
    required: false,
    helperText: 'Add Key Manager Configuration',
};
