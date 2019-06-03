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
import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import Button from '@material-ui/core/Button';
import MUIDataTable from 'mui-datatables';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import Api from '../../../data/api';
import Alert from '../../Shared/Alert';


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
 * @class SubscribeItemObj
 * @extends {React.Component}
 */
class SubscribeItemObj extends React.Component {
    /**
     *
     *
     * @returns
     * @memberof SubscribeItemObj
     */
    render() {
        const {
            classes, policies, selectedPolicy, handleSelectedSubscriptionPolicyChange, apiId, subscribe,
        } = this.props;

        return (
            policies &&
            <div className={classes.root}>
                <Button
                    variant='contained'
                    size='small'
                    color='primary'
                    className={classes.buttonGap}
                    onClick={() => {
                        subscribe(apiId);
                    }}
                >
                    Subscribe
                </Button>
                <Select
                    value={selectedPolicy}
                    onChange={(e) => {
                        handleSelectedSubscriptionPolicyChange(apiId, e.target.value);
                    }}
                >
                    {policies.map(policy => (
                        <MenuItem value={policy}>
                            {policy}
                        </MenuItem>
                    ))}

                </Select>
            </div>
        );
    }
}

SubscribeItemObj.propTypes = {
    classes: PropTypes.object.isRequired,
};

const SubscribeItem = withStyles(styles)(SubscribeItemObj);


/**
 *
 *
 * @class APIList
 * @extends {React.Component}
 */
class APIList extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            selectedSubscriptionPolicy: {},
        };
        this.handleSelectedSubscriptionPolicyChange = this.handleSelectedSubscriptionPolicyChange.bind(this);
        this.subscribe = this.subscribe.bind(this);
        this.updateSelectedSubscriptionPolicy = this.updateSelectedSubscriptionPolicy.bind(this);
    }

    /**
     *
     * Handle onClick of subscription policy select of an API
     * @memberof APIList
     */
    handleSelectedSubscriptionPolicyChange(apiId, policy) {
        const { selectedSubscriptionPolicy } = this.state;

        const newSelectedSubscriptionPolicy = { ...selectedSubscriptionPolicy };
        newSelectedSubscriptionPolicy[apiId] = policy;
        this.setState({ selectedSubscriptionPolicy: newSelectedSubscriptionPolicy }, this.print);
    }

    /**
     *
     * Handle onClick of subscription to an API
     * @memberof APIList
     */
    subscribe(apiId) {
        const api = new Api();
        const { updateSubscriptions, applicationId } = this.props;
        const { selectedSubscriptionPolicy } = this.state;
        const policy = selectedSubscriptionPolicy[apiId];

        if (!policy) {
            Alert.error('Select a policy to subscribe');
            return;
        }
        const promisedSubscribe = api.subscribe(apiId, applicationId, policy);
        promisedSubscribe
            .then((response) => {
                if (response.status !== 201) {
                    Alert.error('subscription error');
                } else {
                    Alert.info('Subscription successful');
                    if (updateSubscriptions) {
                        updateSubscriptions(applicationId);
                    }
                }
            })
            .catch(() => {
                Alert.error('subscription error');
            });
    }

    /**
     *
     * Update the selected subscription policy of APIs when new unsubscribed APIs are received
     * @memberof APIList
     */
    updateSelectedSubscriptionPolicy() {
        const { unsubscribedAPIList } = this.props;
        const { selectedSubscriptionPolicy } = this.state;

        if (unsubscribedAPIList && unsubscribedAPIList.length > 0) {
            const newUnsubscribedAPISubscriptionPolicy = unsubscribedAPIList
                .filter(api => selectedSubscriptionPolicy[api.Id] === undefined
                    || api.Policy.indexOf(selectedSubscriptionPolicy[api.Id]) === -1)
                .reduce((acc, cur) => {
                    acc[cur.Id] = cur.Policy[0];
                    return acc;
                }, {});
            if (Object.keys(newUnsubscribedAPISubscriptionPolicy).length !== 0) {
                const newSelectedSubscriptionPolicy = {
                    ...selectedSubscriptionPolicy,
                    ...newUnsubscribedAPISubscriptionPolicy,
                };
                this.setState({ selectedSubscriptionPolicy: newSelectedSubscriptionPolicy }, this.print);
            }
        }
    }


    /**
     *
     *
     * @returns
     * @memberof APIList
     */
    render() {
        const { APIsNotFound } = this.state;

        if (APIsNotFound) {
            return <ResourceNotFound />;
        }

        this.updateSelectedSubscriptionPolicy();

        const { selectedSubscriptionPolicy } = this.state;
        const { theme, unsubscribedAPIList } = this.props;
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
                            const selectedPolicy = selectedSubscriptionPolicy[apiId];
                            return (
                                <SubscribeItem
                                    selectedPolicy={selectedPolicy}
                                    policies={policies}
                                    apiId={apiId}
                                    subscribe={this.subscribe}
                                    handleSelectedSubscriptionPolicyChange={this.handleSelectedSubscriptionPolicyChange}
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
