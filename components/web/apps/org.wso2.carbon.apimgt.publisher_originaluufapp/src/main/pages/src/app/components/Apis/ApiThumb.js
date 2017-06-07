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
import './Apis.css'
class ApiThumb extends React.Component {
    getGridUI = () => {
        return 'item  col-xs-4 col-lg-4 ' + this.props.listType + '-group-item';
    }

    render() {
        let details_link = "/apis/" + this.props.api.id;
        return <div className={this.getGridUI()} key={this.props.api.id}>
            <div className="thumbnail">
                <img className="group list-group-image" src="http://placehold.it/400x250/000/fff" alt=""/>
                <div className="caption">
                    <h4 className="group inner list-group-item-heading">
                        {this.props.api.name}</h4>
                    <p className="group inner list-group-item-text">
                        {this.props.api.description}</p>
                    <p className="group inner list-group-item-text">
                        {this.props.api.version}</p>
                    <div className="row">
                        <div className="col-xs-12 col-md-6">
                            <p className="lead">
                                {this.props.api.context}</p>
                        </div>
                        <div className="col-xs-12 col-md-6">
                            <Link to={details_link} className="btn btn-success">More details </Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    }
}
export default ApiThumb
