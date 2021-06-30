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
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Icon from '@material-ui/core/Icon';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { app } from 'Settings';

/**
 * @inheritdoc
 * @param {*} theme theme object
 */
const styles = (theme) => ({
    thumbContent: {
        width: theme.custom.tagWise.thumbnail.width - theme.spacing(1),
        backgroundColor: theme.palette.background.paper,
        padding: theme.spacing(1),
    },
    thumbLeft: {
        alignSelf: 'flex-start',
        flex: 1,
    },
    thumbRight: {
        alignSelf: 'flex-end',
    },
    thumbInfo: {
        display: 'flex',
    },
    thumbHeader: {
        width: theme.custom.tagWise.thumbnail.width - theme.spacing(1),
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        cursor: 'pointer',
        justifyContent: 'center',
        margin: 0,
    },
    contextBox: {
        // eslint-disable-next-line radix
        width: parseInt(150 - theme.spacing(0.5)),
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        cursor: 'pointer',
        margin: 0,
        display: 'inline-block',
        lineHeight: '1em',
    },
    thumbWrapper: {
        position: 'relative',
        paddingTop: 20,
        marginRight: theme.spacing(2),
    },
    deleteIcon: {
        fill: 'red',
    },
    textWrapper: {
        color: theme.custom.tagCloud.leftMenu.color,
        '& .material-icons': {
            color: theme.custom.tagCloud.leftMenu.color,
        },
    },
    image: {
        width: theme.custom.tagWise.thumbnail.width,
    },
    imageWrapper: {
        color: theme.palette.text.secondary,
        backgroundColor: theme.palette.background.paper,
        width: theme.custom.tagWise.thumbnail.width + theme.spacing(1),
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
    },
    imageOverlap: {
        position: 'absolute',
        bottom: 1,
        backgroundColor: theme.custom.thumbnail.contentBackgroundColor,
    },
});

/**
 * Get ApiTagThumb
 * @param {*} props properties
 * @returns {*}
 */
function ApiTagThumb(props) {
    const {
        tag, path, classes, theme, style, mainPage,
    } = props;
    const tagLink = path + ':' + tag.value;
    const {
        tagWise: {
            thumbnail: { image },
        },
    } = theme.custom;
    const name = tag.value.split(theme.custom.tagWise.key)[0];
    if (style === 'fixed-left' || !mainPage) {
        return (
            <Link to={tagLink} className={classes.textWrapper}>
                <ListItem button>
                    <ListItemIcon>
                        <Icon>label</Icon>
                    </ListItemIcon>
                    <ListItemText primary={name} />
                </ListItem>
            </Link>
        );
    }

    return (
        <div className={classes.thumbWrapper}>
            <Link to={tagLink} className={classes.imageWrapper}>
                <img src={app.context + image} className={classes.image} alt='' />
            </Link>
            <div className={classNames(classes.thumbContent)}>
                <Link to={tagLink} className={classes.textWrapper}>
                    <Typography className={classes.thumbHeader} variant='h4' gutterBottom title={name}>
                        {name}
                    </Typography>
                </Link>
            </div>
        </div>
    );
}

ApiTagThumb.propTypes = {
    classes: PropTypes.shape({
        thumbWrapper: PropTypes.shape({}).isRequired,
        imageWrapper: PropTypes.shape({}).isRequired,
        thumbContent: PropTypes.shape({}).isRequired,
        imageOverlap: PropTypes.shape({}).isRequired,
        textWrapper: PropTypes.shape({}).isRequired,
        thumbHeader: PropTypes.shape({}).isRequired,
        image: PropTypes.shape({}).isRequired,
    }).isRequired,
    theme: PropTypes.shape({
        custom: PropTypes.shape({
            tagWise: PropTypes.shape({}).isRequired,
        }).isRequired,
    }).isRequired,
    tag: PropTypes.shape({
        value: PropTypes.shape({
            split: PropTypes.func,
        }).isRequired,
    }).isRequired,
    path: PropTypes.shape({}).isRequired,
    style: PropTypes.string.isRequired,
};

export default withStyles(styles, { withTheme: true })(ApiTagThumb);
