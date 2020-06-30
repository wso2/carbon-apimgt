/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import APICategoriesCard from 'AppComponents/AdminPages/Dashboard/APICategoriesCard';
import RateLimitingCard from 'AppComponents/AdminPages/Dashboard/RateLimitingCard';
import TasksWorkflowCard from 'AppComponents/AdminPages/Dashboard/TasksWorkflowCard';

/**
 * Render progress inside a container centering in the container.
 * @returns {JSX} Loading animation.
 */
export default function Dashboard() {
    return (
        <ContentBase width='full' title='Dashboard' pageStyle='paperLess'>
            <Grid container spacing={3} justify='center'>
                <Grid item xs={11} md={6}>
                    <RateLimitingCard />
                </Grid>
                <Grid item xs={11} md={6}>
                    <APICategoriesCard />
                </Grid>
                <Grid item xs={11} md={6}>
                    <TasksWorkflowCard />
                </Grid>
            </Grid>
        </ContentBase>
    );
}
