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
import TablePagination from '@material-ui/core/TablePagination';
import Button from '@material-ui/core/Button';
import Paper from '@material-ui/core/Paper';
import TableFooter from '@material-ui/core/TableFooter';
import TableRow from '@material-ui/core/TableRow';
import Box from '@material-ui/core/Box';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import { FormattedMessage, injectIntl } from 'react-intl';
import { Link } from 'react-router-dom';
import { ScopeValidation, resourceMethods, resourcePaths } from 'AppComponents/Shared/ScopeValidation';
import Alert from 'AppComponents/Shared/Alert';
import Loading from 'AppComponents/Base/Loading/Loading';
import Application from 'AppData/Application';
import GenericDisplayDialog from 'AppComponents/Shared/GenericDisplayDialog';
import Settings from 'AppComponents/Shared/SettingsContext';
import { appSettings } from 'Settings';
import AppsTableContent from './AppsTableContent';
import ApplicationTableHead from './ApplicationTableHead';
import DeleteConfirmation from './DeleteConfirmation';

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
        height: 80,
        background: theme.custom.infoBar.background,
        color: theme.palette.getContrastText(theme.custom.infoBar.background),
        borderBottom: `solid 1px ${theme.palette.grey.A200}`,
        display: 'block',
    },
    mainIconWrapper: {
        paddingTop: theme.spacing(1.5),
        paddingLeft: theme.spacing(3),
        paddingRight: theme.spacing(2.5),
    },
    mainTitleWrapper: {
        display: 'flex',
    },
    createLinkWrapper: {
        paddingLeft: theme.spacing(2),
    },
    appContent: {
        marginTop: theme.spacing(2),
        maxWidth: '95%',
        margin: 'auto',
        maxHeight: theme.spacing(90),
        height: theme.spacing(90),
        overflow: 'scroll',
    },
    dialogContainer: {
        width: 1000,
        padding: theme.spacing(2),
    },
    container: {
        height: '100%',
    },
    appTablePaper: {
        '& table tr td':{
            paddingLeft: theme.spacing(1),
        },
        '& table tr:nth-child(even)': {
            backgroundColor: theme.custom.listView.tableBodyEvenBackgrund,
            '& td, & a, & .material-icons': {
                color: theme.palette.getContrastText(theme.custom.listView.tableBodyEvenBackgrund),
            },
        },
        '& table tr:nth-child(odd)': {
            backgroundColor: theme.custom.listView.tableBodyOddBackgrund,
            '& td, & a, & .material-icons': {
                color: theme.palette.getContrastText(theme.custom.listView.tableBodyOddBackgrund),
            },
        },
        '& table th': {
            backgroundColor: theme.custom.listView.tableHeadBackground,
            color: theme.palette.getContrastText(theme.custom.listView.tableHeadBackground),
            paddingLeft: theme.spacing(1),
        },

    },
});

/**
 * @inheritdoc
 * @class Listing
 * @extends {Component}
 */
class Listing extends Component {
    static contextType = Settings;
    static rowsPerPage = 10;

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
            rowsPerPage: Listing.rowsPerPage,
            open: false,
            isApplicationSharingEnabled: true,
            isDeleteOpen: false,
            totalApps: 0,
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
        const {
            page, rowsPerPage, order, orderBy,
        } = this.state;
        const promisedApplications = Application.all(rowsPerPage, page * rowsPerPage, order, orderBy);
        promisedApplications
            .then((applications) => {
                const { pagination: { total } } = applications;
                // Applications list put into map, to make it efficient when deleting apps (referring back to an App)
                const apps = new Map();
                applications.list.map(app => apps.set(app.applicationId, app)); // Store application against its UUID
                this.setState({ data: apps, totalApps: total });
            })
            .catch((error) => {
                console.log(error);
                const { status } = error;
                if (status === 404) {
                    // eslint-disable-next-line react/no-unused-state
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    window.location = appSettings.context + '/services/configs';
                }
            });
    }

    /**
     * @param{*} event event
     * @param{*} property sorting method
     */
    handleRequestSort = (event, property) => {
        const { orderBy, order } = this.state;
        let currentOrder = 'desc';
        if (orderBy === property) {
            currentOrder = order === 'desc' ? 'asc' : 'desc';
            this.setState({ order: currentOrder }, this.updateApps);
        } else {
            this.setState({ order: currentOrder, orderBy: property }, this.updateApps);
        }
    };

    /**
     *
     * @param {*} event event
     * @param {*} page page
     * @memberof Listing
     */
    handleChangePage = (event, page) => {
        this.setState({ page }, this.updateApps);
    }

    /**
     *
     * @inheritdoc
     * @memberof Listing
     */
    handleChangeRowsPerPage = (event) => {
        const nextRowsPerPage = event.target.value;
        const { rowsPerPage, page } = this.state;
        const rowsPerPageRatio = rowsPerPage / nextRowsPerPage;
        const nextPage =  Math.floor(page * rowsPerPageRatio);
        this.setState({ rowsPerPage: nextRowsPerPage, page: nextPage }, this.updateApps);
    };

    /**
     * @memberof Listing
     */
    handleClose = () => {
        this.setState({ open: false });
    };

    /**
     * @memberof Listing
     */
    handleClickOpen = () => {
        const { history } = this.props;
        history.push('/applications/create');
    };

    /**
     * @param {*} event event
     * @memberof Listing
     */
    handleAppDelete() {
        const { data, deletingId, page } = this.state;
        const { intl } = this.props;
        const newData = new Map([...data]);
        const app = newData.get(deletingId);
        app.deleting = true;

        let message = intl.formatMessage({
            defaultMessage: 'Application {name} deleted successfully!',
            id: 'Applications.Listing.Listing.application.deleted.successfully',
        }, { name: app.name });
        const promisedDelete = Application.deleteApp(deletingId);
        promisedDelete.then((ok) => {
            if (ok) {
                newData.delete(deletingId);
                Alert.info(message);
                this.toggleDeleteConfirmation();
                // Page is reduced by 1, when there is only one application in a particular page and it is deleted (except when in first page)
                if (newData.size === 0 && page !== 0) {
                    this.setState((state) => ({ page: state.page - 1 }));
                }
                this.updateApps();
            }
        }).catch((error) => {
            console.log(error);
            message = intl.formatMessage({
                defaultMessage: 'Error while deleting application {name}',
                id: 'Applications.Listing.Listing.application.deleting.error',
            }, { name: app.name });
            Alert.error(message);
        });
    }

    toggleDeleteConfirmation = (event) => {
        let id = '';
        if (event) {
            id = event.currentTarget.getAttribute('data-appid');
        }
        this.setState(({ isDeleteOpen }) => ({ isDeleteOpen: !isDeleteOpen, deletingId: id }));
    }

    /**
     * @inheritdoc
     */
    render() {
        const {
            data, order, orderBy, rowsPerPage, page, open, isApplicationSharingEnabled,
            isDeleteOpen, totalApps,
        } = this.state;
        if (!data) {
            return <Loading />;
        }
        const { classes, theme, intl } = this.props;
        const strokeColorMain = theme.palette.getContrastText(theme.custom.infoBar.background);
        const paginationEnabled = totalApps > Listing.rowsPerPage;
        return (
            <main className={classes.content}>
                <div className={classes.root}>
                    <Box display='flex' flexDirection='row' justifyContent='flex-start' alignItems='center'>
                        <div className={classes.mainIconWrapper}>
                            <CustomIcon strokeColor={strokeColorMain} width={42} height={42} icon='applications' />
                        </div>
                        <div className={classes.mainTitleWrapper}>
                            <Typography variant='h4' className={classes.mainTitle}>
                                <FormattedMessage
                                    id='Applications.Listing.Listing.applications'
                                    defaultMessage='Applications'
                                />
                            </Typography>
                            {(data.size !== 0 || open) && (
                                <div className={classes.createLinkWrapper}>
                                    <ScopeValidation
                                        resourcePath={resourcePaths.APPLICATIONS}
                                        resourceMethod={resourceMethods.POST}
                                    >
                                        <Link to='/applications/create'>
                                            <Button
                                                variant='contained'
                                                color='primary'
                                            >
                                                <FormattedMessage
                                                    id='Applications.Create.Listing.add.new.application'
                                                    defaultMessage='Add New Application'
                                                />
                                            </Button>
                                        </Link>
                                    </ScopeValidation>
                                </div>
                            )}
                            {data && (
                                <Typography variant='caption' gutterBottom align='left'>
                                    {data.count === 0 && (
                                        <React.Fragment>
                                            <FormattedMessage
                                                id='Applications.Listing.Listing.no.applications.created'
                                                defaultMessage='No Applications created'
                                            />
                                        </React.Fragment>
                                    )}
                                </Typography>
                            )}
                        </div>
                    </Box>
                    <Box display='flex' pl={3}>
                        <Typography variant='caption' gutterBottom align='left'>
                            <FormattedMessage
                                id='Applications.Listing.Listing.logical.description'
                                defaultMessage={`An application is a logical collection of APIs. 
                                        Applications allow you to use a single access token to invoke a
                                         collection of APIs and to subscribe to one API multiple times
                                          and allows unlimited access by default.`}
                            />
                        </Typography>
                    </Box>
                </div>
                <Grid container spacing={0} justify='center' className={classes.container}>
                    <Grid item xs={12}>
                        {data.size > 0 ? (
                            <div className={classes.appContent}>
                                <Paper className={classes.appTablePaper}>
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
                                            toggleDeleteConfirmation={this.toggleDeleteConfirmation}
                                        />
                                        {paginationEnabled
                                            && (
                                                <TableFooter>
                                                    <TableRow>
                                                        <TablePagination
                                                            component='td'
                                                            count={totalApps}
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
                                                    </TableRow>
                                                </TableFooter>
                                            )}
                                    </Table>
                                </Paper>
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
                                        id: 'Applications.Listing.Listing.generic.display.description.text',
                                    })}
                                />
                            )}
                        <DeleteConfirmation
                            handleAppDelete={this.handleAppDelete}
                            isDeleteOpen={isDeleteOpen}
                            toggleDeleteConfirmation={this.toggleDeleteConfirmation}
                        />
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
    intl: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({
        push: PropTypes.func,
    }).isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(Listing));
