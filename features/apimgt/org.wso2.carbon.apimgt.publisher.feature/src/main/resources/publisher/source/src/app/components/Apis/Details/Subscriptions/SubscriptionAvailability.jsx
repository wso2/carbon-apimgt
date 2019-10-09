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
import Button from '@material-ui/core/Button';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import PropTypes from 'prop-types';
import Select from '@material-ui/core/Select';
import TenantAutocomplete from 'AppComponents/Apis/Details/Subscriptions/TenantAutocomplete';
import Alert from 'AppComponents/Shared/Alert';
import { isRestricted } from 'AppData/AuthManager';


const useStyles = makeStyles(theme => ({
    root: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    formControl: {
        margin: theme.spacing.unit * 1,
        marginLeft: theme.spacing.unit * -30,
        minWidth: 300,
    },
    textControl: {
        margin: theme.spacing.unit * 1,
        minWidth: 300,
    },
    selectEmpty: {
        marginTop: theme.spacing(2),
    },
    subscriptionAvailabilityPaper: {
        marginTop: theme.spacing.unit * 2,
        paddingLeft: theme.spacing.unit * 2,
        paddingBottom: theme.spacing.unit * 2,
    },
    grid: {
        display: 'flex',
        margin: theme.spacing.unit * 1.25,
    },
    gridLabel: {
        marginTop: theme.spacing.unit * 3.5,
    },
    saveButton: {
        marginLeft: theme.spacing.unit * 90,
        marginTop: theme.spacing.unit * 2,
    },
}));

/**
 * Allows user to select API availability for tenants
 * @export
 * @returns {React.Component} @inheritdoc
 */
export default function SimpleSelect(props) {
    const classes = useStyles();
    const { api, updateAPI } = props;
    const [tenantList, setTenantList] = React.useState([]);
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

    function subscriptionAvailableTenants() {
        let availabilityValue;
        let availableTenantsList = [];
        if (updateAPI) {
            if (values.availability === 'currentTenant') {
                availabilityValue = 'CURRENT_TENANT';
            } else if (values.availability === 'allTenants') {
                availabilityValue = 'ALL_TENANTS';
            } else if (values.availability === 'specificTenants') {
                availabilityValue = 'SPECIFIC_TENANTS';
                availableTenantsList = tenantList;
            }
            updateAPI({
                subscriptionAvailability: availabilityValue,
                subscriptionAvailableTenants: availableTenantsList,
            })
                .then(() => {
                    Alert.info('Tenant availability updated successfully');
                })
                .catch((error) => {
                    console.error(error);
                    Alert.error('Error occurred while updating tenant availability');
                });
        }
    }

    /**
     * Handles the availability that is selected
     * @param {event} event Validation state object
     */
    function handleChange(event) {
        setValues(oldValues => ({
            ...oldValues,
            [event.target.name]: event.target.value,
        }));
    }
    return (
        <Paper className={classes.subscriptionAvailabilityPaper}>
            <form className={classes.root} autoComplete='off' onSubmit={(e) => { e.preventDefault(); }}>
                <Grid container spacing={1} className={classes.grid}>
                    <Grid item xs={4} className={classes.gridLabel}>
                        <FormLabel>
                            <FormattedMessage
                                id='Apis.Details.Subscriptions.SubscriptionPoliciesManage.subscription.availability'
                                defaultMessage='Subscription Availability'
                            /> {' : '}
                        </FormLabel>
                    </Grid>
                    <Grid item xs={8} justify='space-between' spacing={32}>
                        <FormControl variant='outlined' className={classes.formControl} disabled={isUIElementDisabled}>
                            <InputLabel ref={inputLabel} htmlFor='outlined-age-simple' />
                            <Select
                                value={values.availability}
                                onChange={handleChange}
                                labelWidth={labelWidth}
                                displayEmpty
                                name='availability'
                                inputProps={{
                                    name: 'availability',
                                    id: 'outlined-availabi;ity-simple',
                                }}
                            >
                                <MenuItem value='currentTenant'>Available to current tenant only</MenuItem>
                                <MenuItem value='allTenants'>Available to all the tenants</MenuItem>
                                <MenuItem value='specificTenants'>Available to specific tenants</MenuItem>
                            </Select>
                        </FormControl>
                        <Button
                            variant='contained'
                            color='primary'
                            onClick={subscriptionAvailableTenants}
                            className={classes.saveButton}
                            disabled={isUIElementDisabled}
                        >
                            <FormattedMessage id='Apis.Details.Scopes.CreateScope.save' defaultMessage='Save' />
                        </Button>
                    </Grid>
                    {isSpecificTenants ? (
                        <Grid item xs={8} >
                            <TenantAutocomplete setTenantList={setTenantList} api={api} />
                        </Grid>
                    ) : <Grid item xs={8} />}
                </Grid>
            </form>
        </Paper >
    );
}
SimpleSelect.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
    api: PropTypes.shape({ policies: PropTypes.array }).isRequired,
    updateAPI: PropTypes.func.isRequired,
};

