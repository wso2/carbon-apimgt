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
import Paper from '@material-ui/core/Paper';
import { FormattedMessage } from 'react-intl';
import IconButton from '@material-ui/core/IconButton';
import Icon from '@material-ui/core/Icon';
import { withStyles } from '@material-ui/core/styles';
import classNames from 'classnames';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
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
    content: {
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
    paper: {
        minWidth: theme.custom.tagWise.fixedStyles.width,
        width: theme.custom.tagWise.fixedStyles.width,
        background: theme.custom.tagWise.fixedStyles.background,
        color: theme.palette.getContrastText(theme.custom.tagWise.fixedStyles.background),
        margin: `${theme.spacing(2)}px ${theme.spacing(2)}px 0 0`,
    },
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
        const { listType } = this.state;
        const strokeColorMain = theme.palette.getContrastText(theme.palette.background.paper);

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
                } else if (splits.length > 1 && splits[0] === 'tag'){
                    selectedTag = splits[1];
                }
            }
        }
        return (
            <main className={classes.content}>
                <div className={classes.appBar}>
                    <div className={classes.mainIconWrapper}>
                        <CustomIcon strokeColor={strokeColorMain} width={42} height={42} icon='api' />
                    </div>
                    <div className={classes.mainTitleWrapper}>
                        <Typography variant='h4' className={classes.mainTitle}>
                            <FormattedMessage defaultMessage='APIs' id='Apis.Listing.Listing.apis.main' />
                        </Typography>
                        {apis && (
                            <Typography variant='caption' gutterBottom align='left'>
                                <FormattedMessage defaultMessage='Displaying' id='Apis.Listing.Listing.displaying' />
                                {apis.count}
                                <FormattedMessage defaultMessage='APIs' id='Apis.Listing.Listing.apis.count' />
                            </Typography>
                        )}
                    </div>
                    <div className={classes.buttonRight}>
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
                {active && <ApiBreadcrumbs selectedTag={selectedTag} />}
                <div className={classes.listContentWrapper}>
                    <Paper className={classes.paper}>
                        {active && style === 'fixed-left' && <TagCloudListingTags />}
                        {tagCloudActive && <ApiTagCloud />}
                    </Paper>
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
