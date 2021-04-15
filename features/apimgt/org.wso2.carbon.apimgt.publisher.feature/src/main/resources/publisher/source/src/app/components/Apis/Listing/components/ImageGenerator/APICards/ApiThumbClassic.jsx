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
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import { Link } from 'react-router-dom';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import CardMedia from '@material-ui/core/CardMedia';
import { isRestricted } from 'AppData/AuthManager';
import Typography from '@material-ui/core/Typography';
import Chip from '@material-ui/core/Chip';
import { FormattedMessage, injectIntl } from 'react-intl';
import CircularProgress from '@material-ui/core/CircularProgress';
import green from '@material-ui/core/colors/green';
import API from 'AppData/api';
import DeleteApiButton from 'AppComponents/Apis/Details/components/DeleteApiButton';

import BaseThumbnail from '../BaseThumbnail';

const styles = (theme) => ({
    card: {
        margin: theme.spacing(3 / 2),
        maxWidth: theme.custom.thumbnail.width,
        transition: 'box-shadow 0.3s ease-in-out',
    },
    providerText: {
        textTransform: 'capitalize',
    },
    apiDetails: { padding: theme.spacing(1) },
    apiActions: { justifyContent: 'space-between', padding: `0px 0px ${theme.spacing(1)}px 8px` },
    deleteProgress: {
        color: green[200],
        position: 'absolute',
        marginLeft: '200px',
    },
    thumbHeader: {
        width: '90%',
        whiteSpace: 'nowrap',
        color: theme.palette.text.secondary,
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        cursor: 'pointer',
        margin: 0,
        'padding-left': '5px',
    },
    imageWrapper: {
        color: theme.palette.text.secondary,
        backgroundColor: theme.palette.background.paper,
        width: theme.custom.thumbnail.width + theme.spacing(1),
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
    },
    thumbContent: {
        width: theme.custom.thumbnail.width - theme.spacing(1),
        backgroundColor: theme.palette.background.paper,
        padding: theme.spacing(1),
    },
    thumbLeft: {
        alignSelf: 'flex-start',
        flex: 1,
        width: '25%',
        'padding-left': '5px',
        'padding-right': '65px',
    },
    thumbRight: {
        alignSelf: 'flex-end',
    },
    thumbInfo: {
        display: 'flex',
    },
    contextBox: {
        width: '110px',
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
    imageOverlap: {
        position: 'absolute',
        bottom: 1,
        backgroundColor: theme.custom.thumbnail.contentBackgroundColor,
    },
    row: {
        display: 'inline-block',
    },
    textWrapper: {
        color: theme.palette.text.secondary,
        textDecoration: 'none',
    },
    thumbBy: {
        'padding-left': '5px',
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
});

/**
 *
 * Render API Card component in API listing card view,containing essential API information like name , version ect
 * @class APIThumb
 * @extends {Component}
 */
class APIThumb extends Component {
    /**
     *Creates an instance of APIThumb.
     * @param {*} props
     * @memberof APIThumb
     */
    constructor(props) {
        super(props);
        this.state = { isHover: false, loading: false };
        this.toggleMouseOver = this.toggleMouseOver.bind(this);
        this.setLoading = this.setLoading.bind(this);
    }

    /**
     *
     * Set loading state
     * @param {String} loadingState New state value
     * @memberof APIThumb
     */
    setLoading(loadingState) {
        this.setState({ loading: loadingState });
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
            classes, api, isAPIProduct, theme, updateData,
        } = this.props;
        const { isHover, loading } = this.state;
        let overviewPath = '';
        if (api.apiType) {
            overviewPath = isAPIProduct ? `/api-products/${api.id}/overview` : `/apis/${api.id}/overview`;
        } else {
            overviewPath = `/apis/${api.apiUUID}/documents/${api.id}/details`;
        }
        if (isAPIProduct) {
            api.apiType = API.CONSTS.APIProduct;
        } else {
            api.apiType = API.CONSTS.API;
        }

        if (!api.lifeCycleStatus) {
            api.lifeCycleStatus = api.status;
        }

        return (
            <Card
                onMouseOver={this.toggleMouseOver}
                onFocus={this.toggleMouseOver}
                onMouseOut={this.toggleMouseOver}
                onBlur={this.toggleMouseOver}
                elevation={isHover ? 4 : 1}
                className={classes.card}
            >
                <CardMedia
                    src='None'
                    component={BaseThumbnail}
                    height={theme.custom.thumbnail.height}
                    width={theme.custom.thumbnail.width}
                    title='Thumbnail'
                    api={api}
                />
                <CardContent className={classes.apiDetails}>
                    <div className={classes.textWrapper}>
                        <Link to={overviewPath}>
                            <Typography gutterBottom variant='h4' className={classes.thumbHeader} title={api.name}>
                                {api.name}
                            </Typography>
                        </Link>
                    </div>
                    <div className={classes.row}>
                        <Typography variant='caption' gutterBottom align='left' className={classes.thumbBy}>
                            <FormattedMessage id='by' defaultMessage='By' />
                            <FormattedMessage id='colon' defaultMessage=' : ' />
                            {api.provider}
                        </Typography>
                    </div>
                    <div className={classes.thumbInfo}>
                        {isAPIProduct ? null : (
                            <div className={classes.row}>
                                <div className={classes.thumbLeft}>
                                    <Typography variant='subtitle1'>{api.version}</Typography>
                                </div>

                                <div className={classes.thumbLeft}>
                                    <Typography variant='caption' gutterBottom align='left'>
                                        <FormattedMessage defaultMessage='Version' id='Apis.Listing.ApiThumb.version' />
                                    </Typography>
                                </div>
                            </div>
                        )}
                        <div className={classes.row}>
                            <div className={classes.thumbRight}>
                                <Typography variant='subtitle1' align='right' className={classes.contextBox}>
                                    {api.context}
                                </Typography>
                            </div>

                            <div className={classes.thumbRight}>
                                <Typography variant='caption' gutterBottom align='right' className={classes.context}>
                                    {api.type === 'WS' ? (
                                        <FormattedMessage
                                            defaultMessage='Channel'
                                            id='Apis.Listing.ApiThumb.channel'
                                        />
                                    ) : (
                                        <FormattedMessage
                                            defaultMessage='Context'
                                            id='Apis.Listing.ApiThumb.context'
                                        />
                                    )}
                                </Typography>
                            </div>
                        </div>
                    </div>
                </CardContent>
                <CardActions className={classes.apiActions}>
                    <Chip
                        size='small'
                        classes={{ root: classes.thumbRightBy, label: classes.thumbRightByLabel }}
                        label={api.apiType === API.CONSTS.APIProduct ? api.state : api.lifeCycleStatus}
                        color='default'
                    />
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
                    {!isRestricted(['apim:api_create'], api) && (
                        <>
                            <DeleteApiButton
                                setLoading={this.setLoading}
                                api={api}
                                updateData={updateData}
                                isAPIProduct={isAPIProduct}
                            />
                            {loading && <CircularProgress className={classes.deleteProgress} />}
                        </>
                    )}
                </CardActions>
            </Card>
        );
    }
}

APIThumb.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
        name: PropTypes.string,
        apiType: PropTypes.string.isRequired,
    }).isRequired,
    isAPIProduct: PropTypes.bool.isRequired,
    updateData: PropTypes.func.isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(APIThumb));
