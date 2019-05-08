import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TablePagination from '@material-ui/core/TablePagination';
import TableRow from '@material-ui/core/TableRow';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import Checkbox from '@material-ui/core/Checkbox';
import { Link } from 'react-router-dom';

import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';

import DocTableHeader from './DocTableHeader';
import DocRow from './DocRow';
import DocTableToolBar from './DocTableToolBar';

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

function desc(a, b, orderBy) {
    if (b[orderBy] < a[orderBy]) {
        return -1;
    }
    if (b[orderBy] > a[orderBy]) {
        return 1;
    }
    return 0;
}

function getSorting(order, orderBy) {
    return order === 'desc' ? (a, b) => desc(a, b, orderBy) : (a, b) => -desc(a, b, orderBy);
}

/**
 * Table view of the Document listing
 * @class DocTableView @inheritdoc
 * @extends {React.Component} @inheritdoc
 */
class DocTableView extends React.Component {
    /**
     *Creates an instance of TableView.
     * @param {Object} props @inheritdoc
     * @memberof DocTableView
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
        this.handleSelectADoc = this.handleSelectADoc.bind(this);
        this.handleSelectAllClick = this.handleSelectAllClick.bind(this);
        this.handleChangePage = this.handleChangePage.bind(this);
        this.handleChangeRowsPerPage = this.handleChangeRowsPerPage.bind(this);
        this.handleRequestSort = this.handleRequestSort.bind(this);
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
     * Handle select all doc checkbox in header
     *
     * @param {React.SyntheticEvent} event ignored
     * @param {Boolean} checked is the select all checkbox is selected
     * @memberof DocTableView
     */
    handleSelectAllClick(event, checked) {
        let selected = [];
        if (checked) {
            selected = this.props.docs.map(doc => doc.documentId);
        }
        this.setState({ selected });
    }

    /**
     *
     * Handle onClick event of checkbox for single Doc (One row in the Doc listing table view)
     * @param {React.SyntheticEvent} event onClick event
     * @memberof DocTableView
     */
    handleSelectADoc(event) {
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
     * @memberof DocTableView
     */
    handleChangePage(event, page) {
        this.setState({ page });
    }

    /**
     *
     * Provide implementation for Material UI standard TablePagination callback prop
     * Change the number of rows(Docs) shown in single page
     * @param {React.SyntheticEvent} event Synthetic event for number of rows dropdown menu
     * @memberof DocTableView
     */
    handleChangeRowsPerPage(event) {
        this.setState({ rowsPerPage: event.target.value });
    }

    /**
     *
     * Delete Doc in `selected` list
     * @memberof DocTableView
     */
    handleDeleteDocs() {
        this.setState({ isDeleting: true });
        const { selected } = this.state;
        const { updateAPIsList } = this.props;
        const promisedDeleteAll = selected.map(apiUUID =>
            API.delete(apiUUID)
                .then(() => {
                    updateAPIsList(apiUUID);
                    const index = selected.indexOf(apiUUID);
                    if (index !== -1) selected.splice(index, 1);
                })
                .catch(() => Alert.info(`Error while deleting the ${apiUUID}`)));

        Promise.all(promisedDeleteAll).then((response) => {
            Alert.info(`${response.length} API(s) deleted successfully!`);
            this.setState({ isDeleting: false, selected });
        });
    }

    /**
     *
     * Helper method to check is the given Doc UUID is in the selected Docs array
     * @param {String} id UUID of the Doc which needs to be checked for selected
     * @returns {Boolean} is the given Doc uuid in selected list
     * @memberof DocTableView
     */
    isSelected(id) {
        const { selected } = this.state;
        return selected.indexOf(id) !== -1;
    }

    /**
     *
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof DocTableView @inheritdoc
     */
    render() {
        const { classes, docs, api } = this.props;
        const {
            order, orderBy, selected, rowsPerPage, page, isDeleting,
        } = this.state;
        const emptyRows = rowsPerPage - Math.min(rowsPerPage, docs.length - (page * rowsPerPage));

        return (
            <div className={classes.root}>
                <Paper>
                    <DocTableToolBar
                        loading={isDeleting}
                        handleDeleteDocs={this.handleDeleteDocs}
                        numSelected={selected.length}
                        totalDocCount={docs.length}
                    />
                    <div className={classes.tableWrapper}>
                        <Table className={classes.table} aria-labelledby='tableTitle'>
                            <DocTableHeader
                                numSelected={selected.length}
                                order={order}
                                orderBy={orderBy}
                                onSelectAllClick={this.handleSelectAllClick}
                                onRequestSort={this.handleRequestSort}
                                rowCount={docs.length}
                            />
                            <TableBody>
                                {docs
                                    .sort(getSorting(order, orderBy))
                                    .slice(page * rowsPerPage, (page * rowsPerPage) + rowsPerPage)
                                    .map((doc) => {
                                        const { documentId } = doc;
                                        const isSelected = this.isSelected(documentId);
                                        return (
                                            <DocRow
                                                hover
                                                doc={doc}
                                                api={api}
                                                isSelected={isSelected}
                                                handleSelectADoc={this.handleSelectADoc}
                                            />
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
                        count={docs.length}
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
            </div>
        );
    }
}

DocTableView.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    docs: PropTypes.shape({ list: PropTypes.array }).isRequired,
};

export default withStyles(styles)(DocTableView);