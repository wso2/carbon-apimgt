import React from 'react';
import { FormattedMessage } from 'react-intl';
import {
    Grid, FormControl, FormControlLabel, RadioGroup, Radio, Typography,
} from '@material-ui/core';
import MenuItem from '@material-ui/core/MenuItem';
import { withStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';

const styles = (theme) => ({
    centerItems: {
        margin: 'auto',
    },
    tryoutHeading: {
        display: 'block',
        fontWeight: 400,
    },
    menuItem: {
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
});

const SelectAppPanel = (props) => {
    let {
        selectedApplication, selectedKeyType,
    } = props;

    const {
        subscriptions, handleChanges, classes,
    } = props;

    /**
     * This method is used to handle the updating of key generation
     * request object.
     * @param {*} event event fired
     */
    const handleSelectPanelChange = (event) => {
        const { target } = event;
        const { name, value } = target;
        switch (name) {
            case 'selectedApplication':
                selectedApplication = value;
                break;
            case 'selectedKeyType':
                selectedKeyType = value;
                break;
            default:
                break;
        }
        handleChanges(event);
    };
    return (
        <>
            <Grid x={12} md={6} className={classes.centerItems}>
                <TextField
                    fullWidth
                    id='selected-application'
                    select
                    label={(
                        <FormattedMessage
                            defaultMessage='Applications'
                            id='Apis.Details.ApiConsole.SelectAppPanel.applications'
                        />
                    )}
                    value={selectedApplication}
                    name='selectedApplication'
                    onChange={handleSelectPanelChange}
                    SelectProps={subscriptions}
                    helperText={(
                        <FormattedMessage
                            defaultMessage='Subscribed applications'
                            id='Apis.Details.ApiConsole.SelectAppPanel.select.subscribed.application'
                        />
                    )}
                    margin='normal'
                    variant='outlined'
                >
                    {subscriptions.map((sub) => (
                        <MenuItem
                            value={sub.applicationInfo.applicationId}
                            key={sub.applicationInfo.applicationId}
                            className={classes.menuItem}
                        >
                            {sub.applicationInfo.name}
                        </MenuItem>
                    ))}
                </TextField>
            </Grid>
            <Grid x={12} md={6} className={classes.centerItems}>
                <Typography variant='h6' component='label' id='key-type' color='textSecondary' className={classes.tryoutHeading}>
                    <FormattedMessage
                        id='Apis.Details.ApiConsole.SelectAppPanel.select.key.type.heading'
                        defaultMessage='Key Type'
                    />
                </Typography>
                <FormControl component='fieldset'>
                    <RadioGroup
                        name='selectedKeyType'
                        value={selectedKeyType}
                        onChange={handleSelectPanelChange}
                        aria-labelledby='key-type'
                        row
                    >
                        {(subscriptions !== null && (subscriptions.find((sub) => sub.applicationId
                                === selectedApplication).status === 'UNBLOCKED'
                                || subscriptions.find((sub) => sub.applicationId
                                === selectedApplication).status === 'TIER_UPDATE_PENDING'))
                                && (
                                    <FormControlLabel
                                        value='PRODUCTION'
                                        control={<Radio />}
                                        label={(
                                            <FormattedMessage
                                                id='Apis.Details.ApiConsole.SelectAppPanel.production.radio'
                                                defaultMessage='Production'
                                            />
                                        )}
                                    />
                                )}
                        <FormControlLabel
                            value='SANDBOX'
                            control={<Radio />}
                            label={(
                                <FormattedMessage
                                    id='Apis.Details.ApiConsole.SelectAppPanel.sandbox.radio'
                                    defaultMessage='Sandbox'
                                />
                            )}
                        />
                    </RadioGroup>
                </FormControl>
            </Grid>
        </>
    );
};

export default withStyles(styles)(SelectAppPanel);
