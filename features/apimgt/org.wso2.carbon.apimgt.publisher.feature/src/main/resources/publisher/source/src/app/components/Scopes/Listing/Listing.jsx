/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import 'react-tagsinput/react-tagsinput.css';
import PropTypes from 'prop-types';
import React from 'react';
import API from 'AppData/api';
import { Progress } from 'AppComponents/Shared';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl } from 'react-intl';
import Button from '@material-ui/core/Button';
import withStyles from '@material-ui/core/styles/withStyles';
import { Link } from 'react-router-dom';
import IconButton from '@material-ui/core/IconButton';
import FirstPageIcon from '@material-ui/icons/FirstPage';
import KeyboardArrowLeft from '@material-ui/icons/KeyboardArrowLeft';
import KeyboardArrowRight from '@material-ui/icons/KeyboardArrowRight';
import LastPageIcon from '@material-ui/icons/LastPage';
import TablePagination from '@material-ui/core/TablePagination';
import AddCircle from '@material-ui/icons/AddCircle';
import MUIDataTable from 'mui-datatables';
import Icon from '@material-ui/core/Icon';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import Grid from '@material-ui/core/Grid';
import { isRestricted } from 'AppData/AuthManager';
import { withAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import Alert from 'AppComponents/Shared/Alert';
import Delete from '../Delete/Delete';
import Usage from '../Usage/Usage';

const styles = (theme) => ({
    root: {
        paddingTop: 0,
        paddingLeft: 0,
    },
    buttonProgress: {
        position: 'relative',
        margin: theme.spacing(1),
    },
    headline: { paddingTop: theme.spacing(1.25), paddingLeft: theme.spacing(2.5) },
    heading: {
        flexGrow: 1,
        marginTop: 10,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: theme.spacing(2),
    },
    mainTitle: {
        paddingLeft: 0,
    },
    button: {
        textDecoration: 'none',
        color: theme.palette.getContrastText(theme.palette.primary.main),
        marginLeft: theme.spacing(1),
    },
    buttonIcon: {
        marginRight: theme.spacing(1),
    },
    content: {
        margin: `${theme.spacing(2)}px 0 ${theme.spacing(2)}px 0`,
    },
    head: {
        fontWeight: 200,
    },
    disableLink: {
        pointerEvents: 'none',
    },
});

/**
 * Table pagination for scope table
 *
 * @param {any} props props used for ScopeTablePagination
 * @returns {*} returns the pagination UI render
 */
function ScopeTablePagination(props) {
    const {
        count, page, rowsPerPage, onChangePage,
    } = props;

    /**
     * handleFirstPageButtonClick loads data of the first page
     * */
    function handleFirstPageButtonClick() {
        if (onChangePage) {
            onChangePage(0);
        }
    }

    /**
     * handleBackButtonClick load data of the prev page
     * */
    function handleBackButtonClick() {
        if (onChangePage) {
            onChangePage(page - 1);
        }
    }

    /**
     * handleNextButtonClick load data of the next page
     * */
    function handleNextButtonClick() {
        if (onChangePage) {
            onChangePage(page + 1);
        }
    }

    /**
     * handleLastPageButtonClick load data of the last page
     * */
    function handleLastPageButtonClick() {
        if (onChangePage) {
            onChangePage(Math.max(0, Math.ceil(count / rowsPerPage) - 1));
        }
    }

    return (
        <div
            style={{ display: 'flex' }}
        >
            <IconButton
                onClick={handleFirstPageButtonClick}
                disabled={page === 0}
            >
                <FirstPageIcon />
            </IconButton>
            <IconButton
                onClick={handleBackButtonClick}
                disabled={page === 0}
            >
                <KeyboardArrowLeft />
            </IconButton>
            <IconButton
                onClick={handleNextButtonClick}
                disabled={page >= Math.ceil(count / rowsPerPage) - 1}
            >
                <KeyboardArrowRight />
            </IconButton>
            <IconButton
                onClick={handleLastPageButtonClick}
                disabled={page >= Math.ceil(count / rowsPerPage) - 1}
            >
                <LastPageIcon />
            </IconButton>
        </div>
    );
}

ScopeTablePagination.propTypes = {
    count: PropTypes.number.isRequired,
    page: PropTypes.number.isRequired,
    rowsPerPage: PropTypes.number.isRequired,
    onChangePage: PropTypes.func.isRequired,
};

/**
 * Generate the scopes UI in API details page.
 * @class Scopes
 * @extends {React.Component}
 */
class Listing extends React.Component {
    /**
     * Creates an instance of Scopes.
     * @param {any} props Generic props
     * @memberof Scopes
     */
    constructor(props) {
        super(props);
        this.api_uuid = props.match.params.api_uuid;
        this.api_data = props.api;
        this.state = {
            scopes: null,
            totalScopes: 0,
            page: 0,
            rowsPerPage: 5,
            rowsPerPageOptions: [5, 10, 25, 50, 100],
        };
        this.handleChangePage = this.handleChangePage.bind(this);
        this.handleChangeRowsPerPage = this.handleChangeRowsPerPage.bind(this);
        this.fetchScopeData = this.fetchScopeData.bind(this);
    }

    /**
     * @inheritDoc
     * @memberof Protected
     */
    componentDidMount() {
        this.fetchScopeData();
    }

    /**
     * Fetches scope data
     *
     * @memberof ScopesTable
     */
    fetchScopeData() {
        const { page, rowsPerPage } = this.state;
        const promisedScopes = API.getAllScopes(page * rowsPerPage, rowsPerPage);

        promisedScopes
            .then((response) => {
                this.setState({
                    scopes: response.body.list,
                    totalScopes: response.body.pagination.total,
                });
            })
            .catch((errorMessage) => {
                console.error(errorMessage);
                Alert.error(JSON.stringify(errorMessage));
            });
    }

    /**
     * handleChangePage handle change in selected page
     *
     * @param {any} page selected page
     * */
    handleChangePage(page) {
        this.setState({ page }, this.fetchScopeData);
    }

    /**
     * handleChangeRowsPerPage handle change in rows per page
     *
     * @param {any} event rows per page change event
     * */
    handleChangeRowsPerPage(event) {
        this.setState({ rowsPerPage: event.target.value, page: 0 }, this.fetchScopeData);
    }

    /**
     * Render Scopes section
     * @returns {React.Component} React Component
     * @memberof Scopes
     */
    render() {
        const {
            scopes, page, rowsPerPage, totalScopes, rowsPerPageOptions,
        } = this.state;
        const {
            intl, classes,
        } = this.props;
        const url = '/scopes/create';
        const editUrl = '/scopes/edit';
        const columns = [
            {
                name: 'scopeId',
                options: {
                    display: 'excluded',
                    filter: false,
                },
            },
            intl.formatMessage({
                id: 'Scopes.Listing.Listing.table.header.name',
                defaultMessage: 'Name',
            }),
            intl.formatMessage({
                id: 'Scopes.Listing.Listing.table.header.display.name',
                defaultMessage: 'Display Name',
            }),
            intl.formatMessage({
                id: 'Scopes.Listing.Listing.table.header.description',
                defaultMessage: 'Description',
            }),
            {
                options: {
                    customBodyRender: (value, tableMeta) => {
                        if (tableMeta.rowData) {
                            const roles = value || [];
                            return roles.join(',');
                        }
                        return false;
                    },
                    filter: false,
                    sort: false,
                    label: (
                        <FormattedMessage
                            id='Scopes.Listing.Listing.table.header.roles'
                            defaultMessage='Roles'
                        />
                    ),
                },
            },
            intl.formatMessage({
                id: 'Scopes.Listing.Listing.table.header.number.of.usages',
                defaultMessage: 'Number of usages',
            }),
            {
                options: {
                    customBodyRender: (value, tableMeta) => {
                        if (tableMeta.rowData) {
                            const scopeId = tableMeta.rowData[0];
                            const scopeName = tableMeta.rowData[1];
                            const usageCount = tableMeta.rowData[5];
                            return (
                                <table className={classes.actionTable}>
                                    <tr>
                                        <td>
                                            <Usage
                                                scopeName={scopeName}
                                                scopeId={scopeId}
                                                usageCount={usageCount}
                                            />
                                        </td>
                                        <td>
                                            <Link
                                                to={!isRestricted(['apim:shared_scope_manage'])
                                                    && {
                                                        pathname: editUrl,
                                                        state: {
                                                            scopeName,
                                                            scopeId,
                                                        },
                                                    }}
                                                className={isRestricted(['apim:shared_scope_manage'])
                                                    ? classes.disableLink : ''}
                                            >
                                                <Button disabled={isRestricted(['apim:shared_scope_manage'])}>
                                                    <Icon>edit</Icon>
                                                    <FormattedMessage
                                                        id='Scopes.Listing.Listing.scopes.text.editor.edit'
                                                        defaultMessage='Edit'
                                                    />
                                                </Button>
                                            </Link>
                                        </td>
                                        <td>
                                            <Delete
                                                scopeName={scopeName}
                                                scopeId={scopeId}
                                                fetchScopeData={this.fetchScopeData}
                                            />
                                        </td>
                                    </tr>
                                </table>
                            );
                        }
                        return false;
                    },
                    filter: false,
                    sort: false,
                    label: (
                        <FormattedMessage
                            id='Scopes.Listing.Listing.table.header.actions'
                            defaultMessage='Actions'
                        />
                    ),
                },
            },
        ];
        const options = {
            filterType: 'multiselect',
            selectableRows: 'none',
            title: false,
            filter: false,
            sort: false,
            print: false,
            download: false,
            viewColumns: false,
            customToolbar: false,
            customFooter: () => {
                return (
                    <TablePagination
                        rowsPerPageOptions={rowsPerPageOptions}
                        colSpan={6}
                        count={totalScopes}
                        rowsPerPage={rowsPerPage}
                        page={page}
                        onChangePage={this.handleChangePage}
                        onChangeRowsPerPage={this.handleChangeRowsPerPage}
                        ActionsComponent={ScopeTablePagination}
                    />
                );
            },
        };

        if (!scopes) {
            return <Progress />;
        }
        const scopesList = scopes.filter((sharedScope) => {
            return !sharedScope.shared;
        }).map((sharedScope) => {
            const aScope = [];
            aScope.push(sharedScope.id);
            aScope.push(sharedScope.name);
            aScope.push(sharedScope.displayName);
            aScope.push(sharedScope.description);
            aScope.push(sharedScope.bindings);
            aScope.push(sharedScope.usageCount);
            return aScope;
        });

        if (scopes.length === 0) {
            return (
                <div className={classes.root}>
                    <div className={classes.titleWrapper}>
                        <Typography variant='h4' align='left' className={classes.mainTitle}>
                            <FormattedMessage
                                id='Scopes.Listing.Listing.heading.scope.heading'
                                defaultMessage='Scopes'
                            />
                        </Typography>
                    </div>
                    <InlineMessage type='info' height={140}>
                        <div className={classes.contentWrapper}>
                            <Typography variant='h5' component='h3' className={classes.head}>
                                <FormattedMessage
                                    id='Scopes.Listing.Listing.create.scopes.title'
                                    defaultMessage='Create Scopes'
                                />
                            </Typography>
                            <Typography component='p' className={classes.content}>
                                <FormattedMessage
                                    id='Scopes.Listing.Listing.scopes.enable.fine.gained.access.control'
                                    defaultMessage={
                                        'Scopes enable fine-grained access control to API resources'
                                        + ' based on user roles.'
                                    }
                                />
                            </Typography>
                            <div className={classes.actions}>
                                <Link to={url}>
                                    <Button
                                        variant='contained'
                                        color='primary'
                                        className={classes.button}
                                    >
                                        <FormattedMessage
                                            id='Scopes.Listing.Listing.create.scopes.button'
                                            defaultMessage='Create Scopes'
                                        />
                                    </Button>
                                </Link>
                            </div>
                        </div>
                    </InlineMessage>
                </div>
            );
        }

        return (
            <div className={classes.heading}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        <FormattedMessage
                            id='Scopes.Listing.Listing.heading.scope.heading'
                            defaultMessage='Scopes'
                        />
                    </Typography>
                    <Link
                        to={!isRestricted(['apim:shared_scope_manage']) && url}
                        className={isRestricted(['apim:shared_scope_manage']) ? classes.disableLink : ''}
                    >
                        <Button
                            size='small'
                            className={classes.button}
                            disabled={isRestricted(['apim:shared_scope_manage'])}
                        >
                            <AddCircle className={classes.buttonIcon} />
                            <FormattedMessage
                                id='Scopes.Listing.Listing.heading.scope.add_new'
                                defaultMessage='Add New Scope'
                            />
                        </Button>
                    </Link>
                    {isRestricted(['apim:shared_scope_manage']) && (
                        <Grid item>
                            <Typography variant='body2' color='primary'>
                                <FormattedMessage
                                    id='Scopes.Listing.Listing.update.not.allowed'
                                    defaultMessage={
                                        '*You are not authorized to update scopes of'
                                        + ' the API due to insufficient permissions'
                                    }
                                />
                            </Typography>
                        </Grid>
                    )}
                </div>

                <MUIDataTable title={false} data={scopesList} columns={columns} options={options} />
            </div>
        );
    }
}

Listing.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.object,
    }),
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

Listing.defaultProps = {
    match: { params: {} },
};

export default injectIntl(withAPI(withStyles(styles)(Listing)));
