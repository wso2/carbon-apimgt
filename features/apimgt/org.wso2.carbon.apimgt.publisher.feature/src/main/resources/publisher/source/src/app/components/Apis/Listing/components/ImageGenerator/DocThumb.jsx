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
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import CardMedia from '@material-ui/core/CardMedia';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage, injectIntl } from 'react-intl';
import green from '@material-ui/core/colors/green';

import ThumbnailView from './ThumbnailView';

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
                <CardMedia src='None' component={ThumbnailView} height={140} title='Thumbnail' api={doc} />
                <CardContent className={classes.docDetails}>
                    <Typography gutterBottom variant='h5' component='h2'>
                        {doc.name}
                    </Typography>
                    <Grid container>
                        <Grid item md={6}>
                            <FormattedMessage
                                id='Apis.Listing.components.ImageGenerator.DocThumb.sourceType'
                                defaultMessage='Source Type'
                            />
:
                            <Typography variant='body1'>{doc.sourceType}</Typography>
                        </Grid>
                        <Grid item md={6}>
                            <FormattedMessage
                                id='Apis.Listing.components.ImageGenerator.DocThumb.apiName'
                                defaultMessage='Api Name'
                            />
:
                            <Typography className={classes.providerText} variant='body1' gutterBottom>
                                {doc.apiName}
                            </Typography>
                        </Grid>
                        <Grid item md={6}>
                            <FormattedMessage
                                id='Apis.Listing.components.ImageGenerator.DocThumb.apiVersion'
                                defaultMessage='Api Version'
                            />
:
                            <Typography variant='body1' gutterBottom>
                                {doc.apiVersion}
                            </Typography>
                        </Grid>
                    </Grid>
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
