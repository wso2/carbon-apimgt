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

import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Footer from './Footer/Footer';

const styles = (theme) => ({
    appBar: {
        position: 'relative',
        background: theme.palette.background.appBar,
    },
    icon: {
        marginRight: theme.spacing(2),
    },
    menuIcon: {
        color: theme.palette.getContrastText(theme.palette.background.appBar),
        fontSize: 35,
    },
    userLink: {
        color: theme.palette.getContrastText(theme.palette.background.appBar),
    },
    // Page layout styles
    drawer: {
        top: 64,
    },
    wrapper: {
        minHeight: '100%',
        marginBottom: -50,
        background: theme.custom.wrapperBackground,
    },
    contentWrapper: {
        display: 'flex',
        flexDirection: 'row',
        position: 'relative',
        minHeight: 'calc(100vh - 114px)',
    },
    push: {
        height: 50,
    },
    footer: {
        backgroundColor: theme.palette.grey.A100,
        paddingLeft: theme.spacing(3),
        height: 50,
        alignItems: 'center',
        display: 'flex',
    },
    toolbar: {
        minHeight: 56,
        [`${theme.breakpoints.up('xs')} and (orientation: landscape)`]: {
            minHeight: 48,
        },
        [theme.breakpoints.up('sm')]: {
            minHeight: 64,
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
        <>
            <div className={classes.wrapper}>
                {header}
                <div className={classes.contentWrapper}>{children}</div>

                <div className={classes.push} />
            </div>
            <Footer />
        </>
    );
};

Base.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    children: PropTypes.element.isRequired,
    header: PropTypes.element.isRequired,
};

export default withStyles(styles)(Base);
