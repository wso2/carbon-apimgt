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
import Icon from '@material-ui/core/Icon';
import { Link } from 'react-router-dom';
import Collapse from '@material-ui/core/Collapse';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Grade from '@material-ui/icons/Grade';
import LaunchIcon from '@material-ui/icons/Launch';
import { FormattedMessage, injectIntl } from 'react-intl';
import API from 'AppData/api';
import StarRatingBar from 'AppComponents/Apis/Listing/StarRatingBar';
import VerticalDivider from '../../Shared/VerticalDivider';
import ImageGenerator from '../Listing/ImageGenerator';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import AuthManager from '../../../data/AuthManager';
import { ApiContext } from './ApiContext';
import Environments from './Environments';
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
    apiIcon: {
        height: 45,
        marginTop: 10,
        marginRight: 10,
    },
    starRate: {
        fontSize: 40,
        color: theme.custom.starColor,
    },
    starRateMy: {
        fontSize: 40,
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
    downloadLink: {
        color: 'blue',
    },
    contentToTop: {
        verticlaAlign: 'top',
    },
    viewInPubStoreLauncher: {
        display: 'flex',
        flexDirection: 'column',
        color: theme.palette.getContrastText(theme.palette.background.paper),
        textAlign: 'center',
        textDecoration: 'none',
    },
    linkText: {
        fontSize: theme.typography.fontSize,
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
            showOverview: false,
            checked: false,
        };
        this.getSchema = this.getSchema.bind(this);
    }

    /**
     *
     *
     * @memberof InfoBar
     */


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

    getSchema() {
        const newAPI = new API();
        const { api } = this.context;
        const promisedGraphQL = newAPI.getGraphQLSchemaByAPIId(api.id);
        promisedGraphQL.then((response) => {
            const windowUrl = window.URL || window.webkitURL;
            const binary = new Blob([response.data]);
            const url = windowUrl.createObjectURL(binary);
            const anchor = document.createElement('a');
            anchor.href = url;
            anchor.download = api.provider + '-' + api.name + '-' + api.version + '.graphql';
            anchor.click();
            windowUrl.revokeObjectURL(url);
        });
    }

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
        const user = AuthManager.getUser();
        if (notFound) {
            return <ResourceNotFound message={resourceNotFountMessage} />;
        }

        return (
            <ApiContext.Consumer>
                {({ api }) => (
                    <div className={classes.infoBarMain}>
                        <div className={classes.root}>
                            <Link to='/apis' className={classes.backLink}>
                                <Icon className={classes.backIcon}>keyboard_arrow_left</Icon>
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
                                </Typography>
                            </div>
                            <VerticalDivider height={70} />
                            {user && <StarRatingBar apiId={api.id} isEditable={false} showSummary />}
                            {api.advertiseInfo.advertised && (
                                <React.Fragment>
                                    {user && <VerticalDivider height={70} />}
                                    <a
                                        target='_blank'
                                        rel='noopener noreferrer'
                                        href={api.advertiseInfo.originalStoreUrl}
                                        className={classes.viewInPubStoreLauncher}
                                    >
                                        <div>
                                            <LaunchIcon />
                                        </div>
                                        <div className={classes.linkText}>Visit Publisher Store</div>
                                    </a>
                                    <VerticalDivider height={70} />
                                </React.Fragment>
                            )}
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
                                                            <Icon className={classes.iconOdd}>settings_input_component</Icon>
                                                            <span className={classes.iconTextWrapper}>
                                                                <FormattedMessage
                                                                    id='Apis.Details.InfoBar.list.version'
                                                                    defaultMessage='Version'
                                                                />
                                                            </span>
                                                        </div>
                                                    </TableCell>
                                                    <TableCell>
                                                        {api.version}
                                                    </TableCell>
                                                </TableRow>
                                                <TableRow>
                                                    <TableCell component='th' scope='row'>
                                                        <div className={classes.iconAligner}>
                                                            <Icon className={classes.iconEven}>account_balance_wallet</Icon>
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
                                                            <Icon className={classes.iconOdd}>account_circle</Icon>
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
                                                {/* <TableRow>
                                                    <TableCell component='th' scope='row'>
                                                        <div className={classes.iconAligner}>
                                                            <Icon className={classes.iconEven}>update</Icon>
                                                            <span className={classes.iconTextWrapper}>
                                                                <FormattedMessage
                                                                    id='Apis.Details.InfoBar.last.updated'
                                                                    defaultMessage='Last updated'
                                                                />
                                                            </span>
                                                        </div>
                                                    </TableCell>
                                                    <TableCell>21 May 2018</TableCell>
                                                </TableRow> */}
                                                {user && (
                                                    <TableRow>
                                                        <TableCell component='th' scope='row'>
                                                            <div className={classes.iconAligner}>
                                                                <Grade className={classes.iconEven} />
                                                                <span className={classes.iconTextWrapper}>
                                                                    <FormattedMessage
                                                                        id='Apis.Details.InfoBar.list.context.rating'
                                                                        defaultMessage='Rating'
                                                                    />
                                                                </span>
                                                            </div>
                                                        </TableCell>
                                                        <TableCell>
                                                            <StarRatingBar
                                                                apiId={api.id}
                                                                isEditable={!api.advertiseInfo.advertised}
                                                                showSummary={false}
                                                            />
                                                        </TableCell>
                                                    </TableRow>
                                                )}
                                                { api.type === 'GRAPHQL' && (
                                                    <TableRow>
                                                        <TableCell component='th' scope='row'>
                                                            <div className={classes.iconAligner}>
                                                                <Icon className={classes.iconOdd}>cloud_download</Icon>
                                                                <span className={classes.iconTextWrapper}>
                                                                    <FormattedMessage
                                                                        id='Apis.Details.InfoBar.download.Schema'
                                                                        defaultMessage='Download Schema'
                                                                    />
                                                                </span>
                                                            </div>
                                                        </TableCell>
                                                        <TableCell>
                                                            <Button
                                                                onClick={this.getSchema}
                                                                size='small'
                                                                fontSize='small'
                                                                variant='outlined'
                                                            >
                                                                <FormattedMessage
                                                                    id='Apis.Details.InfoBar.graphQL.schema'
                                                                    defaultMessage='GraphQL Schema'
                                                                />
                                                            </Button>

                                                        </TableCell>
                                                    </TableRow>
                                                )}
                                                {!api.advertiseInfo.advertised ? (
                                                    <TableRow>
                                                        <TableCell
                                                            component='th'
                                                            scope='row'
                                                            className={classes.contentToTop}
                                                        >
                                                            <div className={classes.iconAligner}>
                                                                <Icon className={classes.iconEven}>
                                                                    desktop_windows
                                                                </Icon>
                                                                <span className={classes.iconTextWrapper}>
                                                                    <FormattedMessage
                                                                        id='Apis.Details.InfoBar.available.environments'
                                                                        defaultMessage='Available Environments'
                                                                    />
                                                                </span>
                                                            </div>
                                                        </TableCell>
                                                        <TableCell>
                                                            <Environments />
                                                        </TableCell>
                                                    </TableRow>
                                                ) : (
                                                    <TableRow>
                                                        <TableCell component='th' scope='row'>
                                                            <div className={classes.iconAligner}>
                                                                <Icon className={classes.iconOdd}>account_circle</Icon>
                                                                <span className={classes.iconTextWrapper}>
                                                                    <FormattedMessage
                                                                        id='Apis.Details.InfoBar.owner'
                                                                        defaultMessage='Owner'
                                                                    />
                                                                </span>
                                                            </div>
                                                        </TableCell>
                                                        <TableCell>{api.advertiseInfo.apiOwner}</TableCell>
                                                    </TableRow>
                                                )}
                                            </TableBody>
                                        </Table>
                                    </div>
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
                                            <FormattedMessage
                                                id='Apis.Details.InfoBar.more'
                                                defaultMessage='MORE'
                                            />
                                        </Typography>
                                    )}
                                    {showOverview ? <Icon>arrow_drop_up</Icon> : <Icon>arrow_drop_down</Icon>}
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

InfoBar.contextType = ApiContext;

export default injectIntl(withStyles(styles, { withTheme: true })(InfoBar));
