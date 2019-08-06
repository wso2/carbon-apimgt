import React from 'react';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { createMuiTheme, MuiThemeProvider, withStyles } from '@material-ui/core/styles';
import MUIDataTable from 'mui-datatables';
import { injectIntl } from 'react-intl';
import ImageGenerator from './ImageGenerator';
import StarRatingBar from './StarRating';
import API from '../../../data/api';
import ApiThumb from './ApiThumb';

function LinkGenerator(props) {
    return <Link to={'/apis/' + props.apiId}>{props.apiName}</Link>;
}

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
                    },
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
                    },
                },
            };
            muiTheme = Object.assign(muiTheme, themeAdditions);
        }
        return createMuiTheme(muiTheme);
    };

    componentDidMount() {
        this.getLocalStorage();
        this.getData();
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

    // mock async function
    xhrRequest = () => {
        const api = new API();
        const { page, rowsPerPage } = this;
        return api.getAllAPIs({ limit: this.rowsPerPage, offset: page * rowsPerPage });
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
        const pagination = { page: this.page, count: this.count, rowsPerPage: this.rowsPerPage };
        window.localStorage.setItem('pagination', JSON.stringify(pagination));
    };

    getLocalStorage = () => {
        const storedPagination = window.localStorage.getItem('pagination');
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
                },
            },
            {
                name: 'name',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.name',
                    defaultMessage: 'name',
                }),
                options: {
                    customBodyRender: (value, tableMeta, updateValue) => {
                        if (tableMeta.rowData) {
                            const apiName = tableMeta.rowData[2];
                            const apiId = tableMeta.rowData[0];
                            return <LinkGenerator apiName={apiName} apiId={apiId} />;
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
                return <ApiThumb api={api} />;
            };
            options.title = false;
            options.filter = false;
            options.print = false;
            options.download = false;
            options.viewColumns = false;
            options.customToolbar = false;
        }

        return (
            <MuiThemeProvider theme={this.getMuiTheme()}>
                <MUIDataTable title='' data={data} columns={columns} options={options} />
            </MuiThemeProvider>
        );
    }
}

export default injectIntl(ApiTableView);
