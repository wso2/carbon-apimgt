/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
import { isRestricted } from 'AppData/AuthManager';

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
        <Grid container spacing={1} alignItems='flex-start' xs={11}>
            <Grid item>
                <FormControl component='fieldset' style={{ display: 'flex', marginTop: 20 }}>
                    <FormLabel component='legend'>
                        <FormattedMessage
                            id='Apis.Details.Configuration.Configuration.isdefault.label'
                            defaultMessage='Is Default'
                        />
                    </FormLabel>
                    <RadioGroup
                        aria-label='Is Default'
                        value={api.isDefaultVersion}
                        onChange={({
                            target: { value },
                        }) => configDispatcher({
                            action: 'isDefaultVersion', value: value === 'true',
                        })
                        }
                        style={{ display: 'flow-root' }}
                    >
                        <FormControlLabel
                            disabled={isRestricted(['apim:api_create'], api)}
                            value
                            control={<Radio />}
                            label={(
                                <FormattedMessage
                                    id='Apis.Details.Configuration.Configuration.isdefault.yes'
                                    defaultMessage='Yes'
                                />
                            )}
                        />
                        <FormControlLabel
                            disabled={isRestricted(['apim:api_create'], api)}
                            value={false}
                            control={<Radio />}
                            label={(
                                <FormattedMessage
                                    id='Apis.Details.Configuration.Configuration.isdefault.no'
                                    defaultMessage='No'
                                />
                            )}
                        />
                    </RadioGroup>
                </FormControl>
            </Grid>
            <Grid item xs={1}>
                <Tooltip
                    title={(
                        <FormattedMessage
                            id='Apis.Details.Configuration.Configuration.defaultversion.tooltip'
                            defaultMessage={
                                'If a particular version of an API is default, '
                                + 'That API can be invoked without specifying the version'
                                + ' parameter in the path, The default version will be wired '
                                + 'to that request automatically'
                            }
                        />
                    )}
                    aria-label='add'
                    placement='right-end'
                    interactive
                    style={{ marginTop: 20 }}
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
