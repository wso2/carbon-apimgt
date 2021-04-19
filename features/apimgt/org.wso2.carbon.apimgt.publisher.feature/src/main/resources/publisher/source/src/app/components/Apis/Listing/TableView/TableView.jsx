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
import APILanding from 'AppComponents/Apis/Listing/Landing';
import TopMenu from 'AppComponents/Apis/Listing/components/TopMenu';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import SampleAPIProduct from 'AppComponents/Apis/Listing/SampleAPI/SampleAPIProduct';
import Alert from 'AppComponents/Shared/Alert';

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
            marginLeft: theme.spacing(),
        },
        '& span.material-icons': {
            marginLeft: 0,
            color: '#444',
            marginRight: theme.spacing(),
            fontSize: 18,
        },
    },
});

/**
 * Table view for api listing
 *
 * @class ApiTableView
 * @extends {React.Component}
 */
class TableView extends React.Component {
    /**
     * @inheritdoc
     * @param {*} props properties
     * @memberof ApiTableView
     */
    constructor(props) {
        super(props);
        let { defaultApiView } = props.theme.custom;
        this.showToggle = true;
        if (typeof defaultApiView === 'object' && defaultApiView.length > 0) {
            if (defaultApiView.length === 1) { // We will disable toggle buttons
                this.showToggle = false;
            }
            defaultApiView = defaultApiView[defaultApiView.length - 1];
        } else {
            defaultApiView = localStorage.getItem('publisher.listType') || defaultApiView;
        }
        this.state = {
            apisAndApiProducts: null,
            notFound: true,
            listType: defaultApiView,
            loading: true,
            totalCount: -1,
            rowsPerPage: 10,
            page: 0,
        };
        this.setListType = this.setListType.bind(this);
        this.updateData = this.updateData.bind(this);
    }

    componentDidMount() {
        const { rowsPerPage, page } = this.state;
        this.getData(rowsPerPage, page);
        const userRowsPerPage = parseInt(localStorage.getItem('publisher.rowsPerPage'), 10);
        if (userRowsPerPage) {
            this.setState({ rowsPerPage: userRowsPerPage });
        }
    }

    componentDidUpdate(prevProps) {
        const { isAPIProduct, query } = this.props;
        if (isAPIProduct !== prevProps.isAPIProduct || query !== prevProps.query) {
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
        const { listType, totalCount } = this.state;
        const { theme } = this.props;
        let themeAdditions = {};
        let muiTheme = {
            overrides: {
                MUIDataTable: {
                    root: {
                        backgroundColor: 'transparent',
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
                    MuiTableBody: {
                        root: {
                            justifyContent: totalCount > 4 ? 'center' : 'flex-start',
                        },
                    },
                },
            };
        }
        muiTheme = Object.assign(theme, muiTheme, themeAdditions);
        return createMuiTheme(muiTheme);
    };

    // get apisAndApiProducts
    getData = (rowsPerPage, page) => {
        const { intl } = this.props;
        this.setState({ loading: true });
        return this.xhrRequest(rowsPerPage, page).then((data) => {
            const { body } = data;
            const { list, pagination } = body;
            const { total } = pagination;
            this.setState({
                totalCount: total,
                apisAndApiProducts: list,
                notFound: false,
                rowsPerPage,
                page,
            });
        }).catch(() => {
            Alert.error(intl.formatMessage({
                defaultMessage: 'Error While Loading APIs',
                id: 'Apis.Listing.TableView.TableView.error.loading',
            }));
        }).finally(() => {
            this.setState({ loading: false });
        });
    };

    /**
     *
     * Switch the view between grid and list view
     * @param {String} value UUID(ID) of the deleted API
     * @memberof Listing
     */
    setListType = (value) => {
        localStorage.setItem('publisher.listType', value);
        this.setState({ listType: value });
    };

    changePage = (page) => {
        const { intl } = this.props;
        const { rowsPerPage } = this.state;
        this.setState({ loading: true });
        this.xhrRequest(rowsPerPage, page).then((data) => {
            const { body } = data;
            const { list, pagination } = body;
            this.setState({
                apisAndApiProducts: list,
                notFound: false,
                totalCount: pagination.total,
                page,
            });
        }).catch(() => {
            Alert.error(intl.formatMessage({
                defaultMessage: 'Error While Loading APIs',
                id: 'Apis.Listing.TableView.TableView.error.loading',
            }));
        })
            .finally(() => {
                this.setState({ loading: false });
            });
    };

    xhrRequest = (rowsPerPage, page) => {
        const { isAPIProduct, query } = this.props;
        if (query) {
            const composeQuery = queryString.parse(query);
            composeQuery.limit = rowsPerPage;
            composeQuery.offset = page * rowsPerPage;
            return API.search(composeQuery);
        }
        if (isAPIProduct) {
            return APIProduct.all({ limit: rowsPerPage, offset: page * rowsPerPage });
        } else {
            return API.all({ limit: rowsPerPage, offset: page * rowsPerPage });
        }
    };

    /**
     *
     * Update APIs list if an API get deleted in card or table view
     * @param {String} apiUUID UUID(ID) of the deleted API
     * @memberof Listing
     */
    updateData() {
        const { rowsPerPage, page, totalCount } = this.state;
        let newPage = page;
        if (totalCount - 1 === rowsPerPage * page && page !== 0) {
            newPage = page - 1;
        }
        this.getData(rowsPerPage, newPage);
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
        const {
            loading, totalCount, rowsPerPage, apisAndApiProducts, notFound, listType, page,
        } = this.state;
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
        const options = {
            filterType: 'dropdown',
            responsive: 'stacked',
            search: false,
            count: totalCount,
            serverSide: true,
            page,
            onChangePage: this.changePage,
            selectableRows: 'none',
            rowsPerPage,
            onChangeRowsPerPage: (newNumberOfRows) => {
                let newPage;
                if (page * newNumberOfRows > totalCount) {
                    newPage = 0;
                } else if (totalCount - 1 === newNumberOfRows * page && page !== 0) {
                    newPage = page - 1;
                }
                localStorage.setItem('publisher.rowsPerPage', newNumberOfRows);
                this.getData(newNumberOfRows, newPage);
            },
            textLabels: {
                pagination: {
                    rowsPerPage: intl.formatMessage({
                        id: 'Apis.Listing.ApiTableView.items.per.page',
                        defaultMessage: 'Items per page',
                    }),
                },
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
                        return <ApiThumb api={artifact} isAPIProduct updateData={this.updateData} />;
                    } else {
                        return (
                            <ApiThumb api={artifact} isAPIProduct={isAPIProduct} updateData={this.updateData} />
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
        if (page === 0 && totalCount <= rowsPerPage && rowsPerPage === 10) {
            options.pagination = false;
        } else {
            options.pagination = true;
        }
        if (!apisAndApiProducts) {
            return <Progress per={90} message='Loading APIs ...' />;
        }
        if (notFound) {
            return <ResourceNotFound />;
        }
        if (apisAndApiProducts.length === 0 && !query) {
            return (
                <>
                    <TopMenu
                        data={apisAndApiProducts}
                        count={totalCount}
                        setListType={this.setListType}
                        isAPIProduct={isAPIProduct}
                        listType={listType}
                        showToggle={this.showToggle}
                    />
                    {isAPIProduct ? (
                        <SampleAPIProduct />
                    ) : (
                        <APILanding />
                    )}
                </>
            );
        }

        return (
            <>
                <TopMenu
                    data={apisAndApiProducts}
                    count={totalCount}
                    setListType={this.setListType}
                    isAPIProduct={isAPIProduct}
                    listType={listType}
                    showToggle={this.showToggle}
                    query={query}
                />
                <div className={classes.contentInside}>
                    {loading ? (
                        <Progress
                            per={96}
                            message='Updating page ...'
                        />
                    )
                        : (
                            <MuiThemeProvider theme={this.getMuiTheme()}>
                                <MUIDataTable title='' data={apisAndApiProducts} columns={columns} options={options} />
                            </MuiThemeProvider>
                        )}
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
        custom: PropTypes.shape({}),
    }).isRequired,
    query: PropTypes.string,
};

TableView.defaultProps = {
    query: '',
};
