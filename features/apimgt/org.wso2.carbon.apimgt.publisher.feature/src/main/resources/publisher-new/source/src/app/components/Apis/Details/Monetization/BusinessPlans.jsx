import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import { Grid, Typography } from '@material-ui/core';
import Checkbox from '@material-ui/core/Checkbox';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import { withStyles } from '@material-ui/core/styles';

import API from 'AppData/api';
import { Progress } from 'AppComponents/Shared';
import { classes } from 'istanbul-lib-coverage';

const styles = ({
    root: {
        flexGrow: 1,
    },
    rightDataColumn: {
        display: 'flex',
        flex: 1,
    },
});

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
            policies: [],
            monetizedPolices: null,
            isUnmonetizedPlan: false,
        };
        this.monetizationQuery = this.monetizationQuery.bind(this);
    }

    /**
     *
     * @inheritdoc
     * @memberof BusinessPlans
     */
    componentDidMount() {
        const { api } = this.props;
        api.getSubscriptionPolicies(api.id).then((policies) => {
            const filteredPolicies = policies.filter(policy => policy.tierPlan === 'COMMERCIAL');
            this.setState({ policies: filteredPolicies });
        });
        api.getMonetization(api.id).then((status) => {
            this.setState({ monetizedPolices: status.properties });
        });
    }

    monetizationQuery(policyName) {
        const { monetizedPolices } = this.state;
        if (!(policyName in monetizedPolices || monetizedPolices === null)) {
            this.setState({ isUnmonetizedPlan: true });
        }
        return policyName in monetizedPolices;
    }

    /**
     *
     * @inheritdoc
     * @returns {React.Component} Policies / Business plans list
     * @memberof BusinessPlans
     */
    render() {
        const { policies, monetizedPolices } = this.state;
        if (monetizedPolices === null) {
            return <Progress />;
        }
        const policiesList = policies.map(policy => (
            <Grid item xs={6} spacing={2}>
                <FormControlLabel
                    control={
                        <Checkbox id='monetizationStatus' checked={this.monetizationQuery(policy.name)} />
                    }
                    label={policy.name}
                />
                {
                    Object.keys(policy.monetizationAttributes).map((key) => {
                        if (policy.monetizationAttributes[key] !== null) {
                            return (
                                <Typography component='p' variant='body1'>
                                    { key } : {policy.monetizationAttributes[key] }
                                </Typography>
                            );
                        } else {
                            return false;
                        }
                    })
                }
            </Grid>
        ));
        return (
            <Grid className={classes.root}>
                <Typography variant='subtitle' gutterBottom>
                    <FormattedMessage id='commercial.policies' defaultMessage='Commercial Policies' />
                </Typography>
                <Grid container className={classes.root} spacing={2}>
                    {policiesList}
                    {this.state.isUnmonetizedPlan}
                </Grid>
            </Grid>
        );
    }
}

BusinessPlans.propTypes = {
    api: PropTypes.instanceOf(API).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(BusinessPlans);
