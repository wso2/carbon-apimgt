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
import Create from '@material-ui/icons/Create';
import IconButton from '@material-ui/core/IconButton';
import FirstPageIcon from '@material-ui/icons/FirstPage';
import KeyboardArrowLeft from '@material-ui/icons/KeyboardArrowLeft';
import KeyboardArrowRight from '@material-ui/icons/KeyboardArrowRight';
import LastPageIcon from '@material-ui/icons/LastPage';
import AddCircle from '@material-ui/icons/AddCircle';
import MUIDataTable from 'mui-datatables';
import Icon from '@material-ui/core/Icon';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import Grid from '@material-ui/core/Grid';
import { isRestricted } from 'AppData/AuthManager';
import { withAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import Alert from 'AppComponents/Shared/Alert';
import Box from '@material-ui/core/Box';
import Delete from '../Delete/Delete';
import Usage from '../Usage/Usage';

const styles = (theme) => ({
    contentInside: {
        padding: theme.spacing(3),
        paddingTop: theme.spacing(2),
        '& > div[class^="MuiPaper-root-"]': {
            boxShadow: 'none',
            backgroundColor: 'transparent',
        },
    },
    table: {
        marginLeft: 'auto',
        marginRight: 'auto',
        '& > td[class^=MUIDataTableBodyCell-cellHide-]': {
            display: 'none',
        },
        '& .MUIDataTableBodyCell-cellHide-793': {
            display: 'none',
        },
        '& td': {
            wordBreak: 'break-word',
        },
        '& th': {
            minWidth: '150px',
        },
    },
    root: {
        paddingTop: 0,
        paddingLeft: 0,
    },
    buttonProgress: {
        position: 'relative',
        margin: theme.spacing(1),
    },
    heading: {
        flexGrow: 1,
        marginTop: 10,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: theme.spacing(2),
        marginLeft: 'auto',
        marginRight: 'auto',
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
    disableLink: {
        pointerEvents: 'none',
    },
    headline: {
        paddingTop: theme.spacing(1.25),
        paddingLeft: theme.spacing(2.5),
    },
    head: {
        paddingBottom: theme.spacing(2),
        fontWeight: 200,
    },
    content: {
        paddingBottom: theme.spacing(2),
    },
    buttonLeft: {
        marginRight: theme.spacing(1),
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
        this.state = { scopes: null, page: 0 };
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
        const { page } = this.state;
        const promisedScopes = API.getAllScopes(page * 2000, 2000);

        promisedScopes
            .then((response) => {
                this.setState({
                    scopes: response.body.list,
                });
            })
            .catch((errorMessage) => {
                console.error(errorMessage);
                Alert.error(JSON.stringify(errorMessage));
            });
    }

    /**
     * Render Scopes section
     * @returns {React.Component} React Component
     * @memberof Scopes
     */
    render() {
        const { scopes } = this.state;
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
                                <Box display='flex' flexDirection='column'>
                                    <Usage
                                        scopeName={scopeName}
                                        scopeId={scopeId}
                                        usageCount={usageCount}
                                    />
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
                                    <Delete
                                        scopeName={scopeName}
                                        scopeId={scopeId}
                                        fetchScopeData={this.fetchScopeData}
                                    />
                                </Box>
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
            rowsPerPageOptions: [5, 10, 25, 50, 100],
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
                <div className={classes.contentInside}>
                    <InlineMessage type='info' height={140}>
                        <div className={classes.contentWrapper}>
                            <Typography variant='h5' component='h3' className={classes.head}>
                                <FormattedMessage
                                    id='Scopes.Listing.sample.scope.manager'
                                    defaultMessage='Welcome to WSO2 API Manager'
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
                                <Link
                                    to={!isRestricted(['apim:shared_scope_manage']) && url}
                                    className={isRestricted(['apim:shared_scope_manage']) ? classes.disableLink : ''}
                                >
                                    <Button
                                        size='small'
                                        variant='contained'
                                        color='primary'
                                        className={classes.button}
                                        disabled={isRestricted(['apim:shared_scope_manage'])}
                                    >
                                        <Create />
                                        <FormattedMessage
                                            id='Scopes.Listing.Listing.create.scopes.button'
                                            defaultMessage='Create a new scope'
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
                <Grid className={classes.titleWrapper} xs={12} sm={12} md={11} lg={11} item>
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
                </Grid>
                <Grid className={classes.table} xs={12} sm={12} md={11} lg={11} item>
                    <MUIDataTable title={false} data={scopesList} columns={columns} options={options} />
                </Grid>
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
