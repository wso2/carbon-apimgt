import React, { useState } from 'react';
import {
    Button, Icon, Typography, RadioGroup, Radio, FormControlLabel, FormControl, Grid, Select, MenuItem,
} from '@material-ui/core';
import TextField from '@material-ui/core/TextField';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';

const useStyles = makeStyles((theme) => ({
    addButton: {
        marginTop: theme.spacing(-0.5),
        marginLeft: theme.spacing(5),
    },
    dialog: {
        minWidth: theme.spacing(150),

    },
    quotaHeading: {
        marginTop: theme.spacing(3),
        marginBottom: theme.spacing(2),
    },
    unitTime: {
        display: 'flex',
        minWidth: theme.spacing(60),
    },
    unitTimeSelection: {
        marginTop: theme.spacing(2.6),
        marginLeft: theme.spacing(2),
        minWidth: theme.spacing(15),
    },
}));

/**
 * Created Application Throttling Policies
 */
function CreateApplicationThrottlingPolicy(props) {
    const classes = useStyles();
    const { intl, setIsUpdated } = props;
    const [open, setOpen] = useState(false);
    const [quotaPolicyType, setQuotaPolicyType] = useState('RequestCountLimit');
    const [unitTime, setUnitTime] = useState('min');
    const [dataBandwithUnit, setDataBandwithUnit] = useState('KB');
    const applicationThrottlingPolicy = { defaultLimit: {} };

    const handleClickOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };

    const handleChangeQuotyPolicyType = (event) => {
        setQuotaPolicyType(event.target.value);
    };

    const handleChangeUnitTime = (event) => {
        setUnitTime(event.target.value);
    };

    const handleDataBandwithUnit = (event) => {
        setDataBandwithUnit(event.target.value);
    };

    const handleThrottlingApplicationInput = ({ target: { id, value } }) => {
        if (id === 'dataBandWithValue') {
            applicationThrottlingPolicy.defaultLimit.dataAmount = value;
        } else if (id === 'requestCountValue') {
            applicationThrottlingPolicy.defaultLimit.requestCount = value;
        } else if (id === 'unitTime') {
            applicationThrottlingPolicy.defaultLimit.unitTime = value;
        } else {
            applicationThrottlingPolicy[id] = value;
        }
    };

    /**
   * Save an Application Policy
   */
    function saveApplicationThrottlingPolicy() {
        applicationThrottlingPolicy.defaultLimit.type = quotaPolicyType;
        applicationThrottlingPolicy.defaultLimit.timeUnit = unitTime;
        if (quotaPolicyType === 'BandwidthLimit') {
            applicationThrottlingPolicy.defaultLimit.dataUnit = dataBandwithUnit;
        }
        const restApi = new API();
        const promisedAddApplicationPolicy = restApi.addApplicationThrottlingPolicy(
            applicationThrottlingPolicy,
        );
        promisedAddApplicationPolicy
            .then(() => {
                Alert.info(
                    intl.formatMessage({
                        id: 'Admin.Throttling.Create.Application.Throttling.Policy.add.success',
                        defaultMessage: 'Application Throttling Policy added successfully',
                    }),
                );
                setIsUpdated(true);
            })
            .catch((error) => {
                const { response } = error;
                if (response.body) {
                    const { description } = response.body;
                    Alert.error(description);
                }
            });
        setOpen(false);
    }


    return (
        <div>
            <Button
                variant='contained'
                color='primary'
                onClick={handleClickOpen}
                className={classes.addButton}
            >
                <Icon>add</Icon>
                <FormattedMessage
                    id='Admin.Throttling.Application.Throttling.Policy.add'
                    defaultMessage='Add New Policy'
                />
            </Button>
            <Dialog open={open} onClose={handleClose} aria-labelledby='form-dialog-title' className={classes.dialog}>
                <DialogTitle id='form-dialog-title'>Add Application Level Policy</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        <Typography variant='h6'>
                            <FormattedMessage
                                id='Admin.Throttling.Application.Throttling.Policy.add.general.details'
                                defaultMessage='General Details'
                            />
                        </Typography>
                    </DialogContentText>
                    <TextField
                        autoFocus
                        margin='dense'
                        id='policyName'
                        label='Name'
                        fullWidth
                        onChange={handleThrottlingApplicationInput}
                        required
                    />
                    <TextField
                        autoFocus
                        margin='dense'
                        id='description'
                        label='Description'
                        fullWidth
                        onChange={handleThrottlingApplicationInput}
                    />
                    <DialogContentText>
                        <Typography variant='h6' className={classes.quotaHeading}>
                            <FormattedMessage
                                id='Admin.Throttling.Application.Throttling.Policy.add.general.details'
                                defaultMessage='Quota Limits'
                            />
                        </Typography>
                    </DialogContentText>
                    <FormControl component='fieldset'>
                        <RadioGroup
                            row
                            aria-label='position'
                            name='position'
                            defaultValue='top'
                            onChange={handleChangeQuotyPolicyType}
                            value={quotaPolicyType}
                        >
                            <FormControlLabel
                                value='RequestCountLimit'
                                control={<Radio color='primary' />}
                                label='Request Count '
                                labelPlacement='end'
                            />
                            <FormControlLabel
                                value='BandwidthLimit'
                                control={<Radio color='primary' />}
                                label='Request Bandwidth'
                                labelPlacement='end'
                            />
                        </RadioGroup>
                        {quotaPolicyType === 'RequestCountLimit' ? (
                            <TextField
                                autoFocus
                                margin='dense'
                                id='requestCountValue'
                                label='Request Count'
                                fullWidth
                                onChange={handleThrottlingApplicationInput}
                                required
                            />
                        ) : (
                            <Grid className={classes.unitTime}>
                                <TextField
                                    autoFocus
                                    margin='dense'
                                    id='dataBandWithValue'
                                    label='Data Bandwith'
                                    fullWidth
                                    onChange={handleThrottlingApplicationInput}
                                />
                                <FormControl className={classes.unitTimeSelection}>
                                    <Select
                                        labelId='demo-simple-select-label'
                                        id='demo-simple-select'
                                        value={dataBandwithUnit}
                                        onChange={handleDataBandwithUnit}
                                        fullWidth
                                    >
                                        <MenuItem value='KB'>KB</MenuItem>
                                        <MenuItem value='MB'>MB</MenuItem>
                                    </Select>
                                </FormControl>

                            </Grid>

                        )}
                        <Grid className={classes.unitTime}>
                            <TextField
                                autoFocus
                                margin='dense'
                                id='unitTime'
                                label='Unit Time'
                                type='number'
                                fullWidth
                                onChange={handleThrottlingApplicationInput}
                            />
                            <FormControl className={classes.unitTimeSelection}>
                                <Select
                                    labelId='demo-simple-select-label'
                                    id='demo-simple-select'
                                    value={unitTime}
                                    onChange={handleChangeUnitTime}
                                    fullWidth
                                >
                                    <MenuItem value='min'>Minute(s)</MenuItem>
                                    <MenuItem value='hour'>Hour(s)</MenuItem>
                                    <MenuItem value='day'>Day(s)</MenuItem>
                                    <MenuItem value='week'>Week(s)</MenuItem>
                                    <MenuItem value='month'>Month(s)</MenuItem>
                                    <MenuItem value='year'>Year(s)</MenuItem>
                                </Select>
                            </FormControl>

                        </Grid>
                    </FormControl>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose} color='primary'>
                        <FormattedMessage
                            id='Admin.Throttling.Application.Throttling.Policy.cancel.policy'
                            defaultMessage='Cancel'
                        />
                    </Button>
                    <Button onClick={saveApplicationThrottlingPolicy} color='primary'>
                        <FormattedMessage
                            id='Admin.Throttling.Application.Throttling.Policy.save.policy'
                            defaultMessage='Save'
                        />
                    </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}

export default injectIntl(CreateApplicationThrottlingPolicy);
