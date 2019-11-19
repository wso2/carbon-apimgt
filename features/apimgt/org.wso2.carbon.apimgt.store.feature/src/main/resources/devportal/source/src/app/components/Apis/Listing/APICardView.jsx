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
    /**
     *
     *
     * @returns
     * @memberof APICardView
     */
    render() {
        const { apisNotFound } = this.props;

        if (apisNotFound) {
            return <ResourceNotFound />;
        }

        const {
            theme, unsubscribedAPIList, handleSubscribe, applicationId, intl,
        } = this.props;
        const columns = [
            {
                name: 'Id',
                label: intl.formatMessage({
                    id: 'Apis.Listing.APIList.id',
                    defaultMessage: 'Id',
                }),
                options: {
                    display: 'excluded',
                },
            },
            {
                name: 'Name',
                label: intl.formatMessage({
                    id: 'Apis.Listing.APIList.name',
                    defaultMessage: 'Name',
                }),
            },
            {
                name: 'Policy',
                label: intl.formatMessage({
                    id: 'Apis.Listing.APIList.policy',
                    defaultMessage: 'Policy',
                }),
                options: {
                    customBodyRender: (value, tableMeta, updateValue) => {
                        if (tableMeta.rowData) {
                            const apiId = tableMeta.rowData[0];
                            const policies = value;
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

        return (
            <MUIDataTable
                title={<FormattedMessage defaultMessage='APIs' id='Apis.Listing.APIList.apis' />}
                data={unsubscribedAPIList}
                columns={columns}
                options={{ selectableRows: false, print: false, download: false }}
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
