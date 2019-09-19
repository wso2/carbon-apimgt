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
import FormControl from '@material-ui/core/FormControl';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import Select from '@material-ui/core/Select';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { FormLabel } from '@material-ui/core';
import ChipInput from 'material-ui-chip-input';

/**
 * @inheritdoc
 * @param {*} theme theme object
 */
const styles = theme => ({
    FormControl: {
        padding: theme.spacing.unit * 2,
        width: '100%',
    },
    FormControlOdd: {
        padding: theme.spacing.unit * 2,
        backgroundColor: theme.palette.background.paper,
        width: '100%',
    },
    quotaHelp: {
        position: 'relative',
    },
});

const ApplicationCreate = (props) => {
    /**
     * This method is used to handle the updating of create application
     * request object.
     * @param {*} field field that should be updated in appliction request
     * @param {*} event event fired
     */
    const handleChange = (field, event) => {
        const { applicationRequest, updateApplicationRequest } = props;
        const newRequest = { ...applicationRequest };
        const { target: currentTarget } = event;
        switch (field) {
            case 'name':
                newRequest.name = currentTarget.value;
                break;
            case 'description':
                newRequest.description = currentTarget.value;
                break;
            case 'throttlingPolicy':
                newRequest.throttlingPolicy = currentTarget.value;
                break;
            case 'tokenType':
                newRequest.tokenType = currentTarget.value;
                break;
            case 'attributes':
                newRequest.attributes = currentTarget.value;
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
        isApplicationSharingEnabled,
        handleAddChip,
        handleDeleteChip,
    } = props;
    const tokenTypeList = ['JWT', 'OAUTH'];
    return (
        <form className={classes.container} noValidate autoComplete='off'>
            <Grid container spacing={3} className={classes.root}>
                <Grid item xs={12} md={6}>
                    <FormControl margin='normal' className={classes.FormControl}>
                        <TextField
                            required
                            value={applicationRequest.name}
                            label={intl.formatMessage({
                                defaultMessage: 'Application Name',
                                id: 'Shared.AppsAndKeys.ApplicationCreateForm.application.name',
                            })}
                            InputLabelProps={{
                                shrink: true,
                            }}
                            helperText={intl.formatMessage({
                                defaultMessage:
                                    `Enter a name to identify the Application. 
                                    You will be able to pick this application when subscribing to APIs`,
                                id: 'Shared.AppsAndKeys.ApplicationCreateForm.enter.a.name',
                            })}
                            fullWidth
                            name='name'
                            onChange={e => handleChange('name', e)}
                            placeholder={intl.formatMessage({
                                defaultMessage: 'My Mobile Application',
                                id: 'Shared.AppsAndKeys.ApplicationCreateForm.my.mobile.application',
                            })}
                            autoFocus
                            className={classes.inputText}
                            onBlur={e => validateName(e.target.value)}
                            error={!isNameValid}
                        />
                    </FormControl>

                    {throttlingPolicyList && (
                        <FormControl margin='normal' className={classes.FormControlOdd}>
                            <InputLabel htmlFor='quota-helper' className={classes.quotaHelp}>
                                <FormattedMessage
                                    defaultMessage='Per Token Quota'
                                    id='Shared.AppsAndKeys.ApplicationCreateForm.per.token.quota'
                                />
                            </InputLabel>
                            <Select
                                value={applicationRequest.throttlingPolicy}
                                onChange={e => handleChange('throttlingPolicy', e)}
                                input={<Input name='quota' id='quota-helper' />}
                            >
                                {throttlingPolicyList.map(tier => (
                                    <MenuItem key={tier} value={tier}>
                                        {tier}
                                    </MenuItem>
                                ))}
                            </Select>
                            <Typography variant='caption'>
                                <FormattedMessage
                                    defaultMessage={`Assign API request quota per access token. 
                                    Allocated quota will be shared among all
                                    the subscribed APIs of the application.`}
                                    id='Shared.AppsAndKeys.ApplicationCreateForm.assign.api.request'
                                />
                            </Typography>
                        </FormControl>
                    )}
                    <FormControl margin='normal' className={classes.FormControlOdd}>
                        <InputLabel htmlFor='quota-helper' className={classes.quotaHelp}>
                            <FormattedMessage
                                defaultMessage='Token Type'
                                id='Shared.AppsAndKeys.ApplicationCreateForm.token.type'
                            />
                        </InputLabel>
                        <Select
                            value={applicationRequest.tokenType}
                            onChange={e => handleChange('tokenType', e)}
                            input={<Input name='tokenType' id='quota-helper' />}
                        >
                            {tokenTypeList.map(type => (
                                <MenuItem key={type} value={type}>
                                    {type}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                    <FormControl margin='normal' className={classes.FormControl}>
                        <TextField
                            label='Application Description'
                            value={applicationRequest.description}
                            InputLabelProps={{
                                shrink: true,
                            }}
                            helperText={intl.formatMessage({
                                defaultMessage:
                                    'Describe the application',
                                id: 'Shared.AppsAndKeys.ApplicationCreateForm.describe.the.application',
                            })}
                            fullWidth
                            multiline
                            rowsMax='4'
                            name='description'
                            onChange={e => handleChange('description', e)}
                            placeholder={intl.formatMessage({
                                defaultMessage:
                                    'This application is grouping apis for my mobile application',
                                id: 'Shared.AppsAndKeys.ApplicationCreateForm.this.application',
                            })}
                            className={classes.inputText}
                        />
                    </FormControl>
                    {allAppAttributes && (
                        Object.entries(allAppAttributes).map(item => (
                            item[1].hidden === 'false' ? (
                                <FormControl
                                    margin='normal'
                                    className={classes.FormControl}
                                    key={item[1].attribute}
                                >
                                    <TextField
                                        required={isRequiredAttribute(item[1].attribute)}
                                        label={item[1].attribute}
                                        value={getAttributeValue(item[1].attribute)}
                                        InputLabelProps={{
                                            shrink: true,
                                        }}
                                        helperText={item[1].description}
                                        fullWidth
                                        name={item[1].attribute}
                                        onChange={handleAttributesChange(item[1].attribute)}
                                        placeholder={'Enter ' + item[1].attribute}
                                        className={classes.inputText}
                                    />
                                </FormControl>
                            ) : (null)))
                    )}
                    {isApplicationSharingEnabled && (
                        <FormControl margin='normal' className={classes.FormControl}>
                            <FormLabel component='legend'>
                                <Typography variant='caption'>
                                    <FormattedMessage
                                        defaultMessage='Application Groups'
                                        id='Shared.AppsAndKeys.ApplicationCreateForm.add.groups.label'
                                    />
                                </Typography>
                            </FormLabel>
                            <ChipInput
                                {...applicationRequest}
                                value={applicationRequest.groups || []}
                                onAdd={chip => handleAddChip(chip, applicationRequest.groups)}
                                onDelete={(chip, index) => handleDeleteChip(chip,
                                    index, applicationRequest.groups)}
                            />
                        </FormControl>
                    )}
                </Grid>
            </Grid>
        </form>
    );
};

ApplicationCreate.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    applicationRequest: PropTypes.shape({}).isRequired,
    intl: PropTypes.func.isRequired,
    isNameValid: PropTypes.bool.isRequired,
    allAppAttributes: PropTypes.arrayOf(PropTypes.array).isRequired,
    handleAttributesChange: PropTypes.func.isRequired,
    getAttributeValue: PropTypes.func.isRequired,
    validateName: PropTypes.func.isRequired,
    updateApplicationRequest: PropTypes.func.isRequired,
    isRequiredAttribute: PropTypes.bool.isRequired,
    isApplicationSharingEnabled: PropTypes.func.isRequired,
    handleAddChip: PropTypes.func.isRequired,
    handleDeleteChip: PropTypes.func.isRequired,
    throttlingPolicyList: PropTypes.arrayOf(PropTypes.string).isRequired,
};

export default injectIntl(withStyles(styles)(ApplicationCreate));
