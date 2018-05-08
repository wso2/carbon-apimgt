/**
 * Copyright (c), WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import React from 'react';
import PropTypes from 'prop-types';
import { InputLabel } from 'material-ui/Input';
import { FormControl } from 'material-ui/Form';
import { MenuItem } from 'material-ui/Menu';
import Select from 'material-ui/Select';

const getMenuItems = (endpoints) => {
    const menuItems = [];
    for (const endpoint of endpoints) {
        menuItems.push(<MenuItem key={endpoint.id} value={endpoint.id}>{endpoint.name}</MenuItem>);
    }
    return menuItems;
};

const EndpointsSelector = (props) => {
    const {
        endpoints, currentValue, onChange, type,
    } = props;
    return (
        <FormControl>
            <InputLabel htmlFor='endpointSelector'>Endpoint</InputLabel>
            <Select
                value={currentValue}
                onChange={onChange}
                inputProps={{
                    name: type,
                    id: 'endpointSelector',
                }}
            >
                <MenuItem key='inline' value='inline'>
                    Inline
                </MenuItem>
                {getMenuItems(endpoints)}
            </Select>
        </FormControl>
    );
};

EndpointsSelector.defaultProps = {
    currentValue: 'inline',
};

EndpointsSelector.propTypes = {
    endpoints: PropTypes.shape({}).isRequired,
    currentValue: PropTypes.string,
    onChange: PropTypes.func.isRequired,
    type: PropTypes.string.isRequired,
};
export default EndpointsSelector;
