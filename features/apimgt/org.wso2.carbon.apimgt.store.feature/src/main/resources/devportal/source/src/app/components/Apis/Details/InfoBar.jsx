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
import Icon from '@material-ui/core/Icon';
import { Link, withRouter } from 'react-router-dom';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
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
import Breadcrumb from './Breadcrumb';

const propertyDisplaySuffix = Settings.app.propertyDisplaySuffix || '__display';
/**
 *
 *
 * @param {*} theme
 */
const styles = (theme) => {
    const mainBack = theme.custom.infoBar.background || '#ffffff';
    const infoBarHeight = theme.custom.infoBar.height || 170;
    const starColor = theme.custom.infoBar.starColor || theme.palette.getContrastText(mainBack);

    return {
        table: {
            minWidth: '100%',
        },
        root: {
            minHeight: infoBarHeight,
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
        endpointLabel: {
            display: 'inline-block',
            marginRight: theme.spacing(),
            fontWeight: 500,
        },
        infoWrapper: {
            display: 'flex',
            alignItems: 'center',
        },
        avatarRoot: {
            width: 30,
            height: 30,
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
            descriptionHidden: true,
        };
        this.getSchema = this.getSchema.bind(this);
        this.getProvider = this.getProvider.bind(this);
        this.setRatingUpdate = this.setRatingUpdate.bind(this);
        this.collapseAllDescription = this.collapseAllDescription.bind(this);
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

    collapseAllDescription(e) {
        e.preventDefault();
        this.setState({descriptionHidden: !this.state.descriptionHidden});
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
            notFound, avgRating, total, count, descriptionHidden,
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
        const { additionalProperties, securityScheme } = api;
        let additionalProperties__display = null;
        if (additionalProperties && Object.keys(additionalProperties).length > 0 && additionalProperties.constructor === Object) {
            additionalProperties__display = Object.keys(additionalProperties).filter(aProp => aProp.indexOf(propertyDisplaySuffix) !== -1);
        }

        let securityScheme_display = null;
        if (securityScheme) {
            securityScheme_display = [];
            securityScheme.forEach((scm) => {
                if (scm === 'basic_auth') {
                    securityScheme_display.push(
                        intl.formatMessage({
                            defaultMessage: 'Basic',
                            id: 'Apis.Details.InfoBar.security.basic',
                        })
                    );
                } else if (scm === 'api_key') {
                    securityScheme_display.push(
                        intl.formatMessage({
                            defaultMessage: 'Api Key',
                            id: 'Apis.Details.InfoBar.security.api_key',
                        })
                    );
                } else if (scm === 'oauth2') {
                    securityScheme_display.push(
                        intl.formatMessage({
                            defaultMessage: 'OAuth2',
                            id: 'Apis.Details.InfoBar.security.oauth2',
                        })
                    );
                }
            })
        }
        // Truncating the description
        let descriptionIsBig = false;
        let smallDescription = '';
        if(api.description){
            const limit = 40;
            if(api.description.split(' ').length > limit){
                let newContent = api.description.split(' ').slice(0, limit);
                smallDescription = newContent.join(' ') + '...';
                descriptionIsBig = true;
            }
        }


        const { resourceNotFountMessage } = this.props;
        const user = AuthManager.getUser();
        if (notFound) {
            return <ResourceNotFound message={resourceNotFountMessage} />;
        }

        return (
            <div className={classes.infoBarMain} id='infoBar'>
                <Breadcrumb />
                <div className={classes.root}>
                    {showThumbnail && (
                        <Box>
                            <ApiThumb api={api} customWidth={70} customHeight={50} showInfo={false} />
                        </Box>
                    )}
                    <Box display='flex' flexDirection='column'>
                        <Box display='flex' flexDirection='row' alignItems='center' pt={1}>
                            <Box ml={1} mr={2}>
                                <Link to={'/apis/' + api.id + '/overview'} className={classes.linkTitle}>
                                    <Typography variant='h4'>{api.name}</Typography>
                                </Link>
                            </Box>
                            {!api.advertiseInfo.advertised && user && showRating && (
                                <>  
                                    <StarRatingSummary avgRating={avgRating} reviewCount={total} returnCount={count} />
                                    <VerticalDivider height={30} />
                                    <StarRatingBar
                                        apiId={api.id}
                                        isEditable
                                        showSummary={false}
                                        setRatingUpdate={this.setRatingUpdate}
                                    />
                                </>
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
                        </Box>
                        <Box display='flex' flexDirection='row' alignItems='center' pl={1}>
                            <Grid container spacing={2}>
                                <Grid item xl={2} className={classes.infoWrapper}>
                                    <Typography variant='body2' className={classes.endpointLabel}>
                                        <FormattedMessage
                                            id='Apis.Details.InfoBar.list.endpoint'
                                            defaultMessage='Endpoint'
                                        />
                                    </Typography>
                                    <Environments renderOnlyOne />
                                </Grid>

                                {/* User info */}
                                <Grid item xl={2} className={classes.infoWrapper}>
                                    <Icon className={classes.iconEven}>person</Icon>
                                    <Box display='flex' flexDirection='column' pl={1}>
                                        <Typography variant='body2' gutterBottom align='left'>
                                            {this.getProvider(api)}
                                        </Typography>
                                        <Typography variant='body2' className={classes.endpointLabel}>
                                            {api.businessInformation.businessOwnerEmail}
                                        </Typography>
                                    </Box>
                                </Grid>

                                {/* Context */}
                                <Grid item xl={2} className={classes.infoWrapper}>
                                    <Icon className={classes.iconOdd}>account_balance_wallet</Icon>
                                    <Box display='flex' flexDirection='column' pl={1}>
                                        <Typography variant='body2' gutterBottom align='left'>
                                            <FormattedMessage
                                                id='Apis.Details.InfoBar.list.context'
                                                defaultMessage='Context'
                                            />
                                        </Typography>
                                        <Typography variant='body2' className={classes.endpointLabel}>
                                            {api.context}
                                        </Typography>
                                    </Box>
                                </Grid>

                                {/* Version */}
                                <Grid item xl={2} className={classes.infoWrapper}>
                                    <Icon className={classes.iconEven}>settings_input_component</Icon>
                                    <Box display='flex' flexDirection='column' pl={1}>
                                        <Typography variant='body2' gutterBottom align='left'>
                                            <FormattedMessage
                                                id='Apis.Details.InfoBar.list.version'
                                                defaultMessage='Version'
                                            />
                                        </Typography>
                                        <Typography variant='body2' className={classes.endpointLabel}>
                                            {api.version}
                                        </Typography>
                                    </Box>
                                </Grid>

                                {/* Key Managers */}
                                <Grid item xl={2} className={classes.infoWrapper}>
                                    <Icon className={classes.iconOdd}>vpn_key</Icon>
                                    <Box display='flex' flexDirection='column' pl={1}>
                                        <Typography variant='body2' gutterBottom align='left'>
                                            <FormattedMessage
                                                id='Apis.Details.InfoBar.keyManagers'
                                                defaultMessage='Key Managers'
                                            />
                                        </Typography>
                                        <Typography variant='body2' className={classes.endpointLabel}>
                                            {this.getKeyManagers(api)}
                                        </Typography>
                                    </Box>
                                </Grid>

                                {/* Tags */}
                                <Grid item xl={2} className={classes.infoWrapper}>
                                    <Icon className={classes.iconEven}>bookmark</Icon>
                                    <Box display='flex' flexDirection='column' pl={1}>
                                        <Typography variant='body2' gutterBottom align='left'>
                                            <FormattedMessage
                                                id='Apis.Details.InfoBar.list.tags'
                                                defaultMessage='Tags'
                                            />
                                        </Typography>
                                        <Typography variant='body2' className={classes.endpointLabel}>
                                            {apisTagsWithoutGroups.map((tag, index) => (
                                                <Link to={`/apis?offset=0&query=tag:${tag}`} style={{ lineHeight: 0 }}>
                                                    {tag} {(index + 1) !== apisTagsWithoutGroups.length && ', '}
                                                </Link>
                                            ))}
                                            {apisTagsWithoutGroups.length === 0 && (<FormattedMessage
                                                id='Apis.Details.InfoBar.list.tags.not'
                                                defaultMessage='Not Tagged'
                                            />)}
                                        </Typography>

                                    </Box>
                                </Grid>

                                {api.description &&(<Grid item xs={12} className={classes.infoWrapper}>
                                    <Typography variant='body2' gutterBottom align='left'>
                                        {(descriptionIsBig && descriptionHidden) ? smallDescription : api.description}
                                        {descriptionIsBig && (<a onClick={this.collapseAllDescription} href='#'>
                                            {descriptionHidden ? 'more' : 'less'}
                                            </a>)}
                                    </Typography>
                                </Grid>)}
                            </Grid>
                        </Box>
                    </Box>
                </div>
                {position === 'horizontal' && <div style={{ height: 60 }} />}
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
