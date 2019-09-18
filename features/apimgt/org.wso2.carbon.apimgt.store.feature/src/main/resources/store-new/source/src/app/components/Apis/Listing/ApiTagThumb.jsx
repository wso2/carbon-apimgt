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
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import classNames from 'classnames';

/**
 * @inheritdoc
 * @param {*} theme theme object
 */
const styles = theme => ({
    thumbContent: {
        width: theme.custom.tagThumbnail.width - theme.spacing.unit,
        backgroundColor: theme.palette.background.paper,
        padding: theme.spacing.unit,
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
        width: theme.custom.tagThumbnail.width - theme.spacing.unit,
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        cursor: 'pointer',
        justifyContent: 'center',
        margin: 0,
    },
    contextBox: {
        // eslint-disable-next-line radix
        width: parseInt((150 - theme.spacing.unit) / 2),
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
        marginRight: theme.spacing.unit * 2,
    },
    deleteIcon: {
        fill: 'red',
    },
    textWrapper: {
        color: theme.palette.text.secondary,
        textDecoration: 'none',
    },
    image: {
        width: theme.custom.tagThumbnail.width,
    },
    imageWrapper: {
        color: theme.palette.text.secondary,
        backgroundColor: theme.palette.background.paper,
        width: theme.custom.tagThumbnail.width + theme.spacing.unit,
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
        tag, path, classes, theme,
    } = props;
    const tagLink = path + ':' + tag.value;
    const { thumbnail, tagThumbnail } = theme.custom;
    const name = tag.value.split(theme.custom.tagGroupKey)[0];
    const { contentPictureOverlap } = thumbnail;
    const { defaultTagImage } = tagThumbnail;

    return (
        <div className={classes.thumbWrapper}>
            <Link to={tagLink} className={classes.imageWrapper}>
                <img src={defaultTagImage} className={classes.image} alt='' />
            </Link>
            <div
                className={classNames(classes.thumbContent, {
                    [classes.imageOverlap]: contentPictureOverlap,
                })}
            >
                <Link to={tagLink} className={classes.textWrapper}>
                    <Typography
                        className={classes.thumbHeader}
                        variant='display1'
                        gutterBottom
                        onClick={this}
                        title={name}
                    >
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
            thumbnail: PropTypes.shape({
                contentPictureOverlap: PropTypes.shape({}).isRequired,
            }).isRequired,
            tagThumbnail: PropTypes.shape({
                defaultTagImage: PropTypes.shape({}).isRequired,
            }).isRequired,
            tagGroupKey: PropTypes.string.isRequired,
        }).isRequired,
    }).isRequired,
    listType: PropTypes.shape({}).isRequired,
    tag: PropTypes.shape({
        value: PropTypes.shape({
            split: PropTypes.func,
        }).isRequired,
    }).isRequired,
    path: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(ApiTagThumb);
