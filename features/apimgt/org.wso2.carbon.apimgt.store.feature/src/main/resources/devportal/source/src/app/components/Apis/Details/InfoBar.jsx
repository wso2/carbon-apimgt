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
import Settings from 'Settings';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Icon from '@material-ui/core/Icon';
import { Link, withRouter } from 'react-router-dom';
import Collapse from '@material-ui/core/Collapse';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Grade from '@material-ui/icons/Grade';
import Chip from '@material-ui/core/Chip';
import LaunchIcon from '@material-ui/icons/Launch';
import { FormattedMessage, injectIntl } from 'react-intl';
import API from 'AppData/api';
import StarRatingBar from 'AppComponents/Apis/Listing/StarRatingBar';
import StarRatingSummary from 'AppComponents/Apis/Details/StarRatingSummary';
import { ApiContext } from 'AppComponents/Apis/Details/ApiContext';
import Social from 'AppComponents/Apis/Details/Social/Social';
import VerticalDivider from '../../Shared/VerticalDivider';
import ApiThumb from '../Listing/ApiThumb';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import AuthManager from '../../../data/AuthManager';
import Environments from './Environments';
import Labels from './Labels';

const propertyDisplaySuffix = Settings.app.propertyDisplaySuffix || '__display';
/**
 *
 *
 * @param {*} theme
 */
const styles = (theme) => {
    const mainBack = theme.custom.infoBar.background || '#ffffff';
    const infoBarHeight = theme.custom.infoBar.height || 70;
    const starColor = theme.custom.infoBar.starColor || theme.palette.getContrastText(mainBack);

    return {
        table: {
            minWidth: '100%',
        },
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
        contentWrapper: {
            maxWidth: theme.custom.contentAreaWidth - theme.custom.leftMenu.width,
            alignItems: 'center',
        },
        infoBarMain: {
            width: '100%',
            zIndex: 100,
        },
        buttonView: {
            textAlign: 'left',
            justifyContent: 'left',
            display: 'flex',
            paddingLeft: theme.spacing(3),
            cursor: 'pointer',
        },
        buttonOverviewText: {
            display: 'inline-block',
            paddingTop: 3,
        },
        paper: {
            margin: theme.spacing(1),
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
            color: theme.custom.infoBar.iconEvenColor,
            width: theme.spacing(3),
        },
        iconOdd: {
            color: theme.custom.infoBar.iconOddColor,
            width: theme.spacing(3),
        },
        margin: {
            marginLeft: 30,
        },
        contentToTop: {
            verticlaAlign: 'top',
        },
        viewInPubStoreLauncher: {
            display: 'flex',
            flexDirection: 'column',
            color: theme.palette.getContrastText(mainBack),
            textAlign: 'center',
            textDecoration: 'none',
        },
        linkText: {
            fontSize: theme.typography.fontSize,
        },
        chip: {
            background: theme.custom.infoBar.tagChipBackground,
            color: theme.palette.getContrastText(theme.custom.infoBar.tagChipBackground),
            marginRight: theme.spacing(1),
        },
        expandWrapper: {
            cursor: 'pointer',
            display: 'block',
        },
        linkTitle: {
            color: theme.palette.getContrastText(theme.custom.infoBar.background),
        },
        leftMenu: {},
        leftMenuHorizontal: {},
        leftMenuVerticalLeft: {},
        leftMenuVerticalRight: {},
        leftLInkMain: {},
        leftLInkMainText: {},
        detailsContent: {},
        content: {},
        contentLoader: {},
        contentLoaderRightMenu: {},
    };
};

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
            showOverview: true,
            checked: false,
            avgRating: 0,
            total: 0,
            count: 0,
        };
        this.getSchema = this.getSchema.bind(this);
        this.getProvider = this.getProvider.bind(this);
        this.setRatingUpdate = this.setRatingUpdate.bind(this);
    }

    ditectCurrentMenu = (location) => {
        const routeToCheck = 'overview';
        const { pathname } = location;
        const test1 = new RegExp('/' + routeToCheck + '$', 'g');
        const test2 = new RegExp('/' + routeToCheck + '/', 'g');
        if (pathname.match(test1) || pathname.match(test2)) {
            this.setState({ showOverview: true });
        } else {
            this.setState({ showOverview: false });
        }
    };

    componentDidMount() {
        const { history } = this.props;
        this.ditectCurrentMenu(history.location);
        history.listen((location) => {
            this.ditectCurrentMenu(location);
        });
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
            this.setState((state) => ({ showOverview: !state.showOverview }));
        }
    };

    getProvider(api) {
        let { provider } = api;
        if (
            api.businessInformation
            && api.businessInformation.businessOwner
            && api.businessInformation.businessOwner.trim() !== ''
        ) {
            provider = api.businessInformation.businessOwner;
        }
        return provider;
    }

    getProviderMail(api) {
        let mail;
        if (
            api.businessInformation
            && api.businessInformation.businessOwnerEmail
            && api.businessInformation.businessOwnerEmail.trim() !== ''
        ) {
            mail = '(' + api.businessInformation.businessOwnerEmail + ')';
        }
        return mail;
    }

    getTechnical(api) {
        let owner;
        if (
            api.businessInformation
            && api.businessInformation.technicalOwner
            && api.businessInformation.technicalOwner.trim() !== ''
        ) {
            owner = api.businessInformation.technicalOwner;
        }
        return owner;
    }

    getTechnicalMail(api) {
        let mail;
        if (
            api.businessInformation
            && api.businessInformation.technicalOwnerEmail
            && api.businessInformation.technicalOwnerEmail.trim() !== ''
        ) {
            mail = '(' + api.businessInformation.technicalOwnerEmail + ')';
        }
        return mail;
    }

    getKeyManagers(api) {
        let keyManagers;
        let response;
        if (api.keyManagers) {
            keyManagers = api.keyManagers;
            keyManagers.map(name => {
                if (name === 'all') {
                    response = 'All Applicable';
                } else {
                    response = keyManagers.join();
                }
            });
        }
        return response;
    }

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
            anchor.download = this.getProvider(api) + '-' + api.name + '-' + api.version + '.graphql';
            anchor.click();
            windowUrl.revokeObjectURL(url);
        });
    }

    setRatingUpdate(ratings) {
        if (ratings) {
            const { avgRating, total, count } = ratings;
            this.setState({ avgRating, total, count });
        }
    }

    /**
     *
     *
     * @returns
     * @memberof InfoBar
     */
    render() {
        const { api } = this.context;

        const { classes, theme, intl } = this.props;
        const {
            notFound, showOverview, prodUrlCopied, sandboxUrlCopied, epUrl, avgRating, total, count,
        } = this.state;
        const {
            custom: {
                leftMenu: { position },
                infoBar: { showThumbnail, height },
                tagWise: { key, active },
                social: { showRating },
            },
        } = theme;

        // Remve the tags with a sufix '-group' from tags
        let apisTagsWithoutGroups = [];
        if (!active) {
            apisTagsWithoutGroups = api.tags;
        }
        if (active && api.tags && api.tags.length > 0) {
            for (let i = 0; i < api.tags.length; i++) {
                if (api.tags[i].search(key) != -1 && api.tags[i].split(key).length > 0) {
                    apisTagsWithoutGroups.push(api.tags[i].split(key)[0]);
                } else {
                    apisTagsWithoutGroups.push(api.tags[i]);
                }
            }
        }
        const { additionalProperties } = api;
        let additionalProperties__display = null;
        if (additionalProperties && Object.keys(additionalProperties).length > 0 && additionalProperties.constructor === Object) {
            additionalProperties__display = Object.keys(additionalProperties).filter(aProp => aProp.indexOf(propertyDisplaySuffix) !== -1);
        }

        const { resourceNotFountMessage } = this.props;
        const user = AuthManager.getUser();
        if (notFound) {
            return <ResourceNotFound message={resourceNotFountMessage} />;
        }

        return (
            <div className={classes.infoBarMain} id='infoBar'>
                <div className={classes.root}>
                    {showThumbnail && (
                        <ApiThumb api={api} customWidth={70} customHeight={50} showInfo={false} />
                    )}
                    <div style={{ marginLeft: theme.spacing(1) }}>
                        <Link to={'/apis/' + api.id + '/overview'} className={classes.linkTitle}>
                            <Typography variant='h4'>{api.name}</Typography>
                        </Link>
                        <Typography variant='caption' gutterBottom align='left'>
                            {this.getProvider(api)}
                        </Typography>
                    </div>
                    <VerticalDivider height={70} />
                    {!api.advertiseInfo.advertised && user && showRating && (
                        <StarRatingSummary avgRating={avgRating} reviewCount={total} returnCount={count} />

                    )}
                    {api.advertiseInfo.advertised && (
                        <>
                            <a
                                target='_blank'
                                rel='noopener noreferrer'
                                href={api.advertiseInfo.originalStoreUrl}
                                className={classes.viewInPubStoreLauncher}
                            >
                                <div>
                                    <LaunchIcon />
                                </div>
                                <div className={classes.linkText}>Visit Publisher Dev Portal</div>
                            </a>
                            <VerticalDivider height={70} />
                        </>
                    )}
                    <Social />
                </div>
                {position === 'horizontal' && <div style={{ height: 60 }} />}
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
                                            <TableCell>{api.version}</TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row'>
                                                <div className={classes.iconAligner}>
                                                    <Icon className={classes.iconOdd}>account_balance_wallet</Icon>
                                                    <span className={classes.iconTextWrapper}>
                                                        <FormattedMessage
                                                            id='Apis.Details.InfoBar.list.context'
                                                            defaultMessage='Context'
                                                        />
                                                    </span>
                                                </div>
                                            </TableCell>
                                            <TableCell style={{ 'word-break' : 'break-all' }}>{api.context}</TableCell>
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
                                            <TableCell>{this.getProvider(api)} {this.getProviderMail(api)}</TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row'>
                                                <div className={classes.iconAligner}>
                                                    <Icon className={classes.iconOdd}>account_box</Icon>
                                                    <span className={classes.iconTextWrapper}>
                                                        <FormattedMessage
                                                            id='Apis.Details.InfoBar.technical'
                                                            defaultMessage='Technical Owner'
                                                        />
                                                    </span>
                                                </div>
                                            </TableCell>
                                            <TableCell>{this.getTechnical(api)} {this.getTechnicalMail(api)}</TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row'>
                                                <div className={classes.iconAligner}>
                                                    <Icon className={classes.iconOdd}>vpn_key</Icon>
                                                    <span className={classes.iconTextWrapper}>
                                                        <FormattedMessage
                                                            id='Apis.Details.InfoBar.keyManagers'
                                                            defaultMessage='Key Managers'
                                                        />
                                                    </span>
                                                </div>
                                            </TableCell>
                                            <TableCell>{this.getKeyManagers(api)}</TableCell>
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
                                        {user && !api.advertiseInfo.advertised && showRating && (
                                            <TableRow>
                                                <TableCell component='th' scope='row'>
                                                    <div className={classes.iconAligner}>
                                                        <Grade className={classes.iconOdd} />
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
                                                        isEditable
                                                        showSummary={false}
                                                        setRatingUpdate={this.setRatingUpdate}
                                                    />
                                                </TableCell>
                                            </TableRow>
                                        )}
                                        {api.type === 'GRAPHQL' && (
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
                                            <>
                                                <TableRow>
                                                    <TableCell
                                                        component='th'
                                                        scope='row'
                                                        className={classes.contentToTop}
                                                    >
                                                        <div className={classes.iconAligner}>
                                                            <Icon className={classes.iconOdd}>desktop_windows</Icon>
                                                            <span className={classes.iconTextWrapper}>
                                                                <FormattedMessage
                                                                    id='Apis.Details.InfoBar.gateway.environments'
                                                                    defaultMessage='Gateway Environments'
                                                                />
                                                            </span>
                                                        </div>
                                                    </TableCell>
                                                    <TableCell>
                                                        <Environments />
                                                    </TableCell>
                                                </TableRow>
                                                {api.labels.length !== 0 && (
                                                    <TableRow>
                                                        <TableCell
                                                            component='th'
                                                            scope='row'
                                                            className={classes.contentToTop}
                                                        >
                                                            <div className={classes.iconAligner}>
                                                                <Icon className={classes.iconOdd}>games</Icon>
                                                                <span className={classes.iconTextWrapper}>
                                                                    <FormattedMessage
                                                                        id='Apis.Details.InfoBar.available.gLabels'
                                                                        defaultMessage='Available Gateways'
                                                                    />
                                                                </span>
                                                            </div>
                                                        </TableCell>
                                                        <TableCell>
                                                            <Labels />
                                                        </TableCell>
                                                    </TableRow>
                                                )}
                                            </>
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
                                        {apisTagsWithoutGroups && apisTagsWithoutGroups.length > 0 && (
                                            <TableRow>
                                                <TableCell component='th' scope='row'>
                                                    <div className={classes.iconAligner}>
                                                        <Icon className={classes.iconOdd}>bookmark</Icon>
                                                        <span className={classes.iconTextWrapper}>
                                                            <FormattedMessage
                                                                id='Apis.Details.InfoBar.list.tags'
                                                                defaultMessage='Tags'
                                                            />
                                                        </span>
                                                    </div>
                                                </TableCell>
                                                <TableCell>
                                                    {apisTagsWithoutGroups.map((tag) => (
                                                        <Chip label={tag} className={classes.chip} key={tag} />
                                                    ))}
                                                </TableCell>
                                            </TableRow>
                                        )}
                                        {additionalProperties__display && additionalProperties__display.length > 0 && (
                                            additionalProperties__display.map((displayProp, index) => (
                                                <TableRow>
                                                    <TableCell component='th' scope='row'>
                                                        <div className={classes.iconAligner}>
                                                            <Icon className={classes.iconEven}>adjust</Icon>
                                                            <span className={classes.iconTextWrapper}>
                                                                {displayProp.split(propertyDisplaySuffix)[0]}
                                                            </span>
                                                        </div>
                                                    </TableCell>
                                                    <TableCell>
                                                        {additionalProperties[displayProp]}
                                                    </TableCell>
                                                </TableRow>))
                                        )}
                                    </TableBody>
                                </Table>
                            </div>
                        </div>
                    </Collapse>
                )}
                <div className={classes.infoContentBottom}>
                    <Button className={classes.expandWrapper} onClick={this.toggleOverview}>
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
                            {showOverview ? <Icon>arrow_drop_up</Icon> : <Icon>arrow_drop_down</Icon>}
                        </div>
                    </Button>
                </div>
            </div>
        );
    }
}
InfoBar.defaultProps = {
    classes: {
        leftMenu: {},
        leftMenuHorizontal: {},
        leftMenuVerticalLeft: {},
        leftMenuVerticalRight: {},
        leftLInkMain: {},
        leftLInkMainText: {},
        detailsContent: {},
        content: {},
        contentLoader: {},
        contentLoaderRightMenu: {},
    },
};
InfoBar.propTypes = {
    classes: PropTypes.shape({}),
    theme: PropTypes.object.isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

InfoBar.contextType = ApiContext;

export default injectIntl(withRouter(withStyles(styles, { withTheme: true })(InfoBar)));
