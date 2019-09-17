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
import withStyles from '@material-ui/core/styles/withStyles';
import API from 'AppData/api';
import CONSTS from 'AppData/Constants';
import SubscriptionsTable from './SubscriptionsTable';
import SubscriptionPoliciesManage from './SubscriptionPoliciesManage';
import SubscriptionAvailability from './SubscriptionAvailability';

const styles = theme => ({
    button: {
        margin: theme.spacing.unit,
    },
});

/**
 * Subscriptions component
 *
 * @class Subscriptions
 * @extends {Component}
 */
function Subscriptions(props) {
    const { api, updateAPI } = props;
    const restApi = new API();
    const [tenants, setTenants] = useState([]);
    useEffect(() => {
        restApi.getTenantsByState(CONSTS.TENANT_STATE_ACTIVE)
            .then((result) => {
                setTenants(result.body.count);
            });
    }, []);

    return (
        <div>
            <SubscriptionsTable api={api} />
            <SubscriptionPoliciesManage api={api} updateAPI={updateAPI} />
            {tenants !== 0 && (
                <SubscriptionAvailability api={api} updateAPI={updateAPI} />
            )}
        </div>
    );
}

Subscriptions.propTypes = {
    api: PropTypes.shape({}).isRequired,
    updateAPI: PropTypes.func.isRequired,
};

export default withStyles(styles)(Subscriptions);
