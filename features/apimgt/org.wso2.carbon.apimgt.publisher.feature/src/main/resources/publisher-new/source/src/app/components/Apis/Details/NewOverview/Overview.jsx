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
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import green from '@material-ui/core/colors/green';
import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Divider from '@material-ui/core/Divider';
import Button from '@material-ui/core/Button';
import Chip from '@material-ui/core/Chip';
import FileIcon from '@material-ui/icons/Description';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import Avatar from '@material-ui/core/Avatar';
import ThumbnailView from 'AppComponents/Apis/Listing/components/ThumbnailView';
import Alert from 'AppComponents/Shared/Alert';
import Api from 'AppData/api';
import CheckItem from './CheckItem';
import ApiContext from '../components/ApiContext';
import Resources from './Resources';

const styles = theme => ({
    root: {
        ...theme.mixins.gutters(),
        paddingTop: theme.spacing.unit * 2,
        paddingBottom: theme.spacing.unit * 2,
    },
    buttonSuccess: {
        backgroundColor: green[500],
        '&:hover': {
            backgroundColor: green[700],
        },
    },
    checkItem: {
        textAlign: 'center',
    },
    divider: {
        marginTop: 20,
        marginBottom: 20,
    },
    chip: {
        margin: theme.spacing.unit / 2,
        padding: 0,
        height: 'auto',
        '& span': {
            padding: '0 5px',
        },
    },
    imageContainer: {
        display: 'flex',
    },
    imageWrapper: {
        marginRight: theme.spacing.unit * 3,
    },
    subtitle: {
        marginTop: theme.spacing.unit,
    },
    specialGap: {
        marginTop: theme.spacing.unit * 3,
    },
    resourceTitle: {
        marginBottom: theme.spacing.unit * 3,
    },
    ListRoot: {
        padding: 0,
        margin: 0,
    },
});

class Overview extends React.Component {
    state = {
        documentsList: null,
    };
    componentDidMount() {
        const API = new Api();
        const docs = API.getDocuments(this.props.api.id);
        docs.then((response) => {
            this.setState({ documentsList: response.obj.list });
        }).catch((errorResponse) => {
            const errorData = JSON.parse(errorResponse.message);
            const messageTxt =
                'Error[' + errorData.code + ']: ' + errorData.description + ' | ' + errorData.message + '.';
            console.error(messageTxt);
            Alert.error('Error in fetching documents list of the API');
        });
    }
    showEndpoint (api, type) {
        if(api.endpoint.length > 0){
            for(var i=0; i< api.endpoint.length; i++){
                if( type === "prod" && api.endpoint[i].type === "http"){
                    return api.endpoint[i].inline.endpointConfig.list[0].url;
                } else if( type === "sand" && api.endpoint[i].type === "sandbox_endpoints"){
                    return api.endpoint[i].inline.endpointConfig.list[0].url;
                }
            }
            
        } else {
            return null;
        }
    }
    render() {
        const { documentsList } = this.state;
        const { classes } = this.props;
        console.info(documentsList);
        return (
            <ApiContext.Consumer>
                {({ api }) => (
                    <Grid container spacing={24}>
                        <Grid item xs={12}>
                            <Grid container>
                                <CheckItem itemSuccess itemLabel='Endpoints' />
                                <CheckItem itemSuccess={false} itemLabel='Policies' />
                                <CheckItem itemSuccess itemLabel='Resources' />
                                <CheckItem itemSuccess={false} itemLabel='Scopes' />
                                <CheckItem itemSuccess={false} itemLabel='Documents' />
                                <CheckItem itemSuccess={false} itemLabel='Business Information' />
                                <CheckItem itemSuccess={false} itemLabel='Description' />
                            </Grid>
                        </Grid>
                        {console.info(api)}
                        <Grid item xs={12}>
                            <Grid container spacing={24}>
                                <Grid item xs={12} md={6} lg={6}>
                                    <Paper className={classes.root} elevation={1}>
                                        <Typography variant='h5' component='h3'>
                                            Basic Configuration
                                        </Typography>
                                        <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                            Description
                                        </Typography>
                                        <Typography component='p' variant='body1'>
                                            {api.description && <React.Fragment>{api.description}</React.Fragment>}
                                            {!api.description && (
                                                <React.Fragment>&lt;Description Not Configured&gt;</React.Fragment>
                                            )}
                                        </Typography>
                                        <div className={classes.imageContainer}>
                                            <div className={classes.imageWrapper}>
                                                {/* Thumbnail */}
                                                <Typography
                                                    component='p'
                                                    variant='subtitle2'
                                                    className={classes.subtitle}
                                                >
                                                    API Thumbnail
                                                </Typography>
                                                <ThumbnailView api={api} width={200} height={200} />
                                                {/* Provider */}
                                                <Typography
                                                    component='p'
                                                    variant='subtitle2'
                                                    className={classes.subtitle}
                                                >
                                                    Provider
                                                </Typography>
                                                <Typography component='p' variant='body1'>
                                                    {api.provider && <React.Fragment>{api.provider}</React.Fragment>}
                                                </Typography>
                                                {/* Type */}
                                                <Typography
                                                    component='p'
                                                    variant='subtitle2'
                                                    className={classes.subtitle}
                                                >
                                                    Type
                                                </Typography>
                                                <Typography component='p' variant='body1'>
                                                    {api.type && <React.Fragment>{api.type}</React.Fragment>}
                                                    {!api.type && <React.Fragment>?</React.Fragment>}
                                                </Typography>
                                            </div>
                                            <div>
                                                <Typography
                                                    component='p'
                                                    variant='subtitle2'
                                                    className={classes.subtitle}
                                                >
                                                    Context
                                                </Typography>
                                                <Typography component='p' variant='body1'>
                                                    {api.context && <React.Fragment>{api.context}</React.Fragment>}
                                                </Typography>
                                                {/* Version */}
                                                <Typography
                                                    component='p'
                                                    variant='subtitle2'
                                                    className={classes.subtitle}
                                                >
                                                    Version
                                                </Typography>
                                                <Typography component='p' variant='body1'>
                                                    {api.version && <React.Fragment>{api.version}</React.Fragment>}
                                                </Typography>
                                                {/* Default Version */}
                                                <Typography
                                                    component='p'
                                                    variant='subtitle2'
                                                    className={classes.subtitle}
                                                >
                                                    Default Version
                                                </Typography>
                                                <Typography component='p' variant='body1'>
                                                    {api.isDefaultVersion && <React.Fragment>Yes</React.Fragment>}
                                                    {!api.isDefaultVersion && <React.Fragment>No</React.Fragment>}
                                                </Typography>
                                                {/* Created Time */}
                                                <Typography
                                                    component='p'
                                                    variant='subtitle2'
                                                    className={classes.subtitle}
                                                >
                                                    Created Time
                                                </Typography>
                                                <Typography component='p' variant='body1'>
                                                    {api.createdTime && (
                                                        <React.Fragment>{api.createdTime}</React.Fragment>
                                                    )}
                                                    {!api.createdTime && <React.Fragment>?</React.Fragment>}
                                                </Typography>
                                                {/* Last Updated Time */}
                                                <Typography
                                                    component='p'
                                                    variant='subtitle2'
                                                    className={classes.subtitle}
                                                >
                                                    Last Updated Time
                                                </Typography>
                                                <Typography component='p' variant='body1'>
                                                    {api.lastUpdatedTime && (
                                                        <React.Fragment>{api.lastUpdatedTime}</React.Fragment>
                                                    )}
                                                    {!api.lastUpdatedTime && <React.Fragment>?</React.Fragment>}
                                                </Typography>
                                                {/* Visibility */}
                                                <Typography
                                                    component='p'
                                                    variant='subtitle2'
                                                    className={classes.subtitle}
                                                >
                                                    Visibility
                                                </Typography>
                                                <Typography component='p' variant='body1'>
                                                    {api.visibility && (
                                                        <React.Fragment>{api.visibility}</React.Fragment>
                                                    )}
                                                </Typography>
                                                {/* workflowStatus */}
                                                <Typography
                                                    component='p'
                                                    variant='subtitle2'
                                                    className={classes.subtitle}
                                                >
                                                    Workflow Status
                                                </Typography>
                                                <Typography component='p' variant='body1'>
                                                    {api.workflowStatus && (
                                                        <React.Fragment>{api.workflowStatus}</React.Fragment>
                                                    )}
                                                    {!api.workflowStatus && <React.Fragment>?</React.Fragment>}
                                                </Typography>
                                            </div>
                                        </div>

                                        <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                            Tags
                                        </Typography>
                                        <Typography variant='body1'>
                                            {api.tags &&
                                                api.tags.map(tag => (
                                                    <Chip key={tag} label={tag} className={classes.chip} />
                                                ))}
                                        </Typography>
                                        <Divider className={classes.divider} />
                                        <Link to={'/apis/' + api.id + '/configuration'}>
                                            <Button variant='contained' color='primary'>
                                                Edit
                                            </Button>
                                        </Link>
                                    </Paper>
                                    <Paper className={classNames({ [classes.root]: true, [classes.specialGap]: true })}>
                                        <Typography variant='h5' component='h3' className={classes.resourceTitle}>
                                            Resources
                                        </Typography>
                                        <Resources api={api} />
                                        <Divider className={classes.divider} />
                                        <Link to={'/apis/' + api.id + '/resources'}>
                                            <Button variant='contained' color='primary'>
                                                Edit
                                            </Button>
                                        </Link>
                                    </Paper>
                                </Grid>
                                <Grid item xs={12} md={6} lg={6}>
                                    <Paper className={classes.root}>
                                        <Typography variant='h5' component='h3'>
                                            Endpoints
                                        </Typography>
                                        {/* Production Endpoint (TODO) fix the endpoint
                                        info when it's available with the api object */}
                                        <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                            Production Endpoint
                                        </Typography>
                                        <Typography component='p' variant='body1'>
                                            {this.showEndpoint(api,'prod') && <React.Fragment>{this.showEndpoint(api,'prod')}</React.Fragment>}
                                            {!this.showEndpoint(api,'prod') && (
                                                <React.Fragment>&lt;Not Configured&gt;</React.Fragment>
                                            )}
                                        </Typography>
                                        {/* Sandbox Endpoint (TODO) fix the endpoint info when
                                        it's available with the api object */}
                                        <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                            Sandbox Endpoint
                                        </Typography>
                                        <Typography component='p' variant='body1'>
                                            {this.showEndpoint(api,'sand')&& <React.Fragment>{this.showEndpoint(api,'sand')}</React.Fragment>}
                                            {!this.showEndpoint(api,'sand')&& (
                                                <React.Fragment>&lt;Not Configured&gt;</React.Fragment>
                                            )}
                                        </Typography>
                                        {/* Sandbox Endpoint (TODO) fix the endpoint info when
                                        it's available with the api object */}
                                        <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                            Endpoint Security
                                        </Typography>
                                        <Typography component='p' variant='body1'>
                                            {api.endpointSecurity && <React.Fragment>{api.endpoint}</React.Fragment>}
                                            {!api.endpointSecurity && (
                                                <React.Fragment>&lt;Not configured&gt;</React.Fragment>
                                            )}
                                        </Typography>
                                        <Divider className={classes.divider} />
                                        <Link to={'/apis/' + api.id + '/endpoints'}>
                                            <Button variant='contained' color='primary'>
                                                Edit
                                            </Button>
                                        </Link>
                                    </Paper>

                                    <Paper className={classNames({ [classes.root]: true, [classes.specialGap]: true })}>
                                        <Typography variant='h5' component='h3'>
                                            Business Information
                                        </Typography>
                                        {/* Business Owner */}
                                        <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                            Business Owner
                                        </Typography>
                                        <Typography component='p' variant='body1'>
                                            {api.businessInformation.businessOwner && (
                                                <React.Fragment>{api.businessInformation.businessOwner}</React.Fragment>
                                            )}
                                            {!api.businessInformation.businessOwner && (
                                                <React.Fragment>&lt;Not Configured&gt;</React.Fragment>
                                            )}
                                        </Typography>
                                        {/* Business Email */}
                                        <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                            Business Owner Email
                                        </Typography>
                                        <Typography component='p' variant='body1'>
                                            {api.businessInformation.businessOwnerEmail && (
                                                <React.Fragment>
                                                    {api.businessInformation.businessOwnerEmail}
                                                </React.Fragment>
                                            )}
                                            {!api.businessInformation.businessOwnerEmail && (
                                                <React.Fragment>&lt;Not Configured&gt;</React.Fragment>
                                            )}
                                        </Typography>
                                        {/* Technical Owner */}
                                        <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                            Technical Owner
                                        </Typography>
                                        <Typography component='p' variant='body1'>
                                            {api.businessInformation.technicalOwner && (
                                                <React.Fragment>
                                                    {api.businessInformation.technicalOwner}
                                                </React.Fragment>
                                            )}
                                            {!api.businessInformation.technicalOwner && (
                                                <React.Fragment>&lt;Not Configured&gt;</React.Fragment>
                                            )}
                                        </Typography>
                                        {/* Technical Owner */}
                                        <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                            Technical Owner Email
                                        </Typography>
                                        <Typography component='p' variant='body1'>
                                            {api.businessInformation.technicalOwnerEmail && (
                                                <React.Fragment>
                                                    {api.businessInformation.technicalOwnerEmail}
                                                </React.Fragment>
                                            )}
                                            {!api.businessInformation.technicalOwnerEmail && (
                                                <React.Fragment>&lt;Not Configured&gt;</React.Fragment>
                                            )}
                                        </Typography>
                                        <Divider className={classes.divider} />
                                        <Link to={'/apis/' + api.id + '/configuration'}>
                                            <Button variant='contained' color='primary'>
                                                Edit
                                            </Button>
                                        </Link>
                                    </Paper>
                                    <Paper className={classNames({ [classes.root]: true, [classes.specialGap]: true })}>
                                        <Typography variant='h5' component='h3'>
                                            Scopes
                                        </Typography>
                                        {/* Scopes */}
                                        {api.scopes.length !== 0 && <React.Fragment>{api.scopes}</React.Fragment>}
                                        {api.scopes.length === 0 && (
                                            <Typography component='p' variant='body1' className={classes.subtitle}>
                                                &lt;Not Configured&gt;
                                            </Typography>
                                        )}
                                        <Divider className={classes.divider} />
                                        <Link to={'/apis/' + api.id + '/scopes'}>
                                            <Button variant='contained' color='primary'>
                                                Edit
                                            </Button>
                                        </Link>
                                    </Paper>
                                    <Paper className={classNames({ [classes.root]: true, [classes.specialGap]: true })}>
                                        <Typography variant='h5' component='h3'>
                                            Documents
                                        </Typography>
                                        {/* Scopes */}
                                        {documentsList && documentsList.length !== 0 && (
                                            <List className={classes.ListRoot}>
                                                {documentsList.map(item => (
                                                    <ListItem key={item.id}>
                                                        <Avatar>
                                                            <FileIcon />
                                                        </Avatar>
                                                        <ListItemText primary={item.name} secondary={item.summary} />
                                                    </ListItem>
                                                ))}
                                            </List>
                                        )}
                                        {!documentsList && (
                                            <Typography component='p' variant='body1' className={classes.subtitle}>
                                                &lt;Not Configured&gt;
                                            </Typography>
                                        )}
                                        <Divider className={classes.divider} />
                                        <Link to={'/apis/' + api.id + '/documents'}>
                                            <Button variant='contained' color='primary'>
                                                Edit
                                            </Button>
                                        </Link>
                                    </Paper>
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                )}
            </ApiContext.Consumer>
        );
    }
}

Overview.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
};

export default withStyles(styles)(Overview);
