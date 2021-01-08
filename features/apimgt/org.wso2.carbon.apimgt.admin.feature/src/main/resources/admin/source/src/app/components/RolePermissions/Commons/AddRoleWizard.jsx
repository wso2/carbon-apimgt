/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage, useIntl } from 'react-intl';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import StepContent from '@material-ui/core/StepContent';
import Button from '@material-ui/core/Button';
import Alert from 'AppComponents/Shared/Alert';
import TextField from '@material-ui/core/TextField';
import Box from '@material-ui/core/Box';
import CircularProgress from '@material-ui/core/CircularProgress';
import cloneDeep from 'lodash.clonedeep';

import PermissionAPI from 'AppData/PermissionScopes';
import AddItem from './AddItem';
import SelectPermissionsStep from './SelectPermissionsStep';

const { ROLE_ALIAS, SELECT_PERMISSIONS } = SelectPermissionsStep.CONST;

const useStyles = makeStyles((theme) => ({
    root: {
        width: '100%',
    },
    button: {
        marginTop: theme.spacing(1),
        marginRight: theme.spacing(1),
    },
    actionsContainer: {
        marginBottom: theme.spacing(2),
    },
    resetContainer: {
        padding: theme.spacing(3),
    },
}));

/**
 *
 *
 * @export
 * @returns
 */
export default function AddRoleWizard(props) {
    const {
        appMappings, onClose, onRoleAdd, permissionMappings, roleAliases, setRoleAliases,
    } = props;

    const classes = useStyles();
    const intl = useIntl();
    const [newRole, setNewRole] = useState('');
    const [isSaving, setIsSaving] = useState(false);
    const [validation, setValidation] = useState({});
    const [permissionsValidation, setPermissionsValidation] = useState({});
    const [mappedRole, setMappedRole] = useState();
    const [permissionTypeSelection, setPermissionTypeSelection] = useState(ROLE_ALIAS);

    // No need an effect here due to the component structure
    const [localAppMappings, setLocalAppMappings] = useState(cloneDeep(appMappings));

    const [activeStep, setActiveStep] = React.useState(0);

    const permissionCheckHandler = (event) => {
        const {
            name: scopeName, checked, role: selectedRole, app,
        } = event.target;
        const newAppMappings = { ...localAppMappings };
        newAppMappings[app] = newAppMappings[app].map(({ name, roles, ...rest }) => {
            if (name === scopeName) {
                if (checked) {
                    return { ...rest, name, roles: [...roles, selectedRole] };
                } else {
                    return { ...rest, name, roles: roles.filter((role) => selectedRole !== role) };
                }
            } else {
                return { name, roles, ...rest };
            }
        });
        setPermissionsValidation({ ...permissionsValidation, [scopeName]: checked });
        setLocalAppMappings(newAppMappings);
    };
    const handleNext = () => {
        if (!validation.role && newRole) {
            setActiveStep((prevActiveStep) => prevActiveStep + 1);
        } else {
            Alert.warning('Role name can not be empty!');
        }
    };

    const handleBack = () => {
        setActiveStep((prevActiveStep) => prevActiveStep - 1);
        setLocalAppMappings(cloneDeep(appMappings));
    };

    /**
     * Handle the final step in the wizard, The `Save` action
     */
    const onAddRole = () => {
        setIsSaving(true);
        // Check if user has select at least one permission from the tree if the type is SELECT_PERMISSION
        if (permissionTypeSelection === SELECT_PERMISSIONS) {
            const permissionsValidationConditions = Object.values(permissionsValidation);
            if (!permissionsValidationConditions.length
                || !permissionsValidationConditions.reduce((acc, cu) => acc || cu)) {
                Alert.warning('You need to select at least one permission!');
                setIsSaving(false);
                return;
            }
            Promise.resolve(onRoleAdd(localAppMappings))
                .then(() => {
                    Alert.info(
                        <span>
                            Added scope mapping for
                            <b>{` ${newRole} `}</b>
                            successfully
                        </span>,
                    );
                    onClose();
                })
                .catch((error) => {
                    Alert.error(
                        intl.formatMessage({
                            id: 'RolePermissions.Common.AddRoleWizard.add.scope.error',
                            defaultMessage: 'Something went wrong while adding new scope mapping',
                        }),
                    );
                    console.error(error);
                })
                .finally(() => setIsSaving(false));
        } else {
            if (!mappedRole) {
                Alert.warning("Mapped role selection can't be empty!");
                setIsSaving(false);
                return;
            }
            const updatedRoleAliases = [...roleAliases.list];
            const targetRole = updatedRoleAliases.find(({ role }) => role === mappedRole);
            if (targetRole) {
                targetRole.aliases.push(newRole);
            } else {
                updatedRoleAliases.push({ role: mappedRole, aliases: [newRole] });
            }
            PermissionAPI.updateRoleAliases(updatedRoleAliases).then((response) => {
                setRoleAliases(response.body);
                Alert.info(
                    <span>
                        Add new alias for
                        <b>{` ${newRole} `}</b>
                        successfully
                    </span>,
                );
                onClose();
            }).catch((error) => {
                Alert.error('Something went wrong while adding new role alias');
                console.error(error);
            }).finally(() => setIsSaving(false));
        }
    };

    return (

        <AddItem
            onSave={onAddRole}
            onClose={onClose}
            title={(
                <FormattedMessage
                    id='RolePermissions.Common.AddRoleWizard.add.mapping.title'
                    defaultMessage='Add new scope mapping'
                />
            )}
            buttonText={(
                <FormattedMessage
                    id='RolePermissions.Common.AddRoleWizard.add.mapping.button'
                    defaultMessage='Add scope mapping'
                />
            )}
            dialogProps={{ disableBackdropClick: isSaving, maxWidth: 'md' }}
            dialogActions={(
                <div className={classes.actionsContainer}>
                    <div>
                        <Button
                            onClick={activeStep === 0 ? onClose : handleBack}
                            className={classes.button}
                            disabled={isSaving}
                        >
                            {activeStep === 0 ? 'Cancel' : 'Back'}
                        </Button>
                        <Button
                            variant='contained'
                            disabled={isSaving}
                            color='primary'
                            onClick={activeStep === 1 ? onAddRole : handleNext}
                            className={classes.button}
                        >
                            {activeStep === 1 ? (
                                <>
                                    {isSaving && <CircularProgress size={16} />}
                                    Save
                                </>
                            ) : 'Next'}
                        </Button>
                    </div>
                </div>
            )}
        >
            <div className={classes.root}>
                <Stepper activeStep={activeStep} orientation='vertical'>
                    {['Provide role name', 'Select permissions'].map((label, index) => (
                        <Step key={label}>
                            <StepLabel>{label}</StepLabel>
                            <StepContent>

                                {
                                    (index === 0) && (
                                        <Box pt={3} width='40%'>
                                            <TextField
                                                id='role-input-field-helper-text'
                                                error={Boolean(validation.role)}
                                                value={newRole}
                                                fullWidth
                                                autoFocus
                                                size='small'
                                                label='Role Name'
                                                helperText={
                                                    validation.role
                                                    || 'Type existing user role, '
                                                    + ' If not create a new role from carbon console first'
                                                }
                                                variant='outlined'
                                                onChange={({ target: { value } }) => {
                                                    const trimmedValue = value.trim();
                                                    if (!trimmedValue) {
                                                        setValidation({ role: "Role name can't be empty!" });
                                                    } else if (permissionMappings[trimmedValue]) {
                                                        setValidation({ role: 'Permission mapping exist' });
                                                    } else {
                                                        setValidation({ role: false });
                                                    }
                                                    setNewRole(trimmedValue);
                                                }}
                                                onKeyDown={(event) => (event.which === 13
                                                    || event.keyCode === 13
                                                    || event.key === 'Enter')
                                                    && handleNext()}
                                            />
                                        </Box>
                                    )
                                }
                                {
                                    (index === 1) && (
                                        <>
                                            <SelectPermissionsStep
                                                onCheck={permissionCheckHandler}
                                                role={newRole}
                                                appMappings={localAppMappings}
                                                permissionMappings={permissionMappings}
                                                onMappedRoleSelect={setMappedRole}
                                                mappedRole={mappedRole}
                                                onPermissionTypeSelect={setPermissionTypeSelection}
                                                permissionType={permissionTypeSelection}
                                            />
                                        </>
                                    )
                                }
                            </StepContent>
                        </Step>
                    ))}
                </Stepper>
            </div>
        </AddItem>

    );
}
