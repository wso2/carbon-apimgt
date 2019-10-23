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
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import IconButton from '@material-ui/core/IconButton';
import Icon from '@material-ui/core/Icon';
import { withStyles } from '@material-ui/core/styles';
import classNames from 'classnames';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import API from 'AppData/api';
import ApiBreadcrumbs from './ApiBreadcrumbs';
import ApiTableView from './ApiTableView';
import { ApiContext } from '../Details/ApiContext';
import TagCloudListingTags from './TagCloudListingTags';
import ApiTagCloud from './ApiTagCloud';

const styles = theme => ({
    rightIcon: {
        marginLeft: theme.spacing.unit,
    },
    button: {
        margin: theme.spacing.unit,
        marginBottom: 0,
    },
    buttonRight: {
        alignSelf: 'flex-end',
        display: 'flex',
    },
    ListingWrapper: {
        paddingTop: 10,
        paddingLeft: 35,
        maxWidth: theme.custom.contentAreaWidth,
    },
    appBar: {
        height: 70,
        background: theme.custom.infoBar.background,
        color: theme.palette.getContrastText(theme.custom.infoBar.background),
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
    },
    mainIconWrapper: {
        paddingTop: 13,
        paddingLeft: 20,
        paddingRight: 20,
    },
    mainTitle: {
        paddingTop: 10,
    },
    mainTitleWrapper: {
        flexGrow: 1,
    },
    listContentWrapper: {
        padding: `0 ${theme.spacing.unit * 3}px`,
        display: 'flex',
    },
    iconDefault: {
        color: theme.palette.getContrastText(theme.custom.infoBar.background),
    },
    iconSelected: {
        color: theme.custom.infoBar.listGridSelectedColor,
    },
    content: {
        flexGrow: 1,
        display: 'flex',
        flex: 1,
        flexDirection: 'column',
        paddingBottom: theme.spacing.unit * 3,
    },
    contentWithTags: {
        marginLeft: theme.custom.tagCloud.leftMenu.width,
    },
    contentWithoutTags: {
        marginLeft: 0,
    },
    contentWithTagsHidden: {
        marginLeft: theme.custom.tagCloud.leftMenu.sliderWidth,
    },
    LeftMenu: {
        backgroundColor: theme.custom.tagCloud.leftMenu.background,
        color: theme.custom.tagCloud.leftMenu.color,
        textAlign: 'left',
        fontFamily: theme.typography.fontFamily,
        position: 'absolute',
        bottom: 0,
        paddingLeft: 0,
        width: theme.custom.tagCloud.leftMenu.width,
        top: 0,
        left: 0,
        overflowY: 'auto',
    },
    LeftMenuForSlider: {
        backgroundColor: theme.custom.tagCloud.leftMenu.background,
        color: theme.custom.tagCloud.leftMenu.color,
        textAlign: 'left',
        fontFamily: theme.typography.fontFamily,
        position: 'absolute',
        bottom: 0,
        paddingLeft: 0,
        width: theme.custom.tagCloud.leftMenu.sliderWidth,
        top: 0,
        left: 0,
        overflowY: 'auto',
        display: 'flex',
    },
    sliderButton: {
        fontWeight: 200,
        background: theme.custom.tagCloud.leftMenu.sliderBackground,
        color: theme.palette.getContrastText(theme.custom.tagCloud.leftMenu.sliderBackground),
        height: theme.custom.infoBar.height,
        alignItems: 'center',
        display: 'flex',
        position: 'absolute',
        right: 0,
        cursor: 'pointer',
    },
    rotatedText: {
        transform: 'rotate(90deg)',
        transformOrigin: 'left bottom 0',
        position: 'absolute',
        whiteSpace: 'nowrap',
        top: theme.custom.infoBar.height,
        marginLeft: 4,
        cursor: 'pointer',
    }
});

/**
 * Shared listing page
 *
 * @class CommonListing
 * @extends {Component}
 */
class CommonListing extends React.Component {
    /**
     * Constructor
     *
     * @param {*} props Properties
     */
    constructor(props) {
        super(props);
        this.state = {
            listType: props.theme.custom.defaultApiView,
            allTags: null,
            showLeftMenu: false,
        };
    }

    /**
     *
     * Switch the view between grid and list view
     * @param {String} value view type
     * @memberof CommonListing
     */
    setListType = (value) => {
        this.setState({ listType: value });
    };
    /**
     *
     * Get all tags
     * @memberof CommonListing
     */
    componentDidMount() {
        const restApiClient = new API();
        const promisedTags = restApiClient.getAllTags();
        promisedTags
            .then((response) => {
                this.setState({ allTags: response.body.list });
            })
            .catch((error) => {
                console.log(error);
            });
    }
    toggleLeftMenu = () => {
        this.setState(prevState => ({ showLeftMenu: !prevState.showLeftMenu }));
    };
    /**
     *
     * @inheritdoctheme
     * @returns {React.Component} @inheritdoc
     * @memberof CommonListing
     */
    render() {
        const {
            apis,
            apiType,
            theme,
            classes,
            location: { search },
        } = this.props;
        const {
            custom: {
                tagWise: { key, active, style },
                tagCloud: { active: tagCloudActive },
            },
        } = theme;
        const { listType, allTags, showLeftMenu } = this.state;
        const strokeColorMain = theme.custom.tagCloud.leftMenu.background;
        const searchParam = new URLSearchParams(search);
        const searchQuery = searchParam.get('query');
        let selectedTag = null;
        if (search && searchQuery !== null) {
            // For the tagWise search
            if (active && key) {
                const splits = searchQuery.split(':');
                if (splits.length > 1 && splits[1].search(key) != -1) {
                    const splitTagArray = splits[1].split(key);
                    if (splitTagArray.length > 0) {
                        selectedTag = splitTagArray[0];
                    }
                } else if (splits.length > 1 && splits[0] === 'tag') {
                    selectedTag = splits[1];
                }
            }
        }
        const tagPaneVisible = allTags && allTags.length > 0 && (tagCloudActive || active);
        return (
            <React.Fragment>
                {tagPaneVisible && showLeftMenu && (
                    <div className={classes.LeftMenu}>
                        <div className={classes.sliderButton} onClick={this.toggleLeftMenu}>
                            <Icon>keyboard_arrow_left</Icon>
                        </div>
                        {active && <TagCloudListingTags allTags={allTags} />}
                        {tagCloudActive && <ApiTagCloud allTags={allTags} />}
                    </div>
                )}
                {tagPaneVisible && !showLeftMenu && (
                    <div className={classes.LeftMenuForSlider}>
                        <div className={classes.sliderButton} onClick={this.toggleLeftMenu}>
                            <Icon>keyboard_arrow_right</Icon>
                        </div>
                        <div className={classes.rotatedText} onClick={this.toggleLeftMenu}>
                                <FormattedMessage
                                    defaultMessage='Tag Cloud'
                                    id='Apis.Listing.Listing.ApiTagCloud.title'
                                />
                            </div>
                    </div>
                )}

                <main
                    className={classNames(
                        classes.content,
                        { [classes.contentWithoutTags]: !tagPaneVisible || !showLeftMenu },
                        { [classes.contentWithTagsHidden]: tagPaneVisible && !showLeftMenu },
                        { [classes.contentWithTags]: tagPaneVisible && showLeftMenu },
                    )}
                    id='commonListing'
                >
                    <div className={classes.appBar} id='commonListingAppBar'>
                        <div className={classes.mainIconWrapper}>
                            <CustomIcon strokeColor={strokeColorMain} width={42} height={42} icon='api' />
                        </div>
                        <div className={classes.mainTitleWrapper} id='mainTitleWrapper'>
                            <Typography variant='h4' className={classes.mainTitle}>
                                <FormattedMessage defaultMessage='APIs' id='Apis.Listing.Listing.apis.main' />
                            </Typography>
                            {apis && (
                                <Typography variant='caption' gutterBottom align='left' id='apiCountDisplay'>
                                    <FormattedMessage
                                        defaultMessage='Displaying'
                                        id='Apis.Listing.Listing.displaying'
                                    />
                                    {apis.count}
                                    <FormattedMessage defaultMessage='APIs' id='Apis.Listing.Listing.apis.count' />
                                </Typography>
                            )}
                        </div>
                        <div className={classes.buttonRight} id='listGridWrapper'>
                            <IconButton className={classes.button} onClick={() => this.setListType('list')}>
                                <Icon
                                    className={classNames(
                                        { [classes.iconSelected]: listType === 'list' },
                                        { [classes.iconDefault]: listType === 'grid' },
                                    )}
                                >
                                    list
                                </Icon>
                            </IconButton>
                            <IconButton className={classes.button} onClick={() => this.setListType('grid')}>
                                <Icon
                                    className={classNames(
                                        { [classes.iconSelected]: listType === 'grid' },
                                        { [classes.iconDefault]: listType === 'list' },
                                    )}
                                >
                                    grid_on
                                </Icon>
                            </IconButton>
                        </div>
                    </div>
                    {active && allTags && allTags.length > 0 && <ApiBreadcrumbs selectedTag={selectedTag} />}
                    <div className={classes.listContentWrapper}>
                        {listType === 'grid' && (
                            <ApiContext.Provider value={{ apiType }}>
                                <ApiTableView gridView query={search} />
                            </ApiContext.Provider>
                        )}
                        {listType === 'list' && (
                            <ApiContext.Provider value={{ apiType }}>
                                <ApiTableView gridView={false} query={search} />
                            </ApiContext.Provider>
                        )}
                    </div>
                </main>
            </React.Fragment>
        );
    }
}

CommonListing.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    apiType: PropTypes.string.isRequired,
    apis: PropTypes.shape({}).isRequired,
    location: PropTypes.shape({
        search: PropTypes.string,
    }),
};

CommonListing.defaultProps = {
    location: PropTypes.shape({
        search: '',
    }),
};

export default withStyles(styles, { withTheme: true })(CommonListing);
