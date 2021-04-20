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
import { withStyles } from '@material-ui/core/styles';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Icon from '@material-ui/core/Icon';
import Tooltip from '@material-ui/core/Tooltip';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';

/**
 * @inheritdoc
 * @param {*} theme theme object
 */
const styles = (theme) => ({
    textWrapper: {
        color: theme.custom.tagCloud.leftMenu.color,
        '& .material-icons': {
            color: theme.custom.tagCloud.leftMenu.color,
        },
    },
    listItemText: {
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
    },
});

/**
 * Get APICategoryThumb
 * @param {JSON} props properties
 * @returns {JSX} category link
 */
function APICategoryThumb(props) {
    const {
        category, path, classes,
    } = props;
    const categoryLink = path + ':' + category.name;
    let categoryDesc = category.description;
    if (categoryDesc.length > 50) {
        categoryDesc = categoryDesc.substring(0, 50) + '...';
    }
    return (
        <Link to={categoryLink} className={classes.textWrapper}>
            <Tooltip placement='right' title={category.description.length <= 50 ? '' : category.description}>
                <ListItem button alignItems='flex-start'>
                    <ListItemIcon>
                        <Icon>label</Icon>
                    </ListItemIcon>
                    <ListItemText
                        primary={category.name}
                        secondary={categoryDesc}
                        classes={{ primary: classes.listItemText }}
                    />
                </ListItem>
            </Tooltip>
        </Link>
    );
}

APICategoryThumb.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    category: PropTypes.shape({}).isRequired,
    path: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(APICategoryThumb);
