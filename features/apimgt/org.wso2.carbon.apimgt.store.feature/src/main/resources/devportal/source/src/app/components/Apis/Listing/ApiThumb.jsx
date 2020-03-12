/* eslint-disable react/prop-types */
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
import Card from '@material-ui/core/Card';
import CardMedia from '@material-ui/core/CardMedia';
import CardContent from '@material-ui/core/CardContent';
import { withStyles } from '@material-ui/core/styles';
import Chip from '@material-ui/core/Chip';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import MaterialIcons from 'MaterialIcons';
import StarRatingBar from 'AppComponents/Apis/Listing/StarRatingBar';
import ImageGenerator from './ImageGenerator';
import Api from '../../../data/api';
import { ApiContext } from '../Details/ApiContext';
import classNames from 'classnames';

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    card: {
        margin: theme.spacing(3 / 2),
        maxWidth: theme.custom.thumbnail.width,
        transition: 'box-shadow 0.3s ease-in-out',
        position: 'relative',
    },
    apiDetails: {
        padding: theme.spacing(1),
        background: theme.custom.thumbnail.contentBackgroundColor,
        padding: theme.spacing(1),
        color: theme.palette.getContrastText(theme.custom.thumbnail.contentBackgroundColor),
        '& a': {
            color: theme.palette.getContrastText(theme.custom.thumbnail.contentBackgroundColor),
        },
    },
    suppressLinkStyles: {
        textDecoration: 'none',
        color: theme.palette.text.disabled,
    },
    row: {
        display: 'inline-block',
    },
    thumbBy: {
        'padding-left': '5px',
    },
    media: {
        // ⚠️ object-fit is not supported by IE11.
        objectFit: 'cover',
    },
    thumbContent: {
        width: theme.custom.thumbnail.width - theme.spacing(2),
    },
    thumbLeft: {
        alignSelf: 'flex-start',
        flex: 1,
        width: '25%',
        'padding-left': '5px',
        'padding-right': '65px',
    },
    thumbLeftAction: {
        alignSelf: 'flex-start',
        flex: 1,
        width: '25%',
        'padding-left': '5px',
        'padding-right': '10px',
    },
    thumbRight: {
        alignSelf: 'flex-end',
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
        'padding-left': '5px',
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
        'padding-top': 5,
        'padding-right': 5,
        'padding-bottom': 1.5,
        textAlign: 'left',
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
        color: theme.palette.text.secondary,
        textDecoration: 'none',
    },
    imageWrapper: {
        color: theme.custom.thumbnail.iconColor,
        width: theme.custom.thumbnail.width,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
    },
    imageOverlap: {
        position: 'absolute',
        bottom: 1,
    },
    chipWrapper: {
        marginTop: '15px',
    },
    ratingWrapper: {
        marginTop: '20px',
    },
    textblock: {
        color: theme.palette.text.secondary,
        position: 'absolute',
        bottom: '35px',
        right: '10px',
        background: theme.custom.thumbnail.contentBackgroundColor,
        'padding-left': '10px',
        'padding-right': '10px',
    },
});

const windowURL = window.URL || window.webkitURL;

/**
 *
 * Render API Card component in API listing card view,containing essential API information like name , version ect
 * @class APIThumb
 * @extends {Component}
 */
class ApiThumb extends React.Component {
    /**
     *Creates an instance of APIThumb.
     * @param {*} props
     * @memberof APIThumb
     */
    constructor(props) {
        super(props);
        this.state = {
            category: MaterialIcons.categories[0].name,
            selectedIcon: null,
            color: null,
            backgroundIndex: null,
            imageObj: null,
            isHover: false,
            imageLoaded: false,
        };
        this.toggleMouseOver = this.toggleMouseOver.bind(this);
    }

    /**
     *
     *
     * @memberof ApiThumb
     */
    componentDidMount() {
        const { api } = this.props;
        const restApi = new Api();

        const promisedThumbnail = restApi.getAPIThumbnail(api.id);

        promisedThumbnail.then((response) => {
            if (response && response.data) {
                if (response.headers['content-type'] === 'application/json') {
                    const iconJson = JSON.parse(response.data);
                    this.setState({
                        selectedIcon: iconJson.key,
                        category: iconJson.category,
                        color: iconJson.color,
                        backgroundIndex: iconJson.backgroundIndex,
                    });
                } else if (response && response.data.size > 0) {
                    const url = windowURL.createObjectURL(response.data);
                    this.setState({ imageObj: url });
                }
            }
        }).finally(() => {
            this.setState({ imageLoaded: true })
        })
    }

    /**
     * Clean up resource
     */
    componentWillUnmount() {
        if (this.state.thumbnail) {
            windowURL.revokeObjectURL(this.state.imageObj);
        }
    }

    /**
     * Get Path Prefix depedning on the respective API Type being rendered
     *
     * @returns {String} path
     * @memberof ApiThumb
     */
    getPathPrefix() {
        const path = '/apis/';
        return path;
    }

    /**
     * Toggle mouse Hover state to set the card `raised` property
     *
     * @param {React.SyntheticEvent} event mouseover and mouseout
     * @memberof APIThumb
     */
    toggleMouseOver(event) {
        this.setState({ isHover: event.type === 'mouseover' });
    }

    /**
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof APIThumb
     */
    render() {
        const {
            imageObj, selectedIcon, color, backgroundIndex, category, isHover, imageLoaded,
        } = this.state;
        const path = this.getPathPrefix();
        const { isMonetizationEnabled } = this.context;

        const detailsLink = path + this.props.api.id;
        const {
            api, classes, theme, customWidth, customHeight, showInfo,
        } = this.props;
        const { custom: { thumbnail, social: { showRating } } } = theme;
        const { name, version, context } = api;

        let { provider } = api;
        if (
            api.businessInformation &&
            api.businessInformation.businessOwner &&
            api.businessInformation.businessOwner.trim() !== ''
        ) {
            provider = api.businessInformation.businessOwner;
        }
        if (!api.lifeCycleStatus) {
            api.lifeCycleStatus = api.status;
        }
        const imageWidth = customWidth || thumbnail.width;
        const imageHeight = customHeight || 140;
        const defaultImage = thumbnail.defaultApiImage;

        let ImageView;
        if (!imageLoaded) {
            ImageView = (<div class="image-load-frame">
                <div class="image-load-animation1"></div>
                <div class="image-load-animation2"></div>
            </div>)
        } else if (imageObj) {
            ImageView = (
                <img
                    height={imageHeight}
                    width={imageWidth}
                    src={imageObj}
                    alt='API Thumbnail'
                    className={classes.media}
                />
            );
        } else {
            ImageView = (
                <ImageGenerator
                    width={imageWidth}
                    height={imageHeight}
                    api={api}
                    fixedIcon={{
                        key: selectedIcon,
                        color,
                        backgroundIndex,
                        category,
                        api,
                    }}
                />
            );
        }
        return (
            <Card
                onMouseOver={this.toggleMouseOver}
                onFocus={this.toggleMouseOver}
                onMouseOut={this.toggleMouseOver}
                onBlur={this.toggleMouseOver}
                raised={isHover}
                className={classNames('image-thumbnail', classes.card)}
            >
                {isMonetizationEnabled && (
                    <div className={classes.textblock}>{api.monetizationLabel}</div>
                )}
                <CardMedia>
                    <Link to={detailsLink} className={classes.suppressLinkStyles}>
                        {!defaultImage && ImageView}
                        {defaultImage && <img src={defaultImage} alt='img' />}
                    </Link>
                </CardMedia>
                {showInfo && (
                    <CardContent classes={{ root: classes.apiDetails }}>
                        <Link to={detailsLink} className={classes.textWrapper}>
                            <Typography
                                className={classes.thumbHeader}
                                variant='h5'
                                gutterBottom
                                onClick={this.handleRedirectToAPIOverview}
                                title={name}
                            >
                                {name}
                            </Typography>
                        </Link>
                        <div className={classes.row}>
                            <Typography variant='caption' gutterBottom align='left' className={classes.thumbBy}>
                                <FormattedMessage defaultMessage='By' id='Apis.Listing.ApiThumb.by' />
                                <FormattedMessage defaultMessage=' : ' id='Apis.Listing.ApiThumb.by.colon' />
                                {provider}
                            </Typography>
                        </div>
                        <div className={classes.thumbInfo}>
                            <div className={classes.row}>
                                <div className={classes.thumbLeft}>
                                    <Typography variant='subtitle1'>{version}</Typography>
                                    <Typography variant='caption' gutterBottom align='left'>
                                        <FormattedMessage defaultMessage='Version' id='Apis.Listing.ApiThumb.version' />
                                    </Typography>
                                </div>
                            </div>
                            <div className={classes.row}>
                                <div className={classes.thumbRight}>
                                    <Typography variant='subtitle1' align='right' className={classes.contextBox}>
                                        {context}
                                    </Typography>
                                    <Typography
                                        variant='caption'
                                        gutterBottom
                                        align='right'
                                        className={classes.context}
                                    >
                                        <FormattedMessage defaultMessage='Context' id='Apis.Listing.ApiThumb.context' />
                                    </Typography>
                                </div>
                            </div>
                        </div>
                        <div className={classes.thumbInfo}>
                            {showRating && <div className={classes.thumbLeftAction}>
                                <Typography
                                    variant='subtitle1'
                                    gutterBottom
                                    align='left'
                                    className={classNames('api-thumb-rating', classes.ratingWrapper)}
                                >
                                    <StarRatingBar
                                        apiRating={api.avgRating}
                                        apiId={api.id}
                                        isEditable={false}
                                        showSummary={false}
                                    />
                                </Typography>
                            </div>}
                            <div className={classes.thumbRight}>
                                <Typography
                                    variant='subtitle1'
                                    gutterBottom
                                    align='right'
                                    className={classes.chipWrapper}
                                >
                                    {(api.type === 'GRAPHQL' || api.transportType === 'GRAPHQL') && (
                                        <Chip
                                            label={api.transportType === undefined ? api.type : api.transportType}
                                            color='primary'
                                        />
                                    )}
                                    {(api.lifeCycleStatus === 'PROTOTYPED') && (
                                        <Chip
                                        label={api.apiType === 'APIProduct' ? api.state : api.lifeCycleStatus}
                                        color='default'
                                    />
                                    )}
                                </Typography>
                            </div>
                        </div>
                    </CardContent>
                )}
            </Card>
        );
    }
}
ApiThumb.defaultProps = {
    customWidth: null,
    customHeight: null,
    showInfo: true,
};
ApiThumb.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    customWidth: PropTypes.number,
    customHeight: PropTypes.number,
    showInfo: PropTypes.bool,
};

ApiThumb.contextType = ApiContext;

export default withStyles(styles, { withTheme: true })(ApiThumb);
