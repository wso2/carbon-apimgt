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
import React, { useEffect, useState, useCallback } from 'react';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import PermissionAPI from 'AppData/PermissionScopes';
import Grid from '@material-ui/core/Grid';
import Alert from 'AppComponents/Shared/Alert';
import Progress from 'AppComponents/Shared/Progress';
import Button from '@material-ui/core/Button';
import Box from '@material-ui/core/Box';

import PermissionsSelector from './TreeView/PermissionsSelector';
import AdminTable from './AdminTable/AdminTable';
import AdminTableHead from './AdminTable/AdminTableHead';
import TableBody from './AdminTable/AdminTableBody';
import ListAddOns from './Commons/ListAddOns';
import DeletePermission from './Commons/DeletePermission';
import AddRoleWizard from './Commons/AddRoleWizard';

const headCells = [
    {
        numeric: false, disablePadding: false, label: 'Roles',
    },
    {
        id: 'permissions', numeric: false, disablePadding: false, label: 'Permissions',
    },
];

/**
 *
 * Extract the scope mapping against REST API applications and User roles.
 * This is to make it easy to iterate over the data
 * @param {Array} permissionMapping Raw scope mapping response received from REST API
 * @returns {Array} Two values, REST API wise scope mappings and User role wise scope mappings
 */
function extractMappings(permissionMapping) {
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
            let trimmedRole = role.trim();
            trimmedRole = trimmedRole || '<No_Name>';
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
 * @export @inheritdoc
 * @returns {React.Component} Role -> Permission list component
 */
export default function ListRoles() {
    /*
    Same set of scope mappings, Mapped to corresponding **roles
    i:e
        {
            admin -> [{ name: '', description: '', roles: ''}],
            .
            .
            .
            Internal/Publisher -> [{ name: '', description: '', roles: ''}],
        }
    */
    const [permissionMappings, setPermissionMappings] = useState();
    /*
    Same set of scope mappings, Mapped to corresponding **REST APIs
    i:e
        {
            admin -> [{ name: '', description: '', roles: []}],
            publisher -> [{ name: '', description: '', roles: []}],
            store -> [{ name: '', description: '', roles: []}],
        }
    */
    const [appMappings, setAppMappings] = useState();
    const [isOpen, setIsOpen] = useState(false);

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

    /*
        No need to create handleScopeMappingUpdate all the time ,
        because we pass the updatedAppMappings, don't take anything from state or props
        This method is in the `ListRoles` component because we need this method when deleting a permission map
    */
    const handleScopeMappingUpdate = useCallback(
        (updatedAppMappings) => {
            return PermissionAPI.updateSystemScopes(updatedAppMappings).then((data) => {
                const [newRoleMapping, newAppMapping] = extractMappings(data.body.list);
                setPermissionMappings(newRoleMapping);
                setAppMappings(newAppMapping);
            });
        },
        [],
    );
    const handleDeleteRole = (deletedRole) => {
        const updatedAppMappings = {};
        for (const [app, permissions] of Object.entries(appMappings)) {
            updatedAppMappings[app] = permissions.map((permission) => (
                { ...permission, roles: permission.roles.filter((role) => role !== deletedRole) }
            ));
        }
        return handleScopeMappingUpdate(updatedAppMappings);
    };

    if (!permissionMappings || !appMappings) {
        return <Progress message='Resolving user ...' />;
    }
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
                                onRoleAdd={handleScopeMappingUpdate}
                            />
                        )
                    }
                </Grid>
            </ListAddOns>
            <AdminTable multiSelect={false}>
                <AdminTableHead headCells={headCells} />
                <TableBody rows={Object.entries(permissionMappings).map(([role]) => {
                    return [role,
                        <Box component='span' display='block'>
                            <PermissionsSelector
                                role={role}
                                appMappings={appMappings}
                                onSave={handleScopeMappingUpdate}
                            />
                            <Box pl={1} display='inline'>
                                <DeletePermission
                                    size='small'
                                    variant='outlined'
                                    onDelete={handleDeleteRole}
                                    role={role}
                                >
                                    Delete
                                </DeletePermission>
                            </Box>
                        </Box>,
                    ];
                })}
                />
            </AdminTable>
        </ContentBase>
    );
}
