import React from 'react';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Chip from '@material-ui/core/Chip';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import ThumbnailView from 'AppComponents/Apis/Listing/components/ThumbnailView';
import ApiContext from '../components/ApiContext';

function Configuration(props) {
    const { parentClasses } = props;
    return (
        <ApiContext.Consumer>
            {({ api }) => (
                <Paper className={parentClasses.root} elevation={1}>
                    <div className={parentClasses.titleWrapper}>
                        <Typography variant='h5' component='h3' className={parentClasses.title}>
                            Configuration
                        </Typography>
                        <Link to={'/apis/' + api.id + '/configuration'}>
                            <Button variant='contained' color='default'>
                                Edit
                            </Button>
                        </Link>
                    </div>

                    <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                        Description
                    </Typography>
                    <Typography component='p' variant='body1'>
                        {api.description && <React.Fragment>{api.description}</React.Fragment>}
                        {!api.description && <React.Fragment>&lt;Description Not Configured&gt;</React.Fragment>}
                    </Typography>
                    <div className={parentClasses.imageContainer}>
                        <div className={parentClasses.imageWrapper}>
                            {/* Thumbnail */}
                            <ThumbnailView api={api} width={200} height={200} />
                            {/* Provider */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                Provider
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.provider && <React.Fragment>{api.provider}</React.Fragment>}
                            </Typography>
                            {/* Type */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                Type
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.type && <React.Fragment>{api.type}</React.Fragment>}
                                {!api.type && <React.Fragment>?</React.Fragment>}
                            </Typography>
                            {/* Response Caching */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                Response Caching
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.responseCaching && <React.Fragment>{api.responseCaching}</React.Fragment>}
                                {!api.responseCaching && <React.Fragment>?</React.Fragment>}
                            </Typography>
                        </div>
                        <div>
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                Context
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.context && <React.Fragment>{api.context}</React.Fragment>}
                            </Typography>
                            {/* Version */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                Version
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.version && <React.Fragment>{api.version}</React.Fragment>}
                            </Typography>
                            {/* Default Version */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                Default Version
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.isDefaultVersion && <React.Fragment>Yes</React.Fragment>}
                                {!api.isDefaultVersion && <React.Fragment>No</React.Fragment>}
                            </Typography>
                            {/* Created Time */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                Created Time
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.createdTime && <React.Fragment>{api.createdTime}</React.Fragment>}
                                {!api.createdTime && <React.Fragment>?</React.Fragment>}
                            </Typography>
                            {/* Last Updated Time */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                Last Updated Time
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.lastUpdatedTime && <React.Fragment>{api.lastUpdatedTime}</React.Fragment>}
                                {!api.lastUpdatedTime && <React.Fragment>?</React.Fragment>}
                            </Typography>
                            {/* Access Control */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                Access Control
                                <Tooltip
                                    placement='top'
                                    classes={{
                                        tooltip: parentClasses.htmlTooltip,
                                    }}
                                    disableHoverListener
                                    title={
                                        <React.Fragment>
                                            <strong>All :</strong> The API is viewable, modifiable by all the publishers
                                            and creators.
                                            <br />
                                            <strong>Restricted by roles :</strong> The API can be viewable and
                                            modifiable by only specific publishers and creators with the roles that you
                                            specify
                                        </React.Fragment>
                                    }
                                >
                                    <Button className={parentClasses.helpButton}>
                                        <HelpOutline className={parentClasses.helpIcon} />
                                    </Button>
                                </Tooltip>
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.accessControl && <React.Fragment>{api.accessControl}</React.Fragment>}
                                {api.accessControl !== 'PUBLIC' && ' ( Visible to '}
                                {api.accessControl !== 'PUBLIC' && api.accessControlRoles.join()}
                                {api.accessControl !== 'PUBLIC' && ' ) '}
                            </Typography>
                            {/* Visibility */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                Visibility on Store
                                <Tooltip
                                    placement='top'
                                    classes={{
                                        tooltip: parentClasses.htmlTooltip,
                                    }}
                                    disableHoverListener
                                    title={
                                        <React.Fragment>
                                            <strong>Public :</strong> The API is accessible to everyone and can be
                                            advertised in multiple stores - a central store and/or non-WSO2 stores.
                                            <br />
                                            <strong>Restricted by roles :</strong> The API is visible only to specific
                                            user roles in the tenant store that you specify.
                                        </React.Fragment>
                                    }
                                >
                                    <Button className={parentClasses.helpButton}>
                                        <HelpOutline className={parentClasses.helpIcon} />
                                    </Button>
                                </Tooltip>
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.visibility && <React.Fragment>{api.visibility}</React.Fragment>}
                                {api.visibleRoles !== 'PUBLIC' && ' ( Visible to '}
                                {api.visibleRoles !== 'PUBLIC' && api.visibleRoles.join()}
                                {api.visibleRoles !== 'PUBLIC' && ' ) '}
                            </Typography>

                            {/* workflowStatus */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                Workflow Status
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.workflowStatus && <React.Fragment>{api.workflowStatus}</React.Fragment>}
                                {!api.workflowStatus && <React.Fragment>?</React.Fragment>}
                            </Typography>
                            {/* Transports */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
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
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                Authorization Header
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.authorizationHeader && <React.Fragment>{api.authorizationHeader}</React.Fragment>}
                                {!api.authorizationHeader && <React.Fragment>?</React.Fragment>}
                            </Typography>
                        </div>
                    </div>

                    <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                        Tags
                    </Typography>
                    <Typography variant='body1'>
                        {api.tags && api.tags.map(tag => <Chip key={tag} label={tag} className={parentClasses.chip} />)}
                    </Typography>
                </Paper>
            )}
        </ApiContext.Consumer>
    );
}

Configuration.propTypes = {
    parentClasses: PropTypes.object.isRequired,
};

export default Configuration;
