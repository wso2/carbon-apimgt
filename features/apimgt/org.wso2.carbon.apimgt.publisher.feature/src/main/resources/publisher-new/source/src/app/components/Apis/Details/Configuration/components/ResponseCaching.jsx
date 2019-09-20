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
import Switch from '@material-ui/core/Switch';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import { FormattedMessage } from 'react-intl';
import Tooltip from '@material-ui/core/Tooltip';
import Typography from '@material-ui/core/Typography';
import HelpOutline from '@material-ui/icons/HelpOutline';
import { isRestricted } from 'AppData/AuthManager';
import { makeStyles } from '@material-ui/core';
import Paper from '@material-ui/core/Paper';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';

const useStyles = makeStyles(theme => ({
    iconSpace: {
        marginLeft: theme.spacing(0.5),
    },
    actionSpace: {
        margin: 'auto',
        float: 'right',
    },
    subHeading: {
        fontSize: '1rem',
        fontWeight: 400,
        margin: 0,
        display: 'inline-flex',
        lineHeight: '38px',
    },
    paper: {
        padding: theme.spacing(1, 3),
    },
}));

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function ResponseCaching(props) {
    const { api, configDispatcher } = props;
    const classes = useStyles();
    const [apiFromContext] = useAPI();

    return (
        <Paper className={classes.paper}>
            <Grid container spacing={1} alignItems='flex-start'>
                <Grid item md={12}>
                    <Typography className={classes.subHeading} variant='h6'>
                        <FormattedMessage
                            id='Apis.Details.Configuration.Configuration.response.caching'
                            defaultMessage='Response Caching'
                        />
                        <Tooltip
                            title={
                                <FormattedMessage
                                    id='Apis.Details.Configuration.components.ResponseCaching.tooltip'
                                    defaultMessage={
                                        'If enabled, API response will be cached at the gateway level'
                                        + ' to improve the response time and minimize the backend load'
                                    }
                                />
                            }
                            aria-label='Response cache'
                            placement='right-end'
                            interactive
                        >
                            <HelpOutline className={classes.iconSpace} />
                        </Tooltip>
                    </Typography>
                    <FormControlLabel
                        className={classes.actionSpace}
                        control={
                            <Switch
                                disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                checked={api.responseCachingEnabled}
                                onChange={({ target: { checked } }) => configDispatcher({
                                    action: 'responseCachingEnabled',
                                    value: checked,
                                })
                                }
                                color='primary'
                            />
                        }
                    />
                </Grid>
            </Grid>
        </Paper>
    );
}

ResponseCaching.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
