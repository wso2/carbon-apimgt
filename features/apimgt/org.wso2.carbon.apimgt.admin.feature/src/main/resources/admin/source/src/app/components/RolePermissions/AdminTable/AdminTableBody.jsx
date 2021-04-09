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
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Checkbox from '@material-ui/core/Checkbox';

import { useTableContext } from './AdminTableContext';

/**
 *
 * @param {*} role
 * @returns
 */
function extractRoleName(role) {
    return typeof role[0] === 'string'
        ? role[0].toUpperCase()
        : role[0].props.children[0].toUpperCase();
}

/**
 *
 *
 * @param {*} a
 * @param {*} b
 * @param {*} orderBy
 * @returns
 */
function descendingComparator(a, b, orderBy) {
    if (orderBy === 'role') {
        const roleA = extractRoleName(a);
        const roleB = extractRoleName(b);
        if (roleB < roleA) {
            return -1;
        }
        if (roleB > roleA) {
            return 1;
        }
        return 0;
    }
    return 0;
}
/**
 *
 *
 * @param {*} order
 * @param {*} orderBy
 * @returns
 */
function getComparator(order, orderBy) {
    return order === 'desc'
        ? (a, b) => descendingComparator(a, b, orderBy)
        : (a, b) => -descendingComparator(a, b, orderBy);
}


/**
 *
 *
 * @param {*} array
 * @param {*} comparator
 * @returns
 */
function stableSort(array, comparator) {
    const stabilizedThis = array.map((el, index) => [el, index]);
    stabilizedThis.sort((a, b) => {
        const order = comparator(a[0], b[0]);
        if (order !== 0) return order;
        return a[1] - b[1];
    });
    return stabilizedThis.map((el) => el[0]);
}


/**
 *
 *
 * @param {*} props
 * @returns
 */
function AdminTableBody(props) {
    const {
        selected, setSelected, rowsPerPage, page, order, orderBy, multiSelect,
    } = useTableContext();
    const { rows } = props;

    const handleClick = (event, name) => {
        const selectedIndex = selected.indexOf(name);
        let newSelected = [];

        if (selectedIndex === -1) {
            newSelected = newSelected.concat(selected, name);
        } else if (selectedIndex === 0) {
            newSelected = newSelected.concat(selected.slice(1));
        } else if (selectedIndex === selected.length - 1) {
            newSelected = newSelected.concat(selected.slice(0, -1));
        } else if (selectedIndex > 0) {
            newSelected = newSelected.concat(
                selected.slice(0, selectedIndex),
                selected.slice(selectedIndex + 1),
            );
        }

        setSelected(newSelected);
    };
    const isSelected = (name) => selected.indexOf(name) !== -1;
    const emptyRows = rowsPerPage - Math.min(rowsPerPage, rows.length - page * rowsPerPage);

    return (
        <TableBody>
            {stableSort(rows, getComparator(order, orderBy))
                .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                .map((row, rowIndex) => {
                    const isItemSelected = isSelected(row.name);
                    const labelId = `enhanced-table-checkbox-${rowIndex}`;
                    return (
                        <TableRow
                            hover
                            onClick={(event) => handleClick(event, rowIndex)}
                            role='checkbox'
                            aria-checked={isItemSelected}
                            tabIndex={-1}
                            key={rowIndex} // eslint-disable-line react/no-array-index-key
                            selected={isItemSelected}

                        >
                            {multiSelect && (
                                <TableCell padding='checkbox'>
                                    <Checkbox
                                        checked={isItemSelected}
                                        inputProps={{ 'aria-labelledby': labelId }}
                                    />
                                </TableCell>
                            )}
                            {row.map((column, index) => (
                                <TableCell
                                    component='th'
                                    id={labelId}
                                    scope='row'
                                    padding={multiSelect ? 'none' : 'default'}
                                    align={index === 0 ? 'left' : 'right'}
                                >
                                    {column}
                                </TableCell>
                            ))}
                        </TableRow>
                    );
                })}
            {emptyRows > 0 && (
                <TableRow style={{ height: 60 * emptyRows }}>
                    <TableCell colSpan={6} />
                </TableRow>
            )}
        </TableBody>
    );
}

export default AdminTableBody;
