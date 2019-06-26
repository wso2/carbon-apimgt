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
 * @class APIList
 * @extends {React.Component}
 */
class APIList extends React.Component {
    /**
     *
     *
     * @returns
     * @memberof APIList
     */
    render() {
        const { APIsNotFound } = this.props;

        if (APIsNotFound) {
            return <ResourceNotFound />;
        }

        const {
            theme, unsubscribedAPIList, handleSubscribe, applicationId,
        } = this.props;
        const columns = [
            {
                name: 'Id',
                options: {
                    display: 'excluded',
                },
            },
            {
                name: 'Policy',
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
            'Name',
        ];

        return (
            <MUIDataTable
                title='APIs'
                data={unsubscribedAPIList}
                columns={columns}
                options={{ selectableRows: false }}
            />
        );
    }
}

APIList.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};
export default withStyles(styles, { withTheme: true })(APIList);
