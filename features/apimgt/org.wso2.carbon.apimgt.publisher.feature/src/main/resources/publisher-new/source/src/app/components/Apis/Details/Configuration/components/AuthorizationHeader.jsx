import React from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import { FormattedMessage } from 'react-intl';

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function AuthorizationHeader(props) {
    const { api, configDispatcher } = props;
    return (
        <Grid container spacing={1} alignItems='center'>
            <Grid item>
                <TextField
                    id='outlined-name'
                    label={
                        <FormattedMessage
                            id='Apis.Details.Configuration.Configuration.auth.header.label'
                            defaultMessage='Authorization Header'
                        />
                    }
                    value={api.authorizationHeader || ' '}
                    margin='normal'
                    variant='outlined'
                    onChange={({ target: { value } }) => configDispatcher({ action: 'authorizationHeader', value })}
                />
            </Grid>
            <Grid item>
                <Tooltip
                    title={
                        <FormattedMessage
                            id='Apis.Details.Configuration.Configuration.AuthHeader.tooltip'
                            defaultMessage={
                                'If you want to send the authorization ' +
                                'information under different header name other than Authorization,' +
                                'You may specify that header name here'
                            }
                        />
                    }
                    aria-label='Auth Header'
                    placement='right-end'
                    interactive
                >
                    <HelpOutline />
                </Tooltip>
            </Grid>
        </Grid>
    );
}

AuthorizationHeader.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
