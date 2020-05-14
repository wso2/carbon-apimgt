import React, { useEffect, useState } from 'react';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';

let id;

const InputList = (props) => {
    const { onHostChange, availableHosts } = props;
    const [userHosts, setUserHosts] = useState([]);

    const handleInput = (e) => {
        let tempHosts = userHosts.filter((host) => host.key !== e.target.name);
        tempHosts = [...tempHosts, { key: '' + e.target.name, value: e.target.value }];
        tempHosts.sort((a, b) => {
            return a.key - b.key;
        });
        setUserHosts(tempHosts);
    };

    const handleDelete = (deletingKey) => {
        const tempHosts = userHosts.filter((host) => (host.key !== deletingKey));
        setUserHosts(tempHosts);
    };

    useEffect(() => {
        const nonEmptyHosts = [];
        for (let i = 0; i < userHosts.length; i++) {
            if (userHosts[i].value && userHosts[i].value.trim() !== '') {
                nonEmptyHosts.push(userHosts[i].value.trim());
            }
        }
        onHostChange(nonEmptyHosts);
    }, [userHosts]);

    useEffect(() => {
        id = 1;
        if (availableHosts) {
            setUserHosts(availableHosts.map((host) => {
                return { key: '' + id++, value: host };
            }));
        } else {
            setUserHosts([{ key: '1', value: '' }]);
        }
    }, []);

    const onAddInputField = () => {
        const newUserHosts = [...userHosts, { key: '' + id++, value: '' }];
        setUserHosts(newUserHosts);
    };

    let labelCounter = 1;
    return (
        <div>
            {userHosts.map((host) => {
                return (
                    <div>
                        <TextField
                            margin='dense'
                            name={host.key}
                            onChange={handleInput}
                            label={'Host ' + labelCounter++}
                            value={host.value}
                            helperText='Enter host'
                            variant='outlined'
                        />
                        <Button onClick={() => handleDelete(host.key)}>
                            {'Remove ' + host.key}
                        </Button>
                    </div>
                );
            })}
            <Button onClick={onAddInputField}>Add Host</Button>
        </div>
    );
};

export default InputList;
