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

import React, { useState } from 'react';
import PropTypes from 'prop-types';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import Divider from '@material-ui/core/Divider';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';

import EndpointListing from './EndpointListing';
import EndpointConfig from './EndpointConfig';

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
    const [selectedEndpointInfo, setSelectedEndpointInfo] = useState({});
    const [selectedEpIndex, setSelectedEpIndex] = useState([]);

    const getSelectedEndpoint = (index, type, category) => {
        setSelectedEpIndex([index, category]);
        setSelectedEndpointInfo(() => {
            let selected = {};
            switch (type) {
                case 'load_balance':
                    if (category === 'prod') {
                        selected = endpointConfig.production_endpoints[index];
                    }
                    if (category === 'sandbox') {
                        selected = (endpointConfig.sandbox_endpoints[index]);
                    }
                    break;
                case 'failover':
                    if (category === 'prod') {
                        if (index === 0) {
                            selected = (endpointConfig.production_endpoints);
                        } else {
                            selected = (endpointConfig.production_failovers[index - 1]);
                        }
                    }
                    if (category === 'sandbox') {
                        if (index === 0) {
                            selected = (endpointConfig.sandbox_endpoints);
                        } else {
                            selected = (endpointConfig.sandbox_failovers[index - 1]);
                        }
                    }
                    break;
                default:
                    selected = ({ url: 'http://myendpoint/server' });
                    break;
            }
            return selected;
        });
    };
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
                                getSelectedEndpoint={getSelectedEndpoint}
                                apiEndpoints={endpointConfig.production_endpoints}
                                failOvers={endpointConfig.production_failovers}
                                selectedEpIndex={selectedEpIndex}
                                epType={endpointConfig.endpoint_type}
                                // TODO : addEndpoint={addEndpoint}
                                category='prod'
                            />
                            <Divider />
                            <Typography className={classes.endpointName}>
                                <FormattedMessage id='Sandbox.Endpoints' defaultMessage='Sandbox Endpoints' />
                            </Typography>
                            <EndpointListing
                                getSelectedEndpoint={getSelectedEndpoint}
                                apiEndpoints={endpointConfig.sandbox_endpoints}
                                selectedEpIndex={selectedEpIndex}
                                failOvers={endpointConfig.sandbox_failovers}
                                epType={endpointConfig.endpoint_type}
                                // TODO : addEndpoint={addEndpoint}
                                category='sandbox'
                            />
                        </div>
                    </Paper>
                </Grid>
                <Grid item xs={6}>
                    <EndpointConfig epInfo={selectedEndpointInfo} />
                </Grid>
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
