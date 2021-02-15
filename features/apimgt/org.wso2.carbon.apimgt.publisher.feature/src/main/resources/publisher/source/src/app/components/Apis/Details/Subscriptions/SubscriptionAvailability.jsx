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
import { FormattedMessage } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import FormControl from '@material-ui/core/FormControl';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import PropTypes from 'prop-types';
import Select from '@material-ui/core/Select';
import TenantAutocomplete from 'AppComponents/Apis/Details/Subscriptions/TenantAutocomplete';
import { isRestricted } from 'AppData/AuthManager';

const useStyles = makeStyles((theme) => ({
    root: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    formControl: {
        margin: theme.spacing(1),
        minWidth: 400,
    },
    textControl: {
        margin: theme.spacing(1),
        minWidth: 300,
    },
    selectEmpty: {
        marginTop: theme.spacing(2),
    },
    subscriptionAvailabilityPaper: {
        marginTop: theme.spacing(2),
        paddingLeft: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
    grid: {
        display: 'flex',
        margin: theme.spacing(1.25),
    },
    gridLabel: {
        marginTop: theme.spacing(3.5),
    },
    saveButton: {
        marginTop: theme.spacing(2),
    },
    heading: {
        marginTop: theme.spacing(3),
    },
    tenantsList: {
        height: theme.spacing(12),
    },
}));

/**
 * Allows user to select API availability for tenants
 * @export
 * @returns {React.Component} @inheritdoc
 */
export default function SimpleSelect(props) {
    const classes = useStyles();
    const {
        api, setAvailability, tenantList, setTenantList,
    } = props;
    let currentAvailability;
    if (api.subscriptionAvailability === null || api.subscriptionAvailability === 'CURRENT_TENANT') {
        currentAvailability = 'currentTenant';
    } else if (api.subscriptionAvailability === 'ALL_TENANTS') {
        currentAvailability = 'allTenants';
    } else if (api.subscriptionAvailability === 'SPECIFIC_TENANTS') {
        currentAvailability = 'specificTenants';
    }
    const [values, setValues] = React.useState({
        availability: currentAvailability,
    });
    const inputLabel = React.useRef(null);
    const [labelWidth, setLabelWidth] = React.useState(0);
    const isSpecificTenants = values.availability === 'specificTenants';
    const isUIElementDisabled = isRestricted(['apim:api_publish', 'apim:api_create'], api);

    React.useEffect(() => {
        setLabelWidth(inputLabel.current.offsetWidth);
    }, []);

    /**
     * Handle onchange for the subscription availability dropdown
     * @param {string} value the new value selected for subscription availability
     */
    function subscriptionAvailableTenants(value) {
        let availabilityValue;

        setValues({
            ...values,
            availability: value,
        });

        if (value === 'currentTenant') {
            availabilityValue = 'CURRENT_TENANT';
            setTenantList([]);
        } else if (value === 'allTenants') {
            availabilityValue = 'ALL_TENANTS';
            setTenantList([]);
        } else if (value === 'specificTenants') {
            availabilityValue = 'SPECIFIC_TENANTS';
        }
        setAvailability({
            subscriptionAvailability: availabilityValue,
        });
    }

    return (
        <Grid item xs={12} md={12} lg={12}>
            <Typography variant='h4' className={classes.heading}>
                <FormattedMessage
                    id='Apis.Details.Subscriptions.SubscriptionAvailability.subscription.availability'
                    defaultMessage='Subscription Availability'
                />
            </Typography>
            <Typography variant='caption' gutterBottom>
                <FormattedMessage
                    id='Apis.Details.Subscriptions.SubscriptionAvailability.sub.heading'
                    defaultMessage='Make subscriptions available to tenants'
                />
            </Typography>
            <Paper className={classes.subscriptionAvailabilityPaper}>
                <form className={classes.root} autoComplete='off' onSubmit={(e) => { e.preventDefault(); }}>
                    <Grid container xs={12} spacing={1} className={classes.grid}>
                        <Grid item xs={10}>
                            <FormControl
                                variant='outlined'
                                className={classes.formControl}
                                disabled={isUIElementDisabled}
                            >
                                <InputLabel ref={inputLabel} htmlFor='outlined-age-simple' />
                                <Select
                                    value={values.availability}
                                    onChange={({ target: { value } }) => {
                                        subscriptionAvailableTenants(value);
                                    }}
                                    labelWidth={labelWidth}
                                    displayEmpty
                                    name='availability'
                                    inputProps={{
                                        name: 'availability',
                                        id: 'outlined-availabi;ity-simple',
                                    }}
                                >
                                    <MenuItem value='currentTenant'>
                                        <FormattedMessage
                                            id='Apis.Details.Subscriptions.SubscriptionAvailability.current.tenant.only'
                                            defaultMessage='Available to current tenant only'
                                        />
                                    </MenuItem>
                                    <MenuItem value='allTenants'>
                                        <FormattedMessage
                                            id='Apis.Details.Subscriptions.SubscriptionAvailability.all.tenants'
                                            defaultMessage='Available to all the tenants'
                                        />
                                    </MenuItem>
                                    <MenuItem value='specificTenants'>
                                        <FormattedMessage
                                            id='Apis.Details.Subscriptions.SubscriptionAvailability.specific.tenants'
                                            defaultMessage='Available to specific tenants'
                                        />
                                    </MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                        {isSpecificTenants ? (
                            <Grid item xs={8} className={classes.tenantsList}>
                                <TenantAutocomplete setTenantList={setTenantList} tenantList={tenantList} api={api} />
                            </Grid>
                        ) : <Grid item xs={8} />}
                    </Grid>
                </form>
            </Paper>
        </Grid>
    );
}
SimpleSelect.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
    api: PropTypes.shape({ policies: PropTypes.arrayOf(PropTypes.shape({})) }).isRequired,
    setAvailability: PropTypes.func.isRequired,
    setTenantList: PropTypes.func.isRequired,
    tenantList: PropTypes.shape([]).isRequired,
};
