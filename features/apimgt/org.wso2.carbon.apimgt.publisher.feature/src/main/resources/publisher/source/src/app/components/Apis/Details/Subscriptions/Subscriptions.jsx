import React, { Component } from 'react';
import { Table } from 'antd';
import PropTypes from 'prop-types';

import API from '../../../../data/api';
import { Progress } from '../../../Shared';
import ApiPermissionValidation from '../../../../data/ApiPermissionValidation';
import ResourceNotFound from '../../../Base/Errors/ResourceNotFound';

class Subscriptions extends Component {
    constructor(props) {
        super(props);
        this.api_uuid = props.match.params.api_uuid;
        this.state = {
            subscriptions: null,
            notFound: false,
        };
        this.updateProductionSubscription = this.updateProductionSubscription.bind(this);
    }

    componentDidMount() {
        const api = new API();
        const promisedAPI = api.get(this.api_uuid);
        promisedAPI
            .then((response) => {
                this.setState({ api: response.obj });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
        this.fetchData();
    }

    fetchData() {
        const api = new API();
        const promisedEndpoints = api.subscriptions(this.api_uuid);
        /* TODO: Handle catch case , auth errors and ect ~tmkb */
        promisedEndpoints.then((response) => {
            this.setState({ subscriptions: response.obj.list });
        });
    }

    updateProductionSubscription(event) {
        const { currentState } = event.target.dataset;
        const subscriptionId = event.target.dataset.sample;
        const api = new API();
        let setState = '';
        const eventId = event.target.id;

        if (eventId === 'PROD_ONLY_BLOCKED') {
            if (event.target.checked) {
                if (currentState === 'SANDBOX_ONLY_BLOCKED') {
                    setState = 'BLOCKED';
                } else {
                    setState = 'PROD_ONLY_BLOCKED';
                }
            } else if (currentState === 'BLOCKED') {
                setState = 'SANDBOX_ONLY_BLOCKED';
            } else {
                setState = 'ACTIVE';
            }
        } else if (eventId === 'SANDBOX_ONLY_BLOCKED') {
            if (event.target.checked) {
                if (currentState === 'PROD_ONLY_BLOCKED') {
                    setState = 'BLOCKED';
                } else {
                    setState = 'SANDBOX_ONLY_BLOCKED';
                }
            } else if (currentState === 'BLOCKED') {
                setState = 'PROD_ONLY_BLOCKED';
            } else {
                setState = 'ACTIVE';
            }
        }
        const promisedSubs = api.blockSubscriptions(subscriptionId, setState);
        promisedSubs.then(() => {
            this.fetchData();
        });
    }

    render() {
        const { api, subscriptions } = this.state;
        const { resourceNotFountMessage } = this.props;
        if (this.state.notFound) {
            return <ResourceNotFound message={resourceNotFountMessage} />;
        }
        if (!api) {
            return <Progress />;
        }

        const columns = [
            {
                title: 'Application',
                render: a => <div>{a.applicationInfo.name}</div>,
            },
            {
                title: 'Access Control for',
                render: a => (
                    <div>
                        <ApiPermissionValidation userPermissions={this.state.api.userPermissionsForApi}>
                            <ApiPermissionValidation
                                checkingPermissionType={ApiPermissionValidation.permissionType.MANAGE_SUBSCRIPTION}
                                userPermissions={this.state.api.userPermissionsForApi}
                            >
                                <label htmlFor='production'>
                                    Production
                                    <input
                                        type='checkbox'
                                        checked={
                                            a.subscriptionStatus === 'PROD_ONLY_BLOCKED' ||
                                            a.subscriptionStatus === 'BLOCKED'
                                        }
                                        data-sample={a.subscriptionId}
                                        id='PROD_ONLY_BLOCKED'
                                        data-current={a.subscriptionStatus}
                                        style={{ marginRight: '100px' }}
                                        onChange={this.updateProductionSubscription}
                                    />
                                </label>
                                <label htmlFor='sandbox'>
                                    Sandbox
                                    <input
                                        checked={
                                            a.subscriptionStatus === 'SANDBOX_ONLY_BLOCKED' ||
                                            a.subscriptionStatus === 'BLOCKED'
                                        }
                                        type='checkbox'
                                        id='SANDBOX_ONLY_BLOCKED'
                                        data-sample={a.subscriptionId}
                                        data-current={a.subscriptionStatus}
                                        onChange={this.updateProductionSubscription}
                                    />
                                </label>
                            </ApiPermissionValidation>
                        </ApiPermissionValidation>
                    </div>
                ),
            },
        ];
        return (
            <div>
                <h1>API Subscriptions</h1>

                <Table columns={columns} dataSource={subscriptions} />
            </div>
        );
    }
}

Subscriptions.defaultProps = {
    resourceNotFountMessage: 'Resource not found!',
};

Subscriptions.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.shape({
            api_uuid: PropTypes.string,
        }),
    }).isRequired,
    resourceNotFountMessage: PropTypes.string,
};
export default Subscriptions;
