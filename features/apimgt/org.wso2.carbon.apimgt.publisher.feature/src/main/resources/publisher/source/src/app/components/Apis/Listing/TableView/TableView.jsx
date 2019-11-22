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

import React from 'react';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { createMuiTheme, MuiThemeProvider, withStyles } from '@material-ui/core/styles';
import MUIDataTable from 'mui-datatables';
import { FormattedMessage, injectIntl } from 'react-intl';
import queryString from 'query-string';
import API from 'AppData/api';
import APIProduct from 'AppData/APIProduct';
import Icon from '@material-ui/core/Icon';
import ApiThumb from 'AppComponents/Apis/Listing/components/ImageGenerator/ApiThumb';
import DocThumb from 'AppComponents/Apis/Listing/components/ImageGenerator/DocThumb';
import { Progress } from 'AppComponents/Shared';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import SampleAPI from 'AppComponents/Apis/Listing/SampleAPI/SampleAPI';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import Typography from '@material-ui/core/Typography';
import TopMenu from 'AppComponents/Apis/Listing/components/TopMenu';
import CustomIcon from 'AppComponents/Shared/CustomIcon';

const styles = (theme) => ({
    contentInside: {
        padding: theme.spacing(3),
        paddingTop: theme.spacing(2),
        '& > div[class^="MuiPaper-root-"]': {
            boxShadow: 'none',
            backgroundColor: 'transparent',
        },
    },
    apiNameLink: {
        display: 'flex',
        alignItems: 'center',
        '& span': {
            marginLeft: theme.spacing(1),
        },
    },
});

/**
 *
 *
 * @class TableView
 * @extends {React.Component}
 */
class TableView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            apisAndApiProducts: null,
            notFound: true,
            displayCount: 0,
            listType: props.theme.custom.defaultApiView,
        };
        this.page = 0;
        this.count = 100;
        this.rowsPerPage = 10;
        this.getLocalStorage();
        this.setListType = this.setListType.bind(this);
        this.updateData = this.updateData.bind(this);
    }

    componentDidMount() {
        this.getLocalStorage();
        this.getData();
    }

    componentDidUpdate(prevProps) {
        const { isAPIProduct, query } = this.props;
        if (isAPIProduct !== prevProps.isAPIProduct || query !== prevProps.query) {
            this.getLocalStorage();
            this.getData();
        }
    }

    componentWillUnmount() {
        // The foollowing is resetting the styles for the mui-datatables
        const { theme } = this.props;
        const themeAdditions = {
            overrides: {
                MUIDataTable: {
                    tableRoot: {
                        display: 'table',
                        '& tbody': {
                            display: 'table-row-group',
                        },
                        '& thead': {
                            display: 'table-header-group',
                        },
                    },
                },
            },
        };
        Object.assign(theme, themeAdditions);
    }

    getMuiTheme = () => {
        const { listType } = this.state;
        const { theme } = this.props;
        let themeAdditions = {};
        let muiTheme = {
            overrides: {
                MUIDataTable: {
                    root: {
                        backgroundColor: 'transparent',
                        marginLeft: 40,
                    },
                    paper: {
                        boxShadow: 'none',
                        backgroundColor: 'transparent',
                    },
                    tableRoot: {
                        '& tbody': {
                            backgroundColor: '#fff',
                        },
                    },
                },
                MUIDataTableBodyCell: {
                    root: {
                        backgroundColor: 'transparent',
                    },
                },
                MUIDataTableHeadCell: {
                    fixedHeader: {
                        zIndex: -1,
                    },
                },
            },
        };
        if (listType === 'grid') {
            themeAdditions = {
                overrides: {
                    MUIDataTable: {
                        tableRoot: {
                            display: 'block',
                            '& tbody': {
                                display: 'flex',
                                flexWrap: 'wrap',
                                backgroundColor: 'transparent',
                            },
                            '& thead': {
                                display: 'none',
                            },
                        },
                    },
                },
            };
        }
        muiTheme = Object.assign(theme, muiTheme, themeAdditions);
        return createMuiTheme(muiTheme);
    };

    // get apisAndApiProducts
    getData = () => {
        this.xhrRequest().then((data) => {
            const { body } = data;
            const { list, pagination, count } = body;
            const { total } = pagination;
            this.count = total;
            this.setState({ apisAndApiProducts: list, notFound: false, displayCount: count });
        });
    };

    getLocalStorage = () => {
        const { isAPIProduct } = this.props;
        const paginationSufix = isAPIProduct ? 'products' : 'apis';
        const storedPagination = window.localStorage.getItem('pagination-' + paginationSufix);
        if (storedPagination) {
            const pagination = JSON.parse(storedPagination);
            if (pagination.page && pagination.count && pagination.rowsPerPage) {
                this.page = pagination.page;
                this.count = pagination.count;
                this.rowsPerPage = pagination.rowsPerPage;
            }
        }
    };

    /**
     *
     * Switch the view between grid and list view
     * @param {String} value UUID(ID) of the deleted API
     * @memberof Listing
     */
    setListType = (value) => {
        this.setState({ listType: value });
    };

    setLocalStorage = () => {
        // Set the page to the localstorage
        const { isAPIProduct } = this.props;
        const paginationSufix = isAPIProduct ? 'products' : 'apis';
        const pagination = { page: this.page, count: this.count, rowsPerPage: this.rowsPerPage };
        window.localStorage.setItem('pagination-' + paginationSufix, JSON.stringify(pagination));
    };

    changePage = (page) => {
        this.page = page;
        this.xhrRequest().then((data) => {
            const { body } = data;
            const { list, count } = body;
            this.setState({
                apisAndApiProducts: list,
                notFound: false,
                displayCount: count,
            });
            this.setLocalStorage();
        });
    };

    xhrRequest = () => {
        const { page, rowsPerPage } = this;
        const { isAPIProduct, query } = this.props;
        if (query) {
            const composeQuery = queryString.parse(query);
            composeQuery.limit = this.rowsPerPage;
            composeQuery.offset = page * rowsPerPage;
            return API.search(composeQuery);
        }
        if (isAPIProduct) {
            return APIProduct.all({ limit: this.rowsPerPage, offset: page * rowsPerPage });
        } else {
            return API.all({ limit: this.rowsPerPage, offset: page * rowsPerPage });
        }
    };

    /**
     *
     * Update APIs list if an API get deleted in card or table view
     * @param {String} apiUUID UUID(ID) of the deleted API
     * @memberof Listing
     */
    updateData() {
        const { page, rowsPerPage, count } = this;
        if (count - 1 === rowsPerPage * page && page !== 0) {
            this.page = page - 1;
        }
        this.getData();
    }

    /**
     *
     *
     * @returns
     * @memberof TableView
     */
    render() {
        const {
            intl, isAPIProduct, classes, query,
        } = this.props;
        const columns = [
            {
                name: 'id',
                options: {
                    display: 'excluded',
                    filter: false,
                },
            },
            {
                name: 'name',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.name',
                    defaultMessage: 'Name',
                }),
                options: {
                    customBodyRender: (value, tableMeta, updateValue, tableViewObj = this) => {
                        if (tableMeta.rowData) {
                            const { isAPIProduct } = tableViewObj.props; // eslint-disable-line no-shadow
                            const artifact = tableViewObj.state.apisAndApiProducts[tableMeta.rowIndex];
                            const apiName = tableMeta.rowData[1];
                            const apiId = tableMeta.rowData[0];
                            if (isAPIProduct) {
                                return (
                                    <Link to={'/api-products/' + apiId + '/overview'} className={classes.apiNameLink}>
                                        <CustomIcon width={16} height={16} icon='api-product' strokeColor='#444444' />
                                        <span>{apiName}</span>
                                    </Link>
                                );
                            }
                            if (artifact) {
                                if (artifact.type === 'DOC') {
                                    return (
                                        <Link
                                            to={'/apis/' + artifact.apiUUID + '/documents/' + apiId + '/details'}
                                            className={classes.apiNameLink}
                                        >
                                            <Icon>library_books</Icon>
                                            <FormattedMessage
                                                id='Apis.Listing.TableView.TableView.doc.flag'
                                                defaultMessage=' [Doc]'
                                            />
                                            <span>{apiName}</span>
                                        </Link>
                                    );
                                }
                                return (
                                    <Link to={'/apis/' + apiId + '/overview'} className={classes.apiNameLink}>
                                        <CustomIcon width={16} height={16} icon='api' strokeColor='#444444' />
                                        <span>{apiName}</span>
                                    </Link>
                                );
                            }
                        }
                        return <span />;
                    },
                    sort: false,
                    filter: false,
                },
            },
            {
                name: 'version',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.version',
                    defaultMessage: 'Version',
                }),
                options: {
                    sort: false,
                },
            },
            {
                name: 'context',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.context',
                    defaultMessage: 'Context',
                }),
                options: {
                    sort: false,
                },
            },
            {
                name: 'provider',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.provider',
                    defaultMessage: 'Provider',
                }),
                options: {
                    sort: false,
                },
            },
        ];
        const { page, count, rowsPerPage } = this;
        const {
            apisAndApiProducts, notFound, listType, displayCount,
        } = this.state;
        const options = {
            filterType: 'dropdown',
            responsive: 'stacked',
            serverSide: true,
            search: false,
            count,
            page,
            onTableChange: (action, tableState) => {
                switch (action) {
                    case 'changePage':
                        this.changePage(tableState.page);
                        break;
                    default:
                        break;
                }
            },
            selectableRows: 'none',
            rowsPerPage,
            onChangeRowsPerPage: (numberOfRows) => {
                this.rowsPerPage = numberOfRows;
                if (count - 1 === rowsPerPage * page && page !== 0) {
                    this.page = page - 1;
                }
                this.getData();
                this.setLocalStorage();
            },
        };
        if (listType === 'grid') {
            options.customRowRender = (data, dataIndex, rowIndex, tableViewObj = this) => {
                const { isAPIProduct } = tableViewObj.props; // eslint-disable-line no-shadow
                const artifact = tableViewObj.state.apisAndApiProducts[dataIndex];
                if (artifact) {
                    if (artifact.type === 'DOC') {
                        return <DocThumb doc={artifact} />;
                    } else if (artifact.type === 'APIPRODUCT') {
                        artifact.state = 'PUBLISHED';
                        return <ApiThumb api={artifact} isAPIProduct updateData={tableViewObj.updateData} />;
                    } else {
                        return (
                            <ApiThumb api={artifact} isAPIProduct={isAPIProduct} updateData={tableViewObj.updateData} />
                        );
                    }
                }
                return <span />;
            };
            options.title = false;
            options.filter = false;
            options.print = false;
            options.download = false;
            options.viewColumns = false;
            options.customToolbar = false;
        } else {
            options.customRowRender = null;
            options.title = true;
            options.filter = false;
            options.print = true;
            options.download = true;
            options.viewColumns = true;
        }

        if (!apisAndApiProducts) {
            return <Progress />;
        }
        if (notFound) {
            return <ResourceNotFound />;
        }
        if (apisAndApiProducts.length === 0 && !query) {
            return (
                <>
                    <TopMenu
                        data={apisAndApiProducts}
                        count={displayCount}
                        setListType={this.setListType}
                        isAPIProduct={isAPIProduct}
                        listType={listType}
                    />
                    <div className={classes.contentInside}>
                        {isAPIProduct ? (
                            <InlineMessage type='info' height={140}>
                                <div className={classes.contentWrapper}>
                                    <Typography variant='h5' component='h3' className={classes.head}>
                                        <FormattedMessage
                                            id='Apis.Listing.SampleAPIProduct.manager'
                                            defaultMessage='Welcome to WSO2 API Manager'
                                        />
                                    </Typography>
                                    <Typography component='p' className={classes.content}>
                                        <FormattedMessage
                                            id='Apis.Listing.SampleAPIProduct.description'
                                            defaultMessage={
                                                'The API resources in an API product can come from'
                                                + ' one or more APIs, so you can mix and match resources from multiple '
                                                + ' API resources to create specialized feature sets.'
                                            }
                                        />
                                    </Typography>
                                </div>
                            </InlineMessage>
                        ) : (
                            <SampleAPI />
                        )}
                    </div>
                </>
            );
        }

        return (
            <>
                <TopMenu
                    data={apisAndApiProducts}
                    count={displayCount}
                    setListType={this.setListType}
                    isAPIProduct={isAPIProduct}
                    listType={listType}
                    query={query}
                />
                <div className={classes.contentInside}>
                    <MuiThemeProvider theme={this.getMuiTheme()}>
                        <MUIDataTable title='' data={apisAndApiProducts} columns={columns} options={options} />
                    </MuiThemeProvider>
                </div>
            </>
        );
    }
}

export default injectIntl(withStyles(styles, { withTheme: true })(TableView));

TableView.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func.isRequired }).isRequired,
    isAPIProduct: PropTypes.bool.isRequired,
    theme: PropTypes.shape({
        custom: PropTypes.string,
    }).isRequired,
    query: PropTypes.string,
};

TableView.defaultProps = {
    query: '',
};
