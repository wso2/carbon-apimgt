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
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        // Include code to pass in the data from the backend
        const { api } = this.props;
        const promisedReport = api.getSecurityAuditReport(api.id);

        promisedReport
            .then(response => response.json())
            .then(results => this.setState({ items: results }))
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    // this.setState({ error: true });
                } else if (status === 401) {
                    // doRedirectToLogin();
                }
                // TODO - Re-check the catch code
            });
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div
                width='100%'
                height='calc(100vh - 51px)'
            >
                <Typography variant='h6'>API Security Audit Report</Typography>
                <p>API Security Audit works!</p>
                {this.state.items.map((item) => {
                    return (
                        <div>
                            <p>{item.grade}</p>
                            <p>{item.criticality}</p>
                        </div>
                    );
                })}
            </div>
        );
    }
}

export default APISecurityAudit;
