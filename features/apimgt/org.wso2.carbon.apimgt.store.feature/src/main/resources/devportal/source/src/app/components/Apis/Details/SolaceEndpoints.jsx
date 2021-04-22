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

import React, { useContext, useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import InputBase from '@material-ui/core/InputBase';
import IconButton from '@material-ui/core/IconButton';
import PropTypes from 'prop-types';
import Icon from '@material-ui/core/Icon';
import { FormattedMessage, useIntl } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import Avatar from '@material-ui/core/Avatar';
import Grid from '@material-ui/core/Grid';
import Chip from '@material-ui/core/Chip';
import CopyToClipboard from 'react-copy-to-clipboard';
import Tooltip from '@material-ui/core/Tooltip';
import { upperCaseString } from 'AppData/stringFormatter';
import { ApiContext } from './ApiContext';
import GoToTryOut from './GoToTryOut';

const useStyles = makeStyles((theme) => ({
    root: {
        padding: '2px 4px',
        display: 'flex',
        alignItems: 'center',
        width: '100%',
        border: `solid 1px ${theme.palette.grey[300]}`,
        '& .MuiInputBase-root:before,  .MuiInputBase-root:hover': {
            borderBottom: 'none !important',
            color: theme.palette.primary.main,
        },
        '& .MuiSelect-select': {
            color: theme.palette.primary.main,
            paddingLeft: theme.spacing(),
        },
        '& .MuiInputBase-input': {
            color: theme.palette.primary.main,
        },
        '& .material-icons': {
            fontSize: 16,
            color: `${theme.palette.grey[700]} !important`,
        },
        borderRadius: 10,
        marginRight: theme.spacing(),
    },
    input: {
        marginLeft: theme.spacing(1),
        flex: 1,
    },
    avatar: {
        width: 30,
        height: 30,
        background: 'transparent',
        border: `solid 1px ${theme.palette.grey[300]}`,
    },
    iconStyle: {
        cursor: 'pointer',
        margin: '-10px 0',
        padding: '0 0 0 5px',
        '& .material-icons': {
            fontSize: 18,
            color: '#9c9c9c',
        },
    },

    sectionTitle: {
        color: '#424242',
        fontSize: '0.85rem',
        marginRight: 20,
        fontWeight: 400,
    },
}));

function SolaceEndpoints() {
    const { api } = useContext(ApiContext);
    const [urlCopied, setUrlCopied] = useState(false);

    const intl = useIntl();
    const classes = useStyles();

    const onCopy = () => {
        setUrlCopied(true);
        const caller = function () {
            setUrlCopied(false);
        };
        setTimeout(caller, 2000);
    };

    console.log(api);

    return (
        <>
            {api.solaceAPI && (
                <Grid container spacing={2} xs={12}>
                    <Grid item spacing={2} xs={2}>
                        <Typography
                            variant='subtitle2'
                            component='label'
                            gutterBottom
                            align='left'
                            className={classes.sectionTitle}
                        >
                            <FormattedMessage
                                id='Apis.Details.protocols.and.endpoints'
                                defaultMessage='Protocols & Endpoints'
                            />
                        </Typography>
                    </Grid>
                    <Grid item spacing={2} xs={10}>
                        <GoToTryOut />
                    </Grid>
                    <Grid item spacing={2} xs={12}>
                        <Grid container spacing={2} xs={12}>
                            {api.solaceEndpointURLs.map((e) => (
                                <>
                                    <Grid item spacing={2} xs={12}>
                                        <Typography
                                            component='p'
                                            variant='subtitle2'
                                        >
                                            {e.environmentDisplayName}
                                        </Typography>
                                    </Grid>
                                    <Grid item spacing={2} xs={12}>
                                        {e.solaceURLs.map((p) => (
                                            <Grid container spacing={2} xs={12}>
                                                <Grid item>
                                                    <Typography component='p' variant='body1'>
                                                        <FormattedMessage
                                                            id='Apis.Details.NewOverview.Endpoints.blank'
                                                            defaultMessage='-'
                                                        />
                                                    </Typography>
                                                </Grid>
                                                <Grid item>
                                                    <Chip
                                                        label={upperCaseString(p.protocol)}
                                                        color='primary'
                                                        style={{
                                                            width: '70px',
                                                        }}
                                                    />
                                                </Grid>
                                                <Grid
                                                    xs={10}
                                                    item
                                                    style={{
                                                        display: 'flex',
                                                        alignItems: 'center',
                                                        justifyContent: 'center',
                                                    }}
                                                >
                                                    <Paper id='gateway-envirounment' component='form' className={classes.root}>
                                                        <InputBase
                                                            className={classes.input}
                                                            inputProps={{ 'aria-label': 'api url' }}
                                                            value={p.endpointURL}
                                                        />
                                                        <Avatar className={classes.avatar} sizes={30}>
                                                            <Tooltip
                                                                title={
                                                                    urlCopied
                                                                        ? intl.formatMessage({
                                                                            defaultMessage: 'Copied',
                                                                            id: 'Apis.Details.Environments.copied',
                                                                        })
                                                                        : intl.formatMessage({
                                                                            defaultMessage: 'Copy to clipboard',
                                                                            id: 'Apis.Details.Environments.copy.to.clipboard',
                                                                        })
                                                                }
                                                                interactive
                                                                placement='right'
                                                                className={classes.iconStyle}
                                                            >
                                                                <CopyToClipboard
                                                                    text={p.endpointURL}
                                                                    // text={endpoint.URLs.http}
                                                                    onCopy={() => onCopy('urlCopied')}
                                                                >
                                                                    <IconButton aria-label='Copy the API URL to clipboard'>
                                                                        <Icon color='secondary'>file_copy</Icon>
                                                                    </IconButton>
                                                                </CopyToClipboard>
                                                            </Tooltip>
                                                        </Avatar>
                                                    </Paper>
                                                </Grid>
                                            </Grid>
                                        ))}
                                    </Grid>
                                </>
                            ))}
                        </Grid>
                    </Grid>
                </Grid>
            )}
        </>
    );
}

SolaceEndpoints.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default SolaceEndpoints;
