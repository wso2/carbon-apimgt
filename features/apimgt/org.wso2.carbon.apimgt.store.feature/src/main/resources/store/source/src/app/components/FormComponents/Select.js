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

const Select = (props) => (
	<div className="form-group">
		<select
			name={props.name}
			value={props.selectedOption}
			onChange={props.controlFunc}
			className="form-select">
			<option value="">{props.placeholder}</option>
			{props.options.map(opt => {
				return (
					<option
						key={opt}
						value={opt}>{opt}</option>
				);
			})}
		</select>
	</div>
);

Select.propTypes = {
	name: React.PropTypes.string.isRequired,
	options: React.PropTypes.array.isRequired,
	selectedOption: React.PropTypes.string,
	controlFunc: React.PropTypes.func.isRequired,
	placeholder: React.PropTypes.string
};

export default Select;