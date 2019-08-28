/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { Component } from 'react';

import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Table from '@material-ui/core/Table';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import TableSortLabel from '@material-ui/core/TableSortLabel';
import qs from 'qs';
import TablePagination from '@material-ui/core/TablePagination';
import CustomIcon from '../../Shared/CustomIcon';
import InlineMessage from '../../Shared/InlineMessage';
import Alert from '../../Base/Alert';
import AppsTableContent from './AppsTableContent';
import Loading from '../../Base/Loading/Loading';
import Application from '../../../data/Application';
import NewApp from '../Create/NewApp';
/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    card: {
        minWidth: 275,
        paddingBottom: 20,
    },
    bullet: {
        display: 'inline-block',
        margin: '0 2px',
        transform: 'scale(0.8)',
    },
    pos: {
        marginBottom: 12,
        color: theme.palette.text.secondary,
    },
    createAppWrapper: {
        textDecoration: 'none',
    },
    divider: {
        marginBottom: 20,
    },
    createButton: {
        textDecoration: 'none',
        display: 'inline-block',
        marginLeft: 20,
        alignSelf: 'flex-start',
    },
    titleWrapper: {
        display: 'flex',
    },
    // New styles
    // //////////////////////
    content: {
        flexGrow: 1,
    },
    root: {
        height: 70,
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        display: 'flex',
        alignItems: 'center',
    },
    mainIconWrapper: {
        paddingTop: 13,
        paddingLeft: 35,
        paddingRight: 20,
    },
    mainTitle: {
        paddingTop: 10,
    },
    createLinkWrapper: {
        paddingLeft: theme.spacing.unit * 2,
    },
    appContent: {
        paddingLeft: theme.spacing.unit * 4,
        paddingTop: theme.spacing.unit,
        width: theme.custom.contentAreaWidth,
    },
});
/**
 *
 *
 * @class ApplicationTableHead
 * @extends {Component}
 */
class ApplicationTableHead extends Component {
    static propTypes = {
        onRequestSort: PropTypes.func.isRequired,
        order: PropTypes.string.isRequired,
        orderBy: PropTypes.string.isRequired,
    };

    createSortHandler = property => (event) => {
        this.props.onRequestSort(event, property);
    };

    render() {
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
        const { order, orderBy } = this.props;
        return (
            <TableHead>
                <TableRow>
                    {columnData.map((column) => {
                        return (
                            <TableCell key={column.id} numeric={column.numeric} sortDirection={orderBy === column.id ? order : false}>
                                {column.sorting ? (
                                    <TableSortLabel active={orderBy === column.id} direction={order} onClick={this.createSortHandler(column.id)}>
                                        {column.label}
                                    </TableSortLabel>
                                ) : (
                                    column.label
                                )}
                            </TableCell>
                        );
                    }, this)}
                </TableRow>
            </TableHead>
        );
    }
}
/**
 *
 *
 * @class Listing
 * @extends {Component}
 */
class Listing extends Component {
    constructor(props) {
        super(props);
        this.state = {
            order: 'asc',
            orderBy: 'name',
            selected: [],
            data: null,
            alertMessage: null,
            page: 0,
            rowsPerPage: 10,
        };
        this.handleAppDelete = this.handleAppDelete.bind(this);
    }

    /**
     *
     *
     * @memberof Listing
     */
    componentDidMount() {
        this.updateApps();
    }

    /**
     *
     *
     * @memberof Listing
     */
    updateApps = () => {
        const promised_applications = Application.all();
        promised_applications
            .then((applications) => {
                const apps = new Map(); // Applications list put into map, to make it efficient when deleting apps (referring back to an App)
                applications.list.map(app => apps.set(app.applicationId, app)); // Store application against its UUID
                this.setState({ data: apps });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    this.setState({ isAuthorize: false });
                    const params = qs.stringify({ reference: this.props.location.pathname });
                    this.props.history.push({ pathname: '/login', search: params });
                }
            });
    };

    /**
     *
     *
     * @memberof Listing
     */
    handleRequestSort = (event, property) => {
        const orderBy = property;
        let order = 'desc';
        if (this.state.orderBy === property && this.state.order === 'desc') {
            order = 'asc';
        }
        this.setState({ order, orderBy });
    };

    /**
     *
     *
     * @param {*} event
     * @memberof Listing
     */
    handleAppDelete(event) {
        const id = event.currentTarget.getAttribute('data-appId');
        const app = this.state.data.get(id);
        app.deleting = true;
        this.state.data.set(id, app);
        this.setState({ data: this.state.data });

        const message = 'Application: ' + app.name + ' deleted successfully!';
        const promised_delete = Application.deleteApp(id);
        promised_delete.then((ok) => {
            if (ok) {
                const { data } = this.state;
                data.delete(id);
                this.setState({ data: this.state.data, alertMessage: message });
            }
        });
    }

    /**
     *
     *
     * @memberof Listing
     */
    handleChangePage = (event, page) => {
        this.setState({ page });
    };

    /**
     *
     *
     * @memberof Listing
     */
    handleChangeRowsPerPage = (event) => {
        this.setState({ rowsPerPage: event.target.value });
    };

    /**
     *
     *
     * @returns
     * @memberof Listing
     */
    render() {
        const {
            data, order, orderBy, alertMessage, rowsPerPage, page,
        } = this.state;
        if (!data) {
            return <Loading />;
        }
        const { classes, theme } = this.props;
        const bull = <span className={classes.bullet}>•</span>;
        const strokeColorMain = theme.palette.getContrastText(theme.palette.background.paper);
        return (
            <main className={classes.content}>
                <div className={classes.root}>
                    <div className={classes.mainIconWrapper}>
                        <CustomIcon strokeColor={strokeColorMain} width={42} height={42} icon='applications' />
                    </div>
                    <div className={classes.mainTitleWrapper}>
                        <Typography variant='display1' className={classes.mainTitle}>
                            Applications
                        </Typography>
                        {this.state.data && (
                            <Typography variant='caption' gutterBottom align='left'>
                                {this.state.data.count === 0 ? (
                                    <React.Fragment>No Applications created</React.Fragment>
                                ) : (
                                    <React.Fragment>
                                        Displaying
                                        {' '}
                                        {this.state.data.count}
                                        {' '}
                                        {this.state.data.count === 1 ? 'Application' : 'Applications'}
                                    </React.Fragment>
                                )}
                            </Typography>
                        )}
                    </div>
                    {data.size !== 0 && (
                        <div className={classes.createLinkWrapper}>
                            <NewApp updateApps={this.updateApps} />
                        </div>
                    )}
                </div>
                {alertMessage && <Alert message={alertMessage} />}
                <Grid container spacing={0} justify='center'>
                    <Grid item xs={12}>
                        {data.size > 0 ? (
                            <div className={classes.appContent}>
                                <Typography variant='caption' gutterBottom align='left'>
                                    An application is a logical collection of APIs. Applications allow you to use a single access token to invoke a collection of APIs and to subscribe to one API multiple times with different SLA levels. The DefaultApplication is pre-created and allows unlimited access by default.
                                </Typography>
                                <Table>
                                    <ApplicationTableHead order={order} orderBy={orderBy} onRequestSort={this.handleRequestSort} />
                                    <AppsTableContent handleAppDelete={this.handleAppDelete} apps={data} page={page} rowsPerPage={rowsPerPage} order={order} orderBy={orderBy} />
                                </Table>
                                <TablePagination
                                    component='div'
                                    count={data.size}
                                    rowsPerPage={rowsPerPage}
                                    rowsPerPageOptions={[5, 10, 15]}
                                    labelRowsPerPage='Show'
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
                            </div>
                        ) : (
                            <div className={classes.appContent}>
                                <InlineMessage type='info' style={{ width: 1000, padding: theme.spacing.unit * 2 }}>
                                    <Typography variant='headline' component='h3'>
                                        Create New Application
                                    </Typography>
                                    <Typography component='p'>An application is a logical collection of APIs. Applications allow you to use a single access token to invoke a collection of APIs and to subscribe to one API multiple times with different SLA levels. The DefaultApplication is pre-created and allows unlimited access by default.</Typography>
                                    <NewApp updateApps={this.updateApps} />
                                </InlineMessage>
                            </div>
                        )}
                    </Grid>
                </Grid>
            </main>
        );
    }
}
Listing.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(Listing);
