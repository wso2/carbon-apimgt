import React from 'react';
import PropTypes from 'prop-types';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import TableSortLabel from '@material-ui/core/TableSortLabel';
import Checkbox from '@material-ui/core/Checkbox';
import Tooltip from '@material-ui/core/Tooltip';
import { FormattedMessage } from 'react-intl';

const rows = [
    {
        id: 'name',
        numeric: false, // TODO: Remove this flag , as this is no longer supported in material-ui 4.x
        disablePadding: true,
        label: <FormattedMessage id='name' defaultMessage='Name' />,
    },
    {
        id: 'version',
        numeric: true, // TODO: Remove this flag , as this is no longer supported in material-ui 4.x
        disablePadding: false,
        label: <FormattedMessage id='version' defaultMessage='Version' />,
    },
    {
        id: 'context',
        numeric: true, // TODO: Remove this flag , as this is no longer supported in material-ui 4.x
        disablePadding: false,
        label: <FormattedMessage id='context' defaultMessage='Context' />,
    },
    {
        id: 'subscriptions',
        numeric: true, // TODO: Remove this flag , as this is no longer supported in material-ui 4.x
        disablePadding: false,
        label: <FormattedMessage id='subscriptions' defaultMessage='Subscriptions' />,
    },
    {
        id: 'provider',
        numeric: true, // TODO: Remove this flag , as this is no longer supported in material-ui 4.x
        disablePadding: false,
        label: <FormattedMessage id='provider' defaultMessage='Provider' />,
    },
    {
        id: 'status',
        numeric: true, // TODO: Remove this flag , as this is no longer supported in material-ui 4.x
        disablePadding: false,
        label: <FormattedMessage id='status' defaultMessage='Status' />,
    },
];


/**
 * API Listing table view header of the table
 *
 * @export
 * @class APITableHeader
 * @extends {React.Component}
 */
export default class APITableHeader extends React.Component {
    constructor(props) {
        super(props);
        this.createSortHandler = this.createSortHandler.bind(this);
    }

    createSortHandler(property) {
        return (event) => {
            this.props.onRequestSort(event, property);
        };
    }


    /**
     *
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof APITableHeader
     */
    render() {
        const {
            onSelectAllClick, order, orderBy, numSelected, rowCount,
        } = this.props;

        return (
            <TableHead>
                <TableRow>
                    <TableCell padding='checkbox'>
                        <Checkbox
                            indeterminate={numSelected > 0 && numSelected < rowCount}
                            checked={numSelected === rowCount}
                            onChange={onSelectAllClick}
                        />
                    </TableCell>
                    {rows.map((row) => {
                        return (
                            <TableCell
                                key={row.id}
                                align={row.numeric && 'right'}
                                padding={row.disablePadding ? 'none' : 'default'}
                                sortDirection={orderBy === row.id ? order : false}
                            >
                                <Tooltip
                                    title='Sort'
                                    placement={row.numeric ? 'bottom-end' : 'bottom-start'}
                                    enterDelay={300}
                                >
                                    <TableSortLabel
                                        active={orderBy === row.id}
                                        direction={order}
                                        onClick={this.createSortHandler(row.id)}
                                    >
                                        {row.label}
                                    </TableSortLabel>
                                </Tooltip>
                            </TableCell>
                        );
                    }, this)}
                </TableRow>
            </TableHead>
        );
    }
}

APITableHeader.propTypes = {
    numSelected: PropTypes.number.isRequired,
    onRequestSort: PropTypes.func.isRequired,
    onSelectAllClick: PropTypes.func.isRequired,
    order: PropTypes.string.isRequired,
    orderBy: PropTypes.string.isRequired,
    rowCount: PropTypes.number.isRequired,
};
