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
        width: '150%',
        overflow: 'scroll',
    },
    heading: {
        flexShrink: 0,
        flexBasis: '60%',
    },
});
/**
 * The component for Endpoint configurations.
 * @param {any} props The props that are being passed into the component.
 * @returns {any} The HTML view of the component.
 */
function EndpointConfig(props) {
    const { classes, epInfo } = props;
    const [endpoint, setEndpoint] = useState('');
    const [maxTps, setMaxTps] = useState(300);

    const handleEndpointInputChange = (event) => {
        setEndpoint(event.target.value);
        props.editEndpoint(event.target.value);
    };

    const onMaxTPSChange = (event) => {
        setMaxTps(event.target.value);
    };

    useEffect(() => {
        setEndpoint(epInfo.url);
    }, [epInfo]);

    return (
        <Paper className={classes.epConfigWrapper}>
            <Typography>Endpoint Configuration</Typography>
            <Grid container direction='column'>
                <TextField
                    id='serviceUrl'
                    label={<FormattedMessage id='Service.URL' defaultMessage='Service URL' />}
                    className={classes.textField}
                    value={endpoint}
                    placeholder='http(s)://appserver/service'
                    onChange={handleEndpointInputChange}
                    margin='normal'
                />
                <TextField
                    id='maxTps'
                    label={<FormattedMessage id='Max.TPS' defaultMessage='Max TPS' />}
                    className={classes.textField}
                    value={maxTps}
                    type='number'
                    onChange={onMaxTPSChange}
                    margin='normal'
                />
            </Grid>
            <AdvancedEndpointConfig />
        </Paper>
    );
}

EndpointConfig.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    editEndpoint: PropTypes.func.isRequired,
    epInfo: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(EndpointConfig));
