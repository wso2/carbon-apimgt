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
import { FormattedMessage } from 'react-intl';
import MaterialIcons from 'MaterialIcons';
import CONSTS from 'AppData/Constants';
import APIProduct from 'AppData/APIProduct';
import StarRatingBar from 'AppComponents/Apis/Listing/StarRatingBar';
import ImageGenerator from './ImageGenerator';
import Api from '../../../data/api';
import { ApiContext } from '../Details/ApiContext';

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    thumbContent: {
        width: theme.custom.thumbnail.width - theme.spacing.unit,
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
        width: theme.custom.thumbnail.width - theme.spacing.unit,
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        cursor: 'pointer',
        margin: 0,
    },
    contextBox: {
        width: parseInt((theme.custom.thumbnail.width - theme.spacing.unit) / 2),
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
    imageWrapper: {
        color: theme.palette.text.secondary,
        backgroundColor: theme.palette.background.paper,
        width: theme.custom.thumbnail.width + theme.spacing.unit,
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
 *
 *
 * @class ApiThumb
 * @extends {React.Component}
 */
const windowURL = window.URL || window.webkitURL;

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
            category: MaterialIcons.categories[0].name,
            selectedIcon: null,
            color: null,
            backgroundIndex: null,
            imageObj: null,
        };
    }

    /**
     *
     *
     * @memberof ApiThumb
     */
    componentDidMount() {
        const { apiType } = this.context;
        const { api } = this.props;

        let restApi = null;

        if (apiType === CONSTS.API_TYPE) {
            restApi = new Api();
        } else if (apiType === CONSTS.API_PRODUCT_TYPE) {
            restApi = new APIProduct();
        }
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
        });
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
        const { apiType } = this.context;

        let path = '/apis/';
        if (apiType === CONSTS.API_PRODUCT_TYPE) {
            path = '/api-products/';
        }

        return path;
    }

    /**
     *
     *
     * @returns
     * @memberof ApiThumb
     */
    render() {
        const {
            imageObj, selectedIcon, color, backgroundIndex, category,
        } = this.state;
        const path = this.getPathPrefix();

        const details_link = path + this.props.api.id;
        const { api, classes, theme } = this.props;
        const { thumbnail } = theme.custom;
        const {
            name, version, context, provider,
        } = api;
        const imageWidth = thumbnail.width;
        const defaultImage = thumbnail.defaultApiImage;

        let ImageView;
        if (imageObj) {
            ImageView = <img height={140} src={imageObj} alt='API Product Thumbnail' className={classes.media} />;
        } else {
            ImageView = (
                <ImageGenerator
                    width={imageWidth}
                    height={140}
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
            <div className={classes.thumbWrapper}>
                <Link to={details_link} className={classes.imageWrapper}>
                    {!defaultImage && ImageView}
                    {defaultImage && <img src={defaultImage} />}
                </Link>

                <div
                    className={classNames(classes.thumbContent, {
                        [classes.imageOverlap]: thumbnail.contentPictureOverlap,
                    })}
                >
                    <Link to={details_link} className={classes.textWrapper}>
                        <Typography
                            className={classes.thumbHeader}
                            variant='display1'
                            gutterBottom
                            onClick={this.handleRedirectToAPIOverview}
                            title={name}
                        >
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
                                <StarRatingBar apiRating={api.avgRating} apiId={api.id} isEditable={false} showSummary={false} />
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
            </div>
        );
    }
}

ApiThumb.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

ApiThumb.contextType = ApiContext;

export default withStyles(styles, { withTheme: true })(ApiThumb);
