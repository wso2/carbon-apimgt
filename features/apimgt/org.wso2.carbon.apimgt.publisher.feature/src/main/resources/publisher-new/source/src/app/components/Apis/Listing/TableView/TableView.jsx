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
import { injectIntl } from 'react-intl';
import API from 'AppData/api';
import APIProduct from 'AppData/APIProduct';
import Configurations from 'Config';
import ImageGenerator from 'AppComponents/Apis/Listing/components/ImageGenerator/ImageGenerator';
import ApiThumb from 'AppComponents/Apis/Listing/components/ImageGenerator/ApiThumb';
import { Progress } from 'AppComponents/Shared';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import SampleAPI from 'AppComponents/Apis/Listing/SampleAPI/SampleAPI';
import TopMenu from 'AppComponents/Apis/Listing/components/TopMenu';

const styles = theme => ({
    contentInside: {
        padding: theme.spacing.unit * 3,
        paddingTop: theme.spacing.unit * 2,
        '& > div[class^="MuiPaper-root-"]': {
            boxShadow: 'none',
            backgroundColor: 'transparent',
        },
    },
});

class TableView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            apisAndApiProducts: null,
            notFound: true,
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
        const { isAPIProduct } = this.props;
        if (isAPIProduct !== prevProps.isAPIProduct) {
            this.getLocalStorage();
            this.getData();
        }
    }

    getMuiTheme = () => {
        const { listType } = this.state;
        let { theme } = this.props;
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
            },
        };
        if (listType === 'grid') {
            const themeAdditions = {
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
            muiTheme = Object.assign( theme, muiTheme, themeAdditions);
        }
        return createMuiTheme(muiTheme);
    };

    // get apisAndApiProducts
    getData = () => {
        this.xhrRequest().then((data) => {
            const { body } = data;
            const { list, pagination } = body;
            const { total } = pagination;
            this.count = total;
            this.setState({ apisAndApiProducts: list, notFound: false });
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
            const { list } = body;
            this.setState({
                apisAndApiProducts: list,
                notFound: false,
            });
            this.setLocalStorage();
        });
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
    xhrRequest = () => {
        const { page, rowsPerPage } = this;
        const { isAPIProduct } = this.props;
        if (isAPIProduct) {
            return APIProduct.all({ limit: this.rowsPerPage, offset: page * rowsPerPage });
        } else {
            return API.all({ limit: this.rowsPerPage, offset: page * rowsPerPage });
        }
    };

    render() {
        const { intl, isAPIProduct, classes } = this.props;
        const columns = [
            {
                name: 'id',
                options: {
                    display: 'excluded',
                    filter: false,
                },
            },
            {
                name: 'image',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.image',
                    defaultMessage: 'image',
                }),
                options: {
                    customBodyRender: (value, tableMeta) => {
                        if (tableMeta.rowData) {
                            const apiName = tableMeta.rowData[2];
                            return <ImageGenerator api={apiName} width={30} height={30} />;
                        }
                        return <span />;
                    },
                    sort: false,
                    filter: false,
                },
            },
            {
                name: 'name',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.name',
                    defaultMessage: 'name',
                }),
                options: {
                    customBodyRender: (value, tableMeta, updateValue, tableViewObj = this) => {
                        if (tableMeta.rowData) {
                            const { isAPIProduct } = tableViewObj.props; // eslint-disable-line no-shadow
                            const apiName = tableMeta.rowData[2];
                            const apiId = tableMeta.rowData[0];
                            if (isAPIProduct) {
                                return <Link to={'/api-products/' + apiId + '/overview'}>{apiName}</Link>;
                            }
                            return <Link to={'/apis/' + apiId + '/overview'}>{apiName}</Link>;
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
                    defaultMessage: 'version',
                }),
                options: {
                    sort: false,
                },
            },
            {
                name: 'context',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.context',
                    defaultMessage: 'context',
                }),
                options: {
                    sort: false,
                },
            },
            {
                name: 'provider',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.provider',
                    defaultMessage: 'provider',
                }),
                options: {
                    sort: false,
                },
            },
        ];
        const { page, count, rowsPerPage } = this;
        const { apisAndApiProducts, notFound, listType } = this.state;
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
                this.getData();
                this.setLocalStorage();
            },
        };
        if (listType === 'grid') {
            options.customRowRender = (data, dataIndex, rowIndex, tableViewObj = this) => {
                const { isAPIProduct } = tableViewObj.props; // eslint-disable-line no-shadow
                const [id, name, , version, context, provider] = data;
                const api = {
                    id,
                    name: name.props.api,
                    version,
                    context,
                    provider,
                };
                return <ApiThumb api={api} isAPIProduct={isAPIProduct} updateData={tableViewObj.updateData} />;
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
            options.filter = true;
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
        if (apisAndApiProducts.length === 0) {
            return (
                <React.Fragment>
                    <TopMenu
                        data={apisAndApiProducts}
                        count={count}
                        setListType={this.setListType}
                        isAPIProduct={isAPIProduct}
                        listType={listType}
                    />
                    <div className={classes.contentInside}>
                        <SampleAPI isAPIProduct={isAPIProduct} />;
                    </div>
                </React.Fragment>
            );
        }

        return (
            <React.Fragment>
                <TopMenu
                    data={apisAndApiProducts}
                    count={count}
                    setListType={this.setListType}
                    isAPIProduct={isAPIProduct}
                    listType={listType}
                />
                <div className={classes.contentInside}>
                    <MuiThemeProvider theme={this.getMuiTheme()}>
                        <MUIDataTable title='' data={apisAndApiProducts} columns={columns} options={options} />
                    </MuiThemeProvider>
                </div>
            </React.Fragment>
        );
    }
}

export default injectIntl(withStyles(styles)(TableView));

TableView.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
    isAPIProduct: PropTypes.bool.isRequired,
    theme: PropTypes.shape({
        custom: PropTypes.string,
    }).isRequired,
};
