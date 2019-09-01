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
import { CircularProgressbar } from 'react-circular-progressbar';
import 'react-circular-progressbar/dist/styles.css';
import VisibilitySensor from 'react-visibility-sensor';

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
            report: null,
            overallGrade: 0,
            numErrors: 0,
        };
        this.keyCount = 0;
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        // Include code to pass in the data from the backend
        const { apiId } = this.props;
        const api = new API();

        api.getSecurityAuditReport(apiId)
            .then((response) => {
                this.setState({
                    report: response.body.report,
                    overallGrade: response.body.grade,
                    numErrors: response.body.numErrors,
                });
            })
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
     * @inheritdoc
     */
    getKey() {
        return this.keyCount++;
    }

    /**
     * @inheritdoc
     */
    render() {
        // TODO - Change header elements to Typography - Check on this
        // TODO - Make a criticality map where 1 is lowest and 4 is highest and color code if possible
        // TODO - Add the Progress Circle and Progress Bars
        // TODO - Test the json data after finishing the structure
        const { report, overallGrade, numErrors } = this.state;
        const reportObject = JSON.parse(report);
        return (
            <div>
                {report && (
                    <div
                        width='100%'
                        height='calc(100vh - 51px)'
                    >
                        <Typography variant='h6'>API Security Audit Report</Typography>
                        <div>
                            <h1>Audit Score and Summary</h1>
                            {/** Show total score and possibly a Progress Ring */}
                            <p>Overall Criticality: {reportObject.criticality}</p>
                            <p>Overall Grade: {overallGrade} / 100</p>
                            <p>Number of Errors: {numErrors}</p>
                            <div style={{ width: '100px' }}>
                                <VisibilitySensor>
                                    {({ isVisible }) => {
                                        const gradeProgressScore = isVisible ? overallGrade : 0;
                                        return (
                                            <CircularProgressbar
                                                value={gradeProgressScore}
                                                text={`${gradeProgressScore}`}
                                            />
                                        );
                                    }}
                                </VisibilitySensor>
                            </div>
                        </div>
                        <div>
                            <h1>OpenAPI Format Requirements</h1>
                            <p>Number of Issues: {reportObject.validation.issueCounter}</p>
                            <p>Grade: {reportObject.validation.grade} / 25</p>
                            <p>Criticality: {reportObject.validation.criticality}</p>
                            <p>{
                                (reportObject.validation.issueCounter !== 0) ?
                                    (reportObject.validation.issues.map((issue) => {
                                        return (
                                            <ul>
                                                <li key={this.getKey()}>Description: {issue.message}</li>
                                                <li key={this.getKey()}>Score Impact: {issue.score}</li>
                                            </ul>
                                        );
                                    })) : (null)
                            }
                            </p>
                        </div>
                        <div>
                            <h1>Security</h1>
                            <p>Number of Issues: {reportObject.security.issueCounter}</p>
                            <p>Grade: {reportObject.security.grade} / 25</p>
                            <p>Criticality: {reportObject.security.criticality}</p>
                            <p>{
                                (reportObject.security.issueCounter !== 0) ?
                                    (reportObject.security.issues.map((issue) => {
                                        return (
                                            <ul>
                                                <li key={this.getKey()}>Description: {issue.message}</li>
                                                <li key={this.getKey()}>Score Impact: {issue.score}</li>
                                            </ul>
                                        );
                                    })) : (null)
                            }
                            </p>
                        </div>
                        <div>
                            <h1>Data Validation</h1>
                            <p>Number of Issues: {reportObject.data.issueCounter}</p>
                            <p>Grade: {reportObject.data.grade} / 50</p>
                            <p>Criticality: {reportObject.data.criticality}</p>
                            <p>{
                                (reportObject.data.issueCounter !== 0) ?
                                    (reportObject.data.issues.map((issue) => {
                                        return (
                                            <ul>
                                                <li key={this.getKey()}>Description: {issue.message}</li>
                                                <li key={this.getKey()}>Score Impact: {issue.score}</li>
                                            </ul>
                                        );
                                    })) : (null)
                            }
                            </p>
                        </div>
                    </div>
                )}
            </div>
        );
    }
}

APISecurityAudit.propTypes = {
    apiId: PropTypes.string.isRequired,
};

export default APISecurityAudit;
