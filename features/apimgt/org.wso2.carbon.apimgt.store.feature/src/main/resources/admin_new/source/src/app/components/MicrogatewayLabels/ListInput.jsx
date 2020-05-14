import React, { useState } from 'react';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';

let id = 2;
let userHosts = [{ key: 'host1', value: '' }];

const InputList = ({ onHostChange }) => {
    const [inputFields, setInputFields] = useState([]);

    const handleInput = (e) => {
        const tempHosts = userHosts.filter((host) => host.key !== e.target.name);
        userHosts = [...tempHosts, { key: e.target.name, value: e.target.value }];
        onHostChange(userHosts);
    };

    const onAddInputField = () => {
        setInputFields([...inputFields,
            <TextField
                margin='dense'
                name={'host' + id}
                onChange={handleInput}
                label={'host ' + id}
                fullWidth
                multiline
                helperText='Enter host'
                variant='outlined'
            />]);
        id += 1;
    };

    return (
        <div>
            <TextField
                margin='dense'
                name='host1'
                onChange={handleInput}
                label='host 1'
                fullWidth
                multiline
                helperText='Enter host'
                variant='outlined'
            />
            {inputFields.map((inputField) => {
                return inputField;
            })}
            <Button onClick={onAddInputField}>Add Host</Button>
        </div>
    );
};

export default InputList;
