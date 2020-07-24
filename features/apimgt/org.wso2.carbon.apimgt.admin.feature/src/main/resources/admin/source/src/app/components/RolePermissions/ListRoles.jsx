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
import React, { useEffect, useState, useCallback } from 'react';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import PermissionAPI from 'AppData/PermissionScopes';
import Grid from '@material-ui/core/Grid';
import Alert from 'AppComponents/Shared/Alert';
import Progress from 'AppComponents/Shared/Progress';
import Button from '@material-ui/core/Button';

import PermissionsSelector from './TreeView/PermissionsSelector';
import AdminTable from './AdminTable/AdminTable';
import AdminTableHead from './AdminTable/AdminTableHead';
import TableBody from './AdminTable/AdminTableBody';
import ListAddOns from './Commons/ListAddOns';
import AddRoleWizard from './Commons/AddRoleWizard';


/**
 *
 *
 * @param {Array} permissionMapping
 * @returns
 */
function extractMappings(permissionMapping) {
    // const roles = new Set();
    const roleMapping = {};
    const appMapping = {};
    for (const mapping of permissionMapping) {
        const {
            roles, tag,
        } = mapping;
        const trimmedRoles = [];
        const rolesList = roles.split(',');

        if (appMapping[tag]) {
            appMapping[tag].push(mapping);
        } else {
            appMapping[tag] = [mapping];
        }
        for (const role of rolesList) {
            const trimmedRole = role.trim();
            trimmedRoles.push(trimmedRole);
            if (roleMapping[trimmedRole]) {
                roleMapping[trimmedRole].push(mapping);
            } else {
                roleMapping[trimmedRole] = [mapping];
            }
        }
        mapping.roles = trimmedRoles;
    }
    return [roleMapping, appMapping];
}


/**
 *
 *
 * @export
 * @returns
 */
export default function ListRoles() {
    const [permissionMappings, setPermissionMappings] = useState();
    const [appMappings, setAppMappings] = useState();
    const [isOpen, setIsOpen] = useState(false);

    const handleSave = useCallback(
        (updatedAppMappings) => {
            const payload = [];
            for (const appScopes of Object.values(updatedAppMappings)) {
                for (const scope of appScopes) {
                    payload.push({ ...scope, roles: scope.roles.join(',') });
                }
            }
            return PermissionAPI.updateSystemScopes({ count: payload.length, list: payload }).then((data) => {
                const [roleMapping, appMapping] = extractMappings(data.body.list);
                setPermissionMappings(roleMapping);
                setAppMappings(appMapping);
            });
        },
        [],
    );
    useEffect(() => {
        PermissionAPI.systemScopes().then(
            (data) => {
                const [roleMapping, appMapping] = extractMappings(data.body.list);
                setPermissionMappings(roleMapping);
                setAppMappings(appMapping);
            },
        ).catch((error) => {
            // TODO: Proper error handling here ~tmkb
            Alert.error('Error while retrieving permission info');
            console.error(error);
        });
    }, []);

    const permissionCheckHandler = (event) => {
        const {
            name: scopeName, checked, role: selectedRole, app,
        } = event.target;
        const newAppMappings = { ...appMappings };
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
        setAppMappings(newAppMappings);
    };
    if (!permissionMappings || !appMappings) {
        return <Progress message='Resolving user ...' />;
    }
    const headCells = [
        {
            id: 'roles', numeric: false, disablePadding: false, label: 'Roles',
        },
        {
            id: 'permissions', numeric: false, disablePadding: false, label: 'Permissions',
        },
    ];
    return (
        <ContentBase title='Role Permissions'>
            <ListAddOns>
                <Grid item>
                    <Button
                        variant='contained'
                        color='primary'
                        onClick={() => setIsOpen(true)}
                    >
                        Add role permission
                    </Button>
                    {
                        isOpen && (
                            <AddRoleWizard
                                permissionMappings={permissionMappings}
                                appMappings={appMappings}
                                onClose={() => setIsOpen(false)}
                                onRoleAdd={handleSave}
                            />
                        )
                    }
                </Grid>
            </ListAddOns>
            <AdminTable multiSelect={false}>
                <AdminTableHead headCells={headCells} />
                <TableBody rows={Object.entries(permissionMappings).map(([role]) => {
                    return [role, <PermissionsSelector
                        onCheck={permissionCheckHandler}
                        role={role}
                        appMappings={appMappings}
                        onSave={handleSave}
                    />];
                })}
                />
            </AdminTable>
        </ContentBase>
    );
}
