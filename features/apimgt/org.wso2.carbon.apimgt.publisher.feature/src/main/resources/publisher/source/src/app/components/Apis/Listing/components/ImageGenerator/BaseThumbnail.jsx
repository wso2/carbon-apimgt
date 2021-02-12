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

import React, { useEffect, useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import EditIcon from '@material-ui/icons/Edit';
import ButtonBase from '@material-ui/core/ButtonBase';
import { Link } from 'react-router-dom';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';

import Api from 'AppData/api';
import APIProduct from 'AppData/APIProduct';
import ImageGenerator from './ImageGenerator';

const useStyles = makeStyles((theme) => ({
    suppressLinkStyles: {
        textDecoration: 'none',
        color: theme.palette.text.disabled,
    },
    thumbButton: {
        position: 'absolute',
        left: 0,
        right: 0,
        top: 0,
        bottom: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: theme.palette.common.white,
    },
    thumbBackdrop: {
        position: 'absolute',
        left: 0,
        right: 0,
        top: 0,
        bottom: 0,
        backgroundColor: theme.palette.common.black,
        opacity: 0.4,
    },
    thumb: {
        '&:hover': {
            zIndex: 1,
            '& $thumbBackdrop': {
                opacity: 0.2,
            },
        },
    },
    media: {
        // ⚠️ object-fit is not supported by IE11.
        objectFit: 'cover',
    },
}));


const windowURL = window.URL || window.webkitURL;

const BaseThumbnail = (props) => {
    const {
        api, width, height, thumbnail: thumbnailPop,
        selectedIcon: selectedIconProp,
        color: colorProp,
        backgroundIndex: backgroundIndexProp,
        category: categoryProp,
        isEditable,
        onClick,
        imageUpdate,
    } = props;
    const {
        apiType, id, type,
    } = api;
    const classes = useStyles();
    const [iconJson, setIconJson] = useState({});
    const {
        key,
        color,
        backgroundIndex,
        category,
    } = iconJson;
    const [thumbnail, setThumbnail] = useState(null);
    const [imageLoaded, setImageLoaded] = useState(false);

    useEffect(() => {
        setIconJson({
            selectedIcon: selectedIconProp,
            color: colorProp,
            backgroundIndex: backgroundIndexProp,
            category: categoryProp,
        });
    }, [selectedIconProp, colorProp, backgroundIndexProp, categoryProp]);

    useEffect(() => {
        setThumbnail(thumbnailPop);
    }, [thumbnailPop]);
    /**
     * Load the image from the backend and keeps in the component state
     *
     * @memberof ThumbnailView
     */
    const loadImageData = () => {
        if (type !== 'DOC') {
            const promisedThumbnail = apiType === Api.CONSTS.APIProduct
                ? new APIProduct().getAPIProductThumbnail(id)
                : new Api().getAPIThumbnail(id);

            promisedThumbnail.then((response) => {
                if (response && response.data) {
                    if (response.headers['content-type'] === 'application/json') {
                        setThumbnail(null);
                        setIconJson(JSON.parse(response.data));
                    } else if (response && response.data.size > 0) {
                        const url = windowURL.createObjectURL(response.data);
                        setThumbnail(url);
                    }
                }
            }).finally(() => {
                setImageLoaded(true);
            });
        } else {
            setImageLoaded(true);
        }
    };
    useEffect(() => {
        loadImageData();
    }, []);
    useEffect(() => {
        loadImageData();
    }, [imageUpdate]);
    if (!imageLoaded) {
        return (
            <div className='image-load-frame'>
                <div className='image-load-animation1' />
                <div className='image-load-animation2' />
            </div>
        );
    }
    let overviewPath = '';
    if (apiType) {
        overviewPath = apiType === Api.CONSTS.APIProduct
            ? `/api-products/${api.id}/overview` : `/apis/${api.id}/overview`;
    } else {
        overviewPath = `/apis/${api.apiUUID}/documents/${api.id}/details`;
    }
    const view = thumbnail
        ? <img height={height} width={width} src={thumbnail} alt='API Thumbnail' className={classes.media} />
        : (
            <ImageGenerator
                width={width}
                height={height}
                api={api}
                fixedIcon={{
                    key: key || selectedIconProp,
                    color,
                    backgroundIndex,
                    category,
                    api,
                }}
            />
        );
    return (
        <>
            {isEditable ? (
                <ButtonBase
                    focusRipple
                    className={classes.thumb}
                    onClick={onClick}
                >
                    {view}
                    <span className={classes.thumbBackdrop} />
                    <span className={classes.thumbButton}>
                        <Typography component='span' variant='subtitle1' color='inherit'>
                            <EditIcon />
                        </Typography>
                    </span>
                </ButtonBase>
            ) : (
                <Link className={classes.suppressLinkStyles} to={overviewPath}>
                    {view}
                </Link>
            )}
        </>
    );
};
BaseThumbnail.defaultProps = {
    height: 190,
    width: 250,
    isEditable: false,
};
BaseThumbnail.propTypes = {
    api: PropTypes.shape({}).isRequired,
    height: PropTypes.number,
    width: PropTypes.number,
    isEditable: PropTypes.bool,
    imageUpdate: PropTypes.number.isRequired,
};
export default BaseThumbnail;
