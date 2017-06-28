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
import qs from 'qs'

import ApiThumb from './ApiThumb'
import '../Apis.css'
import API from '../../../data/api.js'
import Loading from '../../Base/Loading/Loading'
import ListingHeader from "./ListingHeader";
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import {Link} from 'react-router-dom'
import ApiProgress from './ApiProgress';

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
        }).catch(error => {
            if (process.env.NODE_ENV !== "production") {
                console.log(error);
            }
            let status = error.status;
            if (status === 404) {
                this.setState({notFound: true});
            }
        });
    }

    setListType = (value) => {
        this.setState({listType: value});
    }

    isActive = (value) => {
        return 'btn ' + ((value === this.state.listType) ? 'active' : 'default');
    }

    render() {
        if (this.state.notFound) {
            return <ResourceNotFound/>
        }
        return (
            <div className="container-fluid">
                <div className="ibox">
                    <div className="ibox-title">
                        <h5>All APIs visible to this account</h5>
                        <h2 className="pull-left">All APIs</h2>
                        <Link className="pull-right btn btn-primary" to="/api/create">Create new API</Link>
                    </div>
                    <div className="clearfix"/>
                    <nav className="navbar navbar-default" role="navigation">
                        {/* Collect the nav links, forms, and other content for toggling */}
                        <div className="collapse navbar-collapse">
                            <ul className="nav navbar-nav">
                                <li>
                                    <button type="button" className="btn">
                                        <i className="fw fw-delete"/>
                                    </button>
                                </li>
                                <li>
                                    <button type="button" className="btn">
                                        <i className="fw fw-refresh"/>
                                    </button>
                                </li>
                            </ul>
                            <div className="col-sm-6">
                                <form className="navbar-form" role="search">
                                    <div className="input-group">
                                        <input type="text" placeholder="Search" className="input-sm form-control"/>
                                        <div className="input-group-btn">
                                            <button className="btn btn-default" type="submit"><i
                                                className="glyphicon glyphicon-search"/></button>
                                        </div>
                                    </div>
                                </form>
                            </div>
                            <ul className="nav navbar-nav navbar-right">
                                <li>
                                    <button type="button"
                                            className={this.isActive('grid')}
                                            onClick={() => this.setListType('grid')}><i className="fw fw-grid"/>
                                    </button>
                                </li>
                                <li>
                                    <button type="button"
                                            className={this.isActive('list')}
                                            onClick={() => this.setListType('list')}><i className="fw fw-list"/>
                                    </button>
                                </li>
                            </ul>
                        </div>
                        {/* /.navbar-collapse */}
                    </nav>

                    <div className="ibox-content">
                        <div className="apis-list">
                            <table className="table table-hover">
                                <tbody>
                                {this.state.apis ?
                                    this.state.apis.list.map((api, i) => {
                                        return <ApiThumb key={api.id} listType={this.state.listType} api={api}/>
                                    }) : <Loading/>
                                }
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default Listing