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
import PropTypes from 'prop-types';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import { withStyles } from '@material-ui/core/styles';

/**
 * @inheritdoc
 * @param {*} theme theme object
 */
const styles = (theme) => ({
    createTitle: {
        color: theme.palette.getContrastText(theme.palette.background.default),
    },
    applicationContent: {
        '& span, & div, & p, & input': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        },
    },
});
/**
 * Base component for all API create forms
 *
 * @param {Object} props title and children components are expected
 * @returns {React.Component} Base element
 */
function ApplicationCreateBase(props) {
    const { title, children, classes } = props;
    return (
        <Box mt={5}>
            <Grid container spacing={3}>
                {/*
            Following two grids control the placement of whole create page
            For centering the content better use `container` props, but
            instead used an empty grid item for flexibility
             */}
                <Grid item sm={12} md={3} />
                <Grid item sm={12} md={6}>
                    <Grid container spacing={5}>
                        <Grid item md={12} className={classes.createTitle}>
                            {title}
                        </Grid>
                        <Grid item md={12} className={classes.applicationContent}>
                            <Paper elevation={0}>{children}</Paper>
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        </Box>

    );
}
ApplicationCreateBase.propTypes = {
    title: PropTypes.element.isRequired,
    children: PropTypes.element.isRequired,
};
export default withStyles(styles)(ApplicationCreateBase);
