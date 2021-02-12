import React, { useState } from 'react';
import IconButton from '@material-ui/core/IconButton';
import OutlinedInput from '@material-ui/core/OutlinedInput';
import InputAdornment from '@material-ui/core/InputAdornment';
import Visibility from '@material-ui/icons/Visibility';
import VisibilityOff from '@material-ui/icons/VisibilityOff';

/**
 * @export
 * @param {*} props sksk
 * @returns {React.Component}
 */
/**
 * Input Field
 * @param {JSON} props props passed from parents.
 * @returns {JSX} key manager connector form.
*/
export default function CustomInputField(props) {
    const {
        value, onChange, name, required, validating, hasErrors,
    } = props;
    const [showPassword, setShowPassword] = useState(false);
    return (
        <OutlinedInput
            type={showPassword ? 'text' : 'password'}
            value={value}
            onChange={onChange}
            endAdornment={(
                <InputAdornment position='end'>
                    <IconButton
                        aria-label='toggle password visibility'
                        onClick={() => setShowPassword(!showPassword)}
                        onMouseDown={() => setShowPassword(!showPassword)}
                        edge='end'
                    >
                        {showPassword ? <Visibility /> : <VisibilityOff />}
                    </IconButton>
                </InputAdornment>
            )}
            name={name}
            error={required && hasErrors('keyconfig', value, validating)}
            labelWidth={70}
            margin='dense'
        />
    );
}
