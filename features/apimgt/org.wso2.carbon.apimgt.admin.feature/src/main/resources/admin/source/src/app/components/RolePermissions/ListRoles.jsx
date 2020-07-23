import React, { useEffect, useState } from 'react';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import PermissionAPI from 'AppData/PermissionScopes';
import TextField from '@material-ui/core/TextField';
import Grid from '@material-ui/core/Grid';
import Alert from 'AppComponents/Shared/Alert';
import Progress from 'AppComponents/Shared/Progress';

import PermissionsSelector from './TreeView/PermissionsSelector';
import PermissionTree from './TreeView/PermissionTree';

import AdminTable from './AdminTable';
import AdminTableHead from './AdminTableHead';
import TableBody from './AdminTableBody';
import ListAddOns from './ListAddOns';
import AddItem from './AddItem';


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
    const [newRole, setNewRole] = useState('');

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
    const onAddRole = () => {
        if (permissionMappings.find((role) => role === newRole) || !newRole) {
            alert('Role already exsists or role empty !!');
            return;
        }
        setPermissionMappings([...permissionMappings, newRole]);
        setNewRole('');
    };
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
                    <AddItem onSave={onAddRole} title='Add new role permissions' buttonText='Add role permissions'>
                        <TextField
                            value={newRole}
                            label='Role Name'
                            variant='outlined'
                            onChange={({ target: { value } }) => {
                                setNewRole(value);
                            }}
                            onKeyDown={(event) => (event.which === 13
                                || event.keyCode === 13
                                || event.key === 'Enter')
                                && onAddRole()}
                        />
                        {/* <PermissionTree onCheck={() => {}} role={newRole} appMappings={appMappings} /> */}
                    </AddItem>
                </Grid>
            </ListAddOns>
            <AdminTable multiSelect={false}>
                <AdminTableHead headCells={headCells} />
                <TableBody rows={Object.entries(permissionMappings).map(([role]) => {
                    return [role, <PermissionsSelector
                        onCheck={permissionCheckHandler}
                        role={role}
                        appMappings={appMappings}
                    />];
                })}
                />
            </AdminTable>
        </ContentBase>
    );
}
