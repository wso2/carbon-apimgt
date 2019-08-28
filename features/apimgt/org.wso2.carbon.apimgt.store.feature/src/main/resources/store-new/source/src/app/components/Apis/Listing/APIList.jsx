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
import qs from 'qs';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Select from '@material-ui/core/Select';
import Button from '@material-ui/core/Button';
import MUIDataTable from 'mui-datatables';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import Api from '../../../data/api';
import Alert from '../../Shared/Alert';

const api = new Api();
let applicationId;
let updateSubscriptions;
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
    constructor(props) {
        super(props);
        this.state = {
            policies: null,
            policy: null,
        };
    }

    /**
     *
     *
     * @memberof SubscribeItemObj
     */
    componentDidMount() {
        const apiId = this.props.apiId;
        const promised_api = api.getAPIById(apiId);
        promised_api
            .then((response) => {
                let policy = null;
                if (response.obj.policies.length > 0) {
                    policy = response.obj.policies[0];
                }
                this.setState({ policies: response.obj.policies, policy });
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

    /**
     *
     *
     * @memberof SubscribeItemObj
     */
    handleChange = (event) => {
        this.setState({ policy: event.target.value });
    };

    /**
     *
     *
     * @memberof SubscribeItemObj
     */
    subscribe = (event) => {
        const { apiId } = this.props;
        const { policy } = this.state;
        if (!policy) {
            Alert.error('Select a policy to subscribe');
            return;
        }
        const promised_subscribe = api.subscribe(apiId, applicationId, policy);
        promised_subscribe
            .then((response) => {
                if (response.status !== 201) {
                    Alert.error('subscription error');
                } else {
                    Alert.error('Subuscription successfull');
                    updateSubscriptions(applicationId);
                }
            })
            .catch((error) => {
                Alert.error('subscription error');
            });
    };

    /**
     *
     *
     * @returns
     * @memberof SubscribeItemObj
     */
    render() {
        const { classes } = this.props;
        const { policies } = this.state;
        return (
            policies && (
                <div className={classes.root}>
                    <Button variant='contained' size='small' color='primary' className={classes.buttonGap} onClick={this.subscribe}>
                        Subscribe
                    </Button>
                    <Select value={this.state.policy} onChange={this.handleChange}>
                        {policies.map(policy => (
                            <option value={policy} key={policy}>
                                {policy}
                            </option>
                        ))}
                    </Select>
                </div>
            )
        );
    }
}
SubscribeItemObj.propTypes = {
    classes: PropTypes.object.isRequired,
};

const SubscribeItem = withStyles(styles)(SubscribeItemObj);

const columns = [
    {
        name: 'id',
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
                    return <SubscribeItem apiId={apiId} />;
                }
            },
        },
    },
    'name',
];

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
            apis: null,
            value: 1,
            order: 'asc',
            orderBy: 'name',
        };
        this.state.listType = this.props.theme.custom.defaultApiView;
    }

    /**
     *
     *
     * @memberof APIList
     */
    setListType = (value) => {
        this.setState({ listType: value });
    };

    /**
     *
     *
     * @param {*} nextProps
     * @memberof APIList
     */
    componentWillReceiveProps(nextProps) {
        this.setState({ data: nextProps.data });
    }

    /**
     *
     *
     * @memberof APIList
     */
    componentDidMount() {
        applicationId = this.props.applicationId;
        updateSubscriptions = this.props.updateSubscriptions;
        const promised_apis = api.getAllAPIs();
        promised_apis
            .then((response) => {
                const { subscriptions } = this.props;
                const { count, list } = response.obj;
                const filteredApis = [];
                for (let i = 0; i < count; i++) {
                    let apiIsSubscribed = false;
                    for (let j = 0; j < subscriptions.length; j++) {
                        if (list[i].id === subscriptions[j].apiIdentifier) {
                            apiIsSubscribed = true;
                        }
                    }
                    if (!apiIsSubscribed) {
                        filteredApis.push(list[i]);
                    }
                }
                this.setState({ apis: filteredApis });
            })
            .catch((error) => {
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    this.setState({ isAuthorize: false });
                    const params = qs.stringify({
                        reference: this.props.location.pathname,
                    });
                    this.props.history.push({ pathname: '/login', search: params });
                }
            });
    }

    /**
     *
     *
     * @returns
     * @memberof APIList
     */
    render() {
        if (this.state.notFound) {
            return <ResourceNotFound />;
        }

        const { apis } = this.state;
        const { theme } = this.props;

        return apis && <MUIDataTable title='APIs' data={apis} columns={columns} options={{ selectableRows: false }} />;
    }
}

APIList.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};
export default withStyles(styles, { withTheme: true })(APIList);
