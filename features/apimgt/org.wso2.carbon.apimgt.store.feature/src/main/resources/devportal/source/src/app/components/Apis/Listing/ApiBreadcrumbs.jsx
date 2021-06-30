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
import { FormattedMessage } from 'react-intl';
import { makeStyles, useTheme } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Breadcrumbs from '@material-ui/core/Breadcrumbs';
import Link from '@material-ui/core/Link';
import { Link as RouterLink } from 'react-router-dom';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import Icon from '@material-ui/core/Icon';

const useStyles = makeStyles((theme) => ({
    root: {
        padding: theme.spacing(1, 3),
    },
    link: {
        display: 'flex',
        alignItems: 'center',
    },
    linkNotActive: {
        display: 'flex',
        alignItems: 'center',
        cursor: 'default',
    },
    icon: {
        marginRight: theme.spacing(0.5),
        width: 20,
        height: 20,
    },
    selectedTagText: {
        textIndent: 4,
    },
    apiGroup: {
        color: theme.palette.grey[800],
    },
}));

/**
 * Render no api breadcrumb section.
 * @param {JSON} props properties passed down from the parent.
 * @returns {JSX} Api breadcrumb section.
 */
export default function ApiBreadcrumbs(props) {
    const classes = useStyles();
    const theme = useTheme();
    const { selectedTag } = props;
    return (
        <Paper elevation={0} className={classes.root}>
            <Breadcrumbs aria-label='breadcrumb'>
                <RouterLink
                    to={theme.custom.tagWise.active && theme.custom.tagWise.style === 'page' ? '/api-groups' : '/apis'}
                    className={classes.apiGroup}
                >
                    <Link color='inherit' className={classes.link}>
                        <Icon className={classes.icon}>dynamic_feed</Icon>
                        <FormattedMessage defaultMessage='API Groups' id='Apis.Listing.ApiBreadcrumbs.apigroups.main' />
                    </Link>
                </RouterLink>

                {selectedTag && (
                    <Link color='inherit' className={classes.linkNotActive}>
                        <CustomIcon width={16} height={16} icon='api' />
                        <span className={classes.selectedTagText}>{selectedTag}</span>
                    </Link>
                )}
            </Breadcrumbs>
        </Paper>
    );
}
