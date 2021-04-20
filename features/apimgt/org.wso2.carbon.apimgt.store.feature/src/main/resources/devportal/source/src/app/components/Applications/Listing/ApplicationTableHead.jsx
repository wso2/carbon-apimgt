import React from 'react';
import PropTypes from 'prop-types';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import TableSortLabel from '@material-ui/core/TableSortLabel';
import { FormattedMessage } from 'react-intl';

/**
 * @inheritdoc
 * @class ApplicationTableHead
 * @extends {Component}
 */
const applicationTableHead = (props) => {
    const createSortHandler = (property) => (event) => {
        props.onRequestSort(event, property);
    };
    const columnData = [
        {
            id: 'name',
            numeric: false,
            disablePadding: true,
            label: (<FormattedMessage
                id='Applications.Listing.ApplicationTableHead.name'
                defaultMessage='Name'
            />),
            sorting: true,
        },
        {
            id: 'owner',
            numeric: false,
            disablePadding: false,
            label: (<FormattedMessage
                id='Applications.Listing.ApplicationTableHead.owner'
                defaultMessage='Owner'
            />),
            sorting: true,
        },
        {
            id: 'throttlingTier',
            numeric: false,
            disablePadding: false,
            label: (<FormattedMessage
                id='Applications.Listing.ApplicationTableHead.policy'
                defaultMessage='Policy'
            />),
            sorting: true,
        },
        {
            id: 'workflowStatus',
            numeric: false,
            disablePadding: false,
            label: (<FormattedMessage
                id='Applications.Listing.ApplicationTableHead.workflow.status'
                defaultMessage='Workflow Status'
            />),
            sorting: false,
        },
        {
            id: 'subscriptions',
            numeric: false,
            disablePadding: false,
            label: (<FormattedMessage
                id='Applications.Listing.ApplicationTableHead.subscriptions'
                defaultMessage='Subscriptions'
            />),
            sorting: false,
        },
        {
            id: 'actions',
            numeric: false,
            disablePadding: false,
            label: (<FormattedMessage
                id='Applications.Listing.ApplicationTableHead.actions'
                defaultMessage='Actions'
            />),
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
                            align='left'
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
