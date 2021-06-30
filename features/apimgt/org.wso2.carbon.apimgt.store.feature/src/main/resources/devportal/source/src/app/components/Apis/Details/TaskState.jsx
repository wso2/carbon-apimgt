/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import React from 'react';
import Grid from '@material-ui/core/Grid';
import Alert from '@material-ui/lab/Alert';
import { FormattedMessage } from 'react-intl';
import CircularProgress from '@material-ui/core/CircularProgress';

/**
 * @export
 * @param {JSON} props props from parent
 * @return {JSX} rendered output
 */
export default function TaskState(props) {
    const {
        completed, errors, inProgress, children, completedMessage, inProgressMessage,
    } = props;
    let severity;
    let message = children;
    if (completed) {
        severity = 'success';
        if (completedMessage) {
            message = completedMessage;
        }
    } else if (inProgress) {
        severity = 'info';
        if (inProgressMessage) {
            message = inProgressMessage;
        }
    } else {
        severity = 'waiting';
    }
    if (errors) {
        severity = 'error';
        if (errors.response) {
            const { body } = errors.response;
            message = (
                <>
                    <b>
                        [
                        {body.code}
                        ]
                    </b>
                    {' '}
                    :
                    {body.description}
                </>
            );
        } else {
            message = (
                <>
                    <FormattedMessage
                        id='Apis.Listing.TaskState.generic.error.prefix'
                        defaultMessage='Error while'
                    />
                    {' '}
                    {inProgressMessage}
                </>
            );
        }
    }

    return (
        <>
            <Grid item xs={12}>
                <Alert
                    icon={inProgress ? <CircularProgress size={20} thickness={2} /> : null}
                    variant={errors ? 'standard' : 'plain'}
                    severity={severity}
                >
                    {message}
                </Alert>
            </Grid>
        </>
    );
}
