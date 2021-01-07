import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import { Grid, Typography } from '@material-ui/core';
import Box from '@material-ui/core/Box';
import CheckIcon from '@material-ui/icons/Check';
import CloseIcon from '@material-ui/icons/Close';
import Table from '@material-ui/core/Table';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import { withStyles } from '@material-ui/core/styles';

import API from 'AppData/api';
import Banner from 'AppComponents/Shared/Banner';
import { Progress } from 'AppComponents/Shared';

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
    box: {
        display: 'block',
    },
    tableCel: {
        width: 50,
    },
    table: {
        width: '100%',
        border: 'solid 1px #ccc',
    },
    tableHeadCell: {
        color: 'black',
        background: theme.palette.grey[200],
    },
    stateWrapper: {
        display: 'flex',
        flexDirection: 'row',
    },
    tableHeadTitle: {
        flex: 1,
        fontWeight: 'bold',
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
        const { classes } = this.props;
        if (monetizedPolices === null) {
            return <Progress />;
        }
        const policiesList = policies.map((policy) => (
            <Grid item xs={12}>
                <Table className={classes.table}>
                    <TableRow>
                        <TableCell variant='head' colSpan={2} className={classes.tableHeadCell}>
                            <Box display='flex'>
                                <Typography component='div' className={classes.tableHeadTitle} variant='subtitle1'>
                                    {policy.name}
                                </Typography>
                                {
                                    this.monetizationQuery(policy.name) ? (
                                        <div className={classes.stateWrapper}>
                                            <div><CheckIcon color='primary' /></div>
                                            <Typography component='div'>
                                                <FormattedMessage
                                                    id='Apis.Details.Monetization.BusinessPlans.monetized'
                                                    defaultMessage='Monetized'
                                                />
                                            </Typography>
                                        </div>
                                    ) : (
                                        <div className={classes.stateWrapper}>
                                            <div><CloseIcon color='error' /></div>
                                            <Typography component='div'>
                                                <FormattedMessage
                                                    id='Apis.Details.Monetization.BusinessPlans.not.monetized'
                                                    defaultMessage='Not Monetized'
                                                />
                                            </Typography>
                                        </div>
                                    )
                                }
                            </Box>
                        </TableCell>
                    </TableRow>
                    {Object.keys(policy.monetizationAttributes).map((key) => {
                        if (policy.monetizationAttributes[key] !== null) {
                            return (
                                <TableRow>
                                    <TableCell className={classes.tableCel} align='left'>
                                        <Typography component='p' variant='subtitle2'>
                                            {key}
                                        </Typography>
                                    </TableCell>
                                    <TableCell align='left'>
                                        {policy.monetizationAttributes[key]}
                                    </TableCell>
                                </TableRow>
                            );
                        } else {
                            return false;
                        }
                    })}

                </Table>
            </Grid>
        ));
        return (
            <Grid container spacing={1}>
                <Grid item xs={12}>
                    <Typography variant='h6'>
                        <FormattedMessage
                            id='Apis.Details.Monetization.BusinessPlans.commercial.policies'
                            defaultMessage='Commercial Policies'
                        />
                    </Typography>
                </Grid>
                <Grid item xs={12}>
                    <Grid container direction='row' spacing={3}>
                        {policiesList}
                    </Grid>
                </Grid>
                <Grid item xs={12}>
                    {
                        (policies.length > 0) ? (
                            <Banner
                                disableActions
                                dense
                                paperProps={{ elevation: 1 }}
                                type='info'
                                message='Click Save to monetize all unmonetized policies'
                            />
                        ) : (
                            <Banner
                                disableActions
                                dense
                                paperProps={{ elevation: 1 }}
                                type='info'
                                message='No commercial policies to monetize'
                            />
                        )
                    }
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
