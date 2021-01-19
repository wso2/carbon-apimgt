import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';

const useStyles = makeStyles((theme) => ({
    container: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    textField: {
        marginLeft: theme.spacing(1),
        marginRight: theme.spacing(1),
        width: 200,
    },
}));

/**
 * Renders a single node.
 * @returns {JSX} Rendered jsx output.
 */
export default function DateAndTimePickers(props) {
    const { label, time, setTime } = props;
    const classes = useStyles();
    const setTimeValue = (event) => {
        setTime(event.targe.value);
    };
    return (
        <form className={classes.container} noValidate>
            <TextField
                id='datetime-local'
                label={label}
                type='datetime-local'
                defaultValue={time}
                className={classes.textField}
                InputLabelProps={{
                    shrink: true,
                }}
                onChange={setTimeValue}
            />
        </form>
    );
}
