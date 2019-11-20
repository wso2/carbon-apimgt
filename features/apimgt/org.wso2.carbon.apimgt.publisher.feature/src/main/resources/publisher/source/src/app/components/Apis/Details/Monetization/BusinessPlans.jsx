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
import { isRestricted } from 'AppData/AuthManager';

const styles = (theme) => ({
    root: {
        flexGrow: 1,
        paddingBottom: '10px',
    },
    margin: {
        margin: theme.spacing(),
    },
    rightDataColumn: {
        display: 'flex',
        flex: 1,
    },
    grid: {
        marginTop: '10px',
        paddingRight: '10px',
        paddingBottom: '10px',
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
            const filteredPolicies = policies.filter((policy) => policy.tierPlan === 'COMMERCIAL');
            this.setState({ policies: filteredPolicies });
        });
        api.getMonetization(api.id).then((status) => {
            this.setState({ monetizedPolices: status.properties });
        });
    }

    monetizationQuery(policyName) {
        const { monetizedPolices } = this.state;
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
        const { api } = this.props;
        if (monetizedPolices === null) {
            return <Progress />;
        }
        const policiesList = policies.map((policy) => (
            <Grid item xs={6} spacing={2}>
                <FormControlLabel
                    control={(
                        <Checkbox
                            id='monetizationStatus'
                            checked={this.monetizationQuery(policy.name)}
                            disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                            color='primary'
                        />
                    )}
                    label={policy.name}
                />
                {
                    Object.keys(policy.monetizationAttributes).map((key) => {
                        if (policy.monetizationAttributes[key] !== null) {
                            if (key === 'currencyType') {
                                return (
                                    <Typography component='p' variant='body1'>
                                        <FormattedMessage
                                            id='Apis.Details.Monetization.BusinessPlans.currencyType'
                                            defaultMessage='Currency Type'
                                        />
                                        {' '}
:
                                        {policy.monetizationAttributes[key]}
                                    </Typography>
                                );
                            } else if (key === 'billingCycle') {
                                return (
                                    <Typography component='p' variant='body1'>
                                        <FormattedMessage
                                            id='Apis.Details.Monetization.BusinessPlans.billingCycle'
                                            defaultMessage='Billing Cycle'
                                        />
                                        {' '}
:
                                        {policy.monetizationAttributes[key]}
                                    </Typography>
                                );
                            } else if (key === 'fixedPrice') {
                                return (
                                    <Typography component='p' variant='body1'>
                                        <FormattedMessage
                                            id='Apis.Details.Monetization.BusinessPlans.fixedPrice'
                                            defaultMessage='Fixed Price'
                                        />
                                        {' '}
:
                                        {policy.monetizationAttributes[key]}
                                    </Typography>
                                );
                            } else if (key === 'pricePerRequest') {
                                return (
                                    <Typography component='p' variant='body1'>
                                        <FormattedMessage
                                            id='Apis.Details.Monetization.BusinessPlans.pricePerRequest'
                                            defaultMessage='Price per Request'
                                        />
                                        {' '}
:
                                        {policy.monetizationAttributes[key]}
                                    </Typography>
                                );
                            } else {
                                return (
                                    <Typography component='p' variant='body1'>
                                        { key }
                                        {' '}
:
                                        {policy.monetizationAttributes[key]}
                                    </Typography>
                                );
                            }
                        } else {
                            return false;
                        }
                    })
                }
            </Grid>
        ));
        return (
            <Grid container className={classes.root}>
                <Grid className={classes.grid} spacing={2}>
                    <Grid>
                        <Typography variant='subtitle' gutterBottom>
                            <FormattedMessage
                                id='Apis.Details.Monetization.BusinessPlans.commercial.policies'
                                defaultMessage='Commercial Policies'
                            />
                        </Typography>
                    </Grid>
                    {
                        (policies.length > 0) ? (
                            <Grid>
                                <Typography>
                                    <FormattedMessage
                                        id='Apis.Details.Monetization.BusinessPlans.unchecked.policies'
                                        defaultMessage='Unchecked polices are not monetized, click `Save` to monetize'
                                    />
                                </Typography>
                            </Grid>
                        ) : (
                            <Grid>
                                <Typography>
                                    <FormattedMessage
                                        id='Apis.Details.Monetization.BusinessPlans.no.commercial.policies.to.monetize'
                                        defaultMessage='No commercial policies to monetize'
                                    />
                                </Typography>
                            </Grid>
                        )
                    }
                    <Grid container className={classes.root} spacing={2}>
                        {policiesList}
                    </Grid>
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
