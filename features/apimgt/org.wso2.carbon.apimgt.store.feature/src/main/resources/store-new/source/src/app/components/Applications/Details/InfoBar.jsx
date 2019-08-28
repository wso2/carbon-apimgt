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
import { KeyboardArrowLeft, ArrowDropDownOutlined, ArrowDropUpOutlined } from '@material-ui/icons';
import Typography from '@material-ui/core/Typography';

import { Link } from 'react-router-dom';
import Collapse from '@material-ui/core/Collapse';
import CheckCircle from '@material-ui/icons/CheckCircle';
import Loading from '../../Base/Loading/Loading';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import API from '../../../data/api';

import VerticalDivider from '../../Shared/VerticalDivider';

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    root: {
        height: 70,
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        display: 'flex',
        alignItems: 'center',
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
    apiIcon: {
        height: 45,
        marginTop: 10,
        marginRight: 10,
    },
    starRate: {
        fontSize: 70,
        color: theme.custom.starColor,
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
        background: theme.palette.background.paper,
        padding: theme.spacing.unit * 3,
    },
    infoContentBottom: {
        background: theme.palette.grey['200'],
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        color: theme.palette.grey['600'],
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
        fontFamily: ['-apple-system', 'BlinkMacSystemFont', '"Segoe UI"', 'Roboto', '"Helvetica Neue"', 'Arial', 'sans-serif', '"Apple Color Emoji"', '"Segoe UI Emoji"', '"Segoe UI Symbol"'].join(','),
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
        width: theme.custom.contentAreaWidth - theme.custom.leftMenuWidth,
        alignItems: 'center',
    },
    ratingBoxWrapper: {
        position: 'relative',
        display: 'flex',
        alignItems: 'center',
    },
    ratingBox: {
        backgroundColor: theme.palette.background.leftMenu,
        border: '1px solid rgb(71, 211, 244)',
        borderRadius: '5px',
        display: 'flex',
        position: 'absolute',
        left: '-310px',
        top: 14,
        height: '40px',
        color: theme.palette.getContrastText(theme.palette.background.leftMenu),
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
});
/**
 *
 *
 * @class InfoBar
 * @extends {React.Component}
 */
class InfoBar extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            api: null,
            applications: null,
            policies: null,
            dropDownApplications: null,
            dropDownPolicies: null,
            notFound: false,
            tabValue: 'Social Sites',
            comment: '',
            commentList: null,
            prodUrlCopied: false,
            sandboxUrlCopied: false,
            showOverview: true,
        };
        this.applicationId = this.props.applicationId;
    }

    /**
     *
     *
     * @memberof InfoBar
     */
    toggleOverview = (todo) => {
        if (typeof todo === 'boolean') {
            this.setState({ showOverview: todo });
        } else {
            this.setState({ showOverview: !this.state.showOverview });
        }
    };

    /**
     *
     *
     * @memberof InfoBar
     */
    componentDidMount() {
        const client = new API();
        // Get application
        const promised_application = client.getApplication(this.applicationId);
        promised_application
            .then((response) => {
                const promised_tier = client.getTierByName(response.obj.throttlingTier, 'application');
                return Promise.all([response, promised_tier]);
            })
            .then((response) => {
                const [application, tier] = response.map(data => data.obj);
                this.setState({ application, tierDescription: tier.description });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    /**
     *
     *
     * @returns
     * @memberof InfoBar
     */
    render() {
        const { classes, theme } = this.props;
        const application = this.state.application;
        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFountMessage} />;
        }
        if (!application) {
            return <Loading />;
        }
        return (
            <div className={classes.infoBarMain}>
                <div className={classes.root}>
                    <Link to='/applications' className={classes.backLink}>
                        <KeyboardArrowLeft className={classes.backIcon} />
                        <div className={classes.backText}>
                            BACK TO
                            {' '}
                            <br />
                            LISTING
                        </div>
                    </Link>
                    <VerticalDivider height={70} />
                    <div style={{ marginLeft: theme.spacing.unit }}>
                        <Typography variant='display1'>{application.name}</Typography>
                        <Typography variant='caption' gutterBottom align='left'>
                            N Subscriptions
                        </Typography>
                    </div>
                </div>

                {this.state.showOverview && (
                    <Collapse in={this.state.showOverview} timeout='auto' unmountOnExit>
                        <div className={classes.infoContent}>
                            <div className={classes.contentWrapper}>
                                <div className={classes.topBar}>
                                    <div className={classes.infoItem}>
                                        <Typography variant='subheading' gutterBottom>
                                            {application.throttlingTier}
                                            {' '}
                                            <Typography variant='caption'>
(
                                                {this.state.tierDescription}
                                                {' '}
)
                                            </Typography>
                                        </Typography>
                                        <Typography variant='caption' gutterBottom align='left'>
                                            Throttling Tier
                                        </Typography>
                                    </div>
                                    <div className={classes.infoItem}>
                                        {application.lifeCycleStatus === 'APPROVED' ? (
                                            <CheckCircle />
                                        ) : (
                                            <Typography variant='subheading' gutterBottom>
                                                {this.state.lifeCycleStatus}
                                            </Typography>
                                        )}
                                        <Typography variant='caption' gutterBottom align='left'>
                                            lifeCycleStatus
                                        </Typography>
                                    </div>
                                </div>
                                <Typography>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut dolor dui, fermentum in ipsum a, pharetra feugiat orci. Suspendisse at sem nunc. Integer in eros eget orci sollicitudin ultricies. Mauris vehicula mollis vulputate. Morbi sed velit vulputate nisl ullamcorper blandit. Quisque diam orci, ultrices at risus vel, auctor vulputate odio. Etiam vel iaculis massa, vel sollicitudin velit. Aenean facilisis vitae elit vitae iaculis. Nam vel tincidunt arcu.</Typography>
                            </div>
                        </div>
                    </Collapse>
                )}
                <div className={classes.infoContentBottom}>
                    <div className={classes.contentWrapper} onClick={this.toggleOverview}>
                        <div className={classes.buttonView}>
                            {this.state.showOverview ? <Typography className={classes.buttonOverviewText}>LESS</Typography> : <Typography className={classes.buttonOverviewText}>MORE</Typography>}
                            {this.state.showOverview ? <ArrowDropUpOutlined /> : <ArrowDropDownOutlined />}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

InfoBar.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(InfoBar);
