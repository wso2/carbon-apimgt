/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
/*
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/* eslint no-param-reassign: ["error", { "props": false }] */
import React from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormGroup from '@material-ui/core/FormGroup';
import Checkbox from '@material-ui/core/Checkbox';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import FormControl from '@material-ui/core/FormControl';
import FormHelperText from '@material-ui/core/FormHelperText';
import { FormattedMessage, injectIntl } from 'react-intl';

const styles = theme => ({
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        paddingBottom: 20,
    },
    root: {
        ...theme.mixins.gutters(),
        paddingTop: theme.spacing.unit * 2,
        paddingBottom: theme.spacing.unit * 2,
    },
    checkItem: {
        textAlign: 'center',
    },
    divider: {
        marginTop: 20,
        marginBottom: 20,
    },
    chip: {
        margin: theme.spacing.unit / 2,
        padding: 0,
        height: 'auto',
        '& span': {
            padding: '0 5px',
        },
    },
    imageContainer: {
        display: 'flex',
    },
    imageWrapper: {
        marginRight: theme.spacing.unit * 3,
        width: 200,
    },
    subtitle: {
        marginTop: theme.spacing.unit,
    },
    specialGap: {
        marginTop: theme.spacing.unit * 3,
    },
    resourceTitle: {
        marginBottom: theme.spacing.unit * 3,
    },
    ListRoot: {
        padding: 0,
        margin: 0,
    },
    title: {
        flex: 1,
    },
    helpButton: {
        padding: 0,
        minWidth: 20,
    },
    helpIcon: {
        fontSize: 16,
    },
    htmlTooltip: {
        backgroundColor: '#f5f5f9',
        color: 'rgba(0, 0, 0, 0.87)',
        maxWidth: 220,
        fontSize: theme.typography.pxToRem(14),
        border: '1px solid #dadde9',
        '& b': {
            fontWeight: theme.typography.fontWeightMedium,
        },
    },
    buttonWrapper: {
        paddingTop: 20,
    },
    descriptionTextField: {
        width: '100%',
        marginBottom: 20,
    },
    rightDataColum: {
        flex: 1,
    },
    formControlLeft: {
        width: 100,
    },
    formControlRight: {
        flex: 1,
    },
    inlineForms: {
        width: '100%',
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'flex-end',
        paddingBottom: 12,
    },
    textFieldRoles: {
        padding: 0,
        margin: '0 0 0 10px',
    },
    group: {
        flexDirection: 'row',
    },
    error: {
        lineHeight: '31px',
    },
    authFormControl: {
        marginTop: 0,
    },
});

/**
 * The component for api security configurations.
 * @param {any} props The input props.
 * @returns {any} The HTML representation of the API Security.
 */
function ApiSecurity(props) {
    const {
        classes, api, isTransportHttps, securityScheme, error, setSecurityScheme, addToArray, removeFromArray,
        securitySchemaValues,
    } = props;

    const getAPISecurityState = (type, apiSecurityScheme) => {
        if (securityScheme) {
            return securityScheme.includes(type);
        } else {
            return apiSecurityScheme.includes(type);
        }
    };

    const isAPISecurityMandatory = (type, apiSecurityScheme) => {
        let tempSecurityScheme = securityScheme;
        if (!tempSecurityScheme) tempSecurityScheme = apiSecurityScheme;

        if (tempSecurityScheme.includes(type)) {
            return 'Mandatory';
        } else {
            return 'Optional';
        }
    };

    const handleAPISecuritySchemeChange = apiSecurityScheme => (event) => {
        const { value, checked } = event.target;
        let tempSecurityScheme = securityScheme;
        if (!securityScheme) tempSecurityScheme = apiSecurityScheme;

        if (checked) {
            addToArray(value, tempSecurityScheme);
        } else if (!checked) {
            removeFromArray(value, tempSecurityScheme);
        }

        const mutualSSLWithOauthOrBasicAuth = tempSecurityScheme.includes(securitySchemaValues.mutualSSL) &&
            (tempSecurityScheme.includes(securitySchemaValues.oauth2) ||
            tempSecurityScheme.includes(securitySchemaValues.basicAuth));

        const mutualSSLOnly = tempSecurityScheme.includes(securitySchemaValues.mutualSSL) &&
            !tempSecurityScheme.includes(securitySchemaValues.oauth2) &&
            !tempSecurityScheme.includes(securitySchemaValues.basicAuth);

        if (!mutualSSLWithOauthOrBasicAuth) {
            if (mutualSSLOnly) {
                addToArray(securitySchemaValues.mutualSSLMandatory, tempSecurityScheme);
                removeFromArray(securitySchemaValues.oauthBasicAuthMandatory, tempSecurityScheme);
            } else {
                addToArray(securitySchemaValues.oauthBasicAuthMandatory, tempSecurityScheme);
                removeFromArray(securitySchemaValues.mutualSSLMandatory, tempSecurityScheme);
            }
        }

        setSecurityScheme(tempSecurityScheme);
    };

    const handleAPISecurityMandatoryStateChange = apiSecurityScheme => (event) => {
        const { value, name } = event.target;
        let tempSecurityScheme = securityScheme;
        if (!securityScheme) tempSecurityScheme = apiSecurityScheme;
        const isMandatory = value === 'Mandatory';

        if (name === 'mutualSSLMandatoryState') {
            if (isMandatory) addToArray(securitySchemaValues.mutualSSLMandatory, tempSecurityScheme);
            else {
                removeFromArray(securitySchemaValues.mutualSSLMandatory, tempSecurityScheme);
                if (tempSecurityScheme.includes(securitySchemaValues.oauth2) ||
                tempSecurityScheme.includes(securitySchemaValues.basicAuth)
                ) {
                    addToArray(securitySchemaValues.oauthBasicAuthMandatory, tempSecurityScheme);
                }
            }
        } else if (name === 'oauthBasicAuthMandatoryState') {
            if (isMandatory) addToArray(securitySchemaValues.oauthBasicAuthMandatory, tempSecurityScheme);
            else {
                removeFromArray(securitySchemaValues.oauthBasicAuthMandatory, tempSecurityScheme);
                if (tempSecurityScheme.includes(securitySchemaValues.mutualSSL)) {
                    addToArray(securitySchemaValues.mutualSSLMandatory, tempSecurityScheme);
                }
            }
        }
        setSecurityScheme(tempSecurityScheme);
    };

    const showMandatoryOption = (apiSecurityScheme) => {
        let tempSecurityScheme = securityScheme;
        if (!tempSecurityScheme) tempSecurityScheme = apiSecurityScheme;

        const mutualSSLWithOauthOrBasicAuth = tempSecurityScheme.includes(securitySchemaValues.mutualSSL) &&
        (tempSecurityScheme.includes(securitySchemaValues.oauth2) ||
        tempSecurityScheme.includes(securitySchemaValues.basicAuth));

        return mutualSSLWithOauthOrBasicAuth;
    };

    const getAPISecurityMessage = (apiSecurityScheme) => {
        let tempSecurityScheme = securityScheme;
        if (!tempSecurityScheme) tempSecurityScheme = apiSecurityScheme;
        if (tempSecurityScheme.includes(securitySchemaValues.mutualSSLMandatory) &&
        tempSecurityScheme.includes(securitySchemaValues.oauthBasicAuthMandatory)) {
            return 'Transport security and Authentication both are set as mandatory';
        } else if (tempSecurityScheme.includes(securitySchemaValues.mutualSSLMandatory) &&
        !tempSecurityScheme.includes(securitySchemaValues.oauthBasicAuthMandatory)) {
            return 'Transport security set as mandatory and Authentication are set as optional';
        } else {
            return 'Transport security set as optional and Authentication are set as mandatory';
        }
    };

    return (
        <div>
            <Grid
                container
                direction='row'
                alignItems='flex-start'
                spacing={4}
                className={classes.buttonSection}
            >
                <Grid item style={{ width: '250px' }}>
                    {/* Transport Security */}
                    {isTransportHttps && (
                        <div>
                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                <FormattedMessage
                                    id='Apis.Details.Configuration.ApiSecurity.transportSecurity'
                                    defaultMessage='Transport Security'
                                />
                                <Tooltip
                                    placement='top'
                                    classes={{
                                        tooltip: classes.htmlTooltip,
                                    }}
                                    disableHoverListener
                                    title={
                                        <React.Fragment>
                                            <FormattedMessage
                                                id='Apis.Details.Configuration.ApiSecurity.transportSecurity.help'
                                                defaultMessage='Enable/Disable Mutual TLS security schema.'
                                            />
                                        </React.Fragment>
                                    }
                                >
                                    <Button className={classes.helpButton}>
                                        <HelpOutline className={classes.helpIcon} />
                                    </Button>
                                </Tooltip>
                            </Typography>
                            <div style={{ 'padding-left': '10px' }} >
                                <FormControl
                                    required
                                    error={error}
                                    component='fieldset'
                                    className={classes.formControl}
                                >
                                    {showMandatoryOption(api.securityScheme) && (
                                        <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                            <FormattedMessage
                                                id='Apis.Details.Configuration.ApiSecurity.transportSecurity.state'
                                                defaultMessage='State'
                                            />
                                            <FormGroup>
                                                <RadioGroup
                                                    name='mutualSSLMandatoryState'
                                                    className={classes.group}
                                                    value={isAPISecurityMandatory(
                                                        securitySchemaValues.mutualSSLMandatory,
                                                        api.securityScheme,
                                                    )}
                                                    onChange={handleAPISecurityMandatoryStateChange(api.securityScheme)}
                                                >
                                                    <FormControlLabel
                                                        value='Mandatory'
                                                        control={<Radio />}
                                                        label='Mandatory'
                                                    />
                                                    <FormControlLabel
                                                        value='Optional'
                                                        control={<Radio />}
                                                        label='Optional'
                                                    />
                                                </RadioGroup>
                                            </FormGroup>
                                        </Typography>
                                    )}
                                    <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                        <FormattedMessage
                                            id='Apis.Details.Configuration.ApiSecurity.transportSecurity.securityScheme'
                                            defaultMessage='Security Scheme'
                                        />
                                        <FormGroup>
                                            <FormControlLabel
                                                control={
                                                    <Checkbox
                                                        checked={getAPISecurityState(
                                                            securitySchemaValues.mutualSSL,
                                                            api.securityScheme,
                                                        )}
                                                        onChange={
                                                            handleAPISecuritySchemeChange(api.securityScheme)}
                                                        value={securitySchemaValues.mutualSSL}
                                                    />
                                                }
                                                label='Mutual TLS'
                                            />
                                        </FormGroup>
                                    </Typography>
                                </FormControl>
                            </div>
                        </div>
                    )}
                </Grid>
                <Grid item tyle={{ width: '250px' }}>
                    {/* Authentication */}
                    <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                        <FormattedMessage
                            id='Apis.Details.Configuration.ApiSecurity.authentication'
                            defaultMessage='Authentication'
                        />
                        <Tooltip
                            placement='top'
                            classes={{
                                tooltip: classes.htmlTooltip,
                            }}
                            disableHoverListener
                            title={
                                <React.Fragment>
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.ApiSecurity.authentication.help'
                                        defaultMessage='OAuth2 is used as the default security schema. If enabled both
                                        OAuth2 and Basic Auth, priority will be given to OAuth2 and if
                                        OAuth2 authentication failed only, the Basic Authentication will take place.'
                                    />
                                </React.Fragment>
                            }
                        >
                            <Button className={classes.helpButton}>
                                <HelpOutline className={classes.helpIcon} />
                            </Button>
                        </Tooltip>
                    </Typography>
                    <div style={{ 'padding-left': '10px' }}>
                        <FormControl
                            required
                            error={error}
                            component='fieldset'
                            className={classes.formControl}
                        >
                            {showMandatoryOption(api.securityScheme) && (
                                <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.ApiSecurity.authentication.state'
                                        defaultMessage='State'
                                    />
                                    <FormGroup>
                                        <RadioGroup
                                            name='oauthBasicAuthMandatoryState'
                                            className={classes.group}
                                            value={isAPISecurityMandatory(
                                                securitySchemaValues.oauthBasicAuthMandatory,
                                                api.securityScheme,
                                            )}
                                            onChange={
                                                handleAPISecurityMandatoryStateChange(api.securityScheme)
                                            }
                                        >
                                            <FormControlLabel
                                                value='Mandatory'
                                                control={<Radio />}
                                                label='Mandatory'
                                            />
                                            <FormControlLabel
                                                value='Optional'
                                                control={<Radio />}
                                                label='Optional'
                                            />
                                        </RadioGroup>
                                    </FormGroup>
                                </Typography>
                            )}
                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                <FormattedMessage
                                    id='Apis.Details.Configuration.ApiSecurity.authentication.securityScheme'
                                    defaultMessage='Security Scheme'
                                />
                                <FormGroup className={classes.group}>
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={getAPISecurityState(
                                                    securitySchemaValues.oauth2,
                                                    api.securityScheme,
                                                )}
                                                onChange={handleAPISecuritySchemeChange(api.securityScheme)}
                                                value={securitySchemaValues.oauth2}
                                            />
                                        }
                                        label='OAuth2'
                                    />
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={getAPISecurityState(
                                                    securitySchemaValues.basicAuth,
                                                    api.securityScheme,
                                                )}
                                                onChange={handleAPISecuritySchemeChange(api.securityScheme)}
                                                value={securitySchemaValues.basicAuth}
                                            />
                                        }
                                        label='Basic Auth'
                                    />
                                </FormGroup>
                            </Typography>
                        </FormControl>
                    </div>
                </Grid>
            </Grid>
            <FormHelperText className={classes.error}>
                <FormattedMessage
                    id='Apis.Details.Configuration.ApiSecurity.mandatoryMessage'
                    defaultMessage={getAPISecurityMessage(api.securityScheme)}
                />
            </FormHelperText>
            {error && (
                <FormHelperText className={classes.error}>
                    <FormattedMessage
                        id='Apis.Details.Configuration.ApiSecurity.error'
                        defaultMessage='Please select at least one API security schema.'
                    />
                </FormHelperText>
            )}
        </div>
    );
}
ApiSecurity.propTypes = {
    classes: PropTypes.shape({
        fileinput: PropTypes.shape({}),
        button: PropTypes.shape({}),
    }).isRequired,
    api: PropTypes.shape({}).isRequired,
    isTransportHttps: PropTypes.bool.isRequired,
    securityScheme: PropTypes.arrayOf(PropTypes.string).isRequired,
    error: PropTypes.bool.isRequired,
    setSecurityScheme: PropTypes.func.isRequired,
    removeFromArray: PropTypes.func.isRequired,
    addToArray: PropTypes.func.isRequired,
    securitySchemaValues: PropTypes.shape({}).isRequired,
};
export default injectIntl(withStyles(styles)(ApiSecurity));
