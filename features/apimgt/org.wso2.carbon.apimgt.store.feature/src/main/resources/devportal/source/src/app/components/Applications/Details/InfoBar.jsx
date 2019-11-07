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
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';
import Collapse from '@material-ui/core/Collapse';
import Icon from '@material-ui/core/Icon';
import Hidden from '@material-ui/core/Hidden';
import { FormattedMessage } from 'react-intl';
import Loading from 'AppComponents/Base/Loading/Loading';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import API from 'AppData/api';

/**
 * @param {*} theme theme details
 * @returns {Object}
 */
const styles = (theme) => {
    const mainBack = theme.custom.infoBar.background || '#ffffff';
    const infoBarHeight = theme.custom.infoBar.height || 70;
    const backIconDisplay = theme.custom.infoBar.showBackIcon ? 'flex' : 'none';
    const starColor = theme.custom.infoBar.starColor || theme.palette.getContrastText(mainBack);

    return {
        root: {
            height: infoBarHeight,
            background: mainBack,
            color: theme.palette.getContrastText(mainBack),
            borderBottom: 'solid 1px ' + theme.palette.grey.A200,
            display: 'flex',
            alignItems: 'center',
            paddingLeft: theme.spacing.unit * 2,
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
            paddingBottom: theme.spacing.unit * 2,
        },
        infoContent: {
            color: theme.palette.getContrastText(mainBack),
            background: mainBack,
            padding: theme.spacing.unit * 3,
            '& td, & th': {
                color: theme.palette.getContrastText(mainBack),
            },
        },
        infoContentBottom: {
            background: theme.custom.infoBar.sliderBackground,
            color: theme.palette.getContrastText(theme.custom.infoBar.sliderBackground),
            borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        },
        infoItem: {
            marginRight: theme.spacing.unit * 4,
        },
        bootstrapRoot: {
            padding: 0,
            'label + &': {
                marginTop: theme.spacing.unit * 3,
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
            marginRight: theme.spacing.unit,
            marginLeft: theme.spacing.unit,
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
            paddingLeft: theme.spacing.unit * 2,
            cursor: 'pointer',
        },
        buttonOverviewText: {
            display: 'inline-block',
            paddingTop: 3,
        },
        button: {
            textDecoration: 'none',
        },
        appNameXSmall: {
            whiteSpace: 'nowrap',
            textOverflow: 'ellipsis',
            overflowX: 'auto',
            maxWidth: 200,
            lineHeight: 1.3,
        },
        appNameSmall: {
            whiteSpace: 'nowrap',
            textOverflow: 'ellipsis',
            overflowX: 'auto',
            maxWidth: 310,
            lineHeight: 1.3,
        },
        appNameMid: {
            whiteSpace: 'nowrap',
            textOverflow: 'ellipsis',
            overflowX: 'auto',
            maxWidth: 640,
            lineHeight: 1.3,
        },
        appNameBig: {
            whiteSpace: 'nowrap',
            textOverflow: 'ellipsis',
            overflowX: 'auto',
            maxWidth: 980,
            lineHeight: 1.3,
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
        };
        this.toggleOverview = this.toggleOverview.bind(this);
    }

    /**
     * @memberof InfoBar
     */
    componentDidMount() {
        const client = new API();
        const { applicationId } = this.props;
        // Get application
        const promisedApplication = client.getApplication(applicationId);

        promisedApplication
            .then((response) => {
                const promisedPolicy = client.getTierByName(response.obj.throttlingPolicy, 'application');
                return Promise.all([response, promisedPolicy]);
            })
            .then((response) => {
                const [application, tier] = response.map(data => data.obj);
                this.setState({ application, tierDescription: tier.description });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
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
     * @returns {div}
     * @memberof InfoBar
     */
    render() {
        const {
            classes, theme, resourceNotFountMessage, applicationId,
        } = this.props;
        const {
            application, tierDescription, showOverview, notFound,
        } = this.state;
        const {
            custom: {
                leftMenu: { position },
            },
        } = theme;

        if (notFound) {
            return <ResourceNotFound message={resourceNotFountMessage} />;
        }

        if (!application) {
            return <Loading />;
        }

        return (
            <div className={classes.infoBarMain}>
                <div className={classes.root}>
                    <Link to='/applications' className={classes.backLink}>
                        <Icon className={classes.backIcon}>keyboard_arrow_left</Icon>
                        <div className={classes.backText}>
                            <FormattedMessage
                                id='Applications.Details.InfoBar.new.back.to'
                                defaultMessage='BACK TO'
                            />{' '}
                            <br />
                            <FormattedMessage id='Applications.Details.InfoBar.listing' defaultMessage='APPLICATIONS' />
                        </div>
                    </Link>
                    <VerticalDivider height={70} />
                    <div style={{ marginLeft: theme.spacing.unit }}>
                        <Hidden smUp>
                            <Typography className={classes.appNameXSmall} variant='h4'>
                                {application.name}
                            </Typography>
                        </Hidden>

                        <Hidden xsDown mdUp>
                            <Typography className={classes.appNameSmall} variant='h4'>
                                {application.name}
                            </Typography>
                        </Hidden>
                        <Hidden smDown lgUp>
                            <Typography className={classes.appNameMid} variant='h4'>
                                {application.name}
                            </Typography>
                        </Hidden>
                        <Hidden mdDown xlUp>
                            <Typography className={classes.appNameBig} variant='h4'>
                                {application.name}
                            </Typography>
                        </Hidden>
                        <Typography variant='caption' gutterBottom align='left'>
                            {application.subscriptionCount}{' '}
                            <FormattedMessage
                                id='Applications.Details.InfoBar.subscriptions'
                                defaultMessage='Subscriptions'
                            />
                        </Typography>
                    </div>
                </div>
                {position === 'horizontal' && <div style={{ height: 60 }} />}
                {showOverview && (
                    <Collapse in={showOverview} timeout='auto' unmountOnExit>
                        <div className={classes.infoContent}>
                            <div className={classes.contentWrapper}>
                                <div className={classes.topBar}>
                                    <div className={classes.infoItem}>
                                        <Typography variant='subtitle1' gutterBottom>
                                            {application.throttlingPolicy}{' '}
                                            <Typography variant='caption'>({tierDescription} )</Typography>
                                        </Typography>
                                        <Typography variant='caption' gutterBottom align='left'>
                                            <FormattedMessage
                                                id='Applications.Details.InfoBar.throttling.tier'
                                                defaultMessage='Throttling Tier'
                                            />
                                        </Typography>
                                    </div>
                                    {Object.entries(application.attributes).map(([key, value]) =>
                                        (value !== '' ? (
                                            <div className={classes.infoItem} key={key}>
                                                <Typography variant='subtitle1' gutterBottom>
                                                    {key}
                                                    {' : '}
                                                    <Typography variant='caption'>{value}</Typography>
                                                </Typography>
                                            </div>
                                        ) : null))}
                                    <div className={classes.infoItem}>
                                        <Link to={`/applications/${applicationId}/edit/`} className={classes.button}>
                                            <Button variant='contained' color='default'>
                                                <FormattedMessage
                                                    id='Applications.Details.InfoBar.edit'
                                                    defaultMessage='Edit'
                                                />
                                            </Button>
                                        </Link>
                                    </div>
                                </div>
                                <Typography>{application.description}</Typography>
                            </div>
                        </div>
                    </Collapse>
                )}
                <div className={classes.infoContentBottom}>
                    <div
                        className={classes.contentWrapper}
                        onClick={this.toggleOverview}
                        onKeyDown={this.toggleOverview}
                    >
                        <div className={classes.buttonView}>
                            {showOverview ? (
                                <Typography className={classes.buttonOverviewText}>
                                    <FormattedMessage id='Applications.Details.InfoBar.less' defaultMessage='LESS' />
                                </Typography>
                            ) : (
                                <Typography className={classes.buttonOverviewText}>
                                    <FormattedMessage id='Applications.Details.InfoBar.more' defaultMessage='MORE' />
                                </Typography>
                            )}
                            {showOverview ? <Icon>arrow_drop_up_circle</Icon> : <Icon>arrow_drop_down_circle</Icon>}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

InfoBar.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    resourceNotFountMessage: PropTypes.string.isRequired,
    applicationId: PropTypes.string.isRequired,
};

export default withStyles(styles, { withTheme: true })(InfoBar);
