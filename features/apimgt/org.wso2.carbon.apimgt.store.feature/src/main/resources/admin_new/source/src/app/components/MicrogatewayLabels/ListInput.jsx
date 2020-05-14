import React, { useEffect, useState } from 'react';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';

let id;
let userHosts;
let existingInputFields;

const InputList = ({ onHostChange, availableHosts }) => {
    const [inputFields, setInputFields] = useState([]);

    const handleInput = (e) => {
        const tempHosts = userHosts.filter((host) => host.key !== e.target.name);
        userHosts = [...tempHosts, { key: e.target.name, value: e.target.value }];
        onHostChange(userHosts.map((host) => { return host.value; }));
    };

    useEffect(() => {
        id = 0;
        userHosts = [{ key: 'host1', value: '' }];
        existingInputFields = null;

        if (availableHosts) {
            userHosts = [];
            existingInputFields = availableHosts.map((host) => {
                id += 1;
                userHosts = [...userHosts, { key: 'host' + id, value: host }];
                return (
                    <TextField
                        margin='dense'
                        name={'host' + id}
                        onChange={handleInput}
                        label={'host ' + id}
                        defaultValue={host}
                        helperText='Enter host'
                        variant='outlined'
                    />
                );
            });
            setInputFields(existingInputFields);
        }
    }, []);

    const onAddInputField = () => {
        id += 1;
        setInputFields([...inputFields,
            <TextField
                margin='dense'
                name={'host' + id}
                onChange={handleInput}
                label={'host ' + id}
                fullWidth
                helperText='Enter host'
                variant='outlined'
            />]);
    };


    return (
        <div>
            {(existingInputFields)
                ? null
                : (
                    <TextField
                        margin='dense'
                        name='host1'
                        onChange={handleInput}
                        label='host 1'
                        fullWidth
                        helperText='Enter host'
                        variant='outlined'
                    />
                )}

            {inputFields.map((inputField) => {
                return inputField;
            })}
            <Button onClick={onAddInputField}>Add Host</Button>
        </div>
    );
};

export default InputList;
