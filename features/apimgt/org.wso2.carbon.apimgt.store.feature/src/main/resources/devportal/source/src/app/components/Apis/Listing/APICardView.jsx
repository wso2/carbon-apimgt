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
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import MUIDataTable from 'mui-datatables';
import { FormattedMessage, injectIntl } from 'react-intl';
import API from 'AppData/api';
import NoApi from 'AppComponents/Apis/Listing/NoApi';
import Loading from 'AppComponents/Base/Loading/Loading';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import SubscriptionPolicySelect from './SubscriptionPolicySelect';


/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    root: {
        display: 'flex',
    },
    buttonGap: {
        marginRight: 10,
    },
});

/**
 *
 *
 * @class APICardView
 * @extends {React.Component}
 */
class APICardView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            data: null,
            loading: true,
        };
        this.page = 0;
        this.count = 100;
        this.rowsPerPage = 10;
        this.pageType = null;
    }
    componentDidMount() {
        this.getData();
    }

    componentDidUpdate(prevProps) {
        const { subscriptions, searchText } = this.props;
        if ( subscriptions.length !== prevProps.subscriptions.length ) {
            this.getData();
        } else if (searchText !== prevProps.searchText) {
            this.page = 0;
            this.getData();
        }
    }

    // get data
    getData = () => {
        const { intl } = this.props;
        this.xhrRequest()
            .then((data) => {
                const { body } = data;
                const { list, pagination } = body;
                const { total } = pagination;
                this.count = total;
                this.setState({ data: this.updateUnsubscribedAPIsList(list) });
            })
            .catch((error) => {
                const { response } = error;
                const { setTenantDomain } = this.props;
                if (response && response.body.code === 901300) {
                    setTenantDomain('INVALID');
                    Alert.error(intl.formatMessage({
                        defaultMessage: 'Invalid tenant domain',
                        id: 'Apis.Listing.ApiTableView.invalid.tenant.domain',
                    }));
                } else {
                    Alert.error(intl.formatMessage({
                        defaultMessage: 'Error While Loading APIs',
                        id: 'Apis.Listing.ApiTableView.error.loading',
                    }));
                }
            })
            .finally(() => {
                this.setState({ loading: false });
            });
    };

    xhrRequest = () => {
        const { searchText } = this.props;
        const { page, rowsPerPage } = this;
        const api = new API();

        if (searchText && searchText !== '') {
            return api.getAllAPIs({ query: searchText, limit: this.rowsPerPage, offset: page * rowsPerPage });
        } else {
            return api.getAllAPIs({ limit: this.rowsPerPage, offset: page * rowsPerPage });
        }
    };

    changePage = (page) => {
        const { intl } = this.props;
        this.page = page;
        this.setState({ loading: true });
        this.xhrRequest()
            .then((data) => {
                const { body } = data;
                const { list } = body;
                this.setState({
                    data: this.updateUnsubscribedAPIsList(list),
                });
            })
            .catch((e) => {
                Alert.error(intl.formatMessage({
                    defaultMessage: 'Error While Loading APIs',
                    id: 'Apis.Listing.ApiTableView.error.loading',
                }));
            })
            .finally(() => {
                this.setState({ loading: false });
            });
    };

    /**
        *
        * Get List of the Ids of all APIs that have been already subscribed
        *
        * @returns {*} Ids of respective APIs
        * @memberof APICardView
        */
    getIdsOfSubscribedEntities() {
        const { subscriptions } = this.props;

        // Get arrays of the API Ids and remove all null/empty references by executing 'fliter(Boolean)'
        const subscribedAPIIds = subscriptions.map((sub) => sub.apiId).filter(Boolean);

        return subscribedAPIIds;
    }

    /**
    *
    * Update list of unsubscribed APIs
    * @memberof APICardView
    */
    updateUnsubscribedAPIsList(list) {

        const subscribedIds = this.getIdsOfSubscribedEntities();
        for (let i = 0; i < list.length; i++) {
            if ((!subscribedIds.includes(list[i].id) && !list[i].advertiseInfo.advertised)
                && list[i].isSubscriptionAvailable) {
            } else {
                list[i].throttlingPolicies = null;
            }
        }
        return list;
        //return unsubscribedAPIList;

    }

    /**
     *
     *
     * @returns
     * @memberof APICardView
     */
    render() {
        const { apisNotFound } = this.props;
        const { loading, data } = this.state;
        const { page, count, rowsPerPage } = this;

        if (apisNotFound) {
            return <ResourceNotFound />;
        }

        const {
            theme, handleSubscribe, applicationId, intl,
        } = this.props;
        const columns = [
            {
                name: 'id',
                label: intl.formatMessage({
                    id: 'Apis.Listing.APIList.id',
                    defaultMessage: 'Id',
                }),
                options: {
                    display: 'excluded',
                },
            },
            {
                name: 'name',
                label: intl.formatMessage({
                    id: 'Apis.Listing.APIList.name',
                    defaultMessage: 'Name',
                }),
            },
            {
                name: 'throttlingPolicies',
                label: intl.formatMessage({
                    id: 'Apis.Listing.APIList.policy',
                    defaultMessage: 'Policy',
                }),
                options: {
                    customBodyRender: (value, tableMeta, updateValue) => {
                        if (tableMeta.rowData) {
                            const apiId = tableMeta.rowData[0];
                            const policies = value;
                            if (!policies) {
                                return (intl.formatMessage({
                                    id: 'Apis.Listing.APICardView.already.subscribed',
                                    defaultMessage: 'Subscribed',
                                }))
                            }
                            return (
                                <SubscriptionPolicySelect
                                    key={apiId}
                                    policies={policies}
                                    apiId={apiId}
                                    handleSubscribe={(app, api, policy) => handleSubscribe(app, api, policy)}
                                    applicationId={applicationId}
                                />
                            );
                        }
                    },
                },
            },
        ];
        const options = {
            search: false,
            title: false,
            filter: false,
            print: false,
            download: false,
            viewColumns: false,
            customToolbar: false,
            responsive: 'stacked',
            serverSide: true,
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
                const { page, count } = this;
                if (page * numberOfRows > count) {
                    this.page = 0;
                }
                this.rowsPerPage = numberOfRows;
                this.getData();
            },
        };
        if (loading) {
            return <Loading />;
        }
        if ((data && data.length === 0) || !data) {
            return <NoApi />;
        }
        return (
            <MUIDataTable
                title={''}
                data={data}
                columns={columns}
                options={options}
            />
        );
    }
}

APICardView.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};
export default injectIntl(withStyles(styles, { withTheme: true })(APICardView));
