/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

import React from 'react';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import { makeStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';

const useStyles = makeStyles((theme) => ({
    createTitle: {
        color: theme.palette.getContrastText(theme.palette.background.default),
    },
    formContent: {
        '& span, & div, & p, & input': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        }
    }
}));

/**
 * Base component for Password change form
 *
 * @param {Object} props title and children components are expected
 * @returns {React.Component} Base element
 */
function ChangePasswordBase(props) {
    const { title, children } = props;
    const classes = useStyles();
    return (
        <Box width={1} mt={5}>
            <Grid justify='center' container spacing={3}>
                <Grid item sm={6} md={4}>
                    <Grid container spacing={4}>
                        <Grid item md={12} className={classes.createTitle}>
                            {title}
                        </Grid>
                        <Grid item md={12} className={classes.formContent}>
                            <Paper elevation={0}>{children}</Paper>
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        </Box>
    );
}
ChangePasswordBase.propTypes = {
    title: PropTypes.element.isRequired,
    children: PropTypes.element.isRequired,
};
export default ChangePasswordBase;
