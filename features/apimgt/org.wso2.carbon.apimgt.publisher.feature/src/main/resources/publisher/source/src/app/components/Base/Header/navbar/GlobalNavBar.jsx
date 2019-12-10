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
import { Drawer, withStyles } from '@material-ui/core';
import PropTypes from 'prop-types';
import Hidden from '@material-ui/core/Hidden';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import GlobalNavLinks from './GlobalNavLinks';

const styles = (theme) => ({
    list: {
        width: theme.custom.drawerWidth,
    },
    drawerStyles: {
        top: 56, // Based on https://github.com/mui-org/material-ui/issues/10076#issuecomment-361232810
        [`${theme.breakpoints.up('xs')} and (orientation: landscape)`]: {
            top: 48,
        },
        [theme.breakpoints.up('sm')]: {
            top: 64,
        },
    },
    listText: {
        color: theme.palette.getContrastText(theme.palette.background.drawer),
    },
    listInline: {
        '& ul': {
            display: 'flex',
            flexDirection: 'row',
        },
    },
});
const GlobalNavBar = (props) => {
    const {
        open, toggleGlobalNavBar, classes, theme,
    } = props;

    const commonStyle = {
        style: { top: 64 },
    };
    const paperStyles = {
        style: {
            backgroundColor: theme.palette.background.drawer,
            top: 64,
        },
    };
    return (
        <>
            <Hidden smDown>
                <VerticalDivider height={32} />
                <div className={classes.listInline}>
                    <GlobalNavLinks smallView />
                </div>
            </Hidden>
            <Hidden mdUp>
                <Drawer
                    className={classes.drawerStyles}
                    PaperProps={paperStyles}
                    SlideProps={commonStyle}
                    ModalProps={commonStyle}
                    BackdropProps={commonStyle}
                    open={open}
                    onClose={toggleGlobalNavBar}
                >
                    <div tabIndex={0} role='button' onClick={toggleGlobalNavBar} onKeyDown={toggleGlobalNavBar}>
                        <div className={classes.list} />
                    </div>
                    <div
                        tabIndex={0}
                        role='button'
                        onClick={toggleGlobalNavBar}
                        onKeyDown={toggleGlobalNavBar}
                    >
                        <div className={classes.list}>
                            <GlobalNavLinks smallView={false} />
                        </div>
                    </div>
                </Drawer>
            </Hidden>
        </>
    );
};

GlobalNavBar.propTypes = {
    open: PropTypes.bool.isRequired,
    toggleGlobalNavBar: PropTypes.func.isRequired,
    classes: PropTypes.shape({
        drawerStyles: PropTypes.string,
        list: PropTypes.string,
        listText: PropTypes.string,
    }).isRequired,
    theme: PropTypes.shape({
        palette: PropTypes.shape({
            getContrastText: PropTypes.func,
            background: PropTypes.shape({
                drawer: PropTypes.string,
                leftMenu: PropTypes.string,
            }),
        }),
    }).isRequired,
};

export default withStyles(styles, { withTheme: true })(GlobalNavBar);
