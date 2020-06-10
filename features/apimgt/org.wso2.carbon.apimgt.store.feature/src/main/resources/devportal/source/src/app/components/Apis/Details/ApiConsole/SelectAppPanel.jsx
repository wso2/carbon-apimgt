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
        marginTop: theme.spacing(1),
        fontWeight: 400,
    },
    menuItem: {
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
});

const SelectAppPanel = (props) => {
    let {
        selectedApplication, selectedKeyType, selectedKeyManager,
    } = props;

    const {
        subscriptions, handleChanges, classes, keyManagers,
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
            case 'selectedKeyManager':
                selectedKeyManager = value;
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
                    id='outlined-select-currency'
                    select
                    label={(
                        <FormattedMessage
                            defaultMessage='Appplications'
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
                <TextField
                    fullWidth
                    id='outlined-select-currency'
                    select
                    label={(
                        <FormattedMessage
                            defaultMessage='Key Managers'
                            id='Apis.Details.ApiConsole.SelectAppPanel.keyManagers'
                        />
                    )}
                    value={selectedKeyManager}
                    name='selectedKeyManager'
                    onChange={handleSelectPanelChange}
                    SelectProps={keyManagers}
                    helperText={(
                        <FormattedMessage
                            defaultMessage='Registered Key Managers'
                            id='Apis.Details.ApiConsole.SelectAppPanel.select.registered.keyManagers'
                        />
                    )}
                    margin='normal'
                    variant='outlined'
                >
                    {keyManagers.map((keyManager) => (
                        <MenuItem
                            value={keyManager.name}
                            key={keyManager.name}
                            className={classes.menuItem}
                        >
                            {keyManager.name}
                        </MenuItem>
                    ))}
                </TextField>
            </Grid>
            <Grid x={12} md={6} className={classes.centerItems}>
                <Typography variant='h6' color='textSecondary' className={classes.tryoutHeading}>
                    <FormattedMessage
                        id='Apis.Details.ApiConsole.SelectAppPanel.select.key.type.heading'
                        defaultMessage='Key Type'
                    />
                </Typography>
                <FormControl component='fieldKeyType'>
                    <RadioGroup
                        name='selectedKeyType'
                        value={selectedKeyType}
                        onChange={handleSelectPanelChange}
                        row
                    >
                        {(subscriptions != null && subscriptions.find((sub) => sub.applicationId
                                === selectedApplication).status === 'UNBLOCKED')
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
