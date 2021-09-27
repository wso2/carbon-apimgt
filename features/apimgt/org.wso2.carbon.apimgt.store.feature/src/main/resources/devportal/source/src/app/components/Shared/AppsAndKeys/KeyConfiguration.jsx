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
import React, { useState } from 'react';
import Box from '@material-ui/core/Box';
import cloneDeep from 'lodash.clonedeep';
import { withStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import FormHelperText from '@material-ui/core/FormHelperText';
import Checkbox from '@material-ui/core/Checkbox';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import CopyToClipboard from 'react-copy-to-clipboard';
import Tooltip from '@material-ui/core/Tooltip';
import IconButton from '@material-ui/core/IconButton';
import Icon from '@material-ui/core/Icon';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import { FormattedMessage, useIntl } from 'react-intl';
import Settings from 'Settings';
import PropTypes from 'prop-types';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import Validation from 'AppData/Validation';
import AppConfiguration from './AppConfiguration';

const styles = (theme) => ({
    FormControl: {
        paddingTop: 0,
        paddingBottom: theme.spacing(2),
        paddingLeft: 0,
        width: '100%',
    },
    FormControlOdd: {
        padding: theme.spacing(2),
        width: '100%',
    },
    button: {
        marginLeft: theme.spacing(1),
    },
    quotaHelp: {
        position: 'relative',
    },
    checkboxWrapper: {
        display: 'flex',
    },
    checkboxWrapperColumn: {
        display: 'flex',
        flexWrap: 'wrap',
        flexDirection: 'row',
        whiteSpace: 'nowrap',
    },
    group: {
        flexDirection: 'row',
    },
    removeHelperPadding: {
        '& p': {
            margin: '8px 0px',
        },
    },
    iconStyle: {
        cursor: 'pointer',
        padding: '0 0 0 10px',
    },
    iconButton: {
        padding: '0 0 0 10px',
        '& .material-icons': {
            fontSize: 16,
        },
    },
    titleColumn: {
        width: 150,
        fontWeight: 500,
    },
    keyInfoTable: {
        marginBottom: 20,
        borderCollapse: 'collapse',
        '& td': {
            paddingBottom: 5,
            borderBottom: 'solid 1px #cccc',
        },
    },
    leftCol: {
        width: 180,
    },
});
/**
 *
 *
 * @class KeyConfiguration
 * @extends {React.Component}
 */
const KeyConfiguration = (props) => {
    const [urlCopied, setUrlCopied] = useState(false);
    const [callbackHelper, setCallbackHelper] = useState(false);
    const intl = useIntl();
    const {
        classes, notFound, isUserOwner, keyManagerConfig, updateKeyRequest, keyRequest, updateHasError, callbackError,
    } = props;
    const {
        selectedGrantTypes, callbackUrl,
    } = keyRequest;
    const {
        applicationConfiguration, availableGrantTypes, description, additionalProperties,
        enableMapOAuthConsumerApps, enableOAuthAppCreation, enableTokenEncryption, enableTokenGeneration,
        id, name, revokeEndpoint, tokenEndpoint, type, userInfoEndpoint,
    } = keyManagerConfig;

    /**
     * Get the display names for the supported grant types
     * @param grantTypes
     * @param grantTypeDisplayNameMap
     */
    const getGrantTypeDisplayList = (grantTypes, grantTypeDisplayNameMap) => {
        const modifiedGrantTypes = {};
        grantTypes.forEach((grantType) => {
            modifiedGrantTypes[grantType] = grantTypeDisplayNameMap[grantType];
            if (!grantTypeDisplayNameMap[grantType]) {
                modifiedGrantTypes[grantType] = grantType;
            }
        });
        return modifiedGrantTypes;
    };
    const callBackHasErrors = (callbackUrlLocal) => {
        if (callbackUrlLocal === '') {
            updateHasError(true);
            setCallbackHelper(intl.formatMessage({
                defaultMessage: 'Call back URL can not be empty when Implicit or Authorization Code grants are selected.',
                id: 'Shared.AppsAndKeys.KeyConfCiguration.Invalid.callback.empty.error.text',
            }));
        } else if (Validation.url.validate(callbackUrl).error) {
            updateHasError(true);
            setCallbackHelper(intl.formatMessage({
                defaultMessage: 'Invalid URL. Please enter a valid URL.',
                id: 'Shared.AppsAndKeys.KeyConfCiguration.Invalid.callback.url.error.text',
            }));
        } else {
            setCallbackHelper(false);
            updateHasError(false);
        }
    };
    /**
     * This method is used to handle the updating of key generation
     * request object.
     * @param {*} field field that should be updated in key request
     * @param {*} event event fired
     */
    const handleChange = (field, event) => {
        const newRequest = cloneDeep(keyRequest);
        const { target: currentTarget } = event;
        let newGrantTypes = [...newRequest.selectedGrantTypes];
        newRequest.keyManager = name;

        switch (field) {
            case 'callbackUrl':
                if (newGrantTypes.includes('implicit') || newGrantTypes.includes('authorization_code')) {
                    callBackHasErrors(currentTarget.value);
                }
                newRequest.callbackUrl = currentTarget.value;
                break;
            case 'grantType':
                if (currentTarget.checked) {
                    newGrantTypes = [...newGrantTypes, currentTarget.id];
                } else {
                    newGrantTypes = newRequest.selectedGrantTypes.filter((item) => item !== currentTarget.id);
                    if (currentTarget.id === 'implicit' || currentTarget.id === 'authorization_code') {
                        newRequest.callbackUrl = '';
                        setCallbackHelper(false);
                        updateHasError(false);
                    }
                }
                newRequest.selectedGrantTypes = newGrantTypes;
                break;
            case 'additionalProperties':
                const clonedAdditionalProperties = newRequest.additionalProperties;

                if(currentTarget.type === 'checkbox') {
                    clonedAdditionalProperties[currentTarget.name] = currentTarget.checked + "";
                } else {
                    clonedAdditionalProperties[currentTarget.name] = currentTarget.value;
                }
                newRequest.additionalProperties = clonedAdditionalProperties;
                break;
            default:
                break;
        }
        updateKeyRequest(newRequest);
    };

    const onCopy = () => {
        setUrlCopied(true);

        const caller = function () {
            setUrlCopied(false);
        };
        setTimeout(caller, 2000);
    };

    const getPreviousValue = (config) => {
        const { additionalProperties } = keyRequest;
        let isPreviousValueSet;
        if (config.type == 'input' && !config.multiple) {
            isPreviousValueSet = !!(additionalProperties && (additionalProperties[config.name]
                || additionalProperties[config.name] === ''));
        } else {
            isPreviousValueSet = !!(additionalProperties && (additionalProperties[config.name]));
        }
        let defaultValue = config.default;
        if (config.multiple && typeof defaultValue === 'string' && defaultValue === '') {
            defaultValue = [];
        }
        return isPreviousValueSet ? additionalProperties[config.name] : defaultValue;
    };
    /**
     *
     *
     * @returns {Component}
     * @memberof KeyConfiguration
     */

    if (notFound) {
        return <ResourceNotFound />;
    }
    const grantTypeDisplayListMap = getGrantTypeDisplayList(
        availableGrantTypes,
        Settings.grantTypes,
    );

    // Check for additional properties for token endpoint and revoke endpoints.
    return (
        <>
            <Box display='flex' alignItems='center'>
                <Table className={classes.table}>
                    <TableBody>
                        {(tokenEndpoint && tokenEndpoint !== '') && (
                            <TableRow>
                                <TableCell component='th' scope='row' className={classes.leftCol}>
                                    <FormattedMessage
                                        defaultMessage='Token Endpoint'
                                        id='Shared.AppsAndKeys.KeyConfiguration.token.endpoint.label'
                                    />
                                </TableCell>
                                <TableCell>
                                    {tokenEndpoint}
                                    <Tooltip
                                        title={
                                            urlCopied
                                                ? intl.formatMessage({
                                                    defaultMessage: 'Copied',
                                                    id: 'Shared.AppsAndKeys.KeyConfiguration.copied',
                                                })
                                                : intl.formatMessage({
                                                    defaultMessage: 'Copy to clipboard',
                                                    id: 'Shared.AppsAndKeys.KeyConfiguration.copy.to.clipboard',
                                                })
                                        }
                                        placement='right'
                                        className={classes.iconStyle}
                                    >
                                        <CopyToClipboard
                                            text={tokenEndpoint}
                                            onCopy={onCopy}
                                        >
                                            <IconButton
                                                aria-label='Copy to clipboard'
                                                classes={{ root: classes.iconButton }}
                                            >
                                                <Icon color='secondary'>file_copy</Icon>
                                            </IconButton>
                                        </CopyToClipboard>
                                    </Tooltip>
                                </TableCell>
                            </TableRow>
                        )}
                        {(revokeEndpoint && revokeEndpoint !== '') && (
                            <TableRow>
                                <TableCell component='th' scope='row' className={classes.leftCol}>
                                    <FormattedMessage
                                        defaultMessage='Revoke Endpoint'
                                        id='Shared.AppsAndKeys.KeyConfiguration.revoke.endpoint.label'
                                    />
                                </TableCell>
                                <TableCell>
                                    {revokeEndpoint}
                                    <Tooltip
                                        title={
                                            urlCopied
                                                ? intl.formatMessage({
                                                    defaultMessage: 'Copied',
                                                    id: 'Shared.AppsAndKeys.KeyConfiguration.copied',
                                                })
                                                : intl.formatMessage({
                                                    defaultMessage: 'Copy to clipboard',
                                                    id: 'Shared.AppsAndKeys.KeyConfiguration.copy.to.clipboard',
                                                })
                                        }
                                        placement='right'
                                        className={classes.iconStyle}
                                    >
                                        <CopyToClipboard
                                            text={revokeEndpoint}
                                            onCopy={onCopy}
                                        >
                                            <IconButton
                                                aria-label='Copy to clipboard'
                                                classes={{ root: classes.iconButton }}
                                            >
                                                <Icon color='secondary'>file_copy</Icon>
                                            </IconButton>
                                        </CopyToClipboard>
                                    </Tooltip>
                                </TableCell>
                            </TableRow>
                        )}
                        {(userInfoEndpoint && userInfoEndpoint !== '') && (
                            <TableRow>
                                <TableCell component='th' scope='row' className={classes.leftCol}>
                                    <FormattedMessage
                                        defaultMessage='User Info Endpoint'
                                        id='Shared.AppsAndKeys.KeyConfiguration.userinfo.endpoint.label'
                                    />
                                </TableCell>
                                <TableCell>
                                    {userInfoEndpoint}
                                    <Tooltip
                                        title={
                                            urlCopied
                                                ? intl.formatMessage({
                                                    defaultMessage: 'Copied',
                                                    id: 'Shared.AppsAndKeys.KeyConfiguration.copied',
                                                })
                                                : intl.formatMessage({
                                                    defaultMessage: 'Copy to clipboard',
                                                    id: 'Shared.AppsAndKeys.KeyConfiguration.copy.to.clipboard',
                                                })
                                        }
                                        placement='right'
                                        className={classes.iconStyle}
                                    >
                                        <CopyToClipboard
                                            text={userInfoEndpoint}
                                            onCopy={onCopy}
                                        >
                                            <IconButton
                                                aria-label='Copy to clipboard'
                                                classes={{ root: classes.iconButton }}
                                            >
                                                <Icon color='secondary'>file_copy</Icon>
                                            </IconButton>
                                        </CopyToClipboard>
                                    </Tooltip>
                                </TableCell>
                            </TableRow>
                        )}
                        <TableRow>
                            <TableCell component='th' scope='row' className={classes.leftCol}>
                                <FormattedMessage
                                    id='Shared.AppsAndKeys.KeyConfiguration.grant.types'
                                    defaultMessage='Grant Types'
                                />

                            </TableCell>
                            <TableCell>
                                <div className={classes.checkboxWrapperColumn}>
                                    {Object.keys(grantTypeDisplayListMap).map((key) => {
                                        const value = grantTypeDisplayListMap[key];
                                        return (
                                            <FormControlLabel
                                                control={(
                                                    <Checkbox
                                                        id={key}
                                                        checked={!!(selectedGrantTypes
                                                                && selectedGrantTypes.includes(key))}
                                                        onChange={(e) => handleChange('grantType', e)}
                                                        value={value}
                                                        disabled={!isUserOwner}
                                                        color='primary'
                                                    />
                                                )}
                                                label={value}
                                                key={key}
                                            />
                                        );
                                    })}
                                </div>
                                <FormHelperText>
                                    <FormattedMessage
                                        defaultMessage={`The application can use the following grant types to generate 
                            Access Tokens. Based on the application requirement,you can enable or disable 
                            grant types for this application.`}
                                        id='Shared.AppsAndKeys.KeyConfiguration.the.application.can'
                                    />
                                </FormHelperText>

                            </TableCell>
                        </TableRow>
                        <TableRow>
                            <TableCell component='th' scope='row' className={classes.leftCol}>
                                <FormattedMessage
                                    defaultMessage='Callback URL'
                                    id='Shared.AppsAndKeys.KeyConfiguration.callback.url.label'
                                />

                            </TableCell>
                            <TableCell>
                                <Box maxWidth={600}>
                                    <TextField
                                        margin='dense'
                                        id='callbackURL'
                                        label={(
                                            <FormattedMessage
                                                defaultMessage='Callback URL'
                                                id='Shared.AppsAndKeys.KeyConfiguration.callback.url.label'
                                            />
                                        )}
                                        value={callbackUrl}
                                        name='callbackURL'
                                        onChange={(e) => handleChange('callbackUrl', e)}
                                        helperText={callbackHelper || (
                                            <FormattedMessage
                                                defaultMessage={`Callback URL is a redirection URI in the client
                            application which is used by the authorization server to send the
                            client's user-agent (usually web browser) back after granting access.`}
                                                id='Shared.AppsAndKeys.KeyConfCiguration.callback.url.helper.text'
                                            />
                                        )}
                                        variant='outlined'
                                        disabled={!isUserOwner
                                            || (selectedGrantTypes && !selectedGrantTypes.includes('authorization_code')
                                                && !selectedGrantTypes.includes('implicit'))}
                                        error={callbackError}
                                        placeholder={intl.formatMessage({
                                            defaultMessage: 'http://url-to-webapp',
                                            id: 'Shared.AppsAndKeys.KeyConfiguration.url.to.webapp',
                                        })}
                                        fullWidth
                                    />
                                </Box>
                            </TableCell>
                        </TableRow>
                        {applicationConfiguration.length > 0 && applicationConfiguration.map((config) => (
                            <AppConfiguration
                                config={config}
                                previousValue={getPreviousValue(config)}
                                isUserOwner={isUserOwner}
                                handleChange={handleChange}
                            />
                        ))}
                    </TableBody>
                </Table>
            </Box>
        </>
    );
};
KeyConfiguration.defaultProps = {
    notFound: false,
    validating: false,
};
KeyConfiguration.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
    keyRequest: PropTypes.shape({
        callbackUrl: PropTypes.string,
        selectedGrantTypes: PropTypes.array,
    }).isRequired,
    isUserOwner: PropTypes.bool.isRequired,
    isKeysAvailable: PropTypes.bool.isRequired,
    keyManagerConfig: PropTypes.any.isRequired,
    notFound: PropTypes.bool,
    setGenerateEnabled: PropTypes.func.isRequired,
    updateKeyRequest: PropTypes.func.isRequired,
    validating: PropTypes.bool,
};


export default withStyles(styles)(KeyConfiguration);
