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
import CheckItem from "./CheckItem";
import TransitionStateButton from "./TransitionStateButton";

import API from '../../../../data/api'

export default class LifeCycleUpdate extends Component {
    constructor() {
        super();
        this.updateLifeCycleState = this.updateLifeCycleState.bind(this);
        this.handleCheckItem = this.handleCheckItem.bind(this);
        this.api = new API();
        this.state = {
            checkedItems: ''
        }
    }

    updateLifeCycleState(event) {
        event.preventDefault();
        let promisedUpdate;
        const newState = event.target.dataset.lcstate; // TODO: check compatibility with IE10 if not work use : event.target.attributes.getNamedItem('data-lcState').value; ~tmkb
        const apiUUID = this.props.api.id;
        if (this.state.checkedItems !== '') {
            promisedUpdate = this.api.updateLcState(apiUUID, newState, this.state.checkedItems);
        } else {
            promisedUpdate = this.api.updateLcState(apiUUID, newState);
        }
        promisedUpdate.then( response => {
            this.props.handleUpdate(true);
        })
    }

    handleCheckItem(event) {
        event.preventDefault();
    }

    render() {
        let is_workflow_pending = this.props.api.workflowStatus.toLowerCase() === "pending";
        return (
            <form className="form-horizontal lifecycle-state">
                <div className="well remove-padding-top remove-padding-bottom">
                    <div className="form-group">
                        <label className="control-label col-xs-12 col-sm-4 col-md-3 col-lg-2 text-center-xs"
                               name="state">
                            <h4>Current State : </h4>
                        </label>
                        <div className="controls col-xs-12 col-sm-8 col-md-9 col-lg-10 text-center-xs">
                            {
                                is_workflow_pending ?
                                    (
                                        <h4>
                                            <p className="form-control-static" name="stateValue">
                                                <strong>Pending lifecycle state change</strong>
                                            </p>
                                        </h4>
                                    ) :
                                    (
                                        <h4>
                                            <p className="form-control-static" name="stateValue">
                                                <strong>{this.props.api.lifeCycleStatus}</strong>
                                            </p>
                                        </h4>
                                    )
                            }
                        </div>
                    </div>
                </div>
                {
                    !is_workflow_pending &&
                    this.props.lcState.checkItemBeanList.map(
                        item => {
                            return <CheckItem key={item.name} item={item} handleCheckItem={this.handleCheckItem}/>
                        }
                    )
                }
                <div className="form-actions">
                    <div className="btn-group btn-group-justified" style={{display: 'block'}}
                         role="group" aria-label="btn">
                        {
                            is_workflow_pending ?
                                (
                                    <div className="btn-group" role="group">
                                        <input type="button" className="btn btn-primary wf-cleanup-btn"
                                               defaultValue="Delete pending lifecycle state change request"/>
                                    </div>
                                ) :
                                (
                                    this.props.lcState.availableTransitionBeanList.map(
                                        transition_state => <TransitionStateButton
                                            updateLifeCycleState={this.updateLifeCycleState}
                                            key={transition_state.event} state={transition_state}/>
                                    )
                                )
                        }
                    </div>
                </div>
            </form>
        );
    }
}