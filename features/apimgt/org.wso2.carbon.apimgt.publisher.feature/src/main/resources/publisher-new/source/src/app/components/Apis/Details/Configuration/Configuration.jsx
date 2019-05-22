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
/* eslint no-param-reassign: ["error", { "props": false }] */
import React from 'react';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Chip from '@material-ui/core/Chip';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import TextField from '@material-ui/core/TextField';
import { FormattedMessage } from 'react-intl';
import InputLabel from '@material-ui/core/InputLabel';
import FormControl from '@material-ui/core/FormControl';
import Select from '@material-ui/core/Select';
import FormHelperText from '@material-ui/core/FormHelperText';
import ThumbnailView from 'AppComponents/Apis/Listing/components/ThumbnailView';
import ApiContext from '../components/ApiContext';

const styles = theme => ({
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        paddingBottom: 20,
    },
    root: {
        ...theme.mixins.gutters(),
        paddingTop: theme.spacing.unit * 2,
        paddingBottom: theme.spacing.unit * 2,
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
        width: 200,
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
    title: {
        flex: 1,
    },
    helpButton: {
        padding: 0,
        minWidth: 20,
    },
    helpIcon: {
        fontSize: 16,
    },
    htmlTooltip: {
        backgroundColor: '#f5f5f9',
        color: 'rgba(0, 0, 0, 0.87)',
        maxWidth: 220,
        fontSize: theme.typography.pxToRem(12),
        border: '1px solid #dadde9',
        '& b': {
            fontWeight: theme.typography.fontWeightMedium,
        },
    },
    buttonWrapper: {
        paddingTop: 20,
    },
    descriptionTextField: {
        width: '100%',
        marginBottom: 20,
    },
    rightDataColum: {
        flex: 1,
    },
    formControlLeft: {
        width: 100,
    },
    formControlRight: {
        flex: 1,
    },
    inlineForms: {
        width: '100%',
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'flex-end',
        paddingBottom: 30,
    },
    textFieldRoles: {
        padding: 0,
        margin: '0 0 0 10px',
    },
});

class Configuration extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            description: null,
            accessControl: null,
            accessControlRoles: null,
            visibility: null,
            visibleRoles: null,
        };
    }
    getAccessControlValue(accessControlRoles, apiAccessControlRoles) {
        if (accessControlRoles && accessControlRoles.length > 0) {
            return accessControlRoles.join();
        } else {
            return apiAccessControlRoles.join();
        }
    }
    handleChange = name => (event) => {
        let { value } = event.target;
        if (name === 'accessControlRoles' || name === 'visibleRoles') {
            value = value.split(',');
        }
        this.setState({
            [name]: value,
        });
    };
    handleSubmit(oldAPI, updateAPI) {
        const {
            description, accessControl, accessControlRoles, visibility, visibleRoles,
        } = this.state;
        if (description) {
            oldAPI.description = description;
        }
        if (accessControl) {
            oldAPI.accessControl = accessControl;
        }
        if (accessControlRoles) {
            oldAPI.accessControlRoles = accessControlRoles;
        }
        if (visibility) {
            oldAPI.visibility = visibility;
        }
        if (visibleRoles) {
            oldAPI.visibleRoles = visibleRoles;
        }
        updateAPI(oldAPI);
    }
    render() {
        const { classes } = this.props;
        const {
            description, accessControl, accessControlRoles, visibility, visibleRoles,
        } = this.state;
        return (
            <div className={classes.root}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        Configuration
                    </Typography>
                </div>
                <ApiContext.Consumer>
                    {({ api, updateAPI }) => (
                        <Grid container spacing={24}>
                            <Grid item xs={12}>
                                <Paper className={classes.root} elevation={1}>
                                    <Typography component='p' variant='body1'>
                                        <TextField
                                            id='outlined-multiline-flexible'
                                            label='Description'
                                            multiline
                                            rowsMax='4'
                                            value={description || api.description}
                                            onChange={this.handleChange('description')}
                                            className={classes.descriptionTextField}
                                            margin='normal'
                                            helperText='Provide a brief description about the API'
                                            variant='outlined'
                                        />
                                    </Typography>
                                    <div className={classes.imageContainer}>
                                        <div className={classes.imageWrapper}>
                                            {/* Thumbnail */}
                                            <ThumbnailView api={api} width={200} height={200} />
                                        </div>
                                        <div className={classes.rightDataColum}>
                                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                                Context
                                            </Typography>
                                            <Typography component='p' variant='body1'>
                                                {api.context && <React.Fragment>{api.context}</React.Fragment>}
                                            </Typography>
                                            {/* Version */}
                                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                                Version
                                            </Typography>
                                            <Typography component='p' variant='body1'>
                                                {api.version && <React.Fragment>{api.version}</React.Fragment>}
                                            </Typography>
                                            {/* Default Version */}
                                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                                Default Version
                                            </Typography>
                                            <Typography component='p' variant='body1'>
                                                {api.isDefaultVersion && <React.Fragment>Yes</React.Fragment>}
                                                {!api.isDefaultVersion && <React.Fragment>No</React.Fragment>}
                                            </Typography>
                                            {/* Created Time */}
                                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                                Created Time
                                            </Typography>
                                            <Typography component='p' variant='body1'>
                                                {api.createdTime && <React.Fragment>{api.createdTime}</React.Fragment>}
                                                {!api.createdTime && <React.Fragment>?</React.Fragment>}
                                            </Typography>
                                            {/* Last Updated Time */}
                                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                                Last Updated Time
                                            </Typography>
                                            <Typography component='p' variant='body1'>
                                                {api.lastUpdatedTime && (
                                                    <React.Fragment>{api.lastUpdatedTime}</React.Fragment>
                                                )}
                                                {!api.lastUpdatedTime && <React.Fragment>?</React.Fragment>}
                                            </Typography>
                                        </div>
                                    </div>
                                    <div className={classes.imageContainer}>
                                        <div className={classes.imageWrapper}>
                                            {/* Provider */}
                                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                                Provider
                                            </Typography>
                                            <Typography component='p' variant='body1'>
                                                {api.provider && <React.Fragment>{api.provider}</React.Fragment>}
                                            </Typography>
                                            {/* Type */}
                                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                                Type
                                            </Typography>
                                            <Typography component='p' variant='body1'>
                                                {api.type && <React.Fragment>{api.type}</React.Fragment>}
                                                {!api.type && <React.Fragment>?</React.Fragment>}
                                            </Typography>
                                            {/* Response Caching */}
                                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                                Response Caching
                                            </Typography>
                                            <Typography component='p' variant='body1'>
                                                {api.responseCaching && (
                                                    <React.Fragment>{api.responseCaching}</React.Fragment>
                                                )}
                                                {!api.responseCaching && <React.Fragment>?</React.Fragment>}
                                            </Typography>
                                            {/* workflowStatus */}
                                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                                Workflow Status
                                            </Typography>
                                            <Typography component='p' variant='body1'>
                                                {api.workflowStatus && (
                                                    <React.Fragment>{api.workflowStatus}</React.Fragment>
                                                )}
                                                {!api.workflowStatus && <React.Fragment>?</React.Fragment>}
                                            </Typography>
                                        </div>
                                        <div className={classes.rightDataColum}>
                                            {/* Transports */}
                                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                                Transports
                                            </Typography>
                                            <Typography component='p' variant='body1'>
                                                {api.transport && api.transport.length !== 0 && (
                                                    <React.Fragment>
                                                        {api.transport.map((item, index) => (
                                                            <span>
                                                                {item}
                                                                {api.transport.length !== index + 1 && ', '}
                                                            </span>
                                                        ))}
                                                    </React.Fragment>
                                                )}
                                                {!api.transport && <React.Fragment>?</React.Fragment>}
                                            </Typography>
                                            {/* Authorization Header */}
                                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                                Authorization Header
                                            </Typography>
                                            <Typography component='p' variant='body1'>
                                                {api.authorizationHeader && (
                                                    <React.Fragment>{api.authorizationHeader}</React.Fragment>
                                                )}
                                                {!api.authorizationHeader && <React.Fragment>?</React.Fragment>}
                                            </Typography>
                                            {/* Access Control */}
                                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                                Access Control
                                                <Tooltip
                                                    placement='top'
                                                    classes={{
                                                        tooltip: classes.htmlTooltip,
                                                    }}
                                                    disableHoverListener
                                                    title={
                                                        <React.Fragment>
                                                            <strong>All :</strong> The API is viewable, modifiable by
                                                            all the publishers and creators.
                                                            <br />
                                                            <strong>Restricted by roles :</strong> The API can be
                                                            viewable and modifiable by only specific publishers and
                                                            creators with the roles that you specify
                                                        </React.Fragment>
                                                    }
                                                >
                                                    <Button className={classes.helpButton}>
                                                        <HelpOutline className={classes.helpIcon} />
                                                    </Button>
                                                </Tooltip>
                                            </Typography>
                                            <div className={classes.inlineForms}>
                                                <FormControl className={classes.formControlLeft}>
                                                    <InputLabel htmlFor='age-native-simple' />
                                                    <Select
                                                        native
                                                        value={accessControl || api.accessControl}
                                                        onChange={this.handleChange('accessControl')}
                                                        inputProps={{
                                                            name: 'accessControl',
                                                            id: 'accessControl',
                                                        }}
                                                    >
                                                        <option value='NONE'>All</option>
                                                        <option value='RESTRICTED'>Restricted by roles</option>
                                                    </Select>
                                                    <FormHelperText>Access Control</FormHelperText>
                                                </FormControl>
                                                {((!accessControl && api.accessControl === 'RESTRICTED') ||
                                                    (accessControl && accessControl === 'RESTRICTED')) && (
                                                    <FormControl className={classes.formControlRight}>
                                                        <TextField
                                                            id='standard-name'
                                                            label='Roles'
                                                            className={classes.textFieldRoles}
                                                            value={this.getAccessControlValue(
                                                                accessControlRoles,
                                                                api.accessControlRoles,
                                                            )}
                                                            onChange={this.handleChange('accessControlRoles')}
                                                            margin='normal'
                                                            helperText='Comma seperated list (e.g:role1,role2,role3)'
                                                        />
                                                    </FormControl>
                                                )}
                                            </div>
                                            {/* Visibility */}
                                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                                Visibility on Store
                                                <Tooltip
                                                    placement='top'
                                                    classes={{
                                                        tooltip: classes.htmlTooltip,
                                                    }}
                                                    disableHoverListener
                                                    title={
                                                        <React.Fragment>
                                                            <strong>Public :</strong> The API is accessible to everyone
                                                            and can be advertised in multiple stores - a central store
                                                            and/or non-WSO2 stores.
                                                            <br />
                                                            <strong>Restricted by roles :</strong> The API is visible
                                                            only to specific user roles in the tenant store that you
                                                            specify.
                                                        </React.Fragment>
                                                    }
                                                >
                                                    <Button className={classes.helpButton}>
                                                        <HelpOutline className={classes.helpIcon} />
                                                    </Button>
                                                </Tooltip>
                                            </Typography>
                                            <div className={classes.inlineForms}>
                                                <FormControl className={classes.formControlLeft}>
                                                    <InputLabel htmlFor='age-native-simple' />
                                                    <Select
                                                        native
                                                        value={visibility || api.visibility}
                                                        onChange={this.handleChange('visibility')}
                                                        inputProps={{
                                                            name: 'visibility',
                                                            id: 'visibility',
                                                        }}
                                                    >
                                                        <option value='PUBLIC'>Public</option>
                                                        <option value='RESTRICTED'>Restricted by roles</option>
                                                    </Select>
                                                    <FormHelperText>Visibility</FormHelperText>
                                                </FormControl>
                                                {((!visibility && api.visibility === 'RESTRICTED') ||
                                                    (visibility && visibility === 'RESTRICTED')) && (
                                                    <FormControl className={classes.formControlRight}>
                                                        <TextField
                                                            id='standard-name'
                                                            label='Roles'
                                                            className={classes.textFieldRoles}
                                                            value={this.getAccessControlValue(
                                                                visibleRoles,
                                                                api.visibleRoles,
                                                            )}
                                                            onChange={this.handleChange('visibleRoles')}
                                                            margin='normal'
                                                            helperText='Comma seperated list (e.g:role1,role2,role3)'
                                                        />
                                                    </FormControl>
                                                )}
                                            </div>
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
                                </Paper>
                                <div className={classes.buttonWrapper}>
                                    <Grid
                                        container
                                        direction='row'
                                        alignItems='flex-start'
                                        spacing={16}
                                        className={classes.buttonSection}
                                    >
                                        <Grid item>
                                            <div>
                                                <Button
                                                    variant='contained'
                                                    color='primary'
                                                    onClick={() => this.handleSubmit(api, updateAPI)}
                                                >
                                                    <FormattedMessage id='save' defaultMessage='Save' />
                                                </Button>
                                            </div>
                                        </Grid>
                                        <Grid item>
                                            <Link to={'/apis/' + api.id + '/overview'}>
                                                <Button>
                                                    <FormattedMessage id='cancel' defaultMessage='Cancel' />
                                                </Button>
                                            </Link>
                                        </Grid>
                                    </Grid>
                                </div>
                            </Grid>
                        </Grid>
                    )}
                </ApiContext.Consumer>
            </div>
        );
    }
}

Configuration.propTypes = {
    state: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
};

export default withStyles(styles)(Configuration);
