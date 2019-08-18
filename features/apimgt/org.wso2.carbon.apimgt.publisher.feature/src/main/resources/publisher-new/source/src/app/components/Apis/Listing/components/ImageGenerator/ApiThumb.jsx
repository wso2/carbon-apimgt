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
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import CardMedia from '@material-ui/core/CardMedia';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Chip from '@material-ui/core/Chip';
import { FormattedMessage, injectIntl } from 'react-intl';
import CircularProgress from '@material-ui/core/CircularProgress';
import green from '@material-ui/core/colors/green';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import DeleteApiButton from 'AppComponents/Apis/Details/components/DeleteApiButton';

import ThumbnailView from './ThumbnailView';

const styles = theme => ({
    card: {
        margin: theme.spacing.unit * (3 / 2),
        maxWidth: theme.spacing.unit * 32,
        transition: 'box-shadow 0.3s ease-in-out',
    },
    providerText: {
        textTransform: 'capitalize',
    },
    apiDetails: { padding: theme.spacing.unit },
    apiActions: { justifyContent: 'space-between', padding: `0px 0px ${theme.spacing.unit}px 0px` },
    deleteProgress: {
        color: green[200],
        position: 'absolute',
        marginLeft: '200px',
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
        this.handleApiDelete = this.handleApiDelete.bind(this);
    }

    /**
     *
     * Delete an API listed in the listing page
     * @param {React.SyntheticEvent} event OnClick event of delete button
     * @param {String} [name=''] API Name use for alerting purpose only
     * @memberof Listing
     */
    handleApiDelete() {
        const { id, name } = this.props.api;
        this.setState({ loading: true });
        const { updateData, isAPIProduct } = this.props;
        if (isAPIProduct) {
            const promisedDelete = API.deleteProduct(id);
            promisedDelete.then((response) => {
                if (response.status !== 200) {
                    Alert.info('Something went wrong while deleting the API Product!');
                    return;
                }
                updateData(id);
                Alert.info(`API Product ${name} deleted Successfully`);
                this.setState({ loading: false });
            });
        } else {
            const promisedDelete = API.delete(id);
            promisedDelete.then((response) => {
                if (response.status !== 200) {
                    Alert.info('Something went wrong while deleting the API!');
                    return;
                }
                updateData(id);
                Alert.info(`API ${name} deleted Successfully`);
                this.setState({ loading: false });
            });
        }
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
        const { classes, api, isAPIProduct } = this.props;
        const { isHover, loading } = this.state;
        api.apiType = API.CONSTS.API;
        if (isAPIProduct) {
            api.apiType = API.CONSTS.APIProduct;
        }

        return (
            <Card
                onMouseOver={this.toggleMouseOver}
                onFocus={this.toggleMouseOver}
                onMouseOut={this.toggleMouseOver}
                onBlur={this.toggleMouseOver}
                raised={isHover}
                className={classes.card}
            >
                <CardMedia src='None' component={ThumbnailView} height={140} title='Thumbnail' api={api} />
                <CardContent className={classes.apiDetails}>
                    <Typography gutterBottom variant='h5' component='h2'>
                        {api.name}
                    </Typography>
                    <Grid container>
                        <Grid item md={6}>
                            <FormattedMessage id='by' defaultMessage='By' />:
                            <Typography className={classes.providerText} variant='body1' gutterBottom>
                                {api.provider}
                            </Typography>
                        </Grid>
                        <Grid item md={6}>
                            <FormattedMessage id='context' defaultMessage='Context' />:
                            <Typography variant='body1' gutterBottom>
                                {api.context}
                            </Typography>
                        </Grid>
                        {isAPIProduct ? null : (
                            <Grid item md={6}>
                                <FormattedMessage id='version' defaultMessage='Version' />:
                                <Typography variant='body1'>{api.version}</Typography>
                            </Grid>
                        )}
                    </Grid>
                </CardContent>
                <CardActions className={classes.apiActions}>
                    <Chip
                        label={(api.apiType === API.CONSTS.APIProduct) ? api.state : api.lifeCycleStatus}
                        color='default'
                    />
                    {api.type === 'GRAPHQL' && (
                        <Chip label={api.type} color='primary' />
                    )}
                    <DeleteApiButton onClick={this.handleApiDelete} api={api} />
                    {loading && <CircularProgress className={classes.deleteProgress} />}
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
    updateData: PropTypes.func.isRequired,
    isAPIProduct: PropTypes.bool.isRequired,
};

export default injectIntl(withStyles(styles)(APIThumb));
