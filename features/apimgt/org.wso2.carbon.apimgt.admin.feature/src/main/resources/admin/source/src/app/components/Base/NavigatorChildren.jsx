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
import { useAppContext } from 'AppComponents/Shared/AppContext';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import Divider from '@material-ui/core/Divider';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import { Link as RouterLink } from 'react-router-dom';
import Link from '@material-ui/core/Link';
import Collapse from '@material-ui/core/Collapse';
import ExpandLess from '@material-ui/icons/ExpandLess';
import ExpandMore from '@material-ui/icons/ExpandMore';

/**
 * Render a list
 * @param {JSON} props .
 * @returns {JSX} Header AppBar components.
 */
function NavigatorChildren(props) {
    const { isSuperTenant } = useAppContext();
    const [open, setOpen] = React.useState(true);
    const { navChildren, navId, classes } = props;
    const handleClick = () => {
        setOpen(!open);
    };
    let navigationChildren = navChildren;
    if (isSuperTenant) {
        navigationChildren = navChildren.filter((menu) => menu.id !== 'Tenant Theme');
    }

    if (!isSuperTenant) {
        navigationChildren = navChildren.filter((menu) => menu.id !== 'Custom Policies');
    }

    return (
        <>
            <ListItem className={classes.categoryHeader} button onClick={handleClick}>
                <ListItemText
                    classes={{
                        primary: classes.categoryHeaderPrimary,
                    }}
                >
                    {navId}
                </ListItemText>
                {open ? <ExpandLess /> : <ExpandMore />}

            </ListItem>
            <Collapse in={open} timeout='auto' unmountOnExit>
                {navigationChildren && navigationChildren.map(({
                    id: childId, icon, path, active,
                }) => (
                    <Link component={RouterLink} to={path || '/'} style={{ textDecoration: 'none' }}>
                        <ListItem
                            key={childId}
                            button
                            className={clsx(classes.item, active && classes.itemActiveItem)}
                        >
                            <ListItemIcon className={classes.itemIcon}>{icon}</ListItemIcon>
                            <ListItemText
                                classes={{
                                    primary: classes.itemPrimary,
                                }}
                            >
                                {childId}
                            </ListItemText>
                        </ListItem>
                    </Link>
                ))}
            </Collapse>
            <Divider className={classes.divider} />
        </>
    );
}

Navigator.NavigatorChildren = {
    classes: PropTypes.shape({}).isRequired,
    navChildren: PropTypes.arrayOf(JSON).isRequired,
    navId: PropTypes.number.isRequired,
};

export default NavigatorChildren;
