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
import { withRouter, Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Collapse from '@material-ui/core/Collapse';
import Icon from '@material-ui/core/Icon';
import IconButton from '@material-ui/core/IconButton';
import Button from '@material-ui/core/Button';
import { FormattedMessage, injectIntl } from 'react-intl';
import Loading from 'AppComponents/Base/Loading/Loading';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import Grid from '@material-ui/core/Grid';
import API from 'AppData/api';
import Application from 'AppData/Application';
import Alert from 'AppComponents/Shared/Alert';
import DeleteConfirmation from '../Listing/DeleteConfirmation';
import AuthManager from 'AppData/AuthManager';
import { ScopeValidation, resourceMethods, resourcePaths } from 'AppComponents/Shared/ScopeValidation';

/**
 * @param {*} theme theme details
 * @returns {Object}
 */
const styles = (theme) => {
    const mainBack = theme.custom.infoBar.background || '#ffffff';
    const infoBarHeight = theme.custom.infoBar.height || 70;
    const starColor = theme.custom.infoBar.starColor || theme.palette.getContrastText(mainBack);

    return {
        root: {
            height: infoBarHeight,
            background: mainBack,
            color: theme.palette.getContrastText(mainBack),
            borderBottom: 'solid 1px ' + theme.palette.grey.A200,
            display: 'flex',
            alignItems: 'center',
            paddingLeft: theme.spacing(2),
        },
        backIcon: {
            color: theme.palette.primary.main,
            fontSize: 56,
            cursor: 'pointer',
        },
        backText: {
            color: theme.palette.primary.main,
            cursor: 'pointer',
            fontFamily: theme.typography.fontFamily,
        },
        starRate: {
            fontSize: 40,
            color: starColor,
        },
        starRateMy: {
            fontSize: 70,
            color: theme.palette.primary.main,
        },
        rateLink: {
            cursor: 'pointer',
            lineHeight: '70px',
        },
        topBar: {
            display: 'flex',
            paddingBottom: theme.spacing(2),
        },
        infoContent: {
            color: theme.palette.getContrastText(mainBack),
            background: mainBack,
            padding: theme.spacing(3),
            '& td, & th': {
                color: theme.palette.getContrastText(mainBack),
            },
        },
        infoContentBottom: {
            background: theme.custom.infoBar.sliderBackground,
            color: theme.palette.getContrastText(theme.custom.infoBar.sliderBackground),
            borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        },
        bootstrapRoot: {
            padding: 0,
            'label + &': {
                marginTop: theme.spacing(3),
            },
        },
        bootstrapInput: {
            borderRadius: 4,
            backgroundColor: theme.palette.common.white,
            border: '1px solid #ced4da',
            padding: '5px 12px',
            width: 350,
            transition: theme.transitions.create(['border-color', 'box-shadow']),
            fontFamily: [
                '-apple-system',
                'BlinkMacSystemFont',
                '"Segoe UI"',
                'Roboto',
                '"Helvetica Neue"',
                'Arial',
                'sans-serif',
                '"Apple Color Emoji"',
                '"Segoe UI Emoji"',
                '"Segoe UI Symbol"',
            ].join(','),
            '&:focus': {
                borderColor: '#80bdff',
                boxShadow: '0 0 0 0.2rem rgba(0,123,255,.25)',
            },
        },
        epWrapper: {
            display: 'flex',
        },
        prodLabel: {
            lineHeight: '30px',
            marginRight: 10,
            width: 100,
        },
        contentWrapper: {
            maxWidth: theme.custom.contentAreaWidth - theme.custom.leftMenu.width,
            alignItems: 'center',
        },
        ratingBoxWrapper: {
            position: 'relative',
            display: 'flex',
            alignItems: 'center',
        },
        ratingBox: {
            backgroundColor: theme.custom.leftMenu.background,
            border: '1px solid rgb(71, 211, 244)',
            borderRadius: '5px',
            display: 'flex',
            position: 'absolute',
            top: 14,
            height: '40px',
            color: theme.palette.getContrastText(theme.custom.leftMenu.background),
            alignItems: 'center',
            left: '0',
            paddingLeft: '5px',
            paddingRight: '5px',
        },
        userRating: {
            display: 'flex',
            alignItems: 'flex-end',
        },
        verticalDividerStar: {
            borderLeft: 'solid 1px ' + theme.palette.grey.A200,
            height: 40,
            marginRight: theme.spacing(1),
            marginLeft: theme.spacing(1),
        },
        backLink: {
            alignItems: 'center',
            textDecoration: 'none',
            display: 'flex',
        },
        ratingSummery: {
            alignItems: 'center',
            flexDirection: 'column',
            display: 'flex',
        },
        infoBarMain: {
            width: '100%',
        },
        buttonView: {
            textAlign: 'left',
            justifyContent: 'left',
            display: 'flex',
            paddingLeft: theme.spacing(2),
            cursor: 'pointer',
        },
        buttonOverviewText: {
            display: 'inline-block',
            paddingTop: 3,
        },
        button: {
            display: 'inline-grid',
            cursor: 'pointer',
            '& .material-icons, & span': {
                color: theme.palette.getContrastText(theme.custom.infoBar.background),
            },
        },
        editButton: {
            display: 'inline-grid',
            cursor: 'pointer',
            '& .material-icons, & span': {
                color: theme.palette.getContrastText(theme.custom.infoBar.background),
            },
        },
        iconButton: {
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexDirection: 'column',
        },
        appNameXSmall: {
            whiteSpace: 'nowrap',
            overflowX: 'auto',
            overflowY: 'hidden',
            maxWidth: 200,
            lineHeight: 1.3,
        },
        appNameSmall: {
            whiteSpace: 'nowrap',
            overflowX: 'auto',
            overflowY: 'hidden',
            maxWidth: 310,
            lineHeight: 1.3,
        },
        appNameMid: {
            whiteSpace: 'nowrap',
            overflowX: 'auto',
            overflowY: 'hidden',
            maxWidth: 640,
            lineHeight: 1.3,
        },
        appNameBig: {
            whiteSpace: 'nowrap',
            overflowX: 'auto',
            overflowY: 'hidden',
            maxWidth: 980,
            lineHeight: 1.3,
        },
        linkTitle: {
            color: theme.palette.getContrastText(theme.custom.infoBar.background),
        },
    };
};
/**
 *
 *
 * @class InfoBar
 * @extends {React.Component}
 */
class InfoBar extends React.Component {
    /**
     * @param {Object} props props passed from above
     */
    constructor(props) {
        super(props);
        this.state = {
            notFound: false,
            showOverview: true,
            isDeleteOpen: false,
            applicationOwner: '',
        };
        this.toggleOverview = this.toggleOverview.bind(this);
        this.handleAppDelete = this.handleAppDelete.bind(this);
        this.handleDeleteConfimation = this.handleDeleteConfimation.bind(this);
        this.toggleDeleteConfirmation = this.toggleDeleteConfirmation.bind(this);
    }

    /**
     * Toggles the showOverview state
     * @param {boolean} todo toggle state
     * @memberof InfoBar
     */
    toggleOverview(todo) {
        if (typeof todo === 'boolean') {
            this.setState({ showOverview: todo });
        } else {
            this.setState(prevState => ({ showOverview: !prevState.showOverview }));
        }
    }

    /**
     * Handles delete confimation
     * @memberof InfoBar
     */
    handleDeleteConfimation() {
        const { isDeleteOpen } = this.state;
        this.setState({ isDeleteOpen: !isDeleteOpen });
    }

    /**
     * Handles application deletion
     * @memberof InfoBar
     */
    handleAppDelete() {
        const { applicationId, intl, application } = this.props;
        const promisedDelete = Application.deleteApp(applicationId);
        let message = intl.formatMessage({
            defaultMessage: 'Application {name} deleted successfully!',
            id: 'Applications.Details.InfoBar.application.deleted.successfully',
        }, {name: application.name});
        promisedDelete.then((ok) => {
            if (ok) {
                Alert.info(message);
                this.toggleDeleteConfirmation();
            }
            this.props.history.push('/applications');
        }).catch((error) => {
            console.log(error);
            message = intl.formatMessage({
                defaultMessage: 'Error while deleting application {name}',
                id: 'Applications.Details.InfoBar.application.deleting.error',
            }, {name: application.name});
            Alert.error(message);
        });
    }

    toggleDeleteConfirmation = () => {
        this.setState(({ isDeleteOpen }) => ({ isDeleteOpen: !isDeleteOpen }));
    }


    /**
     * @returns {div}
     * @memberof InfoBar
     */
    render() {
        const {
            classes, theme, applicationId, application
        } = this.props;
        const applicationOwner = this.props.application.owner;
        const {
            tierDescription, showOverview, notFound, isDeleteOpen,
        } = this.state;
        const {
            custom: {
                leftMenu: { position },
            },
        } = theme;

        if (notFound) {
            return (
              <ResourceNotFound
                message={
                  <FormattedMessage
                    id="Applications.Details.InfoBar.listing.resource.not.found"
                    defaultMessage="Resource Not Fount"
                  />
                }
              />
            );
        }

        if (!application) {
            return <Loading />;
        }
        const isUserOwner = AuthManager.getUser().name === applicationOwner;

        return (
            <div className={classes.infoBarMain}>
                <div className={classes.root}>
                    <Grid item xs={10}>
                        <div style={{ marginLeft: theme.spacing(1) }}>
                            <Link to={'/applications/' + applicationId + '/overview'} className={classes.linkTitle}>
                                <Typography variant='h4'>{application.name}</Typography>
                            </Link>
                        </div>
                        <div style={{ marginLeft: theme.spacing(1) }}>
                            <Typography variant='caption' gutterBottom align='left'>
                                {application.subscriptionCount}{' '}
                                <FormattedMessage
                                    id='Applications.Details.InfoBar.subscriptions'
                                    defaultMessage='Subscriptions'
                                />
                            </Typography>
                        </div>
                    </Grid>
                    {isUserOwner && (
                    <ScopeValidation
                        resourcePath={resourcePaths.SINGLE_APPLICATION}
                        resourceMethod={resourceMethods.PUT}
                    >
                        <VerticalDivider height={70} />
                        <Grid item xs={1} m={1} className={classes.editButton}>
                                <Link to={`/applications/${applicationId}/edit/fromView`} className={classes.editButton}>
                                    <Button
                                        style={{ padding: '4px' }}
                                        color='default'
                                        classes={{ label: classes.iconButton }}
                                        aria-label={(
                                            <FormattedMessage
                                                id='Applications.Details.InfoBar.edit'
                                                defaultMessage='Edit'
                                            />
                                        )}
                                    >
                                        <Icon>edit</Icon>
                                        <Typography variant='caption' style={{ marginTop: '2px' }} >
                                            <FormattedMessage
                                                id='Applications.Details.InfoBar.edit.text'
                                                defaultMessage='Edit'
                                            />
                                        </Typography>
                                    </Button>
                                </Link>
                        </Grid>
                        <VerticalDivider height={70} />
                        <Grid item xs={1} m={1} className={classes.button}>
                            <Button
                                onClick={this.handleDeleteConfimation}
                                disabled={AuthManager.getUser().name !== applicationOwner}
                                color='default'
                                classes={{ label: classes.iconButton }}
                                aria-label={(
                                    <FormattedMessage
                                        id='Applications.Details.InfoBar.delete'
                                        defaultMessage='Delete'
                                    />
                                )}
                            >
                                <Icon>delete</Icon>
                                <Typography variant='caption' style={{ marginTop: '2px' }} >
                                    <FormattedMessage
                                        id='Applications.Details.InfoBar.text'
                                        defaultMessage='Delete'
                                    />
                                </Typography>
                            </Button>
                            <DeleteConfirmation
                                handleAppDelete={this.handleAppDelete}
                                isDeleteOpen={isDeleteOpen}
                                toggleDeleteConfirmation={this.toggleDeleteConfirmation}
                            />
                        </Grid>
                     </ScopeValidation>
                     )}
                </div>
            </div>
        );
    }
}
InfoBar.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    applicationId: PropTypes.string.isRequired,
};

export default injectIntl(withRouter(withStyles(styles, { withTheme: true })(InfoBar)));
