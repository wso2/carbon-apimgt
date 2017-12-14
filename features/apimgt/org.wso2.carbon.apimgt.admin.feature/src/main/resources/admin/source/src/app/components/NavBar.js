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
import {Link, withRouter} from 'react-router-dom'
import MenuItem from 'material-ui/Menu/MenuItem';
import Divider from 'material-ui/Divider';

class NavBar extends Component {
    constructor(props) {
        super(props);
        this.state = {
            expandedSection: ""
        }
        this.toggleSection = this.toggleSection.bind(this);
    }

    render() {
        return (
            //TODO Replace this with an appropriate Expandanble Nested List
            <div>
                <MenuItem style={{marginLeft: '-8px', marginTop: '10px'}} onClick={this.toggleSection}>
                    <b>TASKS</b>
                </MenuItem>
                {((this.state.expandedSection).includes("TASKS")) ?
                    (<div>
                        <MenuItem>
                            <Link name="application_creation" to="/tasks/application_creation">APPLICATION CREATION</Link>
                        </MenuItem>
                        <MenuItem>
                            <Link name="subscription_creation" to="/tasks/subscription_creation">SUBSCRIPTION CREATION</Link>
                        </MenuItem>
                        <MenuItem>
                            <Link name="application_registration" to="/tasks/application_update">APPLICATION UPDATE</Link>
                        </MenuItem>
                        <MenuItem>
                            <Link name="api_state_change" to="/tasks/api_state">API STATE CHANGE</Link>
                        </MenuItem>
                    </div>) : <div/>
                }
                <Divider inset={true} style={{margin: '5px'}}/>

                <MenuItem style={{marginLeft: '-8px', marginTop: '10px'}} onClick={this.toggleSection}><b>THROTTLING POLICIES</b></MenuItem>
                {((this.state.expandedSection).includes("THROTTLING POLICIES")) ?
                    (<div>
                        <MenuItem>
                            <Link name="advanced_throttling" to="/policies/api_policies">API Rate Limits</Link>
                        </MenuItem>
                        <MenuItem>
                            <Link name="application_tiers" to="/policies/application_policies">Application Rate Limits</Link>
                        </MenuItem>
                        <MenuItem>
                            <Link name="subscription_tiers" to="/policies/business_plans">Business Plans</Link>
                        </MenuItem>
                        <MenuItem>
                            <Link name="custom_rules" to="/policies/custom_rules">Custom Rules</Link>
                        </MenuItem>
                        <MenuItem>
                            <Link name="black_list" to="/policies/black_list">Black List</Link>
                        </MenuItem>
                    </div>) : <div/>
                }
                <Divider inset={true} style={{margin: '5px'}}/>

                <MenuItem style={{marginLeft: '-8px', marginTop: '10px'}} onClick={this.toggleSection}>
                    <b>SETTINGS</b>
                </MenuItem>
                <Divider inset={true} style={{margin: '5px'}}/>

                <MenuItem style={{marginLeft: '-8px', marginTop: '10px'}} onClick={this.toggleSection}>
                    <b>LOG ANALYZER</b>
                </MenuItem>
                <Divider inset={true} style={{margin: '5px'}}/>

                <MenuItem style={{marginLeft: '-8px', marginTop: '10px'}} onClick={this.toggleSection}><b>SECURITY</b></MenuItem>
                {((this.state.expandedSection).includes("SECURITY")) ?
                    (<div>
                        <MenuItem>
                            <Link name="json_threat_protection" to="/security/json_threat_protection">JSON Threat Protection Policies</Link>
                        </MenuItem>
                        <MenuItem>
                            <Link name="xml_threat_protection" to="/security/xml_threat_protection">XML Threat Protection Policies</Link>
                        </MenuItem>
                    </div>) : <div/>
                }
                <Divider inset={true} style={{margin: '5px'}}/>
            </div>
        )
    }

    toggleSection(event) {
        console.log("inner text:" + event.target.innerText);
        this.setState({
            expandedSection: event.target.innerText
        });
    }
}

export default withRouter(NavBar)
