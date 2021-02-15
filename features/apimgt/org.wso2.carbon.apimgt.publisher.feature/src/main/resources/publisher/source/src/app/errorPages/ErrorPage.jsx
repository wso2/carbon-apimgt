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

import React from 'react';
import Paper from '@material-ui/core/Paper';
import PropTypes from 'prop-types';
import Box from '@material-ui/core/Box';
import Container from '@material-ui/core/Container';
import { makeStyles } from '@material-ui/core/styles';
import ErrorList from './ErrorList';

const useStyles = makeStyles((theme) => ({
    root: {
        marginTop: theme.spacing(15),
    },
}));

const ErrorPage = (props) => {
    const { errorCode } = props;
    const classes = useStyles();
    return (
        <>
            <Container maxWidth='md' className={classes.root}>
                <Box padding={4}>
                    <Paper elevation={2}>
                        <Box padding={4}>
                            <Box padding={2} border={1} borderColor='error.main'>
                                <ErrorList errorCode={errorCode} />
                            </Box>
                        </Box>
                    </Paper>
                </Box>
            </Container>
        </>
    );
};

ErrorPage.propTypes = {
    errorCode: PropTypes.string.isRequired,
};

export default ErrorPage;
