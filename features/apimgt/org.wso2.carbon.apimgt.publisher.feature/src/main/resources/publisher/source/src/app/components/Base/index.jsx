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

import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import { Grid } from '@material-ui/core';
import { withStyles } from '@material-ui/core/styles';

const styles = theme => ({
    baseContainer: {
        height: '100vh',
        paddingTop: 56,
        [`${theme.breakpoints.up('xs')} and (orientation: landscape)`]: {
            paddingTop: 48,
        },
        [theme.breakpoints.up('sm')]: {
            paddingTop: 64,
        },
    },
});

/**
 * Base Component for the publisher app
 * Adding a padding to Base container to avoid overlapping content with the Header AppBar
 * Following padding top values are based on material UI AppBar height parameters as described in here:
 * https://github.com/mui-org/material-ui/issues/10076#issuecomment-361232810
 * @class Layout
 * @extends {React.Component}
 */
const Base = ({ classes, children, header }) => {
    return (
        <Fragment>
            {header}
            <Grid classes={{ container: classes.baseContainer }} container>
                {children}
            </Grid>
        </Fragment>
    );
};

Base.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    children: PropTypes.element.isRequired,
    header: PropTypes.element.isRequired,
};

export default withStyles(styles)(Base);
