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
import { Link } from 'react-router-dom';
import { injectIntl } from 'react-intl';

import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';

import APITableHeader from './APITableHeader';
import APITableToolBar from './APITableToolBar';

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
 * @class TableView @inheritdoc
 * @extends {React.Component} @inheritdoc
 */
class TableView extends React.Component {
    /**
     *Creates an instance of TableView.
     * @param {Object} props @inheritdoc
     * @memberof TableView
     */
    constructor(props) {
        super(props);
        this.state = {
            order: 'asc',
            orderBy: 'calories',
            selected: [],
            page: 0,
            rowsPerPage: 5,
            isDeleting: false,
        };
        this.isSelected = this.isSelected.bind(this);
        this.handleSelectAnAPI = this.handleSelectAnAPI.bind(this);
        this.handleSelectAllClick = this.handleSelectAllClick.bind(this);
        this.handleDeleteAPIs = this.handleDeleteAPIs.bind(this);
        this.handleChangePage = this.handleChangePage.bind(this);
        this.handleChangeRowsPerPage = this.handleChangeRowsPerPage.bind(this);
    }

    handleRequestSort = (event, property) => {
        const orderBy = property;
        let order = 'desc';

        if (this.state.orderBy === property && this.state.order === 'desc') {
            order = 'asc';
        }

        this.setState({ order, orderBy });
    };

    /**
     * Handle select all api checkbox in header
     *
     * @param {React.SyntheticEvent} event ignored
     * @param {Boolean} checked is the select all checkbox is selected
     * @memberof TableView
     */
    handleSelectAllClick(event, checked) {
        let selected = [];
        if (checked) {
            selected = this.props.apis.list.map(api => api.id);
        }
        this.setState({ selected });
    }

    /**
     *
     * Handle onClick event of checkbox for single API (One row in the APIs listing table view)
     * @param {React.SyntheticEvent} event onClick event
     * @memberof TableView
     */
    handleSelectAnAPI(event) {
        const { id } = event.currentTarget;
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
    }

    /**
     * Provide implementation for Material UI standard TablePagination callback prop
     * Switch to the next/previous page from the current page according to the passed page number
     *
     * @param {React.SyntheticEvent} event
     * @param {Number} page Next/Previous page number
     * @memberof TableView
     */
    handleChangePage(event, page) {
        this.setState({ page });
    }

    /**
     *
     * Provide implementation for Material UI standard TablePagination callback prop
     * Change the number of rows(APIs) shown in single page
     * @param {React.SyntheticEvent} event Synthetic event for number of rows dropdown menu
     * @memberof TableView
     */
    handleChangeRowsPerPage(event) {
        this.setState({ rowsPerPage: event.target.value });
    }

    /**
     *
     * Delete API in `selected` list
     * @memberof TableView
     */
    handleDeleteAPIs() {
        this.setState({ isDeleting: true });
        const { selected } = this.state;
        const { updateAPIsList, intl } = this.props;
        const promisedDeleteAll = selected.map(apiUUID =>
            API.delete(apiUUID)
                .then(() => {
                    updateAPIsList(apiUUID);
                    const index = selected.indexOf(apiUUID);
                    if (index !== -1) selected.splice(index, 1);
                })
                .catch(() => Alert.info(`${intl.formatMessage({
                    id: 'Apis.Listing.TableView.TableView.error.while.deleting.the.api',
                    defaultMessage: 'Error while deleting the',
                })} ${apiUUID}`)));
        Promise.all(promisedDeleteAll).then((response) => {
            Alert.info(`${response.length} + ${intl.formatMessage({
                id: 'Apis.Listing.TableView.TableView.apis.deleted.successfully',
                defaultMessage: 'API(s) deleted successfully!',
            })}`);
            this.setState({ isDeleting: false, selected });
        });
    }

    /**
     *
     * Helper method to check is the given API UUID is in the selected APIs array
     * @param {String} id UUID of the API which needs to be checked for selected
     * @returns {Boolean} is the given API uuid in selected list
     * @memberof TableView
     */
    isSelected(id) {
        const { selected } = this.state;
        return selected.indexOf(id) !== -1;
    }

    /**
     *
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof TableView @inheritdoc
     */
    render() {
        const { classes, apis } = this.props;
        const {
            order, orderBy, selected, rowsPerPage, page, isDeleting,
        } = this.state;
        const emptyRows = rowsPerPage - Math.min(rowsPerPage, apis.list.length - (page * rowsPerPage));

        return (
            <Paper className={classes.root}>
                <APITableToolBar
                    loading={isDeleting}
                    handleDeleteAPIs={this.handleDeleteAPIs}
                    numSelected={selected.length}
                    totalAPIsCount={apis.list.length}
                />
                {/*
                todo: totalAPIsCount verify /apis GET all .count returns the total number of APIs,
                If so use that property ~tmkb
                */}
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
                                    const { id } = api;
                                    const isSelected = this.isSelected(id);
                                    const overviewPath = `apis/${id}/overview`;
                                    return (
                                        <TableRow
                                            hover
                                            role='checkbox'
                                            aria-checked={isSelected}
                                            tabIndex={-1}
                                            key={id}
                                            selected={isSelected}
                                        >
                                            <TableCell onClick={this.handleSelectAnAPI} id={id} padding='checkbox'>
                                                <Checkbox checked={isSelected} />
                                            </TableCell>

                                            <TableCell component='th' scope='row' padding='none'>
                                                <Link to={overviewPath}>{api.name}</Link>
                                            </TableCell>
                                            <TableCell numeric>{api.version}</TableCell>
                                            <TableCell numeric>{api.context}</TableCell>
                                            <TableCell numeric>{Math.floor(Math.random() * 20)}</TableCell>
                                            <TableCell numeric>{api.provider}</TableCell>
                                            <TableCell numeric>{api.lifeCycleStatus}</TableCell>
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
    apis: PropTypes.shape({ list: PropTypes.array, count: PropTypes.number, apiType: PropTypes.string }).isRequired,
    updateAPIsList: PropTypes.func.isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(TableView));
