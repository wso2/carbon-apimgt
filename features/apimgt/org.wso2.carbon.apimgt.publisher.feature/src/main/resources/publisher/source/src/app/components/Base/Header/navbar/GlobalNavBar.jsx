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
import React, { useEffect } from 'react';
import clsx from 'clsx';
import Drawer from '@material-ui/core/Drawer';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import { useTheme } from '@material-ui/core';
import { useLocation } from 'react-router-dom';

import GlobalNavLinks from './GlobalNavLinks';

const useStyles = makeStyles((theme) => ({
    list: {
        width: theme.custom.globalNavBar.opened.drawerWidth,
    },
    drawer: {
        width: theme.custom.globalNavBar.opened.drawerWidth,
        flexShrink: 0,
        whiteSpace: 'nowrap',
    },
    drawerOpen: {
        width: theme.custom.globalNavBar.opened.drawerWidth,
        transition: theme.transitions.create('width', {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.enteringScreen,
        }),
    },
    drawerClose: {
        transition: theme.transitions.create('width', {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
        }),
        overflowX: 'hidden',
        width: theme.spacing(7) + 1,
        [theme.breakpoints.up('sm')]: {
            width: theme.spacing(7) + 1,
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
    paperStyles: {
        backgroundColor: theme.palette.background.drawer,
        top: theme.spacing(8),
    },
}));

const GlobalNavBar = (props) => {
    const {
        open, setOpen,
    } = props;
    const classes = useStyles();
    const theme = useTheme();
    const drawerCommon = { style: { top: theme.spacing(8) } };
    const location = useLocation();

    let isRootPage = false;
    const { pathname } = location;
    if (/^\/(apis|api-products|scopes|service-catalog)($|\/$)/g.test(pathname)) {
        isRootPage = true;
    }
    useEffect(() => {
        if (!isRootPage) {
            setOpen(false);
        }
    }, [location, isRootPage]);
    const pathSegments = pathname && pathname.split('/');
    const [, currentPage] = pathSegments.length > 1 ? pathname.split('/') : ['', ''];
    return (
        <Drawer
            variant={isRootPage ? 'permanent' : 'temporary'}
            className={clsx(classes.drawer, {
                [classes.drawerOpen]: open,
                [classes.drawerClose]: !open,
            })}
            classes={{
                paper: clsx({
                    [classes.drawerOpen]: open,
                    [classes.drawerClose]: !open,
                }),
            }}
            PaperProps={drawerCommon}
            SlideProps={drawerCommon}
            ModalProps={drawerCommon}
            BackdropProps={drawerCommon}
            open={open}
        >
            <GlobalNavLinks selected={currentPage} />
        </Drawer>
    );
};

GlobalNavBar.propTypes = {
    open: PropTypes.bool.isRequired,
};

export default GlobalNavBar;
