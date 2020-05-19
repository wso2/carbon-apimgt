import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import { createMuiTheme } from '@material-ui/core/styles';
import FormGroup from '@material-ui/core/FormGroup';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import Box from '@material-ui/core/Box';

const theme = createMuiTheme();
theme.spacing(2);

let id = 1;

const InputList = (props) => {
    const {
        onInputListChange, initialList, inputLabelPrefix, helperText, addButtonLabel,
    } = props;
    const [userInputItems, setUserInputItems] = useState([]);

    const handleInput = ({ target: { name, value } }) => {
        let tempItems = userInputItems.filter((item) => item.key !== name);
        tempItems = [...tempItems, { key: '' + name, value }];
        tempItems.sort((a, b) => {
            return a.key - b.key;
        });
        setUserInputItems(tempItems);
    };

    const handleDelete = (deletingKey) => {
        const tempItems = userInputItems.filter((item) => (item.key !== deletingKey));
        setUserInputItems(tempItems);
    };

    useEffect(() => {
        const nonEmptyItems = [];
        for (let i = 0; i < userInputItems.length; i++) {
            if (userInputItems[i].value && userInputItems[i].value.trim() !== '') {
                nonEmptyItems.push(userInputItems[i].value.trim());
            }
        }
        onInputListChange(nonEmptyItems);
    }, [userInputItems]);

    useEffect(() => {
        id = 1;
        if (initialList) {
            setUserInputItems(initialList.map((item) => {
                return { key: '' + id++, value: item };
            }));
        } else {
            setUserInputItems([{ key: '' + id++, value: '' }]);
        }
    }, []);

    const onAddInputField = () => {
        const newUserItemList = [...userInputItems, { key: '' + id++, value: '' }];
        setUserInputItems(newUserItemList);
    };

    let labelCounter = 1;
    return (
        <FormGroup>
            <Grid xs={12} container direction='row' spacing={3}>
                <Grid item>
                    {userInputItems.map((item) => {
                        return (
                            <Grid container xs={12} direction='row' spacing={0}>
                                <TextField
                                    margin='dense'
                                    name={item.key}
                                    onChange={handleInput}
                                    label={inputLabelPrefix + ' ' + labelCounter++}
                                    value={item.value}
                                    helperText={helperText}
                                    variant='outlined'
                                />
                                <Box mt={1}>
                                    <Button
                                        color='primary'
                                        onClick={() => handleDelete(item.key)}
                                        disabled={userInputItems.length === 1}
                                    >
                                        <FormattedMessage
                                            id='AdminPages.Addons.InputListBase.textfield.remove.label'
                                            defaultMessage='Remove'
                                        />
                                    </Button>
                                </Box>
                            </Grid>
                        );
                    })}
                </Grid>
                <Grid item>
                    <Box mt={1}>
                        <Button
                            variant='contained'
                            color={theme.palette.action.disabledBackground}
                            onClick={onAddInputField}
                        >
                            {addButtonLabel}
                        </Button>
                    </Box>
                </Grid>
            </Grid>
        </FormGroup>
    );
};

InputList.defaultProps = {
    inputLabelPrefix: 'Item',
    helperText: 'Enter item',
    addButtonLabel: 'Add item',
    initialList: null,
};

InputList.propTypes = {
    inputLabelPrefix: PropTypes.string,
    helperText: PropTypes.string,
    addButtonLabel: PropTypes.string,
    onInputListChange: PropTypes.func.isRequired,
    initialList: PropTypes.shape([]),
};

export default InputList;
