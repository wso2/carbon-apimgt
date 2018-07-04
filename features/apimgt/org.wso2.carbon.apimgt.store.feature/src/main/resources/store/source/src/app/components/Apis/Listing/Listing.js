/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React from 'react'
import qs from 'qs'

import ApiThumb from './ApiThumb'
import APiTableRow from './ApiTableRow'
import API from '../../../data/api.js'
import Loading from '../../Base/Loading/Loading'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';

import Table, { TableBody, TableCell, TableHead, TableRow, TableSortLabel } from 'material-ui/Table';
import IconButton from 'material-ui/IconButton';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import List from 'material-ui-icons/List';
import GridIcon from 'material-ui-icons/GridOn';

const styles = theme => ({
    rightIcon: {
        marginLeft: theme.spacing.unit
    },
    button: {
        margin: theme.spacing.unit,
        marginBottom: 0
    },
    titleBar: {
        display: 'flex',
        justifyContent: 'space-between',
        borderBottomWidth: '1px',
        borderBottomStyle: 'solid',
        borderColor: theme.palette.text.secondary,
        marginBottom: 20,
    },
    buttonLeft: {
        alignSelf: 'flex-start',
        display: 'flex',
    },
    buttonRight: {
        alignSelf: 'flex-end',
        display: 'flex',
    },
    title: {
        display: 'inline-block',
        marginRight: 50
    },
    addButton: {
        display: 'inline-block',
        marginBottom: 20,
        zIndex: 1,
    },
    popperClose: {
        pointerEvents: 'none',
    },
    ListingWrapper: {
        paddingTop: 10,
    }
});

function getSorting(order, orderBy) {
    return order === 'desc'
        ? (a, b) => (b[orderBy] < a[orderBy] ? -1 : 1)
        : (a, b) => (a[orderBy] < b[orderBy] ? -1 : 1);
}

class EnhancedAPITableHead extends React.Component {
    static propTypes = {
        onRequestSort: PropTypes.func.isRequired,
        order: PropTypes.string.isRequired,
        orderBy: PropTypes.string.isRequired,
    };

    createSortHandler = property => event => {
        this.props.onRequestSort(event, property);
    };

    render() {
        const columnData = [
            { id: 'name', numeric: false, disablePadding: true, label: 'Name' },
            { id: 'version', numeric: false, disablePadding: false, label: 'Version' },
            { id: 'context', numeric: false, disablePadding: false, label: 'Context' },
            { id: 'rating', numeric: false, disablePadding: false, label: 'Rating' },
        ];
        const { order, orderBy } = this.props;

        return (
            <TableHead>
                <TableRow>
                    {columnData.map(column => {
                        return (
                            <TableCell
                                key={column.id}
                                numeric={column.numeric}
                                sortDirection={orderBy === column.id ? order : false}
                            >
                                <TableSortLabel
                                    active={orderBy === column.id}
                                    direction={order}
                                    onClick={this.createSortHandler(column.id)}
                                >
                                    {column.label}
                                </TableSortLabel>
                            </TableCell>
                        );
                    }, this)}
                </TableRow>
            </TableHead>
        );
    }
}

class Listing extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            listType: 'grid',
            apis: null,
            value: 1,
            order: 'asc',
            orderBy: 'name'
        };
    }

    setListType = (value) => {
        this.setState({ listType: value });
    }

    componentDidMount() {
        let api = new API();
        let promised_apis = api.getAllAPIs();
        promised_apis.then((response) => {
            this.setState({ apis: response.obj });
        }).catch(error => {
            let status = error.status;
            if (status === 404) {
                this.setState({ notFound: true });
            } else if (status === 401) {
                this.setState({ isAuthorize: false });
                let params = qs.stringify({ reference: this.props.location.pathname });
                this.props.history.push({ pathname: "/login", search: params });
            }
        });
    }

    handleRequestSort = (event, property) => {
        const orderBy = property;
        let order = 'desc';

        if (this.state.orderBy === property && this.state.order === 'desc') {
            order = 'asc';
        }

        this.setState({ order, orderBy });
    };

    render() {
        if (this.state.notFound) {
            return <ResourceNotFound />
        }

        const classes = this.props.classes;
        const { order, orderBy } = this.state;
        return (
            <div>
                <Grid container spacing={0} justify="center">
                    <Grid item xs={12} className={classes.titleBar}>
                        <div className={classes.buttonLeft}>
                            <div className={classes.title}>
                                <Typography variant="display1" gutterBottom>
                                    APIs
                                </Typography>
                            </div>
                        </div>
                        <div className={classes.buttonRight}>
                            <IconButton className={classes.button} onClick={() => this.setListType('list')}>
                                <List />
                            </IconButton>
                            <IconButton className={classes.button} onClick={() => this.setListType('grid')}>
                                <GridIcon />
                            </IconButton>
                        </div>
                    </Grid>

                    <Grid item xs={12}>
                        {
                            this.state.apis ?
                                this.state.listType === "grid" ?
                                    <Grid container className={classes.ListingWrapper} >
                                        {this.state.apis.list.map(api => <ApiThumb api={api} key={api.id} />)}
                                    </Grid>
                                    :
                                    <Table>
                                        <EnhancedAPITableHead order={order} orderBy={orderBy}
                                            onRequestSort={this.handleRequestSort} />
                                        <TableBody>
                                            {this.state.apis.list
                                                .sort(getSorting(order, orderBy))
                                                .map(api => <APiTableRow api={api} key={api.id} />)}
                                        </TableBody>
                                    </Table>
                                :
                                <Loading />
                        }
                    </Grid>
                </Grid>
            </div>
        );
    }
}

Listing.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};
export default withStyles(styles, { withTheme: true })(Listing);
