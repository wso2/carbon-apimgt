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
import React, { useState } from 'react';
import PropTypes from 'prop-types';
import Radio from '@material-ui/core/Radio';
import Button from '@material-ui/core/Button';
import Grid from '@material-ui/core/Grid';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';
import Paper from '@material-ui/core/Paper';
import TextField from '@material-ui/core/TextField';
import Divider from '@material-ui/core/Divider';
import Box from '@material-ui/core/Box';
import Typography from '@material-ui/core/Typography';
import MenuItem from '@material-ui/core/MenuItem';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';

/**
 *
 * Handles the resource level and API level throttling UI switch
 * @export
 * @param {*} props
 * @returns
 */
export default function APIRateLimiting(props) {
    const { api, updateAPI, operationRateLimits } = props;
    const [apiThrottlingPolicy, setApiThrottlingPolicy] = useState(api.apiThrottlingPolicy);
    const isAPILevel = apiThrottlingPolicy === null && 'api';
    return (
        <Paper>
            <Grid container direction='row' spacing={3} justify='flex-start' alignItems='flex-start'>
                <Grid item md={12}>
                    <Box ml={1}>
                        <Typography variant='subtitle1' gutterBottom>
                            Resources Configuration
                            <Tooltip
                                fontSize='small'
                                title='Configurations that are applied commonly to all the resources'
                                aria-label='common configurations'
                                placement='right-end'
                                interactive
                            >
                                <HelpOutline />
                            </Tooltip>
                        </Typography>
                    </Box>
                    <Divider light variant='middle' />
                </Grid>
                <Grid item md={1} />
                <Grid item md={3}>
                    <FormControl component='fieldset'>
                        <FormLabel component='legend'>Apply rate limiting</FormLabel>
                        <RadioGroup
                            aria-label='Apply rate limiting in'
                            value={isAPILevel}
                            onChange={(event) => {
                                setApiThrottlingPolicy(event.target.value === 'api' ? null : '');
                            }}
                            row
                        >
                            <FormControlLabel value='api' control={<Radio />} label='API Level' labelPlacement='end' />
                            <FormControlLabel
                                control={<Radio />}
                                label='Operation Level'
                                labelPlacement='end'
                            />
                        </RadioGroup>
                    </FormControl>
                </Grid>
                <Grid item md={8}>
                    <Box borderLeft={1} pl={1}>
                        <TextField
                            id='operation_throttling_policy'
                            select
                            label='Rate limiting policies'
                            value={api.apiThrottlingPolicy}
                            // onChange={({ target: { value } }) =>
                            //     operationActionsDispatcher({
                            //         action: 'throttlingPolicy',
                            //         event: { value },
                            //     })
                            // }
                            helperText='Selected rate limiting policy will be applied to whole API'
                            margin='dense'
                            variant='outlined'
                        >
                            {operationRateLimits.map(rateLimit => (
                                <MenuItem key={rateLimit.name} value={rateLimit.name}>
                                    {rateLimit.displayName}
                                </MenuItem>
                            ))}
                        </TextField>
                    </Box>
                </Grid>
                <Grid item md={12}>
                    <Divider />
                </Grid>
                <Grid item>
                    <Box ml={1}>
                        <Button disabled={false} variant='outlined' size='small' color='primary'>
                            Save
                            {/* {isSaving && <CircularProgress size={24} />} */}
                        </Button>
                        <Button
                            size='small'
                            // onClick={() => {
                            //     operationActionsDispatcher({ action: 'update', event: { value: initOperation } });
                            //     setIsNotSaved(false);
                            // }}
                        >
                            Reset
                        </Button>
                    </Box>
                </Grid>
            </Grid>
        </Paper>
    );
}
APIRateLimiting.defaultProps = {};
APIRateLimiting.propTypes = {
    api: PropTypes.shape({ id: PropTypes.string }).isRequired,
};
