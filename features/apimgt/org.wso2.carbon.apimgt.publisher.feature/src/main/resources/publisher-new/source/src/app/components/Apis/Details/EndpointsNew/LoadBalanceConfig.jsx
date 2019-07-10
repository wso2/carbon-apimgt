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
import { TextField, MenuItem, Grid } from '@material-ui/core';
import PropTypes from 'prop-types';


const algorithms = [{ id: 0, value: 'Round-Robbin' }, { id: 1, value: 'other' }];
const sessionManagementOps = [
    { id: 0, value: 'Transport' }, { id: 1, value: 'SOAP' }, { id: 2, value: 'Client Id' }, { id: 3, value: 'None' }];

/**
 * The component for loadbalance endpoint configuration.
 * @param {any} props The props that are being passed.
 * @returns {any} The HTML contents of the Configuration component.
 */
function LoadBalanceConfig(props) {
    const { api } = props;
    const [algorithm, setAlgorithm] = useState(algorithms[0]); // TODO: GEt from the props
    const [sessionMgtOp, setSessionManagement] = useState(sessionManagementOps[0]); // TODO: GEt from the props
    const [epTimeout, setEpTimeout] = useState(300);
    const [customAlogrithm, setCustomAlgorithm] = useState('');

    const handleAlogorithmChange = (event) => {
        const index = event.target.value;
        console.log(algorithms[index].value);
        setAlgorithm(algorithms[index]);
    };

    const handleSessionMgtChange = (event) => {
        const index = event.target.value;
        console.log(sessionManagementOps[index], api);
        setSessionManagement(sessionManagementOps[index]);
    };

    const handleTimeoutChange = (event) => {
        const timeoutValue = event.target.value;
        setEpTimeout(timeoutValue);
    };

    const customAlogrithmChange = (event) => {
        const customAlogrithmVal = event.target.value;
        setCustomAlgorithm(customAlogrithmVal);
    };

    return (
        <div>
            <Grid container direction='column'>
                <TextField
                    id='algorithmSelect'
                    select
                    label='Algorithm'
                    value={algorithm.id}
                    onChange={handleAlogorithmChange}
                    helperText='Please select the Loadbalance Algorithm.'
                    margin='normal'
                >
                    {algorithms.map(algo => (
                        <MenuItem key={algo.id} value={algo.id} selected={algorithm}>
                            {algo.value}
                        </MenuItem>
                    ))}
                </TextField>
                {(algorithm.id === 1) ?
                    <TextField
                        id='customAlgoInput'
                        label='Class Name for Algorithm'
                        value={customAlogrithm}
                        onChange={customAlogrithmChange}
                        helperText='Enter the class name of the loadbalance algorithm'
                        margin='normal'
                    /> : <div /> }
                <TextField
                    id='sessionMgtSelect'
                    select
                    label='Session Management'
                    value={sessionMgtOp.id}
                    onChange={handleSessionMgtChange}
                    helperText='Please select the Session Management mechanism.'
                    margin='normal'
                >
                    {sessionManagementOps.map(option => (
                        <MenuItem key={option.id} value={option.id}>
                            {option.value}
                        </MenuItem>
                    ))}
                </TextField>
                <TextField
                    id='sessionTimeout'
                    label='Session Timeout (Millis)'
                    value={epTimeout}
                    onChange={handleTimeoutChange}
                    type='number'
                    placeholder='300'
                    margin='normal'
                />
            </Grid>
        </div>
    );
}

LoadBalanceConfig.propTypes = {
    api: PropTypes.shape({}).isRequired,
}

export default LoadBalanceConfig;
