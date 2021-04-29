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
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import CardMedia from '@material-ui/core/CardMedia';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl } from 'react-intl';
import green from '@material-ui/core/colors/green';

import BaseThumbnail from './BaseThumbnail';

const styles = (theme) => ({
    card: {
        margin: theme.spacing(3 / 2),
        maxWidth: theme.spacing(32),
        transition: 'box-shadow 0.3s ease-in-out',
    },
    providerText: {
        textTransform: 'capitalize',
    },
    docDetails: { padding: theme.spacing(1) },
    docActions: { justifyContent: 'space-between', padding: `0px 0px ${theme.spacing(1)}px 0px` },
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
    },
});

/**
 *
 * Render Doc Card component in Search listing card view,containing essential Doc information like doc name ,
 * source type ect
 * @class DocThumb
 * @extends {Component}
 */
class DocThumb extends Component {
    /**
     *Creates an instance of DocThumb.
     * @param {*} props
     * @memberof DocThumb
     */
    constructor(props) {
        super(props);
        this.state = { isHover: false };
        this.toggleMouseOver = this.toggleMouseOver.bind(this);
    }

    /**
     * Toggle mouse Hover state to set the card `raised` property
     *
     * @param {React.SyntheticEvent} event mouseover and mouseout
     * @memberof DocThumb
     */
    toggleMouseOver(event) {
        this.setState({ isHover: event.type === 'mouseover' });
    }

    /**
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof DocThumb
     */
    render() {
        const { classes, doc } = this.props;
        const { isHover } = this.state;
        return (
            <Card
                onMouseOver={this.toggleMouseOver}
                onFocus={this.toggleMouseOver}
                onMouseOut={this.toggleMouseOver}
                onBlur={this.toggleMouseOver}
                raised={isHover}
                className={classes.card}
            >
                <CardMedia src='None' component={BaseThumbnail} height={140} title='Thumbnail' api={doc} />
                <CardContent className={classes.apiDetails}>
                    <div className={classes.textWrapper}>
                        <Link to='/'>
                            <Typography gutterBottom variant='h4' className={classes.thumbHeader} title={doc.name}>
                                {doc.name}
                            </Typography>
                        </Link>
                    </div>
                    <div className={classes.row}>
                        <Typography variant='caption' gutterBottom align='left' className={classes.thumbBy}>
                            <FormattedMessage
                                id='Apis.Listing.components.ImageGenerator.DocThumb.sourceType'
                                defaultMessage='Source Type:'
                            />
                            {doc.sourceType}
                        </Typography>
                    </div>
                    <div className={classes.thumbInfo}>
                        <div className={classes.row}>
                            <div className={classes.thumbLeft}>
                                <Typography variant='subtitle1'>{doc.apiName}</Typography>
                            </div>

                            <div className={classes.thumbLeft}>
                                <Typography variant='caption' gutterBottom align='left'>
                                    <FormattedMessage
                                        id='Apis.Listing.components.ImageGenerator.DocThumb.apiName'
                                        defaultMessage='API Name'
                                    />
                                </Typography>
                            </div>
                        </div>
                        <div className={classes.row}>
                            <div className={classes.thumbRight}>
                                <Typography variant='subtitle1' align='right' className={classes.contextBox}>
                                    {doc.apiVersion}
                                </Typography>
                            </div>

                            <div className={classes.thumbRight}>
                                <Typography variant='caption' gutterBottom align='right' className={classes.context}>
                                    <FormattedMessage
                                        id='Apis.Listing.components.ImageGenerator.DocThumb.apiVersion'
                                        defaultMessage='API Version'
                                    />
                                </Typography>
                            </div>
                        </div>
                    </div>
                </CardContent>
            </Card>
        );
    }
}

DocThumb.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    doc: PropTypes.shape({
        id: PropTypes.string,
        name: PropTypes.string,
        sourceType: PropTypes.string.isRequired,
        apiName: PropTypes.string.isRequired,
        apiVersion: PropTypes.string.isRequired,
    }).isRequired,
};

export default injectIntl(withStyles(styles)(DocThumb));
