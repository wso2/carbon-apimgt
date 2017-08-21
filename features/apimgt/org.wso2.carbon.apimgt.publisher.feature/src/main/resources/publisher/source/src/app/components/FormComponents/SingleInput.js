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

const SingleInput = (props) => (
	<div className="form-group label-floating is-empty">
		<label className="control-label">{props.title}</label>
		<input
			className="form-control"
			name={props.name}
			type={props.inputType}
			value={props.content}
			onChange={props.controlFunc}
			placeholder={props.placeholder} />
		<p className="help-block">{props.helpText}</p>
	</div>
);

SingleInput.propTypes = {
	inputType: React.PropTypes.oneOf(['text', 'number']).isRequired,
	title: React.PropTypes.string.isRequired,
	name: React.PropTypes.string.isRequired,
	controlFunc: React.PropTypes.func.isRequired,
	content: React.PropTypes.oneOfType([
		React.PropTypes.string,
		React.PropTypes.number,
	]).isRequired,
	placeholder: React.PropTypes.string,
};

export default SingleInput;