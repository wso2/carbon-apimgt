import React from 'react';
import { FormattedMessage } from 'react-intl';
import Grid from '@material-ui/core/Grid';
import MenuItem from '@material-ui/core/MenuItem';
import Box from '@material-ui/core/Box';
import TextField from '@material-ui/core/TextField';

const SelectAppPanel = (props) => {
    const {
        subscriptions, handleChanges, selectedApplication, selectedKeyType,
    } = props;
    return (
        <React.Fragment>
            <Box display='flex' justifyContent='center'>

                <Grid xs={12} md={3} >
                    <Box >
                        <TextField
                            fullWidth
                            id='outlined-select-currency'
                            select
                            label={<FormattedMessage
                                defaultMessage='Appplications'
                                id='Apis.Details.ApiConsole.SelectAppPanel.applications'
                            />}
                            value={selectedApplication}
                            name='selectedApplication'
                            onChange={handleChanges}
                            SelectProps={subscriptions}
                            helperText={<FormattedMessage
                                defaultMessage='Please select an application'
                                id='Apis.Details.ApiConsole.SelectAppPanel.select.an.application'
                            />}
                            margin='normal'
                            variant='outlined'
                        >
                            {subscriptions.map(sub => (
                                <MenuItem value={sub.applicationInfo.applicationId} key={sub.applicationInfo.applicationId}>
                                    {sub.applicationInfo.name}
                                </MenuItem>
                            ))}
                        </TextField>
                    </Box>
                </Grid>
                <Grid xs={12} md={3} >
                    <Box ml={2}>
                        <TextField
                            fullWidth
                            id='outlined-select-currency'
                            select
                            label={<FormattedMessage
                                defaultMessage='Key Type'
                                id='Apis.Details.ApiConsole.SelectAppPanel.key.type'
                            />}
                            value={selectedKeyType}
                            name='selectedKeyType'
                            onChange={handleChanges}
                            helperText={<FormattedMessage
                                defaultMessage='Please select a key type'
                                id='Apis.Details.ApiConsole.SelectAppPanel.select.key.type'
                            />}
                            margin='normal'
                            variant='outlined'
                        >
                            {(subscriptions != null && subscriptions.find(sub => sub.applicationId === selectedApplication).status === 'UNBLOCKED') &&
                            <MenuItem value='PRODUCTION'>
                                <FormattedMessage
                                    id='Apis.Details.ApiConsole.SelectAppPanel.production'
                                    defaultMessage='PRODUCTION'
                                />
                            </MenuItem>
                            }
                            <MenuItem value='SANDBOX'>
                                <FormattedMessage
                                    id='Apis.Details.ApiConsole.SelectAppPanel.sandbox'
                                    defaultMessage='SANDBOX'
                                />
                            </MenuItem>
                        </TextField>
                    </Box>
                </Grid>
            </Box>

        </React.Fragment>
    );
};

export default SelectAppPanel;
