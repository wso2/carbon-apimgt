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
export default function AccessControl(props) {
    const { api, configDispatcher } = props;
    const isRestricted = api.accessControl === 'RESTRICTED';
    return (
        <Grid container spacing={0} alignItems='flex-start'>
            <Grid item>
                <FormControl>
                    <InputLabel htmlFor='accessControl-selector'>
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.AccessControl.head.topic'
                            defaultMessage='Access control'
                        />
                    </InputLabel>
                    <Select
                        value={api.accessControl}
                        onChange={({ target: { value } }) => configDispatcher({ action: 'accessControl', value })}
                        input={<Input name='accessControl' id='accessControl-selector' />}
                    >
                        <MenuItem value='NONE'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.components.AccessControl.dropdown.all'
                                defaultMessage='All'
                            />
                        </MenuItem>
                        <MenuItem value='RESTRICTED'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.components.AccessControl.dropdown.restrict'
                                defaultMessage='Restrict by role'
                            />
                        </MenuItem>
                    </Select>
                    <FormHelperText>
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.AccessControl.form.helper.text'
                            defaultMessage='By default there is no access restrictions'
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
                                        id='Apis.Details.Configuration.components.AccessControl.tooltip.all'
                                        defaultMessage='All :'
                                    />
                                </strong>
                                {'  '}
                                <FormattedMessage
                                    id='Apis.Details.Configuration.components.AccessControl.tooltip.all.desc'
                                    defaultMessage='The API is viewable, modifiable by all the publishers and
                                creators.'
                                />
                                <br />
                                <br />
                                <strong>
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.components.AccessControl.tooltip.restrict'
                                        defaultMessage='Restricted by roles :'
                                    />
                                </strong>
                                {'  '}
                                <FormattedMessage
                                    id='Apis.Details.Configuration.components.AccessControl.tooltip.restrict.desc'
                                    defaultMessage='The API can be viewable and modifiable by only specific
                                    publishers and creators with the roles that you specify'
                                />
                            </p>
                        </React.Fragment>
                    }
                    aria-label='Access control'
                    placement='right-end'
                    interactive
                >
                    <HelpOutline />
                </Tooltip>
            </Grid>
            {isRestricted && (
                <Grid item>
                    <TextField
                        label='Role(s)'
                        margin='dense'
                        variant='outlined'
                        value={api.accessControlRoles.join(',')}
                        onChange={({ target: { value } }) => configDispatcher({ action: 'accessControlRoles', value })}
                        helperText={'Enter role name(s). If there are multiple roles, ' +
                        'separate them using comma (i:e role1,role2,...)'}
                    />
                </Grid>
            )}
        </Grid>
    );
}

AccessControl.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
