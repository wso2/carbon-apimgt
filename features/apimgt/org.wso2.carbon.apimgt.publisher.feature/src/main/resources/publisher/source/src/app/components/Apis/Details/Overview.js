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

import React, {Component} from 'react'
class ActivityItem extends Component {
    constructor(props){
        super(props);
    }
    render(){
        return <div className="feed-element">
            <a href="#" className="pull-left">
                <i className="fw fw-user"></i>
            </a>
            <div className="media-body ">
                <small className="pull-right text-navy">{this.props.when}</small>
                <strong>{this.props.user}</strong> subscribed with <strong>{this.props.tier}</strong> tier. <br />
                <small className="text-muted">{this.props.time}</small>
            </div>
        </div>
    }
}
class Overview extends Component {
    constructor(props) {
        super(props);
        this.state = {
            api: props.api
        }
    }

    componentWillReceiveProps(nextProps) {
        this.setState(
            {
                api: nextProps.api
            }
        );

    }

    render() {
        return (
            <div>
                <div className="wrapper wrapper-content">
                    <h2>{this.state.api.name} <span className="pull-right btn-block view-in-store btn btn-block">View in Store <i className="fw fw-store"></i></span></h2>

                    <div className="row animated fadeInRight">
                        <div className="col-md-4">
                            <div className="api-box float-e-margins">
                                    <span className="label label-primary">{this.state.api.lifeCycleStatus}</span>
                                    <div className="api-box-content no-padding border-left-right">
                                        <div className="square-element setbgcolor"
                                             style={{background: 'rgb(92, 107, 192)'}}>
                                            <div className="api-name-icon text-uppercase"
                                                 style={{fontSize: '900%', color: 'white', textAlign: 'center'}}>A
                                            </div>
                                            <div style={{display: 'none'}}>
                                                <a className="api-name" title="exsample_4">{this.state.api.name}</a>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="api-box-content profile-content">

                                        <p>
                                            Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitat.
                                        </p>
                                        <div className="row m-t-lg">
                                            <div className="col-md-4">
                                                <span className="bar" style={{display: 'none'}}>5,3,9,6,5,9,7,3,5,2</span><svg className="peity" height={16} width={32}><rect fill="#1ab394" x={0} y="7.111111111111111" width="2.3" height="8.88888888888889" /><rect fill="#d7d7d7" x="3.3" y="10.666666666666668" width="2.3" height="5.333333333333333" /><rect fill="#1ab394" x="6.6" y={0} width="2.3" height={16} /><rect fill="#d7d7d7" x="9.899999999999999" y="5.333333333333334" width="2.3" height="10.666666666666666" /><rect fill="#1ab394" x="13.2" y="7.111111111111111" width="2.3" height="8.88888888888889" /><rect fill="#d7d7d7" x="16.5" y={0} width="2.3" height={16} /><rect fill="#1ab394" x="19.799999999999997" y="3.555555555555557" width="2.3" height="12.444444444444443" /><rect fill="#d7d7d7" x="23.099999999999998" y="10.666666666666668" width="2.3" height="5.333333333333333" /><rect fill="#1ab394" x="26.4" y="7.111111111111111" width="2.3" height="8.88888888888889" /><rect fill="#d7d7d7" x="29.7" y="12.444444444444445" width="2.3" height="3.5555555555555554" /></svg>
                                                <h5><strong>169</strong> <br />Requests</h5>
                                            </div>
                                            <div className="col-md-4">
                                                <span className="bar" style={{display: 'none'}}>5,3,2,-1,-3,-2,2,3,5,2</span><svg className="peity" height={16} width={32}><rect fill="#1ab394" x={0} y={0} width="2.3" height={10} /><rect fill="#d7d7d7" x="3.3" y={4} width="2.3" height={6} /><rect fill="#1ab394" x="6.6" y={6} width="2.3" height={4} /><rect fill="#d7d7d7" x="9.899999999999999" y={10} width="2.3" height={2} /><rect fill="#1ab394" x="13.2" y={10} width="2.3" height={6} /><rect fill="#d7d7d7" x="16.5" y={10} width="2.3" height={4} /><rect fill="#1ab394" x="19.799999999999997" y={6} width="2.3" height={4} /><rect fill="#d7d7d7" x="23.099999999999998" y={4} width="2.3" height={6} /><rect fill="#1ab394" x="26.4" y={0} width="2.3" height={10} /><rect fill="#d7d7d7" x="29.7" y={6} width="2.3" height={4} /></svg>
                                                <h5><strong>10</strong> <br />Fault Requests</h5>
                                            </div>
                                            <div className="col-md-4">
                                                <span className="line" style={{display: 'none'}}>5,3,9,6,5,9,7,3,5,2</span><svg className="peity" height={16} width={32}><polygon fill="#1ab394" points="0 15 0 7.166666666666666 3.5555555555555554 10.5 7.111111111111111 0.5 10.666666666666666 5.5 14.222222222222221 7.166666666666666 17.77777777777778 0.5 21.333333333333332 3.833333333333332 24.888888888888886 10.5 28.444444444444443 7.166666666666666 32 12.166666666666666 32 15" /><polyline fill="transparent" points="0 7.166666666666666 3.5555555555555554 10.5 7.111111111111111 0.5 10.666666666666666 5.5 14.222222222222221 7.166666666666666 17.77777777777778 0.5 21.333333333333332 3.833333333333332 24.888888888888886 10.5 28.444444444444443 7.166666666666666 32 12.166666666666666" stroke="#169c81" strokeWidth={1} strokeLinecap="square" /></svg>
                                                <h5><strong>28</strong> <br />Subscriptions</h5>
                                            </div>

                                        </div>
                                        <div className="row m-t-lg">
                                            <div className="col-md-4">
                                                <h5><strong>Context</strong> <br />{this.state.api.context}</h5>
                                            </div>
                                            <div className="col-md-4">
                                                <h5><strong>Version</strong> <br />{this.state.api.version}</h5>
                                            </div>
                                            <div className="col-md-4">
                                                <h5><strong>Status</strong> <br />{this.state.api.lifeCycleStatus}</h5>
                                            </div>

                                        </div>
                                        <div className="user-button">
                                            <div className="row">
                                                <div className="col-md-6">
                                                    <button type="button" className="btn btn-primary btn-sm btn-block"><i className="fa fa-envelope" /> Delete</button>
                                                </div>
                                                <div className="col-md-6">
                                                    <button type="button" className="btn btn-default btn-sm btn-block"><i className="fa fa-coffee" /> New Version</button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                            </div>
                        </div>
                        <div className="col-md-8">
                            <table className="table table-bordered api-detail-table">
                                <tbody>
                                <tr>
                                    <td>Visibility</td>
                                    <td id="inUrl">
                                        {this.state.api.visibility}
                                    </td>
                                </tr>
                                <tr>
                                    <td>Context</td>
                                    <td id="inUrl">{this.state.api.name}</td>
                                </tr>
                                <tr>
                                    <td>Date Last Updated</td>
                                    <td id="inUpdated" className="dateFull">{this.state.api.createdTime}</td>
                                </tr>
                                <tr>
                                    <td>Tier Availability</td>
                                    <td id="tierAvb">Unlimited</td>
                                </tr>
                                <tr>
                                    <td>Default API Version</td>
                                    <td id="defaultAPIVersion">
                                        false
                                    </td>
                                </tr>
                                <tr>
                                    <td>Published Environments</td>
                                    <td>
                                        <b>N/A</b>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Labels</td>
                                    <td id="labelValues"><b>N/A</b></td>
                                </tr>
                                </tbody>
                            </table>
                            <div className="api-box-white float-e-margins">

                                <div className="api-box-content">
                                    <div>
                                        <div className="feed-activity-list">
                                            <h3>Activities</h3>
                                            <ActivityItem when="1m ago"
                                                          time="Today 4:21 pm - 12.06.2014"
                                                          user="Sandra Momot"
                                                          tier="Unlimited"
                                            />
                                            <ActivityItem when="12m ago"
                                                          time="Today 4:01 pm - 12.06.2014"
                                                          user="Monica Smith"
                                                          tier="Unlimited"
                                            /><
                                            ActivityItem when="23m ago"
                                                          time="Today 3:55 pm - 12.06.2014"
                                                          user="Sandra Momot"
                                                          tier="Unlimited"
                                            />
                                            <ActivityItem when="45m ago"
                                                          time="Today 4:21 pm - 12.06.2014"
                                                          user="Sandra Momot"
                                                          tier="Unlimited"
                                            />
                                        </div>
                                        <button className="btn btn-default btn-block m"><i className="fa fa-arrow-down" /> Show More</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default Overview