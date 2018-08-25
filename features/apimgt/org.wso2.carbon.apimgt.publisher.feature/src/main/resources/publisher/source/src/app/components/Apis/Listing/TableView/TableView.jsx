import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TablePagination from '@material-ui/core/TablePagination';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import Checkbox from '@material-ui/core/Checkbox';

import APITableHeader from './APITableHeader';
// import APITableToolBar from './APITableToolBar';

const styles = theme => ({
    root: {
        width: '100%',
        marginTop: theme.spacing.unit * 3,
    },
    table: {
        minWidth: 1020,
    },
    tableWrapper: {
        overflowX: 'auto',
    },
});

/* let counter = 0;
function createData(name, calories, fat, carbs, protein) {
    counter += 1;
    return {
        id: counter,
        name,
        calories,
        fat,
        carbs,
        protein,
    };
} */

/* function desc(a, b, orderBy) {
    if (b[orderBy] < a[orderBy]) {
        return -1;
    }
    if (b[orderBy] > a[orderBy]) {
        return 1;
    }
    return 0;
} */

/* function getSorting(order, orderBy) {
    return order === 'desc' ? (a, b) => desc(a, b, orderBy) : (a, b) => -desc(a, b, orderBy);
} */

/**
 * Table view of the API listing
 *
 * @class TableView
 * @extends {React.Component}
 */
class TableView extends React.Component {
    state = {
        order: 'asc',
        orderBy: 'calories',
        selected: [],
        page: 0,
        rowsPerPage: 5,
    };

    handleRequestSort = (event, property) => {
        const orderBy = property;
        let order = 'desc';

        if (this.state.orderBy === property && this.state.order === 'desc') {
            order = 'asc';
        }

        this.setState({ order, orderBy });
    };

    handleSelectAllClick = (event, checked) => {
        if (checked) {
            this.setState(state => ({ selected: state.data.map(n => n.id) }));
            return;
        }
        this.setState({ selected: [] });
    };

    handleClick = (event, id) => {
        const { selected } = this.state;
        const selectedIndex = selected.indexOf(id);
        let newSelected = [];

        if (selectedIndex === -1) {
            newSelected = newSelected.concat(selected, id);
        } else if (selectedIndex === 0) {
            newSelected = newSelected.concat(selected.slice(1));
        } else if (selectedIndex === selected.length - 1) {
            newSelected = newSelected.concat(selected.slice(0, -1));
        } else if (selectedIndex > 0) {
            newSelected = newSelected.concat(selected.slice(0, selectedIndex), selected.slice(selectedIndex + 1));
        }

        this.setState({ selected: newSelected });
    };

    handleChangePage = (event, page) => {
        this.setState({ page });
    };

    handleChangeRowsPerPage = (event) => {
        this.setState({ rowsPerPage: event.target.value });
    };

    isSelected = id => this.state.selected.indexOf(id) !== -1;

    /**
     *
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof TableView @inheritdoc
     */
    render() {
        const { classes, apis } = this.props;
        const {
            order, orderBy, selected, rowsPerPage, page,
        } = this.state;
        const emptyRows = rowsPerPage - Math.min(rowsPerPage, apis.list.length - (page * rowsPerPage));

        return (
            <Paper className={classes.root}>
                {/* <APITableToolBar numSelected={selected.length} /> */}
                <div className={classes.tableWrapper}>
                    <Table className={classes.table} aria-labelledby='tableTitle'>
                        <APITableHeader
                            numSelected={selected.length}
                            order={order}
                            orderBy={orderBy}
                            onSelectAllClick={this.handleSelectAllClick}
                            onRequestSort={this.handleRequestSort}
                            rowCount={apis.list.length}
                        />
                        <TableBody>
                            {apis.list
                                // .sort(getSorting(order, orderBy))
                                .slice(page * rowsPerPage, (page * rowsPerPage) + rowsPerPage)
                                .map((api) => {
                                    const isSelected = this.isSelected(api.id);
                                    return (
                                        <TableRow
                                            hover
                                            // onClick={event => this.handleClick(event, n.id)}
                                            role='checkbox'
                                            aria-checked={isSelected}
                                            tabIndex={-1}
                                            key={api.id}
                                            selected={isSelected}
                                        >
                                            <TableCell padding='checkbox'>
                                                <Checkbox checked={isSelected} />
                                            </TableCell>
                                            <TableCell component='th' scope='row' padding='none'>
                                                {api.name}
                                            </TableCell>
                                            <TableCell numeric>{api.version}</TableCell>
                                            <TableCell numeric>{api.context}</TableCell>
                                            <TableCell numeric>{Math.floor(Math.random() * 20)}</TableCell>
                                            <TableCell numeric>{api.provider}</TableCell>
                                        </TableRow>
                                    );
                                })}
                            {emptyRows > 0 && (
                                <TableRow style={{ height: 49 * emptyRows }}>
                                    <TableCell colSpan={6} />
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </div>
                <TablePagination
                    component='div'
                    count={apis.list.length}
                    rowsPerPage={rowsPerPage}
                    page={page}
                    backIconButtonProps={{
                        'aria-label': 'Previous Page',
                    }}
                    nextIconButtonProps={{
                        'aria-label': 'Next Page',
                    }}
                    onChangePage={this.handleChangePage}
                    onChangeRowsPerPage={this.handleChangeRowsPerPage}
                />
            </Paper>
        );
    }
}

TableView.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    apis: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(TableView);
