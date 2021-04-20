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
import React from 'react';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import Box from '@material-ui/core/Box';
import TextField from '@material-ui/core/TextField';
import Autocomplete from '@material-ui/lab/Autocomplete';

import PermissionTree from '../TreeView/PermissionTree';

/**
 *
 *
 * @export
 * @returns
 */
export default function SelectPermissionsStep(props) {
    const {
        onCheck, role, appMappings,
        permissionMappings, onMappedRoleSelect, mappedRole, onPermissionTypeSelect, permissionType,
    } = props;

    const { ROLE_ALIAS, SELECT_PERMISSIONS } = SelectPermissionsStep.CONST;

    return (
        <FormControl style={{ width: '100%' }} component='fieldset'>
            <RadioGroup
                aria-label='mapping-type'
                name='mapping-type'
                value={permissionType}
                onChange={(e) => onPermissionTypeSelect(e.target.value)}
            >
                <FormControlLabel
                    value={ROLE_ALIAS}
                    control={<Radio color='primary' />}
                    label='Role alias'
                />
                <Box width={400} display='inline' pl={7} pt={2} pb={2}>
                    <Autocomplete
                        // multiple
                        value={mappedRole}
                        onChange={(e, newValue) => {
                            onMappedRoleSelect(newValue);
                        }}
                        clearOnEscape
                        disabled={permissionType !== ROLE_ALIAS}
                        id='role-select-dropdown'
                        autoFocus
                        options={Object.keys(permissionMappings)}
                        autoHighlight
                        renderInput={(params) => (
                            <TextField
                                {...params}
                                helperText={(
                                    <>
                                        Role
                                        {' '}
                                        <Box
                                            pl={0.5}
                                            pr={0.5}
                                            display='inline'
                                            fontWeight='fontWeightBold'
                                            color='warning.main'
                                        >
                                            {role}

                                        </Box>
                                        {' '}
                                        will be mapped to the selected role
                                    </>
                                )}
                                id='outlined-basic'
                                size='small'
                                label='Mapping role'
                                variant='outlined'
                                inputProps={{
                                    ...params.inputProps,
                                    autoComplete: 'new-password', // disable autocomplete and autofill
                                }}
                            />
                        )}
                    />
                </Box>
                <FormControlLabel
                    value={SELECT_PERMISSIONS}
                    control={<Radio color='primary' />}
                    label='Custom scope assignments'
                />
                <Box pl={7} pt={2}>
                    <PermissionTree
                        disabled={permissionType !== SELECT_PERMISSIONS}
                        onCheck={onCheck}
                        role={role}
                        appMappings={appMappings}
                    />
                </Box>
            </RadioGroup>
        </FormControl>
    );
}
SelectPermissionsStep.CONST = {
    SELECT_PERMISSIONS: 'select-permissions',
    ROLE_ALIAS: 'role-alias',
};
