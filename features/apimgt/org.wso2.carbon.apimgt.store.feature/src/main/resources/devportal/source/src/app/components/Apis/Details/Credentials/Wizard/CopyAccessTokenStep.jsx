/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useContext } from 'react';
import ViewToken from 'AppComponents/Shared/AppsAndKeys/ViewToken';
import { ApiContext } from 'AppComponents/Apis/Details/ApiContext';
import { Box } from '@material-ui/core';
import Grid from '@material-ui/core/Grid';
import { useHistory } from 'react-router-dom';
import { makeStyles } from '@material-ui/core/styles';
import ButtonPanel from './ButtonPanel';

const useStyles = makeStyles((theme) => ({
    tokenWrapper: {
        paddingLeft: theme.spacing(2),
    },
}));

const copyAccessTokenStep = (props) => {
    const {
        currentStep, createdToken, handleReset,
    } = props;
    const history = useHistory();
    const { api, updateSubscriptionData } = useContext(ApiContext);
    const completeStep = () => {
        updateSubscriptionData(history.push(`/apis/${api.id}/credentials`));
    };
    const classes = useStyles();

    return (
        <div className={classes.tokenWrapper}>
            <Grid md={10}>
                <Box my={1} mx={2}>
                    <ViewToken token={{ ...createdToken, isOauth: true }} />
                </Box>
            </Grid>
            <ButtonPanel
                classes={classes}
                currentStep={currentStep}
                handleCurrentStep={completeStep}
                handleReset={handleReset}
            />
        </div>
    );
};

export default copyAccessTokenStep;
