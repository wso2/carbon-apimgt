/* eslint-disable no-unreachable */
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
import MenuItem from '@material-ui/core/MenuItem';
import Select from '@material-ui/core/Select';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import Avatar from '@material-ui/core/Avatar';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import CopyToClipboard from 'react-copy-to-clipboard';
import Tooltip from '@material-ui/core/Tooltip';
import Box from '@material-ui/core/Box';
import Icon from '@material-ui/core/Icon';
import { FormattedMessage, useIntl } from 'react-intl';
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

/**
 *  @inheritdoc
 */
function Environments(props) {
    const { selectedEndpoint, updateSelectedEndpoint } = props;
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

    const getDefaultVersionUrl = () => {
        const { defaultVersionURLs } = selectedEndpoint;
        if (defaultVersionURLs
            && (defaultVersionURLs.https
                || defaultVersionURLs.http
                || defaultVersionURLs.ws
                || defaultVersionURLs.wss)) {
            return (
                <>
                    {`
            ${intl.formatMessage({
                    id: 'Apis.Details.Environments.default.url',
                    defaultMessage: '( Default Version ) ',
                })}
            ${(defaultVersionURLs.https || defaultVersionURLs.http || defaultVersionURLs.ws || defaultVersionURLs.wss)}`}
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
                            text={defaultVersionURLs.https
                                || defaultVersionURLs.http
                                || defaultVersionURLs.ws
                                || defaultVersionURLs.wss}
                            // text={endpoint.URLs.http}
                            onCopy={() => onCopy('urlCopied')}
                        >
                            <IconButton aria-label='Copy the Default Version URL to clipboard'>
                                <Icon color='secondary'>file_copy</Icon>
                            </IconButton>
                        </CopyToClipboard>
                    </Tooltip>
                </>
            );
        } else {
            return null;
        }
    };
    /**
     *  @inheritdoc
     */
    // if (!selectedEndpoint) {
    //     return <Progress />;
    // }
    return (
        <Box display='flex' flexDirection='column' width='100%'>
            <Box mr={5} display='flex' area-label='API URL details' alignItems='center' width='100%' flexDirection='row'>
                {selectedEndpoint && (
                    <>
                        <Typography
                            variant='subtitle2'
                            component='label'
                            for='gateway-envirounment'
                            gutterBottom
                            align='left'
                            className={classes.sectionTitle}
                        >
                            <FormattedMessage
                                id='Apis.Details.Environments.label.url'
                                defaultMessage='URL'
                            />
                        </Typography>
                        <Paper id='gateway-envirounment' component='form' className={classes.root}>
                            {api.endpointURLs.length > 1 && (
                                <>
                                    <Select
                                        value={selectedEndpoint.environmentName}
                                        onChange={updateSelectedEndpoint}
                                        aria-label='Select the Gateway Environment'
                                    >
                                        {api.endpointURLs.map((endpoint) => (
                                            <MenuItem value={endpoint.environmentName}>
                                                {endpoint.environmentDisplayName || endpoint.environmentName}
                                            </MenuItem>
                                        ))}
                                    </Select>
                                    <VerticalDivider height={30} />
                                </>
                            )}

                            <InputBase
                                className={classes.input}
                                inputProps={{ 'aria-label': 'api url' }}
                                value={selectedEndpoint.URLs.https
                                    || selectedEndpoint.URLs.http
                                    || selectedEndpoint.URLs.ws
                                    || selectedEndpoint.URLs.wss}
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
                                        text={selectedEndpoint.URLs.https
                                            || selectedEndpoint.URLs.http
                                            || selectedEndpoint.URLs.ws
                                            || selectedEndpoint.URLs.wss}
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
                    </>
                )}
                {!selectedEndpoint && (api.lifeCycleStatus !== 'PROTOTYPED') && (
                    <Typography variant='subtitle2' component='p' gutterBottom align='left' className={classes.sectionTitle}>
                        <FormattedMessage
                            id='Apis.Details.Environments.label.noendpoint'
                            defaultMessage='No endpoints yet.'
                        />
                    </Typography>
                )}
                <GoToTryOut />
            </Box>
            <Box ml={8} alignItems='center' mt={1}>
                {selectedEndpoint && (
                    <Typography variant='caption'>
                        {getDefaultVersionUrl()}
                    </Typography>
                )}
            </Box>
        </Box>

    );
}
Environments.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default Environments;
