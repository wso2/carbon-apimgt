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
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormGroup from '@material-ui/core/FormGroup';
import Checkbox from '@material-ui/core/Checkbox';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import TextField from '@material-ui/core/TextField';
import { FormattedMessage } from 'react-intl';
import Switch from '@material-ui/core/Switch';
import FormControl from '@material-ui/core/FormControl';
import Select from '@material-ui/core/Select';
import ChipInput from 'material-ui-chip-input';
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
        fontSize: theme.typography.pxToRem(14),
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
        paddingBottom: 12,
    },
    textFieldRoles: {
        padding: 0,
        margin: '0 0 0 10px',
    },
    group: {
        flexDirection: 'row',
    },
    error: {
        lineHeight: '31px',
    },
    authFormControl: {
        marginTop: 0,
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
            tags: null,
            isDefaultVersion: null,
            transport: null,
            authorizationHeader: null,
            responseCaching: null,
            cacheTimeout: null,
        };
        this.handleAddChip = this.handleAddChip.bind(this);
        this.handleDeleteChip = this.handleDeleteChip.bind(this);
    }
    getAccessControlValue(accessControlRoles, apiAccessControlRoles) {
        if (accessControlRoles && accessControlRoles.length > 0) {
            return accessControlRoles.join();
        } else {
            return apiAccessControlRoles.join();
        }
    }
    getDefaultVersion(isDefaultVersion, apiIsDefaultVersion) {
        if (isDefaultVersion === true) {
            return 'yes';
        } else if (isDefaultVersion === false) {
            return 'no';
        } else if (isDefaultVersion === null && apiIsDefaultVersion) {
            return 'yes';
        } else {
            return 'no';
        }
    }
    getTransportState(type, transport, apiTransport) {
        if (transport) {
            return transport.includes(type);
        } else {
            return apiTransport.includes(type);
        }
    }
    handleChange = name => (event) => {
        let { value } = event.target;
        const { checked } = event.target;
        if (name === 'accessControlRoles' || name === 'visibleRoles') {
            value = value.split(',');
        } else if (name === 'isDefaultVersion') {
            value = value === 'yes';
        } else if (name === 'responseCaching') {
            value = checked ? 'Enabled' : 'Disabled';
        }
        this.setState({
            [name]: value,
        });
    };
    handleTransportChange = apiTransport => (event) => {
        const { value, checked } = event.target;
        this.setState((oldState) => {
            let { transport } = oldState;
            if (!transport) transport = apiTransport;
            if (checked && !transport.includes(value)) {
                transport.push(value);
            } else if (!checked && transport.includes(value)) {
                transport.splice(transport.indexOf(value), 1);
            }

            return { transport };
        });
    };
    handleAddChip(chip, apiTags) {
        this.setState((oldState) => {
            let { tags } = oldState;
            if (!tags) tags = apiTags;
            return { tags: [...tags, chip] };
        });
    }
    handleDeleteChip(chip, index, apiTags) {
        this.setState((oldState) => {
            let { tags } = oldState;
            if (!tags) tags = apiTags;
            tags.splice(index, 1);
            return { tags: [...tags] };
        });
    }
    handleSubmit(oldAPI, updateAPI) {
        const {
            description,
            accessControl,
            accessControlRoles,
            visibility,
            visibleRoles,
            tags,
            isDefaultVersion,
            transport,
            authorizationHeader,
            responseCaching,
            cacheTimeout,
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
        if (tags) {
            oldAPI.tags = tags;
        }
        if (isDefaultVersion !== null) {
            oldAPI.isDefaultVersion = isDefaultVersion;
        }
        if (transport) {
            oldAPI.transport = transport;
        }
        if (authorizationHeader) {
            oldAPI.authorizationHeader = authorizationHeader;
        }
        if (responseCaching) {
            oldAPI.responseCaching = responseCaching;
        }
        if (cacheTimeout) {
            oldAPI.cacheTimeout = cacheTimeout;
        }
        updateAPI(oldAPI);
    }
    render() {
        const { classes } = this.props;
        const {
            description,
            accessControl,
            accessControlRoles,
            visibility,
            visibleRoles,
            tags,
            isDefaultVersion,
            transport,
            authorizationHeader,
            responseCaching,
            cacheTimeout,
        } = this.state;
        let error = false;
        if (transport) {
            error = transport.length === 0;
        }

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
                                                <Tooltip
                                                    placement='top'
                                                    classes={{
                                                        tooltip: classes.htmlTooltip,
                                                    }}
                                                    disableHoverListener
                                                    title={
                                                        <React.Fragment>
                                                            Marks one API version in a group as the default so that it
                                                            can be invoked without specifying the version number in the
                                                            URL. For example, if you mark http://host:port/youtube/2.0
                                                            as the default API, requests made to
                                                            http://host:port/youtube/ are automatically routed to
                                                            version 2.0. If you mark an unpublished API as the default,
                                                            the previous default published API will still be used as the
                                                            default until the new default API is published.
                                                        </React.Fragment>
                                                    }
                                                >
                                                    <Button className={classes.helpButton}>
                                                        <HelpOutline className={classes.helpIcon} />
                                                    </Button>
                                                </Tooltip>
                                            </Typography>
                                            <Typography component='p' variant='body1'>
                                                <RadioGroup
                                                    name='isDefaultVersion'
                                                    className={classes.group}
                                                    value={this.getDefaultVersion(
                                                        isDefaultVersion,
                                                        api.isDefaultVersion,
                                                    )}
                                                    onChange={this.handleChange('isDefaultVersion')}
                                                >
                                                    <FormControlLabel value='yes' control={<Radio />} label='Yes' />
                                                    <FormControlLabel value='no' control={<Radio />} label='No' />
                                                </RadioGroup>
                                            </Typography>
                                            {/* Transports */}
                                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                                Transports
                                                <Tooltip
                                                    placement='top'
                                                    classes={{
                                                        tooltip: classes.htmlTooltip,
                                                    }}
                                                    disableHoverListener
                                                    title={
                                                        <React.Fragment>
                                                            HTTP is less secure than HTTPS and makes your API vulnerable
                                                            to security threats.
                                                        </React.Fragment>
                                                    }
                                                >
                                                    <Button className={classes.helpButton}>
                                                        <HelpOutline className={classes.helpIcon} />
                                                    </Button>
                                                </Tooltip>
                                            </Typography>

                                            <FormControl
                                                required
                                                error={error}
                                                component='fieldset'
                                                className={classes.formControl}
                                            >
                                                <FormGroup className={classes.group}>
                                                    <FormControlLabel
                                                        control={
                                                            <Checkbox
                                                                checked={this.getTransportState(
                                                                    'http',
                                                                    transport,
                                                                    api.transport,
                                                                )}
                                                                onChange={this.handleTransportChange(api.transport)}
                                                                value='http'
                                                            />
                                                        }
                                                        label='HTTP'
                                                    />
                                                    <FormControlLabel
                                                        control={
                                                            <Checkbox
                                                                checked={this.getTransportState(
                                                                    'https',
                                                                    transport,
                                                                    api.transport,
                                                                )}
                                                                onChange={this.handleTransportChange(api.transport)}
                                                                value='https'
                                                            />
                                                        }
                                                        label='HTTPS'
                                                    />
                                                    {error && (
                                                        <FormHelperText className={classes.error}>
                                                            Please select at least one transport.
                                                        </FormHelperText>
                                                    )}
                                                </FormGroup>
                                            </FormControl>
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
                                        <div className={classes.rightDataColum}>
                                            {/* Response Caching */}
                                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                                Response Caching
                                                <Tooltip
                                                    placement='top'
                                                    classes={{
                                                        tooltip: classes.htmlTooltip,
                                                    }}
                                                    disableHoverListener
                                                    title={
                                                        <React.Fragment>
                                                            This option determines whether to cache the response
                                                            messages of the API. Caching improves performance because
                                                            the backend server does not have to process the same data
                                                            multiple times. To offset the risk of stale data in the
                                                            cache, set an appropriate timeout period when prompted.
                                                        </React.Fragment>
                                                    }
                                                >
                                                    <Button className={classes.helpButton}>
                                                        <HelpOutline className={classes.helpIcon} />
                                                    </Button>
                                                </Tooltip>
                                            </Typography>
                                            <div className={classes.inlineForms}>
                                                <FormControlLabel
                                                    className={classes.formControlLeft}
                                                    control={
                                                        <Switch
                                                            checked={(() => {
                                                                if (responseCaching && responseCaching === 'Disabled') {
                                                                    return false;
                                                                } else if (
                                                                    responseCaching &&
                                                                    responseCaching === 'Enabled'
                                                                ) {
                                                                    return true;
                                                                } else if (
                                                                    !responseCaching &&
                                                                    api.responseCaching === 'Disabled'
                                                                ) {
                                                                    return false;
                                                                } else {
                                                                    return true;
                                                                }
                                                            })()}
                                                            onChange={this.handleChange('responseCaching')}
                                                            value={responseCaching || api.responseCaching}
                                                            color='primary'
                                                        />
                                                    }
                                                    label={responseCaching || api.responseCaching}
                                                />
                                                {((responseCaching && responseCaching === 'Enabled') ||
                                                    (!responseCaching && api.responseCaching === 'Enabled')) && (
                                                    <FormControl className={classes.formControlRight}>
                                                        <TextField
                                                            id='cacheTimeout'
                                                            className={classes.textFieldRoles}
                                                            value={cacheTimeout || api.cacheTimeout}
                                                            onChange={this.handleChange('cacheTimeout')}
                                                            margin='normal'
                                                            helperText='Cache Timeout (seconds)'
                                                        />
                                                    </FormControl>
                                                )}
                                            </div>
                                            {/* Authorization Header */}
                                            <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                                Authorization Header
                                                <Tooltip
                                                    placement='top'
                                                    classes={{
                                                        tooltip: classes.htmlTooltip,
                                                    }}
                                                    disableHoverListener
                                                    title={
                                                        <React.Fragment>
                                                            A custom authorization header can be defined as a
                                                            replacement to the default <strong>Authorization</strong>
                                                            header used to send a request. If a value is specified here,
                                                            it will be used as the header field to send the access token
                                                            in a request to consume the API
                                                        </React.Fragment>
                                                    }
                                                >
                                                    <Button className={classes.helpButton}>
                                                        <HelpOutline className={classes.helpIcon} />
                                                    </Button>
                                                </Tooltip>
                                            </Typography>
                                            <FormControl>
                                                <TextField
                                                    className={classes.authFormControl}
                                                    id='authorizationHeader'
                                                    value={(() => {
                                                        if (authorizationHeader === null) {
                                                            return api.authorizationHeader;
                                                        } else {
                                                            return authorizationHeader;
                                                        }
                                                    })()}
                                                    onChange={this.handleChange('authorizationHeader')}
                                                    margin='normal'
                                                />
                                            </FormControl>
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
                                                            className={classes.textFieldRoles}
                                                            value={this.getAccessControlValue(
                                                                accessControlRoles,
                                                                api.accessControlRoles,
                                                            )}
                                                            onChange={this.handleChange('accessControlRoles')}
                                                            margin='normal'
                                                            helperText='Comma seperated list of roles
                                                            (e.g:role1,role2,role3)'
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
                                                            className={classes.textFieldRoles}
                                                            value={this.getAccessControlValue(
                                                                visibleRoles,
                                                                api.visibleRoles,
                                                            )}
                                                            onChange={this.handleChange('visibleRoles')}
                                                            margin='normal'
                                                            helperText='Comma seperated list
                                                            of roles (e.g:role1,role2,role3)'
                                                        />
                                                    </FormControl>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                    <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                        Tags
                                    </Typography>
                                    <ChipInput
                                        value={tags || api.tags}
                                        onAdd={chip => this.handleAddChip(chip, api.tags)}
                                        onDelete={(chip, index) => this.handleDeleteChip(chip, index, api.tags)}
                                    />
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
