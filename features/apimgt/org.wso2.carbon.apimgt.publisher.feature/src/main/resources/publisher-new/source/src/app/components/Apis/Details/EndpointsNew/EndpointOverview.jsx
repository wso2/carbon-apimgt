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
import Grow from '@material-ui/core/Slide';
import Divider from '@material-ui/core/Divider';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';

import EndpointListing from './EndpointListing';
import EndpointConfig from './EndpointConfig';


const styles = theme => ({
    overviewWrapper: {
        padding: '5px',
        marginRight: '5px',
    },
    listing: {
        margin: '10px',
        padding: '10px',
    },
    endpointName: {
        fontSize: '1.2rem',
    },
    endpointTypesWrapper: {
        paddingLeft: '10px',
    },
});
/**
 * The endpoint overview component. This component holds the views of endpoint creation and configuration.
 * @param {any} props The props that are being passed to the component.
 * @returns {any} HTML view of the endpoints overview.
 */
function EndpointOverview(props) {
    const { classes, api } = props;
    const [endpoints, setEndpoints] = useState([]);
    const [selectedEndpoint, setSelectedEndpoint] = useState(0);
    const [openConfigs, setOpenConfigs] = useState(false);

    /**
     * Get the selected endpoint from the endpoint listing component.
     * @param {number} endPoint : The index of the selected endpoint.
     */
    function getEndpoint(endPoint) {
        setOpenConfigs(!openConfigs);
        console.log('getEps clicked');
    }

    function editEndpoint(endpointDo) {
        console.log(endpointDo);
    }

    console.log(api);
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
                                getSelected={getEndpoint}
                                endpoints={endpoints}
                                setEndpoints={setEndpoints}
                                epType='prod'
                            />
                            <Divider />
                            <Typography className={classes.endpointName}>
                                <FormattedMessage id='Sandbox.Endpoints' defaultMessage='Sandbox Endpoints' />
                            </Typography>
                            <EndpointListing
                                getSelected={getEndpoint}
                                endpoints={endpoints}
                                setEndpoints={setEndpoints}
                                epType='sandbox'
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
