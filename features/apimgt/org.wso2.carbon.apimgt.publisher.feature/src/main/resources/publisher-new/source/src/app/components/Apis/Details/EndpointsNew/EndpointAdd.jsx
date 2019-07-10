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
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import AddCircle from '@material-ui/icons/AddCircle';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';
import { FormattedMessage, injectIntl } from 'react-intl';

const styles = theme => ({
    addEpButtonWrapper: {
        display: 'flex',
    },
    addAnotherText: {
        padding: theme.spacing.unit * 2,
    },
});

/**
 * The endpoint add component. This component holds the elements to add a new
 * load balanced or failover endpoint.
 * Endpoints are identified by an index.
 * 0 - Standard Endpoint.
 * 1 - Load balance endpoint
 * 2 - Failover endpoint.
 *
 * @param {any} props The props that are being passed.
 * @returns {any} The HTML content of the Endpoint Add component.
 */
function EndpointAdd(props) {
    const { classes, endpointType } = props;
    const [type, setType] = useState('');

    const addEndpointClick = (epType) => {
        setType(epType);
        props.onAddEndpointClick(epType);
    };

    useEffect(() => {
        setType(endpointType);
    }, [endpointType]);
    return (
        <Grid container direction='row' >
            <div className={classes.addAnotherText}>
                <Typography>
                    <FormattedMessage id='Add.Another' defaultMessage='Add Another' />
                </Typography>
            </div>
            <Button
                id='loadBalanceAdd'
                disabled={type !== 'load_balance' && type !== 'http'}
                onClick={() => addEndpointClick('load_balance')}
            >
                <AddCircle />
                <Typography>
                    <FormattedMessage id='Load.Balance.Endpoint' defaultMessage='Load Balanced Endpoint' />
                </Typography>
            </Button>
            <Button
                id='failOverAdd'
                disabled={type !== 'failover' && type !== 'http'}
                onClick={() => addEndpointClick('failover')}
            >
                <AddCircle />
                <Typography>
                    <FormattedMessage id='Fail.Over.Endpoint' defaultMessage='Fail Over Endpoint' />
                </Typography>
            </Button>
        </Grid>
    );
}

EndpointAdd.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    endpointType: PropTypes.string.isRequired,
    onAddEndpointClick: PropTypes.func.isRequired,
};

export default injectIntl(withStyles(styles)(EndpointAdd));
