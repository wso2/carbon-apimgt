import React, { Component } from 'react';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import PropTypes from 'prop-types';

import API from 'AppData/api';
import { Progress } from 'AppComponents/Shared';

import APIPropertyField from './APIPropertyField';

/**
 *
 *
 * @class BusinessPlans
 * @extends {Component}
 */
class BusinessPlans extends Component {
    /**
     *Creates an instance of BusinessPlans.
     * @param {Object} props
     * @memberof BusinessPlans
     */
    constructor(props) {
        super(props);
        this.state = {
            policies: null,
        };
    }

    /**
     *
     * @inheritdoc
     * @memberof BusinessPlans
     */
    componentDidMount() {
        this.props.api.getPolicies().then((policies) => this.setState({ policies }));
    }

    /**
     *
     * @inheritdoc
     * @returns {React.Component} Policies / Business plans list
     * @memberof BusinessPlans
     */
    render() {
        const { policies } = this.state;
        if (!policies) {
            return <Progress />;
        }
        const policiesList = policies.map((policy) => (
            <ListItem key={policy.policyName}>
                <ListItemText primary={policy.policyName} secondary={policy.description || policy.policyName} />
            </ListItem>
        ));
        return (
            <APIPropertyField name='Allowed Policies'>
                <List>{policiesList}</List>
            </APIPropertyField>
        );
    }
}

BusinessPlans.propTypes = {
    api: PropTypes.instanceOf(API).isRequired,
};

export default BusinessPlans;
