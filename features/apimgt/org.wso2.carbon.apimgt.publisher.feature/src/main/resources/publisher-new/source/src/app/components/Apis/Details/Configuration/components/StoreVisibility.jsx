import React from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import FormControl from '@material-ui/core/FormControl';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import { FormattedMessage } from 'react-intl';
import Input from '@material-ui/core/Input';
import TextField from '@material-ui/core/TextField';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import FormHelperText from '@material-ui/core/FormHelperText';
import Select from '@material-ui/core/Select';

/**
 *
 * api.accessControl possible values are `NONE` and `RESTRICTED`
 * @export
 * @param {*} props
 * @returns
 */
export default function StoreVisibility(props) {
    const { api, configDispatcher } = props;
    const isPublic = api.visibility === 'PUBLIC';
    return (
        <Grid container spacing={0} alignItems='flex-start'>
            <Grid item>
                <FormControl>
                    <InputLabel htmlFor='storeVisibility-selector'>
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.storeVisibility.head.topic'
                            defaultMessage='Store Visibility'
                        />
                    </InputLabel>
                    <Select
                        value={api.visibility}
                        onChange={({ target: { value } }) => configDispatcher({ action: 'visibility', value })}
                        input={<Input name='storeVisibility' id='storeVisibility-selector' />}
                    >
                        <MenuItem value='PUBLIC'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.components.StoreVisibility.dropdown.public'
                                defaultMessage='Public'
                            />
                        </MenuItem>
                        <MenuItem value='RESTRICTED'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.components.storeVisibility.dropdown.restrict'
                                defaultMessage='Restrict by role(s)'
                            />
                        </MenuItem>
                    </Select>
                    <FormHelperText>
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.storeVisibility.form.helper.text'
                            defaultMessage='By default API is visible to all store users'
                        />
                    </FormHelperText>
                </FormControl>
            </Grid>
            <Grid item>
                <Tooltip
                    title={
                        <React.Fragment>
                            <p>
                                <strong>
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.components.storeVisibility.tooltip.public'
                                        defaultMessage='Public :'
                                    />
                                </strong>
                                {'  '}
                                <FormattedMessage
                                    id='Apis.Details.Configuration.components.storeVisibility.tooltip.public.desc'
                                    defaultMessage={
                                        'The API is accessible to everyone and can be advertised ' +
                                        'in multiple stores - a central store and/or non-WSO2 stores.'
                                    }
                                />
                                <br />
                                <br />
                                <strong>
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.components.storeVisibility.tooltip.restrict'
                                        defaultMessage='Restricted by roles(s) :'
                                    />
                                </strong>
                                {'  '}
                                <FormattedMessage
                                    id='Apis.Details.Configuration.components.storeVisibility.tooltip.restrict.desc'
                                    defaultMessage={
                                        'The API is visible only to specific user' +
                                        ' roles in the tenant store that you specify.'
                                    }
                                />
                            </p>
                        </React.Fragment>
                    }
                    aria-label='Store Visibility'
                    placement='right-end'
                    interactive
                >
                    <HelpOutline />
                </Tooltip>
            </Grid>
            {!isPublic && (
                <Grid item>
                    <TextField
                        label='Role(s)'
                        margin='dense'
                        variant='outlined'
                        value={api.visibleRoles.join(',')}
                        onChange={({ target: { value } }) => configDispatcher({ action: 'visibleRoles', value })}
                        helperText={
                            'Enter role name(s). If there are multiple roles,' +
                            ' separate them using comma (i:e role1,role2,...)'
                        }
                    />
                </Grid>
            )}
        </Grid>
    );
}

StoreVisibility.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
