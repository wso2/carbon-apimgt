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
import qs from 'qs';
import TablePagination from '@material-ui/core/TablePagination';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import Alert from 'AppComponents/Base/Alert';
import Loading from 'AppComponents/Base/Loading/Loading';
import Application from 'AppData/Application';
import NewApp from 'AppComponents/Applications/Create/NewApp';
import GenericDisplayDialog from 'AppComponents/Shared/GenericDisplayDialog';
import AppsTableContent from './AppsTableContent';
import ApplicationTableHead from './ApplicationTableHead';

/**
 *
 * @inheritdoc
 * @param {*} theme theme object
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
    dialogContainer: {
        width: 1000,
        padding: theme.spacing.unit * 2,
    },
});

/**
 * @inheritdoc
 * @class Listing
 * @extends {Component}
 */
class Listing extends Component {
    /**
     *
     * @param {any} props properties
     */
    constructor(props) {
        super(props);
        this.state = {
            order: 'asc',
            orderBy: 'name',
            data: null,
            alertMessage: null,
            page: 0,
            rowsPerPage: 10,
            open: false,
        };
        this.handleAppDelete = this.handleAppDelete.bind(this);
    }

    /**
     * @memberof Listing
     */
    componentDidMount() {
        this.updateApps();
    }

    /**
     * @memberof Listing
     */
    updateApps = () => {
        const promisedApplications = Application.all();
        promisedApplications
            .then((applications) => {
                // Applications list put into map, to make it efficient when deleting apps (referring back to an App)
                const apps = new Map();
                applications.list.map(app => apps.set(app.applicationId, app)); // Store application against its UUID
                this.setState({ data: apps });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const { status } = error;
                if (status === 404) {
                    // eslint-disable-next-line react/no-unused-state
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    window.location = '/store-new/services/configs';
                }
            });
    };

    /**
     * @param{*} event event
     * @param{*} property sorting method
     */
    handleRequestSort = (event, property) => {
        const { orderBy, order } = this.state;
        let currentOrder = 'desc';
        if (orderBy === property && order === 'desc') {
            currentOrder = 'asc';
        }
        this.setState({ order: currentOrder, orderBy });
    };


    /**
     *
     * @param {*} event event
     * @param {*} page page
     * @memberof Listing
     */
    handleChangePage = (event, page) => {
        this.setState({ page });
    };

    /**
     *
     * @inheritdoc
     * @memberof Listing
     */
    handleChangeRowsPerPage = (event) => {
        this.setState({ rowsPerPage: event.target.value });
    };

    /**
     * @memberof NewApp
     */
    handleClose = () => {
        this.setState({ open: false });
    };

    /**
     * @memberof Listing
     */
    handleClickOpen = () => {
        this.setState({ open: true });
    };

    /**
     * @param {*} event event
     * @memberof Listing
     */
    handleAppDelete(event) {
        const { data } = this.state;
        const id = event.currentTarget.getAttribute('data-appId');
        const newData = new Map([...data]);
        const app = newData.get(id);
        app.deleting = true;
        this.setState({ data: newData });
        const message = 'Application: ' + app.name + ' deleted successfully!';
        const promisedDelete = Application.deleteApp(id);
        promisedDelete.then((ok) => {
            if (ok) {
                newData.delete(id);
                this.setState({ data: newData, alertMessage: message });
            }
        });
    }

    /**
     * @inheritdoc
     */
    render() {
        const {
            data, order, orderBy, alertMessage, rowsPerPage, page, open,
        } = this.state;
        if (!data) {
            return <Loading />;
        }
        const { classes, theme } = this.props;
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
                        {data && (
                            <Typography variant='caption' gutterBottom align='left'>
                                {data.count === 0 ? (
                                    <React.Fragment>No Applications created</React.Fragment>
                                ) : (
                                    <React.Fragment>
                                            Displaying
                                        {' '}
                                        {data.count}
                                        {' '}
                                        {data.count === 1 ? 'Application' : 'Applications'}
                                    </React.Fragment>
                                )}
                            </Typography>
                        )}
                    </div>
                    {(data.size !== 0 || open) && (
                        <div className={classes.createLinkWrapper}>
                            <NewApp
                                updateApps={this.updateApps}
                                open={open}
                                handleClickOpen={this.handleClickOpen}
                                handleClose={this.handleClose}
                            />
                        </div>
                    )}
                </div>
                {alertMessage && <Alert message={alertMessage} />}
                <Grid container spacing={0} justify='center'>
                    <Grid item xs={12}>
                        {data.size > 0 ? (
                            <div className={classes.appContent}>
                                <Typography variant='caption' gutterBottom align='left'>
                                    An application is a logical collection of APIs. Applications allow you to use a
                                    single access token to invoke a collection of APIs and to subscribe to one
                                    API multiple times with different SLA levels. The DefaultApplication is
                                    pre-created and allows unlimited access by default.
                                </Typography>
                                <Table>
                                    <ApplicationTableHead
                                        order={order}
                                        orderBy={orderBy}
                                        onRequestSort={this.handleRequestSort}
                                    />
                                    <AppsTableContent
                                        handleAppDelete={this.handleAppDelete}
                                        apps={data}
                                        page={page}
                                        rowsPerPage={rowsPerPage}
                                        order={order}
                                        orderBy={orderBy}
                                    />
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
                            <GenericDisplayDialog
                                classes={classes}
                                handleClick={this.handleClickOpen}
                                heading='Create New Application'
                                caption={`An application is a logical collection of APIs. Applications
                                          allow you to use a single access token to invoke a collection
                                          of APIs and to subscribe to one API multiple times with different
                                          SLA levels. The DefaultApplication is pre-created and allows unlimited
                                          access by default.`}
                                buttonText='ADD NEW APPLICATION'
                            />
                        )}
                    </Grid>
                </Grid>
            </main>
        );
    }
}
Listing.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(Listing);
