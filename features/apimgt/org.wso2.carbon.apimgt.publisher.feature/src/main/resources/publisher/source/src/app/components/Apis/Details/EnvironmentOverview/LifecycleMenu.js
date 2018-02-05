/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, {Component} from 'react'
import Select from 'material-ui/Select';
import {MenuItem} from 'material-ui/Menu';
import Input, {InputLabel} from 'material-ui/Input';
import {FormControl} from 'material-ui/Form';

class EnvironmentMenu extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const {lifecycleStatus, lifecycleStatuses, handleLifecycleStateChange} = this.props;

        return (
            <FormControl style={{width: "100%"}}>
                <InputLabel htmlFor="lifecycle-simple">Life Cycle State</InputLabel>
                <Select
                    value={lifecycleStatus}
                    onChange={handleLifecycleStateChange}
                    input={<Input name="lifecycle" id="lifecycle-simple"/>}
                >
                    <MenuItem value={"All"}>All</MenuItem>
                    {lifecycleStatuses.map((status, index) =>
                        <MenuItem key={index} value={status.name}>
                            {status.name}
                        </MenuItem>
                    )}
                </Select>
            </FormControl>
        );
    }
}

export default EnvironmentMenu;
