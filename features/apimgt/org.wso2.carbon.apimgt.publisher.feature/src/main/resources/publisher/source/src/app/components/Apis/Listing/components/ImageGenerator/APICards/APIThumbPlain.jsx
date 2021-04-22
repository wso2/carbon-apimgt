import React, { useEffect, useState } from 'react';
import { makeStyles, useTheme } from '@material-ui/core/styles';
import Icon from '@material-ui/core/Icon';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Chip from '@material-ui/core/Chip';
import CircularProgress from '@material-ui/core/CircularProgress';
import { isRestricted } from 'AppData/AuthManager';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import Api from 'AppData/api';
import DeleteApiButton from 'AppComponents/Apis/Details/components/DeleteApiButton';
import Configurations from 'Config';

import getIcon from './ImageUtils';

const useStyles = makeStyles((theme) => ({
    root: {
        minWidth: 200,
        marginTop: 10,
        marginBottom: 10,
        marginRight: 10,
    },
    bullet: {
        display: 'inline-block',
        margin: '0 2px',
        transform: 'scale(0.8)',
    },
    title: {
        fontSize: 14,
    },
    pos: {
        marginBottom: 12,
    },
    thumbHeader: {
        width: '150px',
        color: '#444',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        cursor: 'pointer',
        margin: 0,
        'padding-left': '5px',
    },
    contextBox: {
        maxWidth: 120,
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        paddingLeft: '5px',
    },
    caption: {
        color: theme.palette.grey[700],
    },
    imageDisplay: {
        maxWidth: '40px',
        maxHeight: '40px',
    },
    thumbRightBy: {
        'margin-right': '5px',
        height: 18,
        borderRadius: 8,
    },
    thumbRightByLabel: {
        paddingLeft: 5,
        paddingRight: 5,
    },
}));
const windowURL = window.URL || window.webkitURL;

/**
 * Render a thumbnail
 * @param {JSON} props required pros.
 * @returns {JSX} Thumbnail rendered output.
 */
function APIThumbPlain(props) {
    const theme = useTheme();
    const classes = useStyles();
    const {
        api, showInfo, isAPIProduct, updateData,
    } = props;
    const { custom: { thumbnail } } = theme;
    const {
        name, version, context, provider,
    } = api;

    const [imageConf, setImageConf] = useState({
        selectedIcon: '',
        category: '',
        color: '#ccc',
    });
    const [imageObj, setIMageObj] = useState(null);
    const [imageLoaded, setImageLoaded] = useState(false);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const restApi = new Api();

        const promisedThumbnail = restApi.getAPIThumbnail(api.id);

        promisedThumbnail.then((response) => {
            if (response && response.data) {
                if (response.headers['content-type'] === 'application/json') {
                    const iconJson = JSON.parse(response.data);
                    setImageConf({
                        selectedIcon: iconJson.key,
                        category: iconJson.category,
                        color: iconJson.color,
                    });
                } else if (response && response.data.size > 0) {
                    setIMageObj(windowURL.createObjectURL(response.data));
                }
            }
        }).finally(() => {
            setImageLoaded(true);
        });
    }, []);
    let ImageView;
    if (!imageLoaded) {
        ImageView = (
            <div className='image-load-frame'>
                <div className='image-load-animation1' />
                <div className='image-load-animation2' />
            </div>
        );
    } else if (imageObj) {
        ImageView = (
            <img
                src={imageObj}
                alt='API Thumbnail'
                className={classes.imageDisplay}
            />
        );
    } else {
        ImageView = (
            <Icon className={classes.icon} style={{ fontSize: 40 + 'px', color: imageConf.color }}>
                {getIcon(imageConf.selectedIcon, imageConf.category, theme, api)}
            </Icon>
        );
    }

    if (!showInfo) {
        return (
            <Link to={'/apis/' + api.id} aria-hidden='true'>
                <Box display='flex'>
                    <Box>
                        {!thumbnail.defaultApiImage && ImageView}
                        {thumbnail.defaultApiImage
                        && (
                            <img
                                src={Configurations.app.context + thumbnail.defaultApiImage}
                                alt='img'
                            />
                        )}
                    </Box>
                </Box>

            </Link>
        );
    }
    return (
        <Card className={classes.root} variant='outlined'>
            <CardContent>
                <Box>
                    <Link to={'/apis/' + api.id + '/overview'} aria-hidden='true'>
                        <Box display='flex'>
                            <Box>
                                {!thumbnail.defaultApiImage && ImageView}
                                {thumbnail.defaultApiImage
                                && <img src={Configurations.app.context + thumbnail.defaultApiImage} alt='img' />}
                            </Box>
                            <Typography
                                variant='h5'
                                gutterBottom
                                title={name}
                                className={classes.thumbHeader}
                            >
                                {name}
                            </Typography>
                        </Box>

                    </Link>
                </Box>
                {provider && (
                    <>
                        <Typography
                            variant='caption'
                            gutterBottom
                            align='left'
                            className={classes.caption}
                            component='span'
                        >
                            <FormattedMessage defaultMessage='By' id='Apis.Listing.ApiThumb.by' />
                            <FormattedMessage defaultMessage=' : ' id='Apis.Listing.ApiThumb.by.colon' />
                        </Typography>
                        <Typography variant='body2' component='span'>{provider}</Typography>
                    </>
                )}
                <Box display='flex' mt={2}>
                    <Box flex={1}>
                        <Typography variant='subtitle1'>{version}</Typography>
                        <Typography variant='caption' gutterBottom align='left' className={classes.caption}>
                            <FormattedMessage defaultMessage='Version' id='Apis.Listing.ApiThumb.version' />
                        </Typography>
                    </Box>
                    <Box>
                        <Typography variant='subtitle1' align='right' className={classes.contextBox}>
                            {context}
                        </Typography>
                        <Typography
                            variant='caption'
                            gutterBottom
                            align='right'
                            className={classes.caption}
                            Component='div'
                        >
                            <FormattedMessage defaultMessage='Context' id='Apis.Listing.ApiThumb.context' />
                        </Typography>
                    </Box>
                </Box>

                <Box display='flex' mt={2}>
                    <Box flex={1}>
                        {!isAPIProduct && (
                            <Chip
                                label={api.apiType === Api.CONSTS.APIProduct ? api.state : api.lifeCycleStatus}
                                color='default'
                                size='small'
                                classes={{ root: classes.thumbRightBy, label: classes.thumbRightByLabel }}
                            />
                        )}
                        {(api.type === 'GRAPHQL' || api.transportType === 'GRAPHQL') && (
                            <Chip
                                size='small'
                                classes={{ root: classes.thumbRightBy, label: classes.thumbRightByLabel }}
                                label={api.transportType === undefined
                                    ? api.type : api.transportType}
                                color='primary'
                            />
                        )}
                        {(api.type === 'WS') && (
                            <Chip
                                size='small'
                                classes={{ root: classes.thumbRightBy, label: classes.thumbRightByLabel }}
                                label='WEBSOCKET'
                                color='primary'
                            />
                        )}
                        {(api.type === 'WEBSUB') && (
                            <Chip
                                size='small'
                                classes={{ root: classes.thumbRightBy, label: classes.thumbRightByLabel }}
                                label='WEBSUB'
                                color='primary'
                            />
                        )}
                    </Box>
                    {!isRestricted(['apim:api_create'], api) && (
                        <>
                            <DeleteApiButton
                                setLoading={setLoading}
                                api={api}
                                updateData={updateData}
                                isAPIProduct={isAPIProduct}
                            />
                            {loading && <CircularProgress className={classes.deleteProgress} />}
                        </>
                    )}
                </Box>
            </CardContent>
        </Card>
    );
}


APIThumbPlain.defaultProps = {
    showInfo: true,
};
APIThumbPlain.propTypes = {
    showInfo: PropTypes.bool,
    updateData: PropTypes.func.isRequired,
    isAPIProduct: PropTypes.bool.isRequired,
};

export default APIThumbPlain;
