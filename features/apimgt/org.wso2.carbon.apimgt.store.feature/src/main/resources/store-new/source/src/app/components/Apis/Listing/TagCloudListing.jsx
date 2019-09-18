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
import API from 'AppData/api';
import CONSTS from 'AppData/Constants';
import ApiTagCloud from 'AppComponents/Apis/Listing/ApiTagCloud';
import CustomIcon from '../../Shared/CustomIcon';

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
        width: theme.custom.contentAreaWidth,
    },
    root: {
        height: 70,
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        display: 'flex',
    },
    mainIconWrapper: {
        paddingTop: 13,
        paddingLeft: 35,
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
    },
});

/**
 * Shared listing page
 *
 * @class TagCloudListing
 * @extends {Component}
 */
class TagCloudListing extends React.Component {
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
        };
    }

    /**
     * @memberof TagCloudListing
     */
    componentDidMount() {
        const api = new API();
        const promisedTags = api.getAllTags();
        promisedTags
            .then((response) => {
                if (response.body.count !== 0) {
                    this.setState({ allTags: response.body.list });
                }
            })
            .catch((error) => {
                console.log(error);
            });
    }


    /**
     *
     * Switch the view between grid and list view
     * @param {String} value view type
     * @memberof TagCloudListing
     */
    setListType = (value) => {
        this.setState({ listType: value });
    };

    /**
     *
     * @inheritdoctheme
     * @returns {React.Component} @inheritdoc
     * @memberof TagCloudListing
     */
    render() {
        const {
            theme, classes,
        } = this.props;
        const { listType, allTags } = this.state;
        const apiType = CONSTS.API_TYPE;
        let apisTagCloudGroup;

        if (allTags !== null) {
            apisTagCloudGroup = allTags.filter(item => (theme.custom.tagWiseMode === true
                && item.value.split(theme.custom.tagGroupKey).length > 1));
        }
        const strokeColorMain = theme.palette.getContrastText(theme.palette.background.paper);

        return (
            <main className={classes.content}>
                <div className={classes.root}>
                    <div className={classes.mainIconWrapper}>
                        <CustomIcon strokeColor={strokeColorMain} width={42} height={42} icon='api' />
                    </div>
                    <div className={classes.mainTitleWrapper}>
                        <Typography variant='display1' className={classes.mainTitle}>
                            <FormattedMessage
                                defaultMessage='API Groups'
                                id='Apis.Listing.TagCloudListing.apigroups.main'
                            />
                        </Typography>
                        {apisTagCloudGroup && apisTagCloudGroup.tags && (
                            <Typography variant='caption' gutterBottom align='left'>
                                <FormattedMessage
                                    defaultMessage='Displaying'
                                    id='Apis.Listing.TagCloudListing.displaying'
                                />
                                {apisTagCloudGroup.tags.count}
                                <FormattedMessage
                                    defaultMessage='API Groups'
                                    id='Apis.Listing.TagCloudListing.apigroups.count'
                                />
                            </Typography>
                        )}
                    </div>
                    <div className={classes.buttonRight}>
                        <IconButton className={classes.button} onClick={() => this.setListType('list')}>
                            <Icon color={listType === 'list' ? 'primary' : 'default'}>list</Icon>
                        </IconButton>
                        <IconButton className={classes.button} onClick={() => this.setListType('grid')}>
                            <Icon color={listType === 'grid' ? 'primary' : 'default'}>grid_on</Icon>
                        </IconButton>
                    </div>
                </div>
                {(apisTagCloudGroup && apisTagCloudGroup.length > 0)
                    ? <ApiTagCloud data={apisTagCloudGroup} listType={listType} apiType={apiType} />
                    : (
                        <div className={classes.mainTitle}>
                            <Typography variant='subheading' gutterBottom align='center'>
                                <FormattedMessage
                                    defaultMessage='Tags Connot be Found'
                                    id='Apis.Listing.TagCloudListing.tagsNotFound'
                                />
                            </Typography>
                        </div>
                    )
                }
            </main>
        );
    }
}

TagCloudListing.propTypes = {
    classes: PropTypes.shape({
        listContentWrapper: PropTypes.shape({}).isRequired,
    }).isRequired,
    theme: PropTypes.shape({
        custom: PropTypes.shape({
            tagWiseMode: PropTypes.bool.isRequired,
            tagGroupKey: PropTypes.string.isRequired,
        }),
    }).isRequired,
};

export default withStyles(styles, { withTheme: true })(TagCloudListing);
