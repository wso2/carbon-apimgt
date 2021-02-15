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
import MenuItem from '@material-ui/core/MenuItem';
import TextField from '@material-ui/core/TextField';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import ChipInput from 'material-ui-chip-input';


/**
 * @inheritdoc
 * @param {*} theme theme object
 */
const styles = (theme) => ({
    FormControl: {
        padding: theme.spacing(2),
        width: '100%',
    },
    FormControlOdd: {
        padding: theme.spacing(2),
        backgroundColor: theme.palette.background.paper,
        width: '100%',
    },
    quotaHelp: {
        position: 'relative',
    },
    mandatoryStarText: {
        '& label>span:nth-child(1)': {
            color: 'red',
        },
    },
    applicationForm: {
        '& span, & div, & p, & input': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        }
    }
});

const ApplicationCreate = (props) => {
    /**
     * This method is used to handle the updating of create application
     * request object.
     * @param {*} field field that should be updated in appliction request
     * @param {*} event event fired
     */
    const handleChange = ({ target: { name: field, value } }) => {
        const { applicationRequest, updateApplicationRequest } = props;
        const newRequest = { ...applicationRequest };
        // const { target: currentTarget } = event;
        switch (field) {
            case 'name':
                newRequest.name = value;
                break;
            case 'description':
                newRequest.description = value;
                break;
            case 'throttlingPolicy':
                newRequest.throttlingPolicy = value;
                break;
            case 'tokenType':
                newRequest.tokenType = value;
                break;
            case 'attributes':
                newRequest.attributes = value;
                break;
            default:
                break;
        }
        updateApplicationRequest(newRequest);
    };

    /**
     *
     *
     * @returns {Component}
     * @memberof ApplicationCreate
     */
    const {
        classes,
        throttlingPolicyList,
        applicationRequest,
        isNameValid,
        allAppAttributes,
        handleAttributesChange,
        isRequiredAttribute,
        getAttributeValue,
        intl,
        validateName,
        validateDescription,
        isApplicationSharingEnabled,
        handleAddChip,
        handleDeleteChip,
    } = props;
    const description = applicationRequest.description || '';
    const showDescError = () => {
        const descLength = description.length;
        const remaining = 512 - descLength;
        return (`( ${remaining.toString()} ) ${intl.formatMessage({
            defaultMessage:
                'characters remaining',
            id: 'Shared.AppsAndKeys.ApplicationCreateForm.describe.length.error.suffix',
        })}`)
    }
    return (
        <form noValidate autoComplete='off' className={classes.applicationForm}>
            <TextField
                id='application-name'
                classes={{
                    root: classes.mandatoryStarText,
                }}
                margin='normal'
                variant='outlined'
                autoFocus
                fullWidth
                required
                value={applicationRequest.name}
                label={intl.formatMessage({
                    defaultMessage: 'Application Name',
                    id: 'Shared.AppsAndKeys.ApplicationCreateForm.application.name',
                })}
                helperText={intl.formatMessage({
                    defaultMessage:
                        `Enter a name to identify the Application.
                                    You will be able to pick this application when subscribing to APIs`,
                    id: 'Shared.AppsAndKeys.ApplicationCreateForm.enter.a.name',
                })}
                name='name'
                onChange={handleChange}
                placeholder={intl.formatMessage({
                    defaultMessage: 'My Application',
                    id: 'Shared.AppsAndKeys.ApplicationCreateForm.my.mobile.application',
                })}
                onBlur={(e) => validateName(e.target.value)}
                error={!isNameValid}
                inputProps={{
                    maxLength: 70, alt: intl.formatMessage({
                        defaultMessage: 'Required',
                        id: 'Shared.AppsAndKeys.ApplicationCreateForm.required.alt',
                    })
                }}
            />
            <TextField
                classes={{
                    root: classes.mandatoryStarText,
                }}
                required
                fullWidth
                id='per-token-quota'
                select
                label={(
                    <FormattedMessage
                        defaultMessage='Shared Quota for Application Tokens'
                        id='Shared.AppsAndKeys.ApplicationCreateForm.per.token.quota'
                    />
                )}
                value={applicationRequest.throttlingPolicy}
                name='throttlingPolicy'
                onChange={handleChange}
                helperText={(
                    <FormattedMessage
                        defaultMessage={`Assign API request quota per access token.
                            Allocated quota will be shared among all
                            the subscribed APIs of the application.`}
                        id='Shared.AppsAndKeys.ApplicationCreateForm.assign.api.request'
                    />
                )}
                margin='normal'
                variant='outlined'
                inputProps={{
                    alt: intl.formatMessage({
                        defaultMessage: 'Required',
                        id: 'Shared.AppsAndKeys.ApplicationCreateForm.required.alt',
                    })
                }}
            >
                {throttlingPolicyList.map((policy) => (
                    <MenuItem key={policy} value={policy}>
                        {policy}
                    </MenuItem>
                ))}
            </TextField>
            <TextField
                id='application-description'
                margin='normal'
                variant='outlined'
                fullWidth
                multiline
                rows={4}
                value={description}
                label={intl.formatMessage({
                    defaultMessage: 'Application Description',
                    id: 'Shared.AppsAndKeys.ApplicationCreateForm.application.description.label',
                })}
                helperText={showDescError()}
                name='description'
                onChange={handleChange}
                placeholder={intl.formatMessage({
                    defaultMessage: 'My Mobile Application',
                    id: 'Shared.AppsAndKeys.ApplicationCreateForm.my.mobile.application.placeholder',
                })}
                error={description !== '' && description.length > 512}
                onBlur={(e) => validateDescription(e.target.value)}

            />
            {allAppAttributes && (
                Object.entries(allAppAttributes).map((item) => (
                    item[1].hidden !== 'true' ? (
                        <TextField
                            classes={{
                                root: classes.mandatoryStarText,
                            }}
                            margin='normal'
                            variant='outlined'
                            required={isRequiredAttribute(item[1].attribute)}
                            label={item[1].attribute}
                            value={getAttributeValue(item[1].attribute)}
                            helperText={item[1].description}
                            multiline={item[1].type === 'textarea'}
                            rows={4}
                            fullWidth
                            name={item[1].attribute}
                            onChange={handleAttributesChange(item[1].attribute)}
                            placeholder={'Enter ' + item[1].attribute}
                            className={classes.inputText}
                            inputProps={{
                                alt: isRequiredAttribute(item[1].attribute) && intl.formatMessage({
                                    defaultMessage: 'Required',
                                    id: 'Shared.AppsAndKeys.ApplicationCreateForm.required.alt',
                                })
                            }}
                        />
                    ) : (null)))
            )}
            {isApplicationSharingEnabled && (
                <ChipInput
                    label={(
                        <FormattedMessage
                            defaultMessage='Application Groups'
                            id='Shared.AppsAndKeys.ApplicationCreateForm.add.groups.label'
                        />
                    )}
                    helperText={intl.formatMessage({
                        defaultMessage: 'Type a group and enter',
                        id: 'Shared.AppsAndKeys.ApplicationCreateForm.type.a.group.and.enter',
                    })}
                    margin='normal'
                    variant='outlined'
                    fullWidth
                    {...applicationRequest}
                    value={applicationRequest.groups || []}
                    onAdd={(chip) => handleAddChip(chip, applicationRequest.groups)}
                    onDelete={(chip, index) => handleDeleteChip(
                        chip,
                        index, applicationRequest.groups,
                    )}
                />
            )}
        </form>
    );
};
ApplicationCreate.defaultProps = {
    ApplicationCreate: null,
};
ApplicationCreate.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    applicationRequest: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
    isNameValid: PropTypes.bool.isRequired,
    allAppAttributes: PropTypes.arrayOf(PropTypes.array),
    handleAttributesChange: PropTypes.func.isRequired,
    getAttributeValue: PropTypes.func.isRequired,
    validateName: PropTypes.func.isRequired,
    validateDescription: PropTypes.func.isRequired,
    updateApplicationRequest: PropTypes.func.isRequired,
    isRequiredAttribute: PropTypes.func.isRequired,
    isApplicationSharingEnabled: PropTypes.bool.isRequired,
    handleAddChip: PropTypes.func.isRequired,
    handleDeleteChip: PropTypes.func.isRequired,
    throttlingPolicyList: PropTypes.arrayOf(PropTypes.string).isRequired,
};

export default injectIntl(withStyles(styles)(ApplicationCreate));
