/**
 * Copyright (c), WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, { Component } from 'react';
import Typography from '@material-ui/core/Typography';
import API from 'AppData/api';
import PropTypes from 'prop-types';
import Alert from 'AppComponents/Shared/Alert';

/**
 * This Component hosts the API Security Audit Component
 * More specifically, rendering of the Security Audit
 * Report.
 */
class APISecurityAudit extends Component {
    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            items: [],
        };
        this.keyCount = 0;
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        // Include code to pass in the data from the backend
        const { apiId } = this.props;
        const Api = new API();

        Api.getSecurityAuditReport(apiId.id)
            .then(response => response.json())
            .then(response => this.setState({ items: response }))
            .catch((error) => {
                console.error(error);
                if (error.response) {
                    Alert.error(error.response.body.message);
                } else {
                    Alert.error('Something went wrong while retrieving the API Security Report');
                }
            });
    }

    /**
     * Method to return the index number
     * @returns {int} this.keyCount
     */
    getKey() {
        return this.keyCount++;
    }

    /**
     * @inheritdoc
     */
    render() {
        // TODO - Change header elements to Typography
        // TODO - Make a criticality map where 1 is lowest and 4 is highest and color code if possible
        return (
            <div
                width='100%'
                height='calc(100vh - 51px)'
            >
                <Typography variant='h6'>API Security Audit Report</Typography>
                <p>API Security Audit works!</p>
                {/** Test the json data after finishing the structure */}
                {this.state.items.map((item) => {
                    return (
                        <div>
                            <h1>Audit Score and Summary</h1>
                            {/** Show total score and possibly a Progress Ring */}
                            <p>Overall Grade: {item.grade}</p>
                            <p>Number of issues: {item.issueCounter}</p>
                            <p>Overall Criticality: {item.criticality}</p>

                            {/** Properties to be used are: issueCounter, grade, issues[], criticality */}
                            <h4>OpenAPI format requirements</h4>
                            {/** Show score out of 25 and progress bar here */}
                            <p>Score: {item.validation.grade} / 25</p>
                            <p>Number of issues: {item.validation.issueCounter}</p>
                            <p>Criticality: {item.validation.criticality}</p>
                            { (item.validation.issueCounter !== 0)
                                ? (item.validation.issues.map((issue) => {
                                    return (
                                        <ul>
                                            <li key={this.getKey()}>Description: {issue.message}</li>
                                            <li key={this.getKey()}>Score Impact: {issue.configScore}</li>
                                            <li key={this.getKey()}>Criticality: {issue.criticality}</li>
                                        </ul>
                                    );
                                }))
                                : (null)
                            }

                            <h4>Security</h4>
                            {/** Show score out of 25 and progress bar here */}
                            <p>Score: {item.security.grade} / 25 </p>
                            <p>Number of issues: {item.security.issueCounter}</p>
                            <p>Criticality: {item.security.criticality}</p>
                            { (item.security.issueCounter !== 0)
                                ? (item.security.issues.map((issue) => {
                                    return (
                                        <ul>
                                            <li key={this.getKey()}>Description: {issue.message}</li>
                                            <li key={this.getKey()}>Score Impact: {issue.configScore}</li>
                                            <li key={this.getKey()}>Criticality: {issue.criticality}</li>
                                        </ul>
                                    );
                                }))
                                : (null)
                            }

                            <h4>Data Validation</h4>
                            {/** Show score out of 50 and progress bar here */}
                            <p>Score: {item.data.grade} / 50 </p>
                            <p>Number of issues: {item.data.issueCounter}</p>
                            <p>Criticality: {item.data.criticality}</p>
                            { (item.data.issueCounter !== 0)
                                ? (item.data.issues.map((issue) => {
                                    return (
                                        <ul>
                                            <li key={this.getKey()}>Description: {issue.message}</li>
                                            <li key={this.getKey()}>Score Impact: {issue.configScore}</li>
                                            <li key={this.getKey()}>Criticality: {issue.criticality}</li>
                                        </ul>
                                    );
                                }))
                                : (null)
                            }
                        </div>
                    );
                })}
            </div>
        );
    }
}

APISecurityAudit.propTypes = {
    apiId: PropTypes.string.isRequired,
};

export default APISecurityAudit;
