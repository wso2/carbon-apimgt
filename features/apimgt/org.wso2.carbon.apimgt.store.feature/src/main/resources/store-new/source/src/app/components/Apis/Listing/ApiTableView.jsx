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
import { FormattedMessage, injectIntl } from 'react-intl';
import queryString from 'query-string';
import API from 'AppData/api';
import APIProduct from 'AppData/APIProduct';
import CONSTS from 'AppData/Constants';
import Configurations from 'Config';
import StarRatingBar from 'AppComponents/Apis/Listing/StarRatingBar';
import ImageGenerator from './ImageGenerator';
import ApiThumb from './ApiThumb';
import DocThumb from './DocThumb';
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

const styles = (theme) => ({
    rowImageOverride: {
        '& .material-icons': {
            marginTop: 5,
            color: `${theme.custom.thumbnail.iconColor} !important` ,
            fontSize: `${theme.custom.thumbnail.listViewIconSize}px !important` ,
        }
    }
});
class ApiTableView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            data: [],
        };
        this.page = 0;
        this.count = 100;
        this.rowsPerPage = 10;
        this.pageType = null;
    }

    getMuiTheme = () => {
        const { gridView } = this.props;
        let themeAdditions = {};
        let muiTheme = {
            overrides: {
                MUIDataTable: {
                    root: {
                        backgroundColor: 'transparent',
                        marginLeft: 40,
                        marginBottom: 20,
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
            themeAdditions = {
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
        }
        muiTheme = Object.assign(muiTheme, themeAdditions, Configurations.themes.light);
        return createMuiTheme(muiTheme);
    };

    componentDidMount() {
        this.apiType = this.context.apiType;
        this.getData();
    }

    componentDidUpdate(prevProps) {
        const { query } = this.props;
        if (this.apiType !== this.context.apiType || query !== prevProps.query ) {
            this.apiType = this.context.apiType;
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
        const { query } = this.props;
        const api = new API();
        if (query) {
            const composeQuery = queryString.parse(query);
            composeQuery.limit = this.rowsPerPage;
            composeQuery.offset = page * rowsPerPage;
            return api.search(composeQuery);
        }
        if (apiType === CONSTS.API_TYPE) {
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
        });
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
                name: 'name',
                options: {
                    customBodyRender: (value, tableMeta, updateValue,tableViewObj = this) => {
                        if (tableMeta.rowData) {
                            const artifact = tableViewObj.state.data[tableMeta.rowIndex];
                            return <ImageGenerator api={artifact} width={30} height={30} />;
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
                            const artifact = tableViewObj.state.data[tableMeta.rowIndex];
                            const apiName = tableMeta.rowData[2];
                            const apiId = tableMeta.rowData[0];
                            const { classes } = this.props;
                            if (apiType === CONSTS.API_TYPE) {
                                if (artifact) {
                                    if (artifact.type === 'DOC') {
                                        return (
                                            <Link to={'/apis/' + artifact.apiUUID + '/docs'}>
                                            <ImageGenerator api={artifact} width={30} height={30} />
                                                <FormattedMessage
                                                    id='Apis.Listing.TableView.TableView.doc.flag'
                                                    defaultMessage='[Doc] '
                                                />
                                                {apiName}
                                            </Link>
                                        );
                                    }
                                    return (
                                        <Link to={'/apis/' + apiId + '/overview'} className={classes.rowImageOverride}>
                                            <ImageGenerator api={artifact} width={30} height={30}/>{apiName}</Link>);
                                }
                            } else {
                                return (<Link
                                    to={'/api-products/' + apiId + '/overview'}
                                    className={classes.rowImageOverride}>
                                    <ImageGenerator api={artifact} width={30} height={30}/>{apiName}</Link>);
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
                    customBodyRender: (value, tableMeta, updateValue, tableViewObj = this) => {
                        if (tableMeta.rowData) {
                            const artifact = tableViewObj.state.data[tableMeta.rowIndex];
                            if (artifact) {
                                if (artifact.type !== 'DOC') {
                                    const apiId = tableMeta.rowData[0];
                                    const avgRating = tableMeta.rowData[7];
                                    return <StarRatingBar
                                        apiRating={avgRating}
                                        apiId={apiId}
                                        isEditable={false}
                                        showSummary={false}
                                    />;
                                }
                            }
                        }
                    },
                    options: {
                        sort: false,
                    },
                },
            },
            {
                name: 'avgRating',
                options: {
                    display: 'excluded',
                    filter: false,
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
                const { page, count, } = this;
                if( page*numberOfRows > count){
                    this.page = 0;
                }
                this.rowsPerPage = numberOfRows;
                this.getData();
            },
        };
        if (gridView) {
            options.customRowRender = (data, dataIndex, rowIndex, tableViewObj = this) => {
                const artifact = tableViewObj.state.data[dataIndex];
                if (artifact) {
                    if (artifact.type === 'DOC') {
                        return (<DocThumb doc={artifact} />);
                    } else {
                        return (<ApiThumb api={artifact} />);
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
        }
        if(page === 0 && this.count <= rowsPerPage){
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

export default injectIntl(withStyles(styles)(ApiTableView));
