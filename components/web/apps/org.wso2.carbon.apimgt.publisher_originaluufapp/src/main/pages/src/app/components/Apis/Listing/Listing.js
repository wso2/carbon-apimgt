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

import ApiThumb from '../ApiThumb'
import '../Apis.css'
import API from '../../../data/api.js'
import Loading from '../../Base/Loading/Loading'

class Listing extends React.Component {
    constructor(props) {
        super(props);
        this.state = {listType: 'grid', apis: null};
    }

    componentDidMount() {
        let api = new API();
        let promised_apis = api.getAll();
        promised_apis.then((response) => {
            this.setState({apis: response.obj})

        });
    }

    setListType = (value) => {
        this.setState({listType: value});
    }
    isActive = (value) => {
        return 'btn ' + ((value === this.state.listType) ? 'active' : 'default');
    }

    render() {
        return (
            <div className="container-fluid">
                <div className="well well-sm">
                    <strong>Display</strong>
                    <div className="btn-group">
                        <a href="#" id="list" className={this.isActive('list')}
                           onClick={() => this.setListType('list')}>
                            <span className="glyphicon glyphicon-th-list"/>List
                        </a>
                        <a href="#" id="grid" className={this.isActive('grid')}
                           onClick={() => this.setListType('grid')}>
                            <span className="glyphicon glyphicon-th"/>Grid
                        </a>
                    </div>
                </div>

                <div id="products" className="row list-group">
                    {this.state.apis ?
                        this.state.apis.list.map((api, i) => {
                            return <ApiThumb listType={this.state.listType} api={api}/>
                        }) : <Loading/>
                    }
                </div>

            </div>


        );
    }
}

export default Listing