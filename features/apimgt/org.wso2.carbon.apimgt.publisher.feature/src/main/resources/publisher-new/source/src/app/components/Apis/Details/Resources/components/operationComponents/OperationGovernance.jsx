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

import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Divider from '@material-ui/core/Divider';
import Typography from '@material-ui/core/Typography';
import Switch from '@material-ui/core/Switch';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function OperationGovernance(props) {
    const { operation, operationActionsDispatcher, operationRateLimits } = props;
    return (
        <Fragment>
            <Grid item md={12}>
                <Typography variant='subtitle1'>
                    Security
                    <Divider variant='middle' />
                </Typography>
            </Grid>
            <Grid item md={1} />
            <Grid item md={11}>
                <FormControl component='fieldset'>
                    <FormControlLabel
                        control={
                            <Switch
                                checked={operation.authType.toLowerCase() !== 'none'}
                                onChange={({ target: { checked } }) =>
                                    operationActionsDispatcher({
                                        action: 'authType',
                                        event: { value: checked },
                                    })
                                }
                                size='small'
                                color='primary'
                            />
                        }
                        label='Enabled'
                        labelPlacement='start'
                    />
                </FormControl>

            </Grid>
            <Grid item md={1} />
            <Grid item md={11}>
                <TextField
                    id='outlined-select-currency'
                    select
                    label='Throttling policy'
                    // className={classes.textField}
                    value={operation.throttlingPolicy}
                    onChange={({ target: { value } }) =>
                        operationActionsDispatcher({
                            action: 'throttlingPolicy',
                            event: { value },
                        })
                    }
                    // SelectProps={{
                    //     MenuProps: {
                    //         className: classes.menu,
                    //     },
                    // }}
                    helperText='Select a rate limit policy for this operation'
                    margin='dense'
                    variant='outlined'
                >
                    {operationRateLimits.map(rateLimit => (
                        <MenuItem key={rateLimit.name} value={rateLimit.name}>
                            {rateLimit.displayName}
                        </MenuItem>
                    ))}
                </TextField>
            </Grid>
        </Fragment>
    );
}

OperationGovernance.propTypes = {
    operation: PropTypes.shape({
        target: PropTypes.string.isRequired,
        verb: PropTypes.string.isRequired,
        spec: PropTypes.shape({}).isRequired,
    }).isRequired,
    operationActionsDispatcher: PropTypes.func.isRequired,
    operationRateLimits: PropTypes.arrayOf(PropTypes.shape({})),
};

OperationGovernance.defaultProps = {
    operationRateLimits: [],
};
