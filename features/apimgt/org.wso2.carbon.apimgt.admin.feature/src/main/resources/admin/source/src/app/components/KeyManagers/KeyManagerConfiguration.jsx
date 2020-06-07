import React from 'react';
import TextField from '@material-ui/core/TextField';
import Checkbox from '@material-ui/core/Checkbox';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Box from '@material-ui/core/Box';
import FormLabel from '@material-ui/core/FormLabel';
import FormControl from '@material-ui/core/FormControl';
import FormGroup from '@material-ui/core/FormGroup';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import { FormattedMessage } from 'react-intl';


/**
 * Keymanager Connector configuration
 * @export
 * @param {*} props
 * @returns {React.Component}
 */
export default function KeyManagerConfiguration(props) {
    const { keymanagerConnectorConfigurations, additionalProperties, setAdditionalProperties } = props;
    const onChange = (e) => {
        let finalValue;
        const { name, value, type } = e.target;
        if (type === 'checkbox') {
            if (additionalProperties[name]) {
                finalValue = additionalProperties[name];
            } else {
                finalValue = [];
            }
            if (e.target.checked) {
                finalValue.push(value);
            } else {
                const newValue = value.filter((v) => v !== e.target.value);
                finalValue = newValue;
            }
        } else {
            finalValue = value;
        }
        setAdditionalProperties(name, finalValue);
    };
    const getComponent = (keymanagerConnectorConfiguration) => {
        let value = '';
        if (additionalProperties[keymanagerConnectorConfiguration.name]) {
            value = additionalProperties[keymanagerConnectorConfiguration.name];
        }
        if (keymanagerConnectorConfiguration.type === 'input') {
            if (keymanagerConnectorConfiguration.mask) {
                return (
                    <TextField
                        autoFocus
                        margin='dense'
                        name={keymanagerConnectorConfiguration.name}
                        label={keymanagerConnectorConfiguration.label}
                        fullWidth
                        required
                        type='password'
                        variant='outlined'
                        value={value}
                        defaultValue={keymanagerConnectorConfiguration.default}
                        autoComplete='off'
                        onChange={onChange}
                    />
                );
            }
            return (
                <TextField
                    autoFocus
                    margin='dense'
                    name={keymanagerConnectorConfiguration.name}
                    label={keymanagerConnectorConfiguration.label}
                    fullWidth
                    required
                    variant='outlined'
                    value={value}
                    defaultValue={keymanagerConnectorConfiguration.default}
                    onChange={onChange}
                />
            );
        } else if (keymanagerConnectorConfiguration.type === 'select') {
            return (
                <FormControl component='fieldset'>
                    <FormLabel component='legend'>{keymanagerConnectorConfiguration.label}</FormLabel>
                    <FormGroup>
                        {keymanagerConnectorConfiguration.values.map((selection) => (
                            <FormControlLabel
                                control={(
                                    <Checkbox
                                        checked={value.includes(selection)}
                                        onChange={onChange}
                                        value={selection}
                                        color='primary'
                                        name={keymanagerConnectorConfiguration.name}
                                    />
                                )}
                                label={selection}
                            />
                        ))}
                    </FormGroup>
                </FormControl>
            );
        } else if (keymanagerConnectorConfiguration.type === 'options') {
            return (
                <FormControl component='fieldset'>
                    <FormLabel component='legend'>{keymanagerConnectorConfiguration.label}</FormLabel>
                    <RadioGroup
                        aria-label={keymanagerConnectorConfiguration.label}
                        name={keymanagerConnectorConfiguration.name}
                        value={value}
                        onChange={onChange}
                    >
                        {keymanagerConnectorConfiguration.values.map((selection) => (
                            <FormControlLabel value={selection} control={<Radio />} label={selection} />
                        ))}
                    </RadioGroup>
                </FormControl>
            );
        } else {
            return (
                <TextField
                    autoFocus
                    margin='dense'
                    name={keymanagerConnectorConfiguration.name}
                    label={keymanagerConnectorConfiguration.label}
                    fullWidth
                    required
                    variant='outlined'
                    value={value}
                    defaultValue={keymanagerConnectorConfiguration.default}
                    onChange={onChange}
                />
            );
        }
    };
    return (
        keymanagerConnectorConfigurations.map((keymanagerConnectorConfiguration) => (
            <Box mb={3}>
                {getComponent(keymanagerConnectorConfiguration)}
            </Box>
        )));
}
KeyManagerConfiguration.defaultProps = {
    keymanagerConnectorConfigurations: [],
    required: false,
    helperText: <FormattedMessage
        id='KeyManager.Connector.Configuration.Helper.text'
        defaultMessage='Add KeyManager Connector Configurations'
    />,
};
