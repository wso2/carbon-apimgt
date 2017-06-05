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

const TextArea = (props) => (
	<div className="form-group">
		<label className="form-label">{props.title}</label>
		<textarea
			className="form-input"
			style={props.resize ? null : {resize: 'none'}}
			name={props.name}
			rows={props.rows}
			value={props.content}
			onChange={props.controlFunc}
			placeholder={props.placeholder} />
	</div>
);

TextArea.propTypes = {
	title: React.PropTypes.string.isRequired,
	rows: React.PropTypes.number.isRequired,
	name: React.PropTypes.string.isRequired,
	content: React.PropTypes.string.isRequired,
	resize: React.PropTypes.bool,
	placeholder: React.PropTypes.string,
	controlFunc: React.PropTypes.func.isRequired
};

export default TextArea;