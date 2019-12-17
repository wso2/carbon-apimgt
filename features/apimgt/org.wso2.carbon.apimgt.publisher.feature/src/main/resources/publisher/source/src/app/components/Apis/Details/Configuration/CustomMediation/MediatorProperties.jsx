/* eslint-disable no-restricted-globals */
/* eslint-disable jsx-a11y/alt-text */
/* eslint-disable require-jsdoc */
import React, { useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Dialog from '@material-ui/core/Dialog';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogActions from '@material-ui/core/DialogActions';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import Box from '@material-ui/core/Box';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';

const useStyles = makeStyles((theme) => ({
    paper: {
        padding: theme.spacing(1, 0),
    },
    mandatoryStar: {
        color: theme.palette.error.main,
        marginLeft: theme.spacing(0.1),
    },
    logo: {
        height: 50,
        width: 50,
        paddingRight: 10,
    },
    radioWrapper: {
        flexDirection: 'row',
    },
    mediatorName: {
        padding: 10,
        fontWeight: 'bold',
    },
    dialog: {
        overflow: 'hidden',
    },
    marginRight: {
        marginRight: theme.spacing(1),
    },
}));

function MediatorProperties(props) {
    const classes = useStyles();
    const {
        setEditing, editing, mediatorLogo, mediatorName,
    } = props;

    const scopes = ['default', 'transport', 'axis2', 'axis2-client'];
    const actions = ['set', 'remove'];
    const [scopeValue, setScopeValue] = useState('default');
    const [actionValue, setActionValue] = useState('set');
    const [provideBy, setProvideBy] = useState('value');
    const [dialogWidth] = useState('md');
    const [namespaceKey, setNamespaceKey] = useState(null);
    const [namespaceValue, setNamespaceValue] = useState(null);

    const scopeChange = (event) => {
        setScopeValue(event.target.value);
    };

    const actionChange = (event) => {
        setActionValue(event.target.value);
    };

    function cancelEditing() {
        setEditing(false);
    }
    function doneEditing() {
        setEditing(false);
    }

    function handleChangeProvideBy(event) {
        const inputValue = event.target.value;
        setProvideBy(inputValue);
    }

    const handleChange = (name) => (event) => {
        const { value } = event.target;
        if (name === 'namespaceKey') {
            setNamespaceKey(value);
        } else if (name === 'namespaceValue') {
            setNamespaceValue(value);
        }
    };

    return (
        <Dialog
            disableBackdropClick
            disableEscapeKeyDown
            fullWidth
            maxWidth={dialogWidth}
            aria-labelledby='confirmation-dialog-title'
            open={editing}
            className={classes.dialog}
        >
            <DialogTitle id='confirmation-dialog-title'>
                <FormattedMessage
                    id='Apis.Details.Configuration.CustomMediation.MediatorProperties.properties.configuration'
                    defaultMessage='Mediator Configurations'
                />
            </DialogTitle>
            <div>
                <DialogContent dividers>
                    <div style={{ display: 'flex' }}>
                        <img src={mediatorLogo} className={classes.logo} />
                        <Typography className={classes.mediatorName}>
                            {mediatorName}
                            {' '}
                            Mediator
                        </Typography>
                    </div>
                </DialogContent>
                <DialogContent dividers>
                    <Typography style={{ fontWeight: 'bold' }}>
                        <FormattedMessage
                            id='Apis.Details.Configuration.CustomMediation.MediatorProperties.general'
                            defaultMessage='General Details'
                        />
                    </Typography>
                    <Grid container spacing={2}>
                        <>
                            <Grid item md={12}>
                                <form noValidate autoComplete='off'>
                                    <TextField
                                        autoFocus
                                        fullWidth
                                        id='outlined-name'
                                        // error={validity.name}
                                        label={(
                                            <>
                                                <FormattedMessage
                                                    id='Apis.Details.Configuration.CustomMediation.MediatorProperties.+
                                                    name'
                                                    defaultMessage='Name'
                                                />
                                                <sup className={classes.mandatoryStar}>*</sup>
                                            </>
                                        )}
                                        name='name'
                                        margin='normal'
                                        variant='outlined'
                                    />
                                </form>
                            </Grid>

                            <Grid item md={8} xs={6}>
                                <TextField
                                    id='scope'
                                    select
                                    fullWidth
                                    label='Scope'
                                    value={scopeValue}
                                    onChange={(event) => {
                                        scopeChange(event);
                                    }}
                                    helperText='Selected scope will be applied to the mediator.'
                                    margin='dense'
                                    variant='outlined'
                                >
                                    {scopes.map((scope) => (
                                        <MenuItem key={scope} value={scope}>
                                            {scope}
                                        </MenuItem>
                                    ))}
                                </TextField>
                            </Grid>
                            <Grid item md={4} xs={6}>
                                <TextField
                                    id='action'
                                    select
                                    fullWidth
                                    label='Action'
                                    value={actionValue}
                                    onChange={(event) => {
                                        actionChange(event);
                                    }}
                                    helperText='Selected action will be applied to the mediator.'
                                    margin='dense'
                                    variant='outlined'
                                >
                                    {actions.map((action) => (
                                        <MenuItem key={action} value={action}>
                                            {action}
                                        </MenuItem>
                                    ))}
                                </TextField>
                            </Grid>

                            <RadioGroup
                                value={provideBy}
                                onChange={() => handleChangeProvideBy(event)}
                                className={classes.radioWrapper}
                            >
                                <FormControlLabel
                                    value='value'
                                    control={<Radio color='primary' />}
                                    label={(
                                        <FormattedMessage
                                            id='Apis.Details.Configuration.CustomMediation.MediatorProperties.value'
                                            defaultMessage='Value'
                                        />
                                    )}
                                />
                                <FormControlLabel
                                    value='expression'
                                    control={<Radio color='primary' />}
                                    label={(
                                        <FormattedMessage
                                            id='Apis.Details.Configuration.CustomMediation.MediatorProperties.+
                                            expression'
                                            defaultMessage='Expression'
                                        />
                                    )}
                                />
                            </RadioGroup>

                            <Grid item md={12}>
                                {provideBy === 'value' && (
                                    <form>
                                        <TextField
                                            fullWidth
                                            id='outlined-name'
                                            // error={validity.name}
                                            label={(
                                                <>
                                                    <FormattedMessage
                                                        id='Apis.Details.Configuration.CustomMediation.+
                                                        MediatorProperties.value'
                                                        defaultMessage='Value'
                                                    />
                                                </>
                                            )}
                                            name='valuefield'
                                            //   onChange={onChange}
                                            margin='normal'
                                            variant='outlined'
                                        />
                                    </form>
                                )}
                                {provideBy === 'expression' && (
                                    <form>
                                        <TextField
                                            fullWidth
                                            id='outlined-name'
                                            // error={validity.name}
                                            label={(
                                                <>
                                                    <FormattedMessage
                                                        id='Apis.Details.Configuration.CustomMediation.+
                                                        MediatorProperties.expression'
                                                        defaultMessage='Expression'
                                                    />
                                                </>
                                            )}
                                            name='expressionfield'
                                            margin='normal'
                                            variant='outlined'
                                        />
                                    </form>
                                )}
                            </Grid>
                        </>
                    </Grid>
                    <Typography style={{ fontWeight: 'bold' }}>
                        <FormattedMessage
                            id='Apis.Details.Configuration.CustomMediation.MediatorProperties.namespace'
                            defaultMessage='Namespace'
                        />
                    </Typography>
                    <Grid container spacing={2}>
                        <>
                            <Grid item md={5} xs={6}>
                                <form noValidate autoComplete='off'>
                                    <TextField
                                        fullWidth
                                        id='outlined-name'
                                        label={(
                                            <>
                                                <FormattedMessage
                                                    id='Apis.Details.Configuration.CustomMediation.MediatorProperties.
                                                    namespace.name'
                                                    defaultMessage='Name'
                                                />
                                                <sup className={classes.mandatoryStar}>*</sup>
                                            </>
                                        )}
                                        name='namespaceKey'
                                        onChange={handleChange('namespaceKey')}
                                        value={namespaceKey === null ? '' : namespaceKey}
                                        margin='normal'
                                        variant='outlined'
                                    />
                                </form>
                            </Grid>
                            <Grid item md={5} xs={6}>
                                <form noValidate autoComplete='off'>
                                    <TextField
                                        fullWidth
                                        id='outlined-name'
                                        label={(
                                            <>
                                                <FormattedMessage
                                                    id='Apis.Details.Configuration.CustomMediation.MediatorProperties.
                                                    namespace.value'
                                                    defaultMessage='Value'
                                                />
                                                <sup className={classes.mandatoryStar}>*</sup>
                                            </>
                                        )}
                                        name='namespaceValue'
                                        onChange={handleChange('namespaceValue')}
                                        value={namespaceValue === null ? '' : namespaceValue}
                                        margin='normal'
                                        variant='outlined'
                                    />
                                </form>
                            </Grid>
                            <Grid item md={2} xs={6}>
                                <Box
                                    justifyContent='flex-end'
                                    alignItems='center'
                                    display='flex'
                                    style={{ height: '100%' }}
                                >
                                    <Button
                                        variant='contained'
                                        color='primary'
                                        className={classes.marginRight}
                                        disabled={!namespaceValue || !namespaceKey}
                                    >
                                        <FormattedMessage
                                            id='Apis.Details.Properties.Properties.add'
                                            defaultMessage='Add'
                                        />
                                    </Button>

                                    <Button>
                                        <FormattedMessage
                                            id='Apis.Details.Properties.Properties.cancel'
                                            defaultMessage='Cancel'
                                        />
                                    </Button>
                                </Box>
                            </Grid>
                        </>
                    </Grid>
                </DialogContent>
            </div>
            <DialogActions>
                <Button onClick={cancelEditing} color='primary'>
                    <FormattedMessage
                        id='Apis.Details.Configuration.CustomMediation.MediatorProperties.cancel.btn'
                        defaultMessage='Cancel'
                    />
                </Button>
                <Button onClick={doneEditing} color='primary' variant='contained'>
                    <FormattedMessage
                        id='Apis.Details.Configuration.CustomMediation.MediatorProperties.configure.btn'
                        defaultMessage='Configure'
                    />
                </Button>
            </DialogActions>
        </Dialog>
    );
}

MediatorProperties.propTypes = {
    setEditing: PropTypes.func.isRequired,
    editing: PropTypes.bool.isRequired,
    mediatorLogo: PropTypes.string.isRequired,
    mediatorName: PropTypes.string.isRequired,
};

export default MediatorProperties;
