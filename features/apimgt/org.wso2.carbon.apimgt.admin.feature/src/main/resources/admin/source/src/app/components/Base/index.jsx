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
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import CssBaseline from '@material-ui/core/CssBaseline';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';

/**
 * Render copyright
 * @returns {JSX}.
 */
function Copyright() {
    return (
        <Typography variant='body2' color='textSecondary' align='center'>
            <FormattedMessage
                id='Base.Footer.Footer.product_details'
                defaultMessage='WSO2 API-M v4.0.0 | © 2021 WSO2 Inc'
            />
        </Typography>
    );
}

const drawerWidth = 256;

const styles = (theme) => ({
    root: {
        display: 'flex',
        minHeight: '100vh',
    },
    drawer: {
        [theme.breakpoints.up('sm')]: {
            width: drawerWidth,
            flexShrink: 0,
        },
    },
    app: {
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
    },

    footer: {
        padding: theme.spacing(2),
        background: '#eaeff1',
        position: 'relative',
        bottom: 0,
    },
});

/**
 * Render base page component.
 * @param {JSON} props component props.
 * @returns {JSX} Loading animation.
 */
function Base(props) {
    const {
        classes, children, leftMenu, header,
    } = props;


    return (
        <div className={classes.root}>
            <CssBaseline />
            <nav className={classes.drawer}>
                {leftMenu}
            </nav>
            <div className={classes.app}>
                {header}
                {children}
                <footer className={classes.footer}>
                    <Copyright />
                </footer>
            </div>
        </div>
    );
}

Base.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    children: PropTypes.element.isRequired,
    header: PropTypes.element.isRequired,
    leftMenu: PropTypes.element.isRequired,
};

export default withStyles(styles)(Base);
