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
import { injectIntl } from 'react-intl';
import API from 'AppData/api';
import NoApi from 'AppComponents/Apis/Listing/NoApi';
import Loading from 'AppComponents/Base/Loading/Loading';
import Alert from 'AppComponents/Shared/Alert';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import SubscriptionPolicySelect from './SubscriptionPolicySelect';


const styles = () => ({
    root: {
        display: 'flex',
    },
    buttonGap: {
        marginRight: 10,
    },
});

/**
 * @class APICardView
 * @param {number} page page number
 * @extends {React.Component}
 */
class APICardView extends React.Component {
    /**
     * @param {JSON} props properties passed from parent
     */
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

    /**
     * component mount callback
     */
    componentDidMount() {
        this.getData();
    }

    /**
     * @param {JSON} prevProps props from previous component instance
     */
    componentDidUpdate(prevProps) {
        const { subscriptions, searchText } = this.props;
        if (subscriptions.length !== prevProps.subscriptions.length) {
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
            .catch(() => {
                Alert.error(intl.formatMessage({
                    defaultMessage: 'Error While Loading APIs',
                    id: 'Apis.Listing.ApiTableView.error.loading',
                }));
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
            return api.getAllAPIs({ query: `${searchText} status:published`, limit: this.rowsPerPage, offset: page * rowsPerPage });
        } else {
            return api.getAllAPIs({ query: 'status:published', limit: this.rowsPerPage, offset: page * rowsPerPage });
        }
    };

    /**
    * Update list of unsubscribed APIs
    * @param {Array} list array of apis
    * @returns {Array} filtered list of apis
    * @memberof APICardView
    */
    updateUnsubscribedAPIsList(list) {
        const listLocal = list;
        const subscribedIds = this.getIdsOfSubscribedEntities();
        for (let i = 0; i < listLocal.length; i++) {
            if (!((!subscribedIds.includes(listLocal[i].id) && !listLocal[i].advertiseInfo.advertised)
                && listLocal[i].isSubscriptionAvailable)) {
                listLocal[i].throttlingPolicies = null;
            }
        }
        return listLocal;
        // return unsubscribedAPIList;
    }

    /**
     * @returns {JSX} render api card view
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
            handleSubscribe, applicationId, intl,
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
                name: 'isSubscriptionAvailable',
                label: intl.formatMessage({
                    id: 'Apis.Listing.APIList.isSubscriptionAvailable',
                    defaultMessage: 'Is Subscription Available',
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
                name: 'version',
                label: intl.formatMessage({
                    id: 'Apis.Listing.APIList.version',
                    defaultMessage: 'Version',
                }),
            },
            {
                name: 'throttlingPolicies',
                label: intl.formatMessage({
                    id: 'Apis.Listing.APIList.subscription.status',
                    defaultMessage: 'Subscription Status',
                }),
                options: {
                    customBodyRender: (value, tableMeta) => {
                        if (tableMeta.rowData) {
                            const apiId = tableMeta.rowData[0];
                            const isSubscriptionAvailable = tableMeta.rowData[1];
                            const policies = value;
                            if (!isSubscriptionAvailable) {
                                return (intl.formatMessage({
                                    id: 'Apis.Listing.APICardView.not.allowed',
                                    defaultMessage: 'Not Allowed',
                                }));
                            }
                            if (!policies) {
                                return (intl.formatMessage({
                                    id: 'Apis.Listing.APICardView.already.subscribed',
                                    defaultMessage: 'Subscribed',
                                }));
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
                        return <span />;
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
                    default:
                        break;
                }
            },
            selectableRows: 'none',
            rowsPerPage,
            onChangeRowsPerPage: (numberOfRows) => {
                const { page: pageInner, count: countInner } = this;
                if (pageInner * numberOfRows > countInner) {
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
                title=''
                data={data}
                columns={columns}
                options={options}
            />
        );
    }
}

APICardView.propTypes = {
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};
export default injectIntl(withStyles(styles, { withTheme: true })(APICardView));
