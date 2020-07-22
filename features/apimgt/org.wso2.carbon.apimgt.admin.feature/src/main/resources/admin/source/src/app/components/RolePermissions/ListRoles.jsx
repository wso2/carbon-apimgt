import React, { useEffect, useState } from 'react';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import PermissionAPI from 'AppData/PermissionScopes';
import { makeStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import TextField from '@material-ui/core/TextField';
import IconButton from '@material-ui/core/IconButton';
import AddIcon from '@material-ui/icons/Add';
import clsx from 'clsx';
import Alert from 'AppComponents/Shared/Alert';

import PermissionsTree from './TreeView/PermissionsTree';
import AdminTable from './AdminTable';
import AdminTableHead from './AdminTableHead';

const useStyles = makeStyles({
    table: {
        minWidth: 650,
    },
});


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
        const rolesList = roles.split(',');
        if (appMapping[tag]) {
            appMapping[tag].push(mapping);
        } else {
            appMapping[tag] = [mapping];
        }
        for (const role of rolesList) {
            const trimmedRole = role.trim();
            if (roleMapping[trimmedRole]) {
                roleMapping[trimmedRole].push(mapping);
            } else {
                roleMapping[trimmedRole] = [mapping];
            }
        }
        // .forEach((scope) => roles.add(scope.trim()));
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

    const classes = useStyles();
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
        });
    }, []);
    const onAddEntry = () => {
        if (permissionMappings.find((role) => role === newRole) || !newRole) {
            alert('Role already exsists or role empty !!');
            return;
        }
        setPermissionMappings([...permissionMappings, newRole]);
        setNewRole('');
    };
    if (!permissionMappings || !appMappings) {
        // TODO: ~tmkb add loader
        return null;
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
        <ContentBase>
            <TextField
                value={newRole}
                label='New Value'
                variant='outlined'
                onChange={({ target: { value } }) => {
                    setNewRole(value);
                }}
                onKeyDown={(event) => (event.which === 13
                    || event.keyCode === 13
                    || event.key === 'Enter')
                    && onAddEntry()}
            />
            <IconButton
                onClick={onAddEntry}
                aria-label='delete'
                className={classes.margin}
            >
                <AddIcon fontSize='small' />
            </IconButton>
            <AdminTable multiSelect={false}>
                <AdminTableHead headCells={headCells} />
            </AdminTable>
            {/*
            <TableContainer component={Paper}>
                <Table
                    className={clsx(classes.table)}
                    aria-label='simple table'
                >
                    <TableHead>
                        <TableRow>
                            <TableCell>Role</TableCell>
                            <TableCell align='right'>Permissions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {Object.entries(permissionMappings).map(([role, scopes]) => (
                            <TableRow key={role}>
                                <TableCell component='th' scope='row'>
                                    {role}
                                </TableCell>
                                <TableCell align='right'>
                                    {<PermissionsTree scopes={scopes} appMappings={appMappings} />}
                                </TableCell>
                            </TableRow >
                        ))
}
                    </TableBody >
                </Table >
            </TableContainer > */}
        </ContentBase>
    );
}
