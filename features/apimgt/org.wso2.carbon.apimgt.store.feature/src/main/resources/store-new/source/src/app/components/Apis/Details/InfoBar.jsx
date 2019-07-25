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
import {
    KeyboardArrowLeft, StarRate, FileCopy, ArrowDropDownOutlined, ArrowDropUpOutlined,
} from '@material-ui/icons';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import HighlightOff from '@material-ui/icons/HighlightOff';
import { Link } from 'react-router-dom';
import CopyToClipboard from 'react-copy-to-clipboard';
import Tooltip from '@material-ui/core/Tooltip';
import Collapse from '@material-ui/core/Collapse';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import CalendarViewDay from '@material-ui/icons/CalendarViewDay';
import AccountBalanceWallet from '@material-ui/icons/AccountBalanceWallet';
import AccountCircle from '@material-ui/icons/AccountCircle';
import CheckCircle from '@material-ui/icons/CheckCircle';
import Cloud from '@material-ui/icons/Cloud';
import Build from '@material-ui/icons/Build';
import Update from '@material-ui/icons/Update';
import LinkIcon from '@material-ui/icons/Link';
import Button from '@material-ui/core/Button';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import { FormattedMessage, injectIntl, } from 'react-intl';
import VerticalDivider from '../../Shared/VerticalDivider';
import ImageGenerator from '../Listing/ImageGenerator';
import Api from '../../../data/api';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import Loading from '../../Base/Loading/Loading';
import { ApiContext } from './ApiContext';

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    table: {
        minWidth: '100%',
    },
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
    rootx: {
        height: 180,
    },
    container: {
        display: 'flex',
    },
    paper: {
        margin: theme.spacing.unit,
    },
    svg: {
        width: 100,
        height: 100,
    },
    polygon: {
        fill: theme.palette.common.white,
        stroke: theme.palette.divider,
        strokeWidth: 1,
    },
    leftCol: {
        width: 200,
    },
    iconAligner: {
        display: 'flex',
        justifyContent: 'flex-start',
        alignItems: 'center',
    },
    iconTextWrapper: {
        display: 'inline-block',
        paddingLeft: 20,
    },
    iconEven: {
        color: theme.palette.secondary.light,
        width: theme.spacing.unit * 3,
    },
    iconOdd: {
        color: theme.palette.secondary.main,
        width: theme.spacing.unit * 3,
    },
    margin: {
        marginLeft: 30,
    },
});
/**
 *
 *
 * @class StarRatingBar
 * @extends {React.Component}
 */
class StarRatingBar extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            rating: null,
            dummyRateValue: 1,
            showRateNow: false,
        };

        this.handleMouseOver = this.handleMouseOver.bind(this);
        this.handleRatingUpdate = this.handleRatingUpdate.bind(this);
        this.handleMouseOut = this.handleMouseOut.bind(this);
    }

    /**
     *
     *
     * @memberof StarRatingBar
     */
    updateRating() {
        const api = new Api();

        // get user rating
        const promised_rating = api.getRatingFromUser(this.props.apiIdProp, null);
        promised_rating.then((response) => {
            this.setState({
                // rating: response.obj,
                // dummyRateValue: response.obj.userRating,
            });
        });
    }

    /**
     *
     *
     * @memberof StarRatingBar
     */
    componentDidMount() {
        this.updateRating();
    }

    /**
     *
     *
     * @param {*} index
     * @memberof StarRatingBar
     */
    handleMouseOver(index) {
        this.setState({ rating: index });
    }

    /**
     *
     *
     * @memberof StarRatingBar
     */
    handleMouseOut() {
        this.setState({ rating: this.state.previousRating });
    }

    /**
     *
     *
     * @memberof StarRatingBar
     */
    handleRatingUpdate() { }

    /**
     *
     *
     * @memberof StarRatingBar
     */
    handleClickAway = () => {
        this.setState({
            showRateNow: false,
        });
    };

    /**
     *
     *
     * @memberof StarRatingBar
     */
    showRateBox = () => {
        this.setState({
            showRateNow: true,
        });
    };

    /**
     *
     *
     * @param {*} index
     * @memberof StarRatingBar
     */
    highlightUs(index) {
        this.setState({ dummyRateValue: index });
    }

    /**
     *
     *
     * @memberof StarRatingBar
     */
    unhighlightUs() {
        this.setState({ dummyRateValue: 1 });
    }

    /**
     *
     *
     * @param {*} rateIndex
     * @memberof StarRatingBar
     */
    doRate(rateIndex) {
        this.setState({ rateIndex, showRateNow: false });

        const api = new Api();
        const ratingInfo = { rating: rateIndex / 2 };
        const promise = api.addRating(this.props.apiIdProp, ratingInfo);
        promise
            .then((response) => {
                this.updateRating();
                // message.success("Rating updated successfully");
            })
            .catch((error) => {
                // message.error("Error occurred while adding ratings!");
            });
    }

    /**
     *
     *
     * @returns
     * @memberof StarRatingBar
     */
    render() {
        const { classes, theme } = this.props;
        if (!this.state.rating) {
            return <span />;
        }
        return (
            <React.Fragment>
                {this.state.rating.count > 0 ? (
                    <React.Fragment>
                        <StarRate className={classes.starRate} />
                        <div className={classes.ratingSummery}>
                            <div className={classes.userRating}>
                                <Typography variant='display1'>{this.state.rating.avgRating * 2}</Typography>
                                <Typography variant='caption'>/10</Typography>
                            </div>
                            <Typography variant='caption' gutterBottom align='left'>
                                {this.state.rating.count}
                                {' '}
                                {this.state.rating.count == 1 ? 'user' : 'users'}
                            </Typography>
                        </div>
                    </React.Fragment>
                ) : (
                        <StarRate
                            onClick={this.showRateBox}
                            className={classes.starRate}
                            style={{ color: theme.palette.grey.A200 }}
                        />
                    )}
                <VerticalDivider height={32} />
                <div className={classes.ratingBoxWrapper}>
                    {this.state.showRateNow && (
                        <div className={classes.ratingBox}>
                            <HighlightOff />
                            <VerticalDivider height={32} />
                            {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map(i => (
                                <StarRate
                                    color={
                                        i <= this.state.rating.userRating * 2 || i <= this.state.dummyRateValue
                                            ? 'primary'
                                            : ''
                                    }
                                    onMouseOver={() => this.highlightUs(i)}
                                    onMouseLeave={() => this.unhighlightUs()}
                                    onClick={() => this.doRate(i)}
                                />
                            ))}
                        </div>
                    )}
                    {this.state.rating.userRating ? (
                        <React.Fragment>
                            <StarRate className={classes.starRateMy} onClick={this.showRateBox} />
                            <div className={classes.ratingSummery} onClick={this.showRateBox}>
                                <Typography variant='display1'>{this.state.rating.userRating * 2}</Typography>
                                <Typography variant='caption' gutterBottom align='left'>
                                    <FormattedMessage id='Apis.Details.InfoBar.you' defaultMessage='YOU' />
                                </Typography>
                            </div>
                        </React.Fragment>
                    ) : (
                            <React.Fragment>
                                <StarRate
                                    onClick={this.showRateBox}
                                    className={classes.starRate}
                                    style={{ color: theme.palette.grey.A200 }}
                                />
                                <Typography onClick={this.showRateBox} className={classes.rateLink}>
                                    <FormattedMessage
                                        id='Apis.Details.InfoBar.rate.this.api'
                                        defaultMessage='Rate this API'
                                    />
                                </Typography>
                            </React.Fragment>
                        )}
                </div>
            </React.Fragment>
        );
    }
}

StarRatingBar.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

StarRatingBar = withStyles(styles, { withTheme: true })(StarRatingBar);
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
            showOverview: false,
            checked: false,
            epUrl: '',
        };
    }

    /**
     *
     *
     * @memberof InfoBar
     */
    onCopy = name => () => {
        this.setState({
            [name]: true,
        });
        const that = this;
        const elementName = name;
        const caller = function () {
            that.setState({
                [elementName]: false,
            });
        };
        setTimeout(caller, 4000);
    };

    /**
     *
     *
     * @memberof InfoBar
     */
    toggleOverview = (todo) => {
        if (typeof todo === 'boolean') {
            this.setState({ showOverview: todo });
        } else {
            this.setState(state => ({ showOverview: !state.showOverview }));
        }
    };

    /**
     *
     *
     * @returns
     * @memberof InfoBar
     */
    render() {
        const { classes, theme, intl } = this.props;
        const {
            notFound, showOverview, prodUrlCopied, sandboxUrlCopied, epUrl,
        } = this.state;
        const { resourceNotFountMessage } = this.props;
        if (notFound) {
            return <ResourceNotFound message={resourceNotFountMessage} />;
        }

        return (
            <ApiContext.Consumer>
                {({ api }) => (
                    <div className={classes.infoBarMain}>
                        <div className={classes.root}>
                            <Link to='/apis' className={classes.backLink}>
                                <KeyboardArrowLeft className={classes.backIcon} />
                                <div className={classes.backText}>
                                    <FormattedMessage id='Apis.Details.InfoBar.back.to' defaultMessage='BACK TO' />
                                    <br />
                                    <FormattedMessage id='Apis.Details.InfoBar.listing' defaultMessage='LISTING' />
                                </div>
                            </Link>
                            <VerticalDivider height={70} />
                            <ImageGenerator api={api} width='70' height='50' />
                            <div style={{ marginLeft: theme.spacing.unit }}>
                                <Typography variant='display1'>{api.name}</Typography>
                                <Typography variant='caption' gutterBottom align='left'>
                                    {api.provider}
                                    | 21-May 2018
                                </Typography>
                            </div>
                            <VerticalDivider height={70} />
                            <StarRatingBar apiIdProp={api.id} />
                        </div>

                        {showOverview && (
                            <Collapse in={showOverview}>
                                <div className={classes.infoContent}>
                                    <div className={classes.contentWrapper}>
                                        <Typography>{api.description}</Typography>
                                        <Table className={classes.table}>
                                            <TableBody>
                                                <TableRow>
                                                    <TableCell component='th' scope='row' className={classes.leftCol}>
                                                        <div className={classes.iconAligner}>
                                                            <CalendarViewDay className={classes.iconOdd} />
                                                            <span className={classes.iconTextWrapper}>
                                                                <FormattedMessage
                                                                    id='Apis.Details.InfoBar.list.version'
                                                                    defaultMessage='Version'
                                                                />
                                                            </span>
                                                        </div>
                                                    </TableCell>
                                                    <TableCell>{api.version}
                                                    </TableCell>
                                                </TableRow>
                                                <TableRow>
                                                    <TableCell component='th' scope='row'>
                                                        <div className={classes.iconAligner}>
                                                            <AccountBalanceWallet className={classes.iconEven} />
                                                            <span className={classes.iconTextWrapper}>
                                                                <FormattedMessage
                                                                    id='Apis.Details.InfoBar.list.context'
                                                                    defaultMessage='Context'
                                                                />
                                                            </span>
                                                        </div>
                                                    </TableCell>
                                                    <TableCell>{api.context}</TableCell>
                                                </TableRow>
                                                <TableRow>
                                                    <TableCell component='th' scope='row'>
                                                        <div className={classes.iconAligner}>
                                                            <AccountCircle className={classes.iconOdd} />
                                                            <span className={classes.iconTextWrapper}>
                                                                <FormattedMessage
                                                                    id='Apis.Details.InfoBar.provider'
                                                                    defaultMessage='Provider'
                                                                />
                                                            </span>
                                                        </div>
                                                    </TableCell>
                                                    <TableCell>{api.provider}</TableCell>
                                                </TableRow>
                                                <TableRow>
                                                    <TableCell component='th' scope='row'>
                                                        <div className={classes.iconAligner}>
                                                            <Update className={classes.iconEven} />
                                                            <span className={classes.iconTextWrapper}>
                                                                <FormattedMessage
                                                                    id='Apis.Details.InfoBar.last.updated'
                                                                    defaultMessage='Last updated'
                                                                />
                                                            </span>
                                                        </div>
                                                    </TableCell>
                                                    <TableCell>21 May 2018</TableCell>
                                                </TableRow>
                                            </TableBody>
                                        </Table>
                                    </div>
                                </div>
                                <div className={classes.infoContent}>
                                    <Typography variant='subtitle2' >
                                        Available Environments</Typography>
                                    <Grid container spacing={16} item xs={8}>
                                        {api.endpointURLs.map(i => {
                                            return <Grid key={i} item xs={6} >
                                                <ExpansionPanel>
                                                    <ExpansionPanelSummary
                                                        expandIcon={<ExpandMoreIcon />}
                                                        aria-controls="panel1a-content"
                                                        id="panel1a-header"
                                                    >
                                                        <div className={classes.iconAligner}>
                                                            {i.environmentType === 'hybrid' && (
                                                                <Cloud className={classes.iconEven} />
                                                            )}
                                                            {i.environmentType === 'production' && (
                                                                <CheckCircle className={classes.iconEven} />
                                                            )}
                                                            {i.environmentType === 'sandbox' && (
                                                                <Build className={classes.iconEven} />
                                                            )}<span className={classes.iconTextWrapper}>
                                                                <Typography className={classes.heading}>{i.environmentName} </Typography></span>
                                                        </div>
                                                    </ExpansionPanelSummary>
                                                    <ExpansionPanelDetails>
                                                        <Grid container item xs={12} spacing={16}>
                                                            <Typography className={classes.heading}>Gateway URLs</Typography>
                                                            <Grid item xs={12} >
                                                                <TextField
                                                                    defaultValue={i.environmentURLs.http}
                                                                    id='bootstrap-input'
                                                                    InputProps={{
                                                                        disableUnderline: true,
                                                                        classes: {
                                                                            root: classes.bootstrapRoot,
                                                                            input: classes.bootstrapInput,
                                                                        },
                                                                    }}
                                                                    InputLabelProps={{
                                                                        shrink: true,
                                                                        className: classes.bootstrapFormLabel,
                                                                    }}
                                                                />
                                                                <Tooltip
                                                                    title={prodUrlCopied ? 'Copied' : 'Copy to clipboard'}
                                                                    placement='right'
                                                                >
                                                                    <CopyToClipboard
                                                                        text={epUrl}
                                                                        onCopy={this.onCopy('prodUrlCopied')}
                                                                    >
                                                                        <FileCopy color='secondary' />
                                                                    </CopyToClipboard>
                                                                </Tooltip>
                                                            </Grid>
                                                            <Grid item xs={12} >
                                                                <TextField
                                                                    defaultValue={i.environmentURLs.https}
                                                                    id='bootstrap-input'
                                                                    InputProps={{
                                                                        disableUnderline: true,
                                                                        classes: {
                                                                            root: classes.bootstrapRoot,
                                                                            input: classes.bootstrapInput,
                                                                        },
                                                                    }}
                                                                    InputLabelProps={{
                                                                        shrink: true,
                                                                        className: classes.bootstrapFormLabel,
                                                                    }}
                                                                />
                                                                <Tooltip
                                                                    title={prodUrlCopied ? 'Copied' : 'Copy to clipboard'}
                                                                    placement='right'
                                                                >
                                                                    <CopyToClipboard
                                                                        text={epUrl}
                                                                        onCopy={this.onCopy('prodUrlCopied')}
                                                                    >
                                                                        <FileCopy color='secondary' />
                                                                    </CopyToClipboard>
                                                                </Tooltip>
                                                            </Grid></Grid>
                                                    </ExpansionPanelDetails>
                                                </ExpansionPanel>
                                            </Grid>
                                        })}
                                    </Grid>
                                </div>
                            </Collapse>
                        )}
                        <div className={classes.infoContentBottom}>
                            <div className={classes.contentWrapper} onClick={this.toggleOverview}>
                                <div className={classes.buttonView}>
                                    {showOverview ? (
                                        <Typography className={classes.buttonOverviewText}>
                                            <FormattedMessage id='Apis.Details.InfoBar.less' defaultMessage='LESS' />
                                        </Typography>
                                    ) : (
                                            <Typography className={classes.buttonOverviewText}>
                                                <FormattedMessage id='Apis.Details.InfoBar.more' defaultMessage='MORE' />
                                            </Typography>
                                        )}
                                    {showOverview ? <ArrowDropUpOutlined /> : <ArrowDropDownOutlined />}
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </ApiContext.Consumer>
        );
    }
}

InfoBar.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(InfoBar));
