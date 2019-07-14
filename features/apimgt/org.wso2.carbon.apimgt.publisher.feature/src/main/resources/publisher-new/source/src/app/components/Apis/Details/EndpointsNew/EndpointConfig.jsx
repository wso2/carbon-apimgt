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

import React, { useState, useEffect } from 'react';
import {
    Grid,
    Typography,
    withStyles,
    Paper,
    TextField,
} from '@material-ui/core';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import AdvancedEndpointConfig from './AdvanceEndpointConfig';

const styles = theme => ({
    epConfigWrapper: {
        padding: theme.spacing.unit * 3,
        height: 'max-content',
        marginLeft: theme.spacing.unit,
    },
    heading: {
        flexShrink: 0,
        flexBasis: '60%',
    },
    noEpSelectedText: {
        padding: '24px',
        fontSize: '30px',
        color: theme.palette.grey[300],
        fontStyle: 'strong',
    },
});
/**
 * The component for Endpoint configurations.
 * @param {any} props The props that are being passed into the component.
 * @returns {any} The HTML view of the component.
 */
function EndpointConfig(props) {
    const { classes, epInfo, changeEndpointURL } = props;
    const [endpoint, setEndpoint] = useState('');

    const handleEndpointInputChange = (event) => {
        setEndpoint(event.target.value);
    };

    const setOriginalEndpoint = () => {
        changeEndpointURL(endpoint);
    };

    useEffect(() => {
        setEndpoint(epInfo.url);
    }, [props]);
    return (
        <div>
            {endpoint === undefined ? (
                <Typography className={classes.noEpSelectedText}>
                    Select an Endpoint to Configure
                </Typography>
            ) : (
                <Paper className={classes.epConfigWrapper}>
                    <Typography>Endpoint Configuration</Typography>
                    <div>
                        <Grid container direction='column'>
                            <TextField
                                id='serviceUrl'
                                label={
                                    <FormattedMessage
                                        id='Apis.Details.EndpointsNew.EndpointConfig.service.url'
                                        defaultMessage='Service URL'
                                    />
                                }
                                className={classes.textField}
                                value={endpoint}
                                placeholder='http(s)://appserver/service'
                                onChange={handleEndpointInputChange}
                                margin='normal'
                                onBlur={setOriginalEndpoint}
                            />
                        </Grid>
                        <AdvancedEndpointConfig />
                    </div>
                </Paper>)}
        </div>
    );
}

EndpointConfig.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    editEndpoint: PropTypes.func.isRequired,
    epInfo: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(EndpointConfig));
