/*
 * Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import IconButton from '@material-ui/core/IconButton';
import Delete from '@material-ui/icons/Delete';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Dialog from '@material-ui/core/Dialog';
import Slide from '@material-ui/core/Slide';
import Button from '@material-ui/core/Button';
import { FormattedMessage } from 'react-intl';
import { ScopeValidation, resourceMethods, resourcePaths } from 'AppComponents/Shared/ScopeValidation';
import PropTypes from 'prop-types';

/**
 *
 *
 * @class SubscriptionTableData
 * @extends {React.Component}
 */
class SubscriptionTableData extends React.Component {
    /**
     *Creates an instance of SubscriptionTableData.
     * @param {*} props properties
     * @memberof SubscriptionTableData
     */
    constructor(props) {
        super(props);
        this.state = {
            openMenu: false,
        };
        this.handleRequestClose = this.handleRequestClose.bind(this);
        this.handleRequestOpen = this.handleRequestOpen.bind(this);
        this.handleRequestDelete = this.handleRequestDelete.bind(this);
    }

    /**
     *
     *
     * @memberof SubscriptionTableData
     */
    handleRequestClose() {
        this.setState({ openMenu: false });
    }

    /**
    *
    *
    * @memberof SubscriptionTableData
    */
    handleRequestOpen() {
        this.setState({ openMenu: true });
    }

    /**
     *
     * Handle onclick for subscription delete
     * @param {*} subscriptionId subscription id
     * @memberof SubscriptionTableData
     */
    handleRequestDelete(subscriptionId) {
        const { handleSubscriptionDelete } = this.props;
        this.setState({ openMenu: false });
        if (handleSubscriptionDelete) {
            handleSubscriptionDelete(subscriptionId);
        }
    }

    /**
    * @inheritdoc
    * @memberof SubscriptionTableData
    */
    render() {
        const {
            subscription: {
                apiInfo, apiProductInfo, status, throttlingPolicy, subscriptionId, apiId, apiProductId,
            },
        } = this.props;
        const { openMenu } = this.state;

        let link;
        if (apiId !== null) {
            link = <Link to={'/apis/' + apiId}>{apiInfo.name}</Link>;
        } else if (apiProductId !== null) {
            link = <Link to={'/api-products/' + apiProductId}>{apiProductInfo.name}</Link>;
        }

        return (
            <TableRow hover>
                <TableCell style={{ paddingLeft: 0 }}>
                    { link }
                </TableCell>
                <TableCell>{throttlingPolicy}</TableCell>
                <TableCell>{status}</TableCell>

                <TableCell>
                    <div>
                        <ScopeValidation
                            resourcePath={resourcePaths.SINGLE_SUBSCRIPTION}
                            resourceMethod={resourceMethods.DELETE}
                        >
                            <IconButton aria-label='Delete' onClick={this.handleRequestOpen}>
                                <Delete />
                            </IconButton>
                        </ScopeValidation>

                        <Dialog open={openMenu} transition={Slide}>
                            <DialogTitle>Confirm</DialogTitle>
                            <DialogContent>
                                <DialogContentText>
                                    <FormattedMessage
                                        id='Applications.Details.SubscriptionTableData.delete.subscription.confirmation'
                                        defaultMessage='Are you sure you want to delete the Subscription?'
                                    />
                                </DialogContentText>
                            </DialogContent>
                            <DialogActions>
                                <Button dense color='primary' onClick={this.handleRequestClose}>
                                    <FormattedMessage
                                        id='Applications.Details.SubscriptionTableData.cancel'
                                        defaultMessage='Cancel'
                                    />
                                </Button>
                                <Button dense color='primary' onClick={() => this.handleRequestDelete(subscriptionId)}>
                                    <FormattedMessage
                                        id='Applications.Details.SubscriptionTableData.delete'
                                        defaultMessage='Delete'
                                    />
                                </Button>
                            </DialogActions>
                        </Dialog>
                    </div>
                </TableCell>
            </TableRow>
        );
    }
}
SubscriptionTableData.propTypes = {
    subscription: PropTypes.shape({
        apiInfo: PropTypes.shape({
            name: PropTypes.string.isRequired,
        }).isRequired,
        throttlingPolicy: PropTypes.string.isRequired,
        subscriptionId: PropTypes.string.isRequired,
        apiId: PropTypes.string.isRequired,
        status: PropTypes.string.isRequired,
    }).isRequired,
    handleSubscriptionDelete: PropTypes.func.isRequired,
};
export default SubscriptionTableData;
