import React from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import Tooltip from '@material-ui/core/Tooltip';
import FormControl from '@material-ui/core/FormControl';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormLabel from '@material-ui/core/FormLabel';
import HelpOutline from '@material-ui/icons/HelpOutline';
import { FormattedMessage } from 'react-intl';

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function DefaultVersion(props) {
    const { api, configDispatcher } = props;
    return (
        <Grid container spacing={1} alignItems='flex-start'>
            <Grid item>
                <FormControl component='fieldset'>
                    <FormLabel component='legend'>
                        <FormattedMessage
                            id='Apis.Details.Configuration.Configuration.isdefault.label'
                            defaultMessage='Is Default'
                        />
                    </FormLabel>
                    <RadioGroup
                        aria-label='Is Default'
                        value={api.isDefaultVersion}
                        onChange={({ target: { value } }) =>
                            configDispatcher({ action: 'isDefaultVersion', value: value === 'true' })
                        }
                    >
                        <FormControlLabel
                            value
                            control={<Radio />}
                            label={
                                <FormattedMessage
                                    id='Apis.Details.Configuration.Configuration.isdefault.yes'
                                    defaultMessage='Yes'
                                />
                            }
                        />
                        <FormControlLabel
                            value={false}
                            control={<Radio />}
                            label={
                                <FormattedMessage
                                    id='Apis.Details.Configuration.Configuration.isdefault.no'
                                    defaultMessage='No'
                                />
                            }
                        />
                    </RadioGroup>
                </FormControl>
            </Grid>
            <Grid item>
                <Tooltip
                    title={
                        <FormattedMessage
                            id='Apis.Details.Configuration.Configuration.defaultversion.tooltip'
                            defaultMessage={
                                'If a particular version of an API is default, ' +
                                'That API can be invoked without specifying the version' +
                                ' parameter in the path, The default version will be wired ' +
                                'to that request automatically'
                            }
                        />
                    }
                    aria-label='add'
                    placement='right-end'
                    interactive
                >
                    <HelpOutline />
                </Tooltip>
            </Grid>
        </Grid>
    );
}

DefaultVersion.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
