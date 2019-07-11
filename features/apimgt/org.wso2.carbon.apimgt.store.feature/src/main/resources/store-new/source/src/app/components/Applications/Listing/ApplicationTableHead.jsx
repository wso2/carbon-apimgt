import React from 'react';
import PropTypes from 'prop-types';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import TableSortLabel from '@material-ui/core/TableSortLabel';
/**
 * @inheritdoc
 * @class ApplicationTableHead
 * @extends {Component}
 */
const applicationTableHead = (props) => {
    const createSortHandler = property => (event) => {
        props.onRequestSort(event, property);
    };
    const columnData = [
        {
            id: 'name',
            numeric: false,
            disablePadding: true,
            label: 'Name',
            sorting: true,
        },
        {
            id: 'throttlingTier',
            numeric: false,
            disablePadding: false,
            label: 'Policy',
            sorting: true,
        },
        {
            id: 'lifeCycleStatus',
            numeric: false,
            disablePadding: false,
            label: 'Workflow Status',
            sorting: true,
        },
        {
            id: 'subscriptions',
            numeric: false,
            disablePadding: false,
            label: 'Subscriptions',
            sorting: true,
        },
        {
            id: 'actions',
            numeric: false,
            disablePadding: false,
            label: 'Actions',
            sorting: false,
        },
    ];
    const { order, orderBy } = props;
    return (
        <TableHead>
            <TableRow>
                {columnData.map((column) => {
                    return (
                        <TableCell
                            key={column.id}
                            numeric={column.numeric}
                            sortDirection={orderBy === column.id ? order : false}
                        >
                            {column.sorting ? (
                                <TableSortLabel
                                    active={orderBy === column.id}
                                    direction={order}
                                    onClick={createSortHandler(column.id)}
                                >
                                    {column.label}
                                </TableSortLabel>
                            ) : (
                                column.label
                            )}
                        </TableCell>
                    );
                })}
            </TableRow>
        </TableHead>
    );
};
applicationTableHead.propTypes = {
    onRequestSort: PropTypes.func.isRequired,
    order: PropTypes.string.isRequired,
    orderBy: PropTypes.string.isRequired,
};
export default applicationTableHead;
