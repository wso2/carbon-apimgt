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

const styles = theme => ({
    addEpBtnContainer: {
        flexGrow: 1,
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
    const { classes } = props;
    const [type, setType] = useState(props.type);

    const addEndpointClick = (epType) => {
        setType(epType);
        props.onAddEndpointClick(epType);
    };

    useEffect(() => {
        setType(props.type);
    }, [props.type]);

    console.log('Type from props', type);
    return (
        <div>
            {(type === 0) ?
                <Grid container direction='row'>
                    <Grid item xs={6}>
                        <Typography>Add Another</Typography>
                    </Grid>
                    <Grid
                        container
                        alignItems='flex-end'
                        item
                        xs={6}
                        className={classes.addEpBtnContainer}
                    >
                        <div>
                            <Button id='loadBalanceAdd' onClick={() => addEndpointClick(1)}>
                                <AddCircle /><Typography>Load Balanced Endpoint</Typography>
                            </Button>
                            <Button id='failOverAdd' onClick={() => addEndpointClick(2)}>
                                <AddCircle /><Typography>Fail Over Endpoint</Typography>
                            </Button>
                        </div>
                    </Grid>
                </Grid> :
                <Grid item container alignItems='flex-start' xs={6} className={classes.addEpBtnContainer}>
                    <Button onClick={() => addEndpointClick(type)}>
                        <AddCircle />
                    </Button>
                </Grid>
            }
        </div>
    );
}

export default withStyles(styles)(EndpointAdd);
