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
import { FormattedMessage } from 'react-intl';
import { ScopeValidation, resourceMethods, resourcePaths } from '../../Shared/ScopeValidation';

const styles = (theme) => ({
    root: {
        display: 'flex',
    },
    buttonGap: {
        marginLeft: 20,
        '& span span': {
            color: theme.palette.getContrastText(theme.palette.primary.main),
        },
    },
    select: {
        width: 100,
    },
});

/**
 * @class SubscriptionPolicySelect
 * @extends {React.Component}
 */
class SubscriptionPolicySelect extends React.Component {
    /**
     * Create instance of SubscriptionPolicySelect
     * @param {JSON} props Props pass from the parent.
     * @returns {void}
     */
    constructor(props) {
        super(props);
        this.state = {
            selectedPolicy: null,
        };
    }

    /**
     * Calls when component did mount.
     * @returns {void}
     */
    componentDidMount() {
        const { policies } = this.props;

        this.setState({ selectedPolicy: policies[0] });
    }

    /**
     * renders method.
     * @returns {JSX} Policy selection component.
     */
    render() {
        const {
            classes, policies, apiId, handleSubscribe, applicationId,
        } = this.props;
        const { selectedPolicy } = this.state;

        return (
            policies
            && (
                <div className={classes.root}>
                    <Select
                        className={classes.select}
                        value={selectedPolicy}
                        onChange={(e) => {
                            this.setState({ selectedPolicy: e.target.value });
                        }}
                    >
                        {policies.map((policy) => (
                            <MenuItem value={policy} key={policy}>
                                {policy}
                            </MenuItem>
                        ))}

                    </Select>
                    <ScopeValidation
                        resourcePath={resourcePaths.SUBSCRIPTIONS}
                        resourceMethod={resourceMethods.POST}
                    >
                        <Button
                            variant='contained'
                            size='small'
                            className={classes.buttonGap}
                            onClick={() => {
                                handleSubscribe(applicationId, apiId, selectedPolicy);
                            }}
                        >
                            <FormattedMessage defaultMessage='Subscribe' id='Apis.Listing.SubscriptionPolicySelect.subscribe' />
                        </Button>
                    </ScopeValidation>
                </div>
            )
        );
    }
}

SubscriptionPolicySelect.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    policies: PropTypes.shape({}).isRequired,
    apiId: PropTypes.string.isRequired,
    handleSubscribe: PropTypes.func.isRequired,
    applicationId: PropTypes.string.isRequired,
};

export default withStyles(styles, { withTheme: true })(SubscriptionPolicySelect);
