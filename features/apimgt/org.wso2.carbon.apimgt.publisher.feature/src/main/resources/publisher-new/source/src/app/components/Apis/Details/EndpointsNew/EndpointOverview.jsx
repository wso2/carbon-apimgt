/**
 * Copyright (c)  WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React from 'react';
import PropTypes from 'prop-types';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import Divider from '@material-ui/core/Divider';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';

import EndpointListing from './EndpointListing';


const styles = theme => ({
    overviewWrapper: {
        padding: theme.spacing.unit,
        marginRight: theme.spacing.unit,
    },
    listing: {
        margin: theme.spacing.unit,
        padding: theme.spacing.unit,
    },
    endpointName: {
        fontSize: '1.2rem',
    },
    endpointTypesWrapper: {
        paddingLeft: theme.spacing.unit,
    },
});
/**
 * The endpoint overview component. This component holds the views of endpoint creation and configuration.
 * @param {any} props The props that are being passed to the component.
 * @returns {any} HTML view of the endpoints overview.
 */
function EndpointOverview(props) {
    const { classes, api } = props;
    const { endpointConfig } = api;

    return (
        <div className={classes.overviewWrapper}>
            <Grid container>
                <Grid item xs={6}>
                    <Paper className={classes.endpointTypesWrapper}>
                        <div>
                            <Typography className={classes.endpointName}>
                                <FormattedMessage id='Production.Endpoints' defaultMessage='Production Endpoints' />
                            </Typography>
                            <EndpointListing
                                // getSelected={getEndpoint}
                                apiEndpoints={endpointConfig.production_endpoints}
                                failOvers={endpointConfig.production_failovers}
                                // setEndpoints={setEndpoints}
                                epType={endpointConfig.endpoint_type}
                                category='prod'
                            />
                            <Divider />
                            <Typography className={classes.endpointName}>
                                <FormattedMessage id='Sandbox.Endpoints' defaultMessage='Sandbox Endpoints' />
                            </Typography>
                            <EndpointListing
                                // getSelected={getEndpoint}
                                apiEndpoints={endpointConfig.sandbox_endpoints}
                                // setEndpoints={setEndpoints}
                                failOvers={endpointConfig.sandbox_failovers}
                                epType={endpointConfig.endpoint_type}
                                category='sandbox'
                            />
                        </div>
                    </Paper>
                </Grid>
                {/* <Grid item xs={6}>
                </Grid> */}
            </Grid>
        </div>
    );
}

EndpointOverview.propTypes = {
    classes: PropTypes.shape({
        overviewWrapper: PropTypes.shape({}),
        endpointTypesWrapper: PropTypes.shape({}),
        endpointName: PropTypes.shape({}),
    }).isRequired,
    api: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(EndpointOverview));
