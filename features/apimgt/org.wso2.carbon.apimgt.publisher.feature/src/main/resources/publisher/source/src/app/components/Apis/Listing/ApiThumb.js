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

import React from 'react'
import {BrowserRouter as Router, Route, Link} from 'react-router-dom'
import ApiProgress from './ApiProgress'
class ApiThumb extends React.Component {
    getGridUI = () => {
        return 'item  col-xs-4 col-lg-4 ' + this.props.listType + '-group-item';
    }

    render() {
        let details_link = "/apis/" + this.props.api.id;
        if (this.props.listType === "grid") {
            return (
                <div className={this.getGridUI()} key={this.props.api.id}>
                    <div className="thumbnail">
                        <div className="caption">
                            <h4 className="group inner list-group-item-heading">
                                {this.props.api.name}</h4>
                            <p className="group inner list-group-item-text">
                                {this.props.api.description}</p>
                            <p className="group inner list-group-item-text">
                                {this.props.api.version}</p>
                            <p className="lead">
                                {this.props.api.context}</p>
                            <Link to={details_link} className="btn btn-default">More details </Link>

                        </div>
                    </div>
                </div>
            );
        } else {
            return <tr key={this.props.api.id}>
                <td>
                    <input type="checkbox"/>
                </td>
                <td className="apis-status">
                    <span className="label label-primary">Published</span>
                </td>
                <td className="apis-title">
                    <a href="project_detail.html">{this.props.api.name}</a>
                    <br />
                    <small>{this.props.api.version}</small>
                    <br />
                    <small>{this.props.api.context}</small>
                </td>
                <td className="apis-status">
                    <ApiProgress resources="true" tiers="false"/>
                </td>
                <td className="apis-actions">
                    <a href="#" className="btn btn-white"><i className="fw fw-edit"/> Edit </a>
                    <a href="#" className="btn btn-white"><i className="fw fw-delete"/> Delete </a>
                </td>
            </tr>
        }

    }
}
export default ApiThumb
