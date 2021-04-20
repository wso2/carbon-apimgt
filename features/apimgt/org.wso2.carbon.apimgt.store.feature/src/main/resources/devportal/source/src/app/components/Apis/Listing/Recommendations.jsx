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
import { withTheme } from '@material-ui/styles';
import Configurations from 'Config';
import StarRatingBar from 'AppComponents/Apis/Listing/StarRatingBar';
import withSettings from 'AppComponents/Shared/withSettingsContext';
import Loading from 'AppComponents/Base/Loading/Loading';
import Alert from 'AppComponents/Shared/Alert';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import ImageGenerator from './APICards/ImageGenerator';
import RecommendedApiThumb from './RecommendedApiThumb';
import { ApiContext } from '../Details/ApiContext';

const styles = (theme) => ({
    rowImageOverride: {
        '& .material-icons': {
            marginTop: 5,
            color: `${theme.custom.thumbnail.iconColor} !important`,
            fontSize: `${theme.custom.thumbnail.listViewIconSize}px !important`,
        },
    },
    apiNameLink: {
        display: 'flex',
        alignItems: 'center',
        '& span': {
            marginLeft: theme.spacing(1),
        },
        color: theme.palette.getContrastText(theme.custom.listView.tableBodyEvenBackgrund),
    },
});
/**
 * Table view for api listing
 *
 * @class Recommendations
 * @extends {React.Component}
 */
class Recommendations extends React.Component {
    /**
     * @inheritdoc
     * @param {*} props properties
     * @memberof Recommendations
     */
    constructor(props) {
        super(props);
        this.state = {
            data: null,
            loading: true,
        };
    }

    /**
     * @memberof Recommendations
    */
    componentDidMount() {
        this.getData();
    }

    /**
     * @memberof Recommendations
     * @param {JSON} prevProps previous props
    */
    componentDidUpdate(prevProps) {
        const { query, selectedTag } = this.props;
        if (
            query !== prevProps.query
            || prevProps.selectedTag !== selectedTag
        ) {
            this.getData();
        }
    }

    getMuiTheme = () => {
        const { gridView, theme } = this.props;
        let themeAdditions = {};
        let muiTheme = {
            overrides: {
                MUIDataTable: {
                    root: {
                        backgroundColor: 'transparent',
                        marginLeft: 40,
                        marginBottom: 20,
                        width: '100%',
                    },
                    paper: {
                        boxShadow: 'none',
                        backgroundColor: 'transparent',
                        width: '100%',
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
                        '& td': {
                            whiteSpace: 'nowrap',
                        },
                        '& tr:nth-child(even)': {
                            backgroundColor: theme.custom.listView.tableBodyEvenBackgrund,
                            '& td': {
                                color: theme.palette.getContrastText(theme.custom.listView.tableBodyEvenBackgrund),
                            },
                        },
                        '& tr:nth-child(odd)': {
                            backgroundColor: theme.custom.listView.tableBodyOddBackgrund,
                            '& td': {
                                color: theme.palette.getContrastText(theme.custom.listView.tableBodyOddBackgrund),
                            },
                        },
                        '& th': {
                            backgroundColor: theme.custom.listView.tableHeadBackground,
                            color: theme.palette.getContrastText(theme.custom.listView.tableHeadBackground),
                        },
                    },
                },
                MUIDataTableBodyCell: {
                    root: {
                        backgroundColor: 'transparent',
                        width: '100%',
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
                                marginLeft: 0,
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
        muiTheme = Object.assign(muiTheme, themeAdditions, Configurations);
        return createMuiTheme(muiTheme);
    };


    // get data
    getData = () => {
        const { intl } = this.props;
        this.xhrRequest()
            .then((data) => {
                const { body } = data;
                const { list } = body;
                this.setState({ data: list });
            })
            .catch((error) => {
                const { response } = error;
                const { setTenantDomain } = this.props;
                if (response && response.body.code === 901300) {
                    setTenantDomain('INVALID');
                    Alert.error(intl.formatMessage({
                        defaultMessage: 'Invalid tenant domain',
                        id: 'Apis.Listing.Recommendations.invalid.tenant.domain',
                    }));
                } else {
                    Alert.error(intl.formatMessage({
                        defaultMessage: 'Error While Loading APIs',
                        id: 'Apis.Listing.Recommendations.error.loading',
                    }));
                }
            })
            .finally(() => {
                this.setState({ loading: false });
            });
    };

    xhrRequest = () => {
        const api = new API();
        return api.getApiRecommendations();
    };

    /**
     * @inheritdoc
     * @returns {Component}x
     * @memberof Recommendations
     */
    render() {
        const { intl, gridView } = this.props;
        const { loading } = this.state;
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
                    customBodyRender: (value, tableMeta, updateValue, tableViewObj = this) => {
                        if (tableMeta.rowData) {
                            const artifact = tableViewObj.state.data[tableMeta.rowIndex];
                            return <ImageGenerator api={artifact} width={30} height={30} />;
                        }
                        return <span />;
                    },
                    sort: false,
                    filter: false,
                    display: 'excluded',
                },
            },
            {
                name: 'name',
                label: intl.formatMessage({
                    id: 'Apis.Listing.Recommendations.name',
                    defaultMessage: 'Name',
                }),
                options: {
                    customBodyRender: (tableMeta, tableViewObj = this) => {
                        if (tableMeta.rowData) {
                            const artifact = tableViewObj.state.data[tableMeta.rowIndex];
                            const apiName = tableMeta.rowData[2];
                            const apiId = tableMeta.rowData[0];
                            const { classes } = this.props;

                            if (artifact) {
                                return (
                                    <Link
                                        to={'/apis/' + apiId + '/overview'}
                                        className={classes.apiNameLink}
                                    >
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
                name: 'rating',
                label: intl.formatMessage({
                    defaultMessage: 'Rating',
                    id: 'Apis.Listing.Recommendations.rating',
                }),
                options: {
                    customBodyRender: (value, tableMeta, updateValue, tableViewObj = this) => {
                        if (tableMeta.rowData) {
                            const artifact = tableViewObj.state.data[tableMeta.rowIndex];
                            if (artifact) {
                                if (artifact.type !== 'DOC') {
                                    const apiId = tableMeta.rowData[0];
                                    const avgRating = tableMeta.rowData[8];
                                    return (
                                        <StarRatingBar
                                            apiRating={avgRating}
                                            apiId={apiId}
                                            isEditable={false}
                                            showSummary={false}
                                        />
                                    );
                                } else {
                                    return (<span />);
                                }
                            } else {
                                return (<span />);
                            }
                        } else {
                            return (<span />);
                        }
                    },
                    sort: false,
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
        const { data } = this.state;
        const options = {
            filterType: 'dropdown',
            responsive: 'stacked',
            serverSide: true,
            search: false,
        };
        if (gridView) {
            // eslint-disable-next-line no-shadow
            options.customRowRender = (data, dataIndex, rowIndex, tableViewObj = this) => {
                const artifact = tableViewObj.state.data[dataIndex];
                if (artifact) {
                    return <tr key={rowIndex}><td><RecommendedApiThumb api={artifact} /></td></tr>;
                }
                return <span />;
            };
            options.title = true;
            options.filter = false;
            options.print = false;
            options.download = false;
            options.viewColumns = false;
            options.customToolbar = false;
            options.rowsPerPageOptions = false;
            options.pagination = false;
        } else {
            options.filter = false;
        }
        if (loading) {
            return <Loading />;
        }
        if ((data && data.length === 0) || !data) {
            return null;
        }
        return (
            <MuiThemeProvider theme={this.getMuiTheme()}>
                <MUIDataTable title='Recommended APIs for you' data={data} columns={columns} options={options} />
            </MuiThemeProvider>
        );
    }
}

Recommendations.contextType = ApiContext;

export default withSettings(injectIntl(withTheme(withStyles(styles)(Recommendations))));
