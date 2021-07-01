/**
 * Copyright (c)  WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, { useState, useEffect, useContext } from 'react';
import PropTypes from 'prop-types';
import {
    Grid, TextField, MenuItem, InputAdornment,
    Icon,
    ListItem,
    ListItemAvatar,
    ListItemText,
} from '@material-ui/core';
import { RemoveRedEye } from '@material-ui/icons';
import { FormattedMessage, injectIntl } from 'react-intl';
import { withStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import Button from '@material-ui/core/Button';
import AddCircle from '@material-ui/icons/AddCircle';
import TableHead from '@material-ui/core/TableHead';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import isEmpty from 'lodash.isempty';
import { isRestricted } from 'AppData/AuthManager';
import APIContext from 'AppComponents/Apis/Details/components/ApiContext';
import APIValidation from 'AppData/APIValidation';
import Alert from 'AppComponents/Shared/Alert';
import EditableParameterRow from './EditableParameterRow';

const styles = () => ({
    FormControl: {
        padding: 0,
        width: '100%',
    },
    radioWrapper: {
        display: 'flex',
        flexDirection: 'row',
    },
    addParameter: {
        marginRight: '16px',
    },
    marginRight: {
        marginRight: '8px',
    },
    buttonIcon: {
        marginRight: '16px',
    },
    button: {
        marginTop: '5px',
    },
    listItem: {
        marginTop: '25px',
    },
    eye: {
        cursor: 'pointer',
    },
});

/**
 * The base component for advanced endpoint configurations.
 * @param {any} props The props that are being passed
 * @returns {any} The html representation of the component.
 */
function EndpointSecurity(props) {
    const { api } = useContext(APIContext);
    const {
        intl, securityInfo, onChangeEndpointAuth, classes, isProduction,
        saveEndpointSecurityConfig,
        closeEndpointSecurityConfig,
    } = props;
    const [endpointSecurityInfo, setEndpointSecurityInfo] = useState({
        type: '',
        username: '',
        password: '',
        grantType: '',
        tokenUrl: '',
        clientId: '',
        clientSecret: '',
        customParameters: {},
    });
    const [securityValidity, setSecurityValidity] = useState();

    const [showAddParameter, setShowAddParameter] = useState(false);
    const [clientSecretIsMasked, setClientSecretIsMasked] = useState(true);
    // Implementation of useState variables for parameter name and value
    const [parameterName, setParameterName] = useState(null);
    const [parameterValue, setParameterValue] = useState(null);
    const endpointType = isProduction ? 'production' : 'sandbox';
    const [isUsernameUpdated, setIsUsernameUpdated] = useState(false);
    const [isPasswordUpdated, setIsPasswordUpdated] = useState(false);
    const iff = (condition, then, otherwise) => (condition ? then : otherwise);

    const authTypes = [
        {
            key: 'NONE',
            value: intl.formatMessage({
                id: 'Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.none',
                defaultMessage: 'None',
            }),
        },
        {
            key: 'BASIC',
            value: intl.formatMessage({
                id: 'Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.basic',
                defaultMessage: 'Basic Auth',
            }),
        },
        {
            key: 'DIGEST',
            value: intl.formatMessage({
                id: 'Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.digest.auth',
                defaultMessage: 'Digest Auth',
            }),
        },
        {
            key: 'OAUTH',
            value: intl.formatMessage({
                id: 'Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.oauth',
                defaultMessage: 'OAuth 2.0',
            }),
        },
    ];
    const grantTypes = [
        {
            key: 'CLIENT_CREDENTIALS',
            value: intl.formatMessage({
                id: 'Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.oauth.grant.type.client',
                defaultMessage: 'Client Credentials',
            }),
        },
        {
            key: 'PASSWORD',
            value: intl.formatMessage({
                id: 'Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.oauth.grant.type.password',
                defaultMessage: 'Resource Owner Password',
            }),
        },
    ];
    useEffect(() => {
        let tmpSecurity = {};
        if (securityInfo !== null) {
            tmpSecurity = { ...securityInfo };
            const {
                type, username, password, grantType, tokenUrl, clientId, clientSecret, customParameters,
            } = securityInfo;
            tmpSecurity.type = type === null ? 'NONE' : type;
            tmpSecurity.username = username;
            tmpSecurity.password = password === '' ? '**********' : password;
            tmpSecurity.grantType = grantType;
            tmpSecurity.tokenUrl = tokenUrl;
            tmpSecurity.clientId = clientId === '' ? '********' : clientId;
            tmpSecurity.clientSecret = clientSecret === '' ? '********' : clientSecret;
            tmpSecurity.customParameters = customParameters;
        }
        setEndpointSecurityInfo(tmpSecurity);
    }, [props]);

    /**
     * Validating whether token url is in a proper format
     * @param {*} value value of the field
     * @returns {*} boolean value
     */
    const validateTokenUrl = (value) => {
        const state = APIValidation.url.required().validate(value).error;
        if (state === null) {
            return true;
        } else {
            return false;
        }
    };

    /**
     * Validate Security Info properties
     * @param {*} field value of the field
     */
    const validateAndUpdateSecurityInfo = (field) => {
        if (!endpointSecurityInfo[field]) {
            setSecurityValidity({ ...securityValidity, [field]: false });
        } else {
            let validity = true;
            if (field === 'tokenUrl') {
                validity = validateTokenUrl(endpointSecurityInfo[field]);
            }
            setSecurityValidity({ ...securityValidity, [field]: validity });
        }
        const type = isProduction ? 'production' : 'sandbox';
        onChangeEndpointAuth(endpointSecurityInfo, type);
    };

    /**
     * Show or hide the Add Parameter component
     */
    const toggleAddParameter = () => {
        setShowAddParameter(!showAddParameter);
    };

    /**
     * Show or hide the Client Secret
     */
    const toggleClientSecretMask = () => {
        setClientSecretIsMasked(!clientSecretIsMasked);
    };

    /**
     * Set the custom parameter name or value property
     * @param {*} name name of the field edited
     * @returns {*} fills the parameter name or parameter value states
     */
    const handleParameterChange = (name) => (event) => {
        const { value } = event.target;
        if (name === 'parameterName') {
            setParameterName(value);
        } else if (name === 'parameterValue') {
            setParameterValue(value);
        }
    };

    /**
     * Check if the field is empty or not
     * @param {*} itemValue value of the field
     * @returns {*} boolean value
     */
    const validateEmpty = (itemValue) => {
        if (itemValue === null) {
            return false;
        } else if (itemValue === '') {
            return true;
        } else {
            return false;
        }
    };

    /**
     * Add new custom parameter
     */
    const handleAddToList = () => {
        const customParametersCopy = endpointSecurityInfo.customParameters;

        if (customParametersCopy !== null
            && Object.prototype.hasOwnProperty.call(customParametersCopy, parameterName)) {
            Alert.warning('Parameter name: ' + parameterName + ' already exists');
        } else {
            customParametersCopy[parameterName] = parameterValue;
            setParameterName(null);
            setParameterValue(null);
        }
        setEndpointSecurityInfo({ ...endpointSecurityInfo, customParameters: customParametersCopy });
        onChangeEndpointAuth(endpointSecurityInfo, endpointType);
    };

    /**
     * Update existing custom parameter name-value pair
     * @param {*} oldRow previous name-value pair
     * @param {*} newRow new name-value pair
     */
    const handleUpdateList = (oldRow, newRow) => {
        const customParametersCopy = endpointSecurityInfo.customParameters;
        const { oldName, oldValue } = oldRow;
        const { newName, newValue } = newRow;
        if (customParametersCopy !== null
            && Object.prototype.hasOwnProperty.call(customParametersCopy, newName) && oldName === newName) {
            // Only the value is updated
            if (newValue && oldValue !== newValue) {
                customParametersCopy[oldName] = newValue;
            }
        } else {
            delete customParametersCopy[oldName];
            customParametersCopy[newName] = newValue;
        }
        setEndpointSecurityInfo({ ...endpointSecurityInfo, customParameters: customParametersCopy });
        onChangeEndpointAuth(endpointSecurityInfo, endpointType);
    };

    /**
     * Delete existing custom parameter name-value pair
     * @param {*} oldName name property of the name-value pair to be removed
     */
    const handleDelete = (oldName) => {
        const customParametersCopy = endpointSecurityInfo.customParameters;
        if (customParametersCopy !== null && Object.prototype.hasOwnProperty.call(customParametersCopy, oldName)) {
            delete customParametersCopy[oldName];
        }
        setEndpointSecurityInfo({ ...endpointSecurityInfo, customParameters: customParametersCopy });
        onChangeEndpointAuth(endpointSecurityInfo, endpointType);
    };

    /**
     * Keyboard shortcut to execute adding custom parameters when pressing the Enter key
     * @param {*} event event containing the key that was pressed
     */
    const handleKeyDown = (event) => {
        if (event.key === 'Enter') {
            handleAddToList();
        }
    };

    /**
     * Render the custom parameters component
     * @returns {*} list of items added
     */
    const renderCustomParameters = () => {
        const items = [];
        for (const name in endpointSecurityInfo.customParameters) {
            if (Object.prototype.hasOwnProperty.call(endpointSecurityInfo.customParameters, name)) {
                items.push(<EditableParameterRow
                    oldName={name}
                    oldValue={endpointSecurityInfo.customParameters[name]}
                    handleUpdateList={handleUpdateList}
                    handleDelete={handleDelete}
                    customParameters={endpointSecurityInfo.customParameters}
                    {...props}
                    isRestricted={isRestricted}
                    api={api}
                />);
            }
        }
        return items;
    };

    return (
        <Grid container direction='row' spacing={2}>
            <Grid item xs={6}>
                <TextField
                    disabled={isRestricted(['apim:api_create'], api)}
                    fullWidth
                    select
                    value={endpointSecurityInfo && endpointSecurityInfo.type}
                    variant='outlined'
                    onChange={(event) => {
                        setEndpointSecurityInfo({
                            ...endpointSecurityInfo,
                            type: event.target.value,
                        });
                    }}
                    inputProps={{
                        name: 'key',
                        id: 'auth-type-select',
                    }}
                    onBlur={() => validateAndUpdateSecurityInfo(isProduction)}
                >
                    {authTypes.map((type) => (
                        <MenuItem value={type.key}>{type.value}</MenuItem>
                    ))}
                </TextField>
            </Grid>
            <Grid item xs={6} />

            {(endpointSecurityInfo.type === 'OAUTH')
                && (
                    <>
                        <Grid item xs={6}>
                            <TextField
                                disabled={isRestricted(['apim:api_create'], api)}
                                required
                                fullWidth
                                select
                                label={(
                                    <FormattedMessage
                                        id={'Apis.Details.Endpoints.GeneralConfiguration'
                                        + '.EndpointSecurity.grant.type.input'}
                                        defaultMessage='Grant Type'
                                    />
                                )}
                                variant='outlined'
                                onChange={(event) => setEndpointSecurityInfo(
                                    { ...endpointSecurityInfo, grantType: event.target.value },
                                )}
                                value={endpointSecurityInfo.grantType}
                                inputProps={{
                                    name: 'key',
                                    id: 'grant-type-select',
                                }}
                                onBlur={() => validateAndUpdateSecurityInfo('grantType')}
                            >
                                {grantTypes.map((type) => (
                                    <MenuItem value={type.key}>{type.value}</MenuItem>
                                ))}
                            </TextField>
                        </Grid>


                        {(endpointSecurityInfo.grantType === 'CLIENT_CREDENTIALS'
                        || endpointSecurityInfo.grantType === 'PASSWORD') && (
                            <>
                                <Grid item xs={6}>
                                    <TextField
                                        disabled={isRestricted(['apim:api_create'], api)}
                                        required
                                        fullWidth
                                        error={securityValidity && securityValidity.tokenUrl === false}
                                        helperText={
                                            securityValidity && securityValidity.tokenUrl === false ? (
                                                <FormattedMessage
                                                    id={'Apis.Details.Endpoints.GeneralConfiguration'
                                            + '.EndpointSecurity.no.tokenUrl.error'}
                                                    defaultMessage={'Token URL should not be empty'
                                                    + ' or formatted incorrectly'}
                                                />
                                            ) : (
                                                <FormattedMessage
                                                    id={'Apis.Details.Endpoints.GeneralConfiguration.'
                                            + 'EndpointSecurity.tokenUrl.message'}
                                                    defaultMessage='Enter Token URL'
                                                />
                                            )
                                        }
                                        variant='outlined'
                                        id='auth-tokenUrl'
                                        label={(
                                            <FormattedMessage
                                                id={'Apis.Details.Endpoints.GeneralConfiguration.'
                                                + 'EndpointSecurity.token.url.input'}
                                                defaultMessage='Token URL'
                                            />
                                        )}
                                        onChange={(event) => setEndpointSecurityInfo(
                                            { ...endpointSecurityInfo, tokenUrl: event.target.value },
                                        )}
                                        value={endpointSecurityInfo.tokenUrl}
                                        onBlur={() => validateAndUpdateSecurityInfo('tokenUrl')}
                                    />
                                </Grid>

                                <Grid item xs={6}>
                                    <TextField
                                        disabled={isRestricted(['apim:api_create'], api)}
                                        required
                                        fullWidth
                                        error={securityValidity && securityValidity.clientId === false}
                                        helperText={
                                            securityValidity && securityValidity.clientId === false ? (
                                                <FormattedMessage
                                                    id={'Apis.Details.Endpoints.GeneralConfiguration.'
                                            + 'EndpointSecurity.no.clientId.error'}
                                                    defaultMessage='Client ID should not be empty'
                                                />
                                            ) : (
                                                <FormattedMessage
                                                    id={'Apis.Details.Endpoints.GeneralConfiguration.'
                                                    + 'EndpointSecurity.clientId.message'}
                                                    defaultMessage='Enter Client ID'
                                                />
                                            )
                                        }
                                        variant='outlined'
                                        id='auth-clientId'
                                        label={(
                                            <FormattedMessage
                                                id={'Apis.Details.Endpoints.GeneralConfiguration.'
                                                + 'EndpointSecurity.clientId.input'}
                                                defaultMessage='Client ID'
                                            />
                                        )}
                                        onChange={(event) => setEndpointSecurityInfo(
                                            { ...endpointSecurityInfo, clientId: event.target.value },
                                        )}
                                        value={endpointSecurityInfo.clientId}
                                        onBlur={() => validateAndUpdateSecurityInfo('clientId')}
                                        InputProps={{
                                            autoComplete: 'new-password',
                                        }}
                                    />
                                </Grid>

                                <Grid item xs={6}>
                                    <TextField
                                        disabled={isRestricted(['apim:api_create'], api)}
                                        required
                                        fullWidth
                                        error={securityValidity && securityValidity.clientSecret === false}
                                        helperText={
                                            securityValidity && securityValidity.clientSecret === false ? (
                                                <FormattedMessage
                                                    id={'Apis.Details.Endpoints.GeneralConfiguration.'
                                            + 'EndpointSecurity.no.clientSecret.error'}
                                                    defaultMessage='Client Secret should not be empty'
                                                />
                                            ) : (
                                                <FormattedMessage
                                                    id={'Apis.Details.Endpoints.GeneralConfiguration.'
                                            + 'EndpointSecurity.clientSecret.message'}
                                                    defaultMessage='Enter Client Secret'
                                                />
                                            )
                                        }
                                        variant='outlined'
                                        id='auth-clientSecret'
                                        type={clientSecretIsMasked ? 'password' : 'text'}
                                        label={(
                                            <FormattedMessage
                                                id={'Apis.Details.Endpoints.GeneralConfiguration.'
                                                + 'EndpointSecurity.clientSecret.input'}
                                                defaultMessage='Client Secret'
                                            />
                                        )}
                                        onChange={(event) => setEndpointSecurityInfo(
                                            { ...endpointSecurityInfo, clientSecret: event.target.value },
                                        )}
                                        value={endpointSecurityInfo.clientSecret}
                                        onBlur={() => validateAndUpdateSecurityInfo('clientSecret')}
                                        InputProps={{
                                            autoComplete: 'new-password',
                                            endAdornment: (
                                                <InputAdornment position='end'>
                                                    <RemoveRedEye
                                                        className={classes.eye}
                                                        onClick={toggleClientSecretMask}
                                                    />
                                                </InputAdornment>
                                            ),
                                        }}
                                    />
                                </Grid>
                            </>
                        )}
                    </>
                )}

            {(endpointSecurityInfo.type === 'BASIC'
                || endpointSecurityInfo.type === 'DIGEST'
                || endpointSecurityInfo.grantType === 'PASSWORD') && (
                <>
                    <Grid item xs={6}>
                        <TextField
                            disabled={isRestricted(['apim:api_create'], api)}
                            required
                            fullWidth
                            error={securityValidity && securityValidity.username === false}
                            helperText={
                                securityValidity && securityValidity.username === false ? (
                                    <FormattedMessage
                                        id={'Apis.Details.Endpoints.GeneralConfiguration.'
                                        + 'EndpointSecurity.no.username.error'}
                                        defaultMessage='Username should not be empty'
                                    />
                                ) : (
                                    <FormattedMessage
                                        id={'Apis.Details.Endpoints.GeneralConfiguration.'
                                        + 'EndpointSecurity.username.message'}
                                        defaultMessage='Enter Username'
                                    />
                                )
                            }
                            variant='outlined'
                            id='auth-userName'
                            label={(
                                <FormattedMessage
                                    id='Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.user.name.input'
                                    defaultMessage='Username'
                                />
                            )}
                            onChange={(event) => {
                                setEndpointSecurityInfo({ ...endpointSecurityInfo, username: event.target.value });
                                setIsUsernameUpdated(true);
                            }}
                            value={endpointSecurityInfo.username}
                            onBlur={() => validateAndUpdateSecurityInfo('username')}
                        />
                    </Grid>

                    <Grid item xs={6}>
                        <TextField
                            disabled={isRestricted(['apim:api_create'], api)}
                            required
                            fullWidth
                            error={(securityValidity && securityValidity.password === false)
                                || (isUsernameUpdated && !isPasswordUpdated)}
                            helperText={
                                securityValidity && securityValidity.password === false ? (
                                    <FormattedMessage
                                        id={'Apis.Details.Endpoints.GeneralConfiguration.'
                                        + 'EndpointSecurity.no.password.error'}
                                        defaultMessage='Password should not be empty'
                                    />
                                ) : iff(isUsernameUpdated && !isPasswordUpdated,
                                    <FormattedMessage
                                        id={'Apis.Details.Endpoints.GeneralConfiguration.'
                                        + 'EndpointSecurity.change.password.error'}
                                        defaultMessage='Password change is required when the username is changed'
                                    />,
                                    <FormattedMessage
                                        id={'Apis.Details.Endpoints.GeneralConfiguration.'
                                        + 'EndpointSecurity.password.message'}
                                        defaultMessage='Enter Password'
                                    />)
                            }
                            variant='outlined'
                            type='password'
                            id='auth-password'
                            label={(
                                <FormattedMessage
                                    id='Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.password.input'
                                    defaultMessage='Password'
                                />
                            )}
                            value={endpointSecurityInfo.password}
                            onChange={(event) => {
                                setEndpointSecurityInfo({ ...endpointSecurityInfo, password: event.target.value });
                                setIsPasswordUpdated(true);
                            }}
                            onBlur={() => validateAndUpdateSecurityInfo('password')}
                            InputProps={{
                                autoComplete: 'new-password',
                            }}
                        />
                    </Grid>
                </>
            )}

            {endpointSecurityInfo.type === 'OAUTH' && (endpointSecurityInfo.grantType === 'CLIENT_CREDENTIALS'
            || endpointSecurityInfo.grantType === 'PASSWORD')
            && (
                <Grid item xs={12}>
                    <ListItem
                        className={classes.listItem}
                    >
                        <ListItemAvatar>
                            <Icon color='primary'>info</Icon>
                        </ListItemAvatar>
                        <ListItemText>
                            <FormattedMessage
                                id='Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.add.new.parameter.info'
                                defaultMessage={'You can add any additional payload parameters'
                                + ' required for the endpoint below'}
                            />
                        </ListItemText>
                    </ListItem>
                    <Button
                        size='medium'
                        className={classes.button}
                        onClick={toggleAddParameter}
                        disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                    >
                        <AddCircle className={classes.buttonIcon} />
                        <FormattedMessage
                            id='Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurity.add.new.parameter'
                            defaultMessage='Add New Parameter'
                        />
                    </Button>
                </Grid>
            )}

            <Grid item xs={12} />

            {(endpointSecurityInfo.type === 'OAUTH')
            && (!isEmpty(endpointSecurityInfo.customParameters) || showAddParameter) && (
                <Grid item xs={12}>
                    <Table className={classes.table}>
                        <TableHead>
                            <TableRow>
                                <TableCell>
                                    <FormattedMessage
                                        id={'Apis.Details.Endpoints.GeneralConfiguration'
                                            + '.EndpointSecurity.label.parameter.name'}
                                        defaultMessage='Parameter Name'
                                    />
                                </TableCell>
                                <TableCell>
                                    <FormattedMessage
                                        id={'Apis.Details.Endpoints.GeneralConfiguration'
                                            + '.EndpointSecurity.label.parameter.value'}
                                        defaultMessage='Parameter Value'
                                    />
                                </TableCell>
                                <TableCell />
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {showAddParameter
                            && (
                                <>
                                    <TableRow>
                                        <TableCell>
                                            <TextField
                                                fullWidth
                                                required
                                                id='outlined-required'
                                                label={intl.formatMessage({
                                                    id: 'Apis.Details.Endpoints.GeneralConfiguration'
                                                    + '.EndpointSecurity.input.parameter.name',
                                                    defaultMessage: 'Parameter Name',
                                                })}
                                                margin='normal'
                                                variant='outlined'
                                                className={classes.addParameter}
                                                value={parameterName === null ? '' : parameterName}
                                                onChange={handleParameterChange('parameterName')}
                                                onKeyDown={handleKeyDown('parameterName')}
                                                helperText={validateEmpty(parameterName)
                                                    ? 'Invalid parameter name' : ''}
                                                error={validateEmpty(parameterName)}
                                                disabled={isRestricted(
                                                    ['apim:api_create', 'apim:api_publish'],
                                                    api,
                                                )}
                                            />
                                        </TableCell>
                                        <TableCell>
                                            <TextField
                                                fullWidth
                                                required
                                                id='outlined-required'
                                                label={intl.formatMessage({
                                                    id: 'Apis.Details.Endpoints.GeneralConfiguration'
                                                        + '.EndpointSecurity.input.parameter.value',
                                                    defaultMessage: 'Parameter Value',
                                                })}
                                                margin='normal'
                                                variant='outlined'
                                                className={classes.addParameter}
                                                value={parameterValue === null ? '' : parameterValue}
                                                onChange={handleParameterChange('parameterValue')}
                                                onKeyDown={handleKeyDown('parameterValue')}
                                                error={validateEmpty(parameterValue)}
                                                disabled={isRestricted(
                                                    ['apim:api_create', 'apim:api_publish'],
                                                    api,
                                                )}
                                            />
                                        </TableCell>
                                        <TableCell align='right'>
                                            <Button
                                                variant='contained'
                                                color='primary'
                                                disabled={
                                                    !parameterValue
                                                            || !parameterName
                                                            || isRestricted(
                                                                ['apim:api_create', 'apim:api_publish'], api,
                                                            )
                                                }
                                                onClick={handleAddToList}
                                                className={classes.marginRight}
                                            >
                                                <FormattedMessage
                                                    id='Apis.Details.Properties.Properties.add'
                                                    defaultMessage='Add'
                                                />
                                            </Button>

                                            <Button onClick={toggleAddParameter}>
                                                <FormattedMessage
                                                    id='Apis.Details.Properties.Properties.cancel'
                                                    defaultMessage='Cancel'
                                                />
                                            </Button>
                                        </TableCell>
                                    </TableRow>
                                </>
                            )}
                            {((endpointType === 'production') || (endpointType === 'sandbox')) && (
                                renderCustomParameters()
                            )}
                        </TableBody>
                    </Table>
                </Grid>
            )}
            <Grid className={classes.advanceDialogActions}>
                <Button
                    onClick={() => saveEndpointSecurityConfig(endpointSecurityInfo, endpointType)}
                    color='primary'
                    autoFocus
                    variant='contained'
                    style={{ marginTop: '10px', marginRight: '10px', marginBottom: '10px' }}
                    disabled={(endpointSecurityInfo.type !== 'NONE'
                                && endpointSecurityInfo.grantType !== 'CLIENT_CREDENTIALS'
                                && (!isUsernameUpdated && !isPasswordUpdated))
                                || (isUsernameUpdated && !isPasswordUpdated)}
                >
                    <FormattedMessage
                        id='Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurityConfig.config.save.button'
                        defaultMessage='Submit'
                    />
                </Button>
                <Button
                    onClick={closeEndpointSecurityConfig}
                    style={{ marginTop: '10px', marginBottom: '10px' }}
                >
                    <FormattedMessage
                        id='Apis.Details.Endpoints.GeneralConfiguration.EndpointSecurityConfig.cancel.button'
                        defaultMessage='Close'
                    />
                </Button>
            </Grid>
        </Grid>
    );
}

EndpointSecurity.propTypes = {
    intl: PropTypes.shape({}).isRequired,
    securityInfo: PropTypes.shape({}).isRequired,
    onChangeEndpointAuth: PropTypes.func.isRequired,
};

export default withStyles(styles)(injectIntl(EndpointSecurity));
