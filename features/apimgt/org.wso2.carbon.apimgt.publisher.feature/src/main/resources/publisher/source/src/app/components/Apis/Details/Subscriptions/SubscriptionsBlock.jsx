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

import React, { Component } from 'react';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';

const blockType = {
    BLOCK_ALL: 'BLOCKED',
    BLOCK_PRODUCTION: 'PROD_ONLY_BLOCKED',
};

class SubscriptionsBlock extends Component {
    constructor(props) {
        super(props);
        this.state = {
            [blockType.BLOCK_ALL]: props.subscriptionStatus === blockType.BLOCK_ALL,
            [blockType.BLOCK_PRODUCTION]: props.subscriptionStatus === blockType.BLOCK_PRODUCTION,
        };
        this.handleChange = this.handleChange.bind(this);
    }

    /**
     * Handle onChange of subscription block
     * @param event onChange event
     */
    handleChange(event) {
        const { [blockType.BLOCK_ALL]: blockAll, [blockType.BLOCK_PRODUCTION]: blockProduction } = this.state;
        const {
            subscriptionId, blockAllSubs, blockProductionSubs, unblockSubs,
        } = this.props;
        const { value, checked } = event.target;

        if (checked) {
            let newBlockAll = blockAll;
            let newBlockProduction = blockProduction;

            if (value === blockType.BLOCK_ALL) {
                newBlockAll = checked;
                if (blockProduction === true) {
                    newBlockProduction = false;
                }
                if (blockAllSubs) {
                    blockAllSubs(subscriptionId);
                }
            }
            if (value === blockType.BLOCK_PRODUCTION) {
                newBlockProduction = checked;
                if (blockAll === true) {
                    newBlockAll = false;
                }
                if (blockProductionSubs) {
                    blockProductionSubs(subscriptionId);
                }
            }
            this.setState({ [blockType.BLOCK_ALL]: newBlockAll, [blockType.BLOCK_PRODUCTION]: newBlockProduction });
        } else {
            if (unblockSubs) {
                unblockSubs(subscriptionId);
            }
            this.setState({ [value]: checked });
        }
    }

    render() {
        const { [blockType.BLOCK_ALL]: blockAll, [blockType.BLOCK_PRODUCTION]: blockProduction } = this.state;

        return (
            <FormGroup row>
                <FormControlLabel
                    control={
                        <Checkbox
                            id='block-all'
                            checked={blockAll}
                            onChange={event => this.handleChange(event)}
                            value={blockType.BLOCK_ALL}
                            color='primary'
                        />
                    }
                    label={
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.SubscriptionsBlock.production.and.sandbox'
                            defaultMessage='Production and Sandbox'
                        />
                    }
                />
                <FormControlLabel
                    control={
                        <Checkbox
                            id='block-production'
                            checked={blockProduction}
                            onChange={event => this.handleChange(event)}
                            value={blockType.BLOCK_PRODUCTION}
                            color='primary'
                        />
                    }
                    label={
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.SubscriptionsBlock.production.only'
                            defaultMessage='Production only'
                        />
                    }
                />
            </FormGroup>
        );
    }
}

SubscriptionsBlock.propTypes = {
    subscriptionId: PropTypes.string.isRequired,
    subscriptionStatus: PropTypes.string.isRequired,
    blockAllSubs: PropTypes.func.isRequired,
    blockProductionSubs: PropTypes.func.isRequired,
    unblockSubs: PropTypes.func.isRequired,
};

export default SubscriptionsBlock;
