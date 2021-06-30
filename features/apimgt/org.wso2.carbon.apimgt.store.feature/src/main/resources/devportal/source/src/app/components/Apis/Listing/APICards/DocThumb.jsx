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
import { FormattedMessage } from 'react-intl';
import MaterialIcons from 'MaterialIcons';
import ImageGenerator from './ImageGenerator';
import { ApiContext } from '../../Details/ApiContext';

const styles = (theme) => ({
    thumbContent: {
        width: theme.custom.thumbnail.width - theme.spacing(1),
        backgroundColor: theme.palette.background.paper,
        padding: theme.spacing(1),
        minHeight: 130,
    },
    thumbLeft: {
        alignSelf: 'flex-start',
        flex: 1,
    },
    thumbRight: {
        alignSelf: 'flex-end',
        display: 'flex',
        flexDirection: 'column',
    },
    thumbInfo: {
        display: 'flex',
    },
    thumbHeader: {
        width: theme.custom.thumbnail.width - theme.spacing(1),
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        cursor: 'pointer',
        margin: 0,
    },
    contextBox: {
        width: parseInt((theme.custom.thumbnail.width - theme.spacing(1)) / 2, 10),
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
        paddingTop: 15,
        marginLeft: theme.spacing(2),
    },
    deleteIcon: {
        fill: 'red',
    },
    textWrapper: {
        color: theme.palette.text.secondary,
        textDecoration: 'none',
    },
    imageWrapper: {
        color: theme.palette.text.secondary,
        backgroundColor: theme.palette.background.paper,
        width: theme.custom.thumbnail.width + theme.spacing(1),
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

const windowURL = window.URL || window.webkitURL;
/**
 *
 *
 * @class DocThumb
 * @extends {React.Component}
 */
class DocThumb extends React.Component {
    /**
     * Creates an instance of DocThumb.
     * @param {JSON} props properties
     * @memberof DocThumb
     */
    constructor(props) {
        super(props);
        this.state = {
            category: MaterialIcons.categories[0].name,
            selectedIcon: null,
            color: null,
            backgroundIndex: null,
            imageObj: null,
        };
    }

    /**
     * Clean up resource
     */
    componentWillUnmount() {
        const { thumbnail, imageObj } = this.state;
        if (thumbnail) {
            windowURL.revokeObjectURL(imageObj);
        }
    }

    /**
     * @returns {JSX} doc thumbnail
     * @memberof DocThumb
     */
    render() {
        const {
            selectedIcon, color, backgroundIndex, category,
        } = this.state;
        const { doc, classes, theme } = this.props;
        const {
            doc: {
                name, sourceType, apiName, apiVersion, id, apiUUID,
            },
        } = this.props;
        const detailsLink = '/apis/' + apiUUID + '/documents/' + id + '/details';
        const { thumbnail } = theme.custom;
        const imageWidth = thumbnail.width;
        const defaultImage = thumbnail.defaultApiImage;

        const ImageView = (
            <ImageGenerator
                width={imageWidth}
                height={140}
                api={doc}
                fixedIcon={{
                    key: selectedIcon,
                    color,
                    backgroundIndex,
                    category,
                    doc,
                }}
            />
        );

        return (
            <div className={classes.thumbWrapper}>
                <Link to={detailsLink} className={classes.imageWrapper}>
                    {!defaultImage && ImageView}
                    {defaultImage && <img src={defaultImage} alt='document' />}
                </Link>

                <div
                    className={classNames(classes.thumbContent, {
                        [classes.imageOverlap]: thumbnail.contentPictureOverlap,
                    })}
                >
                    <Link to={detailsLink} className={classes.textWrapper}>
                        <Typography
                            className={classes.thumbHeader}
                            variant='h4'
                            gutterBottom
                            onClick={this.handleRedirectToAPIOverview}
                            title={name}
                        >
                            {name}
                        </Typography>
                    </Link>
                    <Typography variant='caption' gutterBottom align='left'>
                        <FormattedMessage defaultMessage='Source Type:' id='Apis.Listing.DocThumb.sourceType' />
                        {sourceType}
                    </Typography>
                    <div className={classes.thumbInfo}>
                        <div className={classes.thumbLeft}>
                            <Typography variant='subtitle1'>{apiName}</Typography>
                            <Typography variant='caption' gutterBottom align='left'>
                                <FormattedMessage defaultMessage='Api Name' id='Apis.Listing.DocThumb.apiName' />
                            </Typography>
                        </div>
                        <div className={classes.thumbRight}>
                            <Typography variant='subtitle1' align='right' className={classes.contextBox}>
                                {apiVersion}
                            </Typography>
                            <Typography variant='caption' gutterBottom align='right' component='div'>
                                <FormattedMessage defaultMessage='API Version' id='Apis.Listing.DocThumb.apiVersion' />
                            </Typography>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

DocThumb.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

DocThumb.contextType = ApiContext;

export default withStyles(styles, { withTheme: true })(DocThumb);
