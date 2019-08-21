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

import React from 'react';
import { Link } from 'react-router-dom';
import { createMuiTheme, MuiThemeProvider, withStyles } from '@material-ui/core/styles';
import MUIDataTable from 'mui-datatables';
import { injectIntl } from 'react-intl';
import API from 'AppData/api';
import APIProduct from 'AppData/APIProduct';
import CONSTS from 'AppData/Constants';
import ImageGenerator from './ImageGenerator';
import StarRatingBar from './StarRating';
import ApiThumb from './ApiThumb';
import { ApiContext } from '../Details/ApiContext';

class StarRatingColumn extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            rating: null,
        };
        this.api = new API();
    }

    componentDidMount() {
        const promised_rating = this.api.getRatingFromUser(this.props.apiId, null);
        promised_rating
            .then((response) => {
                const rating = response.obj;
                this.setState({
                    rating: rating.userRating,
                });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    render() {
        const { rating } = this.state;
        return rating && <StarRatingBar rating={rating} />;
    }
}

class ApiTableView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            data: [],
        };
        this.page = 0;
        this.count = 100;
        this.rowsPerPage = 10;
        this.getLocalStorage();
        this.pageType = null;
    }

    getMuiTheme = () => {
        const { gridView } = this.props;
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
                        border: 'solid 1px #fff',
                        '& a': {
                            display: 'flex',
                            alignItems: 'center',
                        },
                        '& a > div': {
                            paddingRight: 10,
                        },
                        '& tr:nth-child(even)': {
                            backgroundColor: '#fff',
                        },
                    }
                },
                MUIDataTableBodyCell: {
                    root: {
                        backgroundColor: 'transparent',
                    },
                },
            },
        };
        if (gridView) {
            const themeAdditions = {
                overrides: {
                    MUIDataTable: {
                        tableRoot: {
                            display: 'block',
                            '& tbody': {
                                display: 'flex',
                                flexWrap: 'wrap',
                                marginLeft: 40,
                            },
                            '& thead': {
                                display: 'none',
                            },
                        },
                        paper: {
                            boxShadow: 'none',
                            backgroundColor: 'transparent',
                        },
                    },
                },
            };
            muiTheme = Object.assign(muiTheme, themeAdditions);
        }
        return createMuiTheme(muiTheme);
    };

    componentDidMount() {
        this.apiType = this.context.apiType;
        this.getLocalStorage();
        this.getData();
    }

    componentDidUpdate() {
        if (this.apiType !== this.context.apiType) {
            this.apiType = this.context.apiType;
            this.getLocalStorage();
            this.getData();
        }
    }

    // get data
    getData = () => {
        this.xhrRequest().then((data) => {
            const { body } = data;
            const { list, pagination } = body;
            const { total } = pagination;
            this.count = total;
            this.setState({ data: list });
        });
    };

    xhrRequest = () => {
        const { page, rowsPerPage } = this;
        const { apiType } = this.context;
        if (apiType === CONSTS.API_TYPE) {
            const api = new API();
            return api.getAllAPIs({ limit: this.rowsPerPage, offset: page * rowsPerPage });
        } else {
            const apiProduct = new APIProduct();
            return apiProduct.getAllAPIProducts({ limit: this.rowsPerPage, offset: page * rowsPerPage });
        }
    };

    changePage = (page) => {
        this.page = page;
        this.xhrRequest().then((data) => {
            const { body } = data;
            const { list } = body;
            this.setState({
                data: list,
            });
            this.setLocalStorage();
        });
    };

    setLocalStorage = () => {
        // Set the page to the localstorage
        const { apiType } = this.context;
        const paginationSufix = apiType === API_PRODUCT_TYPE ? 'products' : 'apis';
        const pagination = { page: this.page, count: this.count, rowsPerPage: this.rowsPerPage };
        window.localStorage.setItem('pagination-' + paginationSufix, JSON.stringify(pagination));
    };

    getLocalStorage = () => {
        const { paginationSufix } = this.props;
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

    render() {
        const { intl, gridView } = this.props;
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
                    customBodyRender: (value, tableMeta, updateValue) => {
                        if (tableMeta.rowData) {
                            const apiName = tableMeta.rowData[2];
                            return <ImageGenerator api={apiName} width={30} height={30} />;
                        }
                    },
                    sort: false,
                    filter: false,
                    display: 'excluded',
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
                            const { apiType } = this.context;
                            const apiName = tableMeta.rowData[2];
                            const apiId = tableMeta.rowData[0];
                            if (apiType === CONSTS.API_TYPE) {
                                return <Link to={'/apis/' + apiId + '/overview'}><ImageGenerator api={apiName} width={30} height={30} />{apiName}</Link>;
                            } else {
                                return <Link to={'/api-products/' + apiId + '/overview'}><ImageGenerator api={apiName} width={30} height={30} />{apiName}</Link>;
                            }
                        }
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
            {
                name: 'type',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.type',
                    defaultMessage: 'type',
                }),
                options: {
                    sort: false,
                },
            },
            {
                name: 'rating',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.rating',
                    defaultMessage: 'rating',
                }),
                options: {
                    customBodyRender: (value, tableMeta, updateValue) => {
                        if (tableMeta.rowData) {
                            const apiId = tableMeta.rowData[0];
                            return <StarRatingColumn apiId={apiId} />;
                        }
                    },
                    options: {
                        sort: false,
                    },
                },
            },
        ];
        const { page, count, rowsPerPage } = this;
        const { data } = this.state;
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
        if (gridView) {
            options.customRowRender = (data, dataIndex, rowIndex) => {
                const api = {};
                api.id = data[0];
                api.name = data[1].props.api;
                api.version = data[3];
                api.context = data[4];
                api.provider = data[5];
                api.type = data[6];
                return <ApiThumb api={api} />;
            };
            options.title = false;
            options.filter = false;
            options.print = false;
            options.download = false;
            options.viewColumns = false;
            options.customToolbar = false;
        }
        if(page === 0 && data.length < rowsPerPage){
            options.pagination = false;
        }
        return (
            <MuiThemeProvider theme={this.getMuiTheme()}>
                <MUIDataTable title='' data={data} columns={columns} options={options} />
            </MuiThemeProvider>
        );
    }
}

ApiTableView.contextType = ApiContext;

export default injectIntl(ApiTableView);
