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
import Chip from '@material-ui/core/Chip';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage } from 'react-intl';
import ImageGenerator from './ImageGenerator';
import StarRatingBar from './StarRating';
import Api from '../../../data/api';

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    thumbContent: {
        width: theme.custom.imageThumbnail.width - theme.spacing.unit,
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
        width: theme.custom.imageThumbnail.width,
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        cursor: 'pointer',
        margin: 0,
    },
    contextBox: {
        width: parseInt((theme.custom.imageThumbnail.width - theme.spacing.unit) / 2),
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
    },
    deleteIcon: {
        fill: 'red',
    },
    imageWrapper: {
        color: theme.palette.text.secondary,
        textDecoration: 'none',
    },
    imageOverlap: {
        position: 'absolute',
        bottom: 1,
        backgroundColor: theme.custom.imageThumbnail.contentBackgroundColor,
    },
});
/**
 *
 *
 * @class ApiThumb
 * @extends {React.Component}
 */
class ApiThumb extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            active: true,
            loading: false,
            open: false,
            overview_link: '',
            isRedirect: false,
            openMoreMenu: false,
            rating: 0,
        };
    }

    /**
     *
     *
     * @memberof ApiThumb
     */
    componentDidMount() {
        const api = new Api();
        const promised_rating = api.getRatingFromUser(this.props.api.id, null);
        promised_rating.then((response) => {
            this.setState({ rating: response.obj.userRating });
        });
    }

    /**
     *
     *
     * @returns
     * @memberof ApiThumb
     */
    render() {
        const details_link = '/apis/' + this.props.api.id;
        const { api, classes, theme } = this.props;
        const { imageThumbnail } = theme.custom;
        const {
            name, version, context, provider,
        } = this.props.api;
        const { rating } = this.state;
        const starColor = theme.palette.getContrastText(theme.custom.imageThumbnail.contentBackgroundColor);
        const imageWidth = theme.custom.imageThumbnail.width;
        const defaultImage = theme.custom.imageThumbnail.defaultApiImage;
        return (
            <Grid item xs={12} sm={6} md={4} lg={3} xl={3} className={classes.thumbWrapper}>
                <Link to={details_link} className={classes.imageWrapper}>
                    {!defaultImage && <ImageGenerator api={api} width={imageWidth} />}
                    {defaultImage && <img src={defaultImage} />}
                </Link>
                <div
                    className={classNames(classes.thumbContent, {
                        [classes.imageOverlap]: imageThumbnail.contentPictureOverlap,
                    })}
                >
                    <Link to={details_link} className={classes.imageWrapper}>
                        <Typography className={classes.thumbHeader} variant='display1' gutterBottom onClick={this.handleRedirectToAPIOverview} title={name}>
                            {name}
                        </Typography>
                    </Link>
                    <Typography variant='caption' gutterBottom align='left'>
                        <FormattedMessage defaultMessage='By:' id='Apis.Listing.ApiThumb.by' />
                        {provider}
                    </Typography>
                    <div className={classes.thumbInfo}>
                        <div className={classes.thumbLeft}>
                            <Typography variant='subheading'>{version}</Typography>
                            <Typography variant='caption' gutterBottom align='left'>
                                <FormattedMessage defaultMessage='Version' id='Apis.Listing.ApiThumb.version' />
                            </Typography>
                        </div>
                        <div className={classes.thumbRight}>
                            <Typography variant='subheading' align='right' className={classes.contextBox}>
                                {context}
                            </Typography>
                            <Typography variant='caption' gutterBottom align='right'>
                                <FormattedMessage defaultMessage='Context' id='Apis.Listing.ApiThumb.context' />
                            </Typography>
                        </div>
                    </div>
                    <div className={classes.thumbInfo}>
                        <div className={classes.thumbLeft}>
                            <Typography variant='subheading' gutterBottom align='left'>
                                <StarRatingBar rating={rating} starColor={starColor} />
                            </Typography>
                        </div>
                        <div className={classes.thumbRight}>
                            <Typography variant='subheading' gutterBottom align='right'>
                                {api.type === 'GRAPHQL' && (
                                    <Chip label={api.type} color='primary' />
                                )}
                            </Typography>
                        </div>
                    </div>
                </div>
            </Grid>
        );
    }
}

ApiThumb.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(ApiThumb);
