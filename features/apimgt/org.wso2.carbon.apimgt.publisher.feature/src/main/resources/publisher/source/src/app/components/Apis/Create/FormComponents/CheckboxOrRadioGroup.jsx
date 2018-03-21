/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import React from 'react';
import PropTypes from 'prop-types';

const CheckboxOrRadioGroup = props => (
    <div>
        <span className='form-label'>{props.title}</span>
        <div className='checkbox-group'>
            {props.options.map((option) => {
                return (
                    <label htmlFor={props.setName} key={option} className='form-label capitalize'>
                        <input
                            className='form-checkbox'
                            name={props.setName}
                            onChange={props.controlFunc}
                            value={option}
                            checked={props.selectedOptions.indexOf(option) > -1}
                            type={props.type}
                        />
                        {option}
                    </label>
                );
            })}
        </div>
    </div>
);

CheckboxOrRadioGroup.propTypes = {
    title: PropTypes.string.isRequired,
    type: PropTypes.oneOf(['checkbox', 'radio']).isRequired,
    setName: PropTypes.string.isRequired,
    options: PropTypes.arrayOf(PropTypes.string).isRequired,
    selectedOptions: PropTypes.arrayOf(PropTypes.string).isRequired,
    controlFunc: PropTypes.func.isRequired,
};

export default CheckboxOrRadioGroup;
