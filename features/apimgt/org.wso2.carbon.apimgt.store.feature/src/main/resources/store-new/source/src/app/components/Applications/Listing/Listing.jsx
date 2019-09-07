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

import React, { Component, useContext } from 'react';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Table from '@material-ui/core/Table';
import TablePagination from '@material-ui/core/TablePagination';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import { FormattedMessage, injectIntl } from 'react-intl';
import Alert from 'AppComponents/Shared/Alert';
import Loading from 'AppComponents/Base/Loading/Loading';
import Application from 'AppData/Application';
import NewApp from 'AppComponents/Applications/Create/NewApp';
import GenericDisplayDialog from 'AppComponents/Shared/GenericDisplayDialog';
import Settings from 'AppComponents/Shared/SettingsContext';
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
    static contextType = Settings;
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
            page: 0,
            rowsPerPage: 10,
            open: false,
            isApplicationSharingEnabled: true,
        };
        this.handleAppDelete = this.handleAppDelete.bind(this);
    }

    /**
     * @memberof Listing
     */
    componentDidMount() {
        this.updateApps();
        this.isApplicationGroupSharingEnabled();
    }

    /**
     * retrieve Settings from the context and check the application sharing enabled
     * @param {*} settingsData required data
     */
    isApplicationGroupSharingEnabled = () => {
        const settingsContext = this.context;
        const enabled = settingsContext.settings.applicationSharingEnabled;
        this.setState({ isApplicationSharingEnabled: enabled });
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
        const { intl } = this.props;
        const id = event.currentTarget.getAttribute('data-appId');
        const newData = new Map([...data]);
        const app = newData.get(id);
        app.deleting = true;
        this.setState({ data: newData });

        const message = intl.formatMessage({
            defaultMessage: 'Application {name} deleted successfully!',
            id: 'Applications.Listing.Listing.application.deleted.successfully',
        }, { name: app.name });
        const promisedDelete = Application.deleteApp(id);
        promisedDelete.then((ok) => {
            if (ok) {
                newData.delete(id);
                Alert.info(message);
                this.setState({ data: newData });
            }
        });
    }

    /**
     * @inheritdoc
     */
    render() {
        const {
            data, order, orderBy, rowsPerPage, page, open, isApplicationSharingEnabled,
        } = this.state;
        if (!data) {
            return <Loading />;
        }
        const { classes, theme, intl } = this.props;
        const strokeColorMain = theme.palette.getContrastText(theme.palette.background.paper);
        return (
            <main className={classes.content}>
                <div className={classes.root}>
                    <div className={classes.mainIconWrapper}>
                        <CustomIcon strokeColor={strokeColorMain} width={42} height={42} icon='applications' />
                    </div>
                    <div className={classes.mainTitleWrapper}>
                        <Typography variant='display1' className={classes.mainTitle}>
                            <FormattedMessage
                                id='Applications.Listing.Listing.applications'
                                defaultMessage='Applications'
                            />

                        </Typography>
                        {data && (
                            <Typography variant='caption' gutterBottom align='left'>
                                {data.count === 0 ? (
                                    <React.Fragment>
                                        <FormattedMessage
                                            id='Applications.Listing.Listing.no.applications.created'
                                            defaultMessage='No Applications created'
                                        />
                                    </React.Fragment>
                                ) : (
                                    <React.Fragment>
                                        <FormattedMessage
                                            id='Applications.Listing.Listing.displaying'
                                            defaultMessage='Displaying'
                                        />
                                        {' '}
                                        {data.count}
                                        {' '}
                                        {data.count === 1
                                            ? (
                                                <FormattedMessage
                                                    id='Applications.Listing.Listing.displaying.application'
                                                    defaultMessage='Application'
                                                />
                                            )
                                            : (
                                                <FormattedMessage
                                                    id='Applications.Listing.Listing.displaying.applications'
                                                    defaultMessage='Applications'
                                                />
                                            )}
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
                <Grid container spacing={0} justify='center'>
                    <Grid item xs={12}>
                        {data.size > 0 ? (
                            <div className={classes.appContent}>
                                <Typography variant='caption' gutterBottom align='left'>
                                    <FormattedMessage
                                        id='Applications.Listing.Listing.logical.description'
                                        defaultMessage={`An application is a logical collection of APIs. 
                                        Applications allow you to use a single access token to invoke a
                                         collection of APIs and to subscribe to one API multiple times pre-created
                                          and allows unlimited access by default.`}
                                    />
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
                                        isApplicationSharingEnabled={isApplicationSharingEnabled}
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
                                caption={intl.formatMessage({
                                    defaultMessage: `An application is a logical collection of APIs. Applications
                                    allow you to use a single access token to invoke a collection
                                    of APIs and to subscribe to one API multiple times with different
                                    SLA levels. The DefaultApplication is pre-created and allows unlimited
                                    access by default.`,
                                    id: 'Applications.Listing.Listing.generic.display.description',
                                })}
                                buttonText={intl.formatMessage({
                                    defaultMessage: 'ADD NEW APPLICATION',
                                    id: 'Applications.Listing.Listing.generic.display.description',
                                })}
                            />
                        )}
                    </Grid>
                </Grid>
            </main>
        );
    }
}
Listing.propTypes = {
    classes: PropTypes.shape({
        root: PropTypes.string,
        flex: PropTypes.string,
        content: PropTypes.string,
        mainIconWrapper: PropTypes.string,
        mainTitle: PropTypes.string,
        mainTitleWrapper: PropTypes.string,
        createLinkWrapper: PropTypes.string,
        appContent: PropTypes.string,
    }).isRequired,
    theme: PropTypes.shape({}).isRequired,
    intl: PropTypes.func.isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(Listing));
