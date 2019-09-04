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
import ApiTableView from './ApiTableView';
import { ApiContext } from '../Details/ApiContext';

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
        };
    }

    /**
     * @memberof CommonListing
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
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            });
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
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof CommonListing
     */
    render() {
        const {
            apis, apiType, theme, classes, location: { search },
        } = this.props;
        const { listType, allTags } = this.state;
        const strokeColorMain = theme.palette.getContrastText(theme.palette.background.paper);

        return (
            <main className={classes.content}>
                <div className={classes.root}>
                    <div className={classes.mainIconWrapper}>
                        <CustomIcon strokeColor={strokeColorMain} width={42} height={42} icon='api' />
                    </div>
                    <div className={classes.mainTitleWrapper}>
                        <Typography variant='display1' className={classes.mainTitle}>
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
                            <Icon color={listType === 'list' ? 'primary' : 'default'}>list</Icon>
                        </IconButton>
                        <IconButton className={classes.button} onClick={() => this.setListType('grid')}>
                            <Icon color={listType === 'grid' ? 'primary' : 'default'}>grid_on</Icon>
                        </IconButton>
                    </div>
                </div>
                {(allTags && apiType === CONSTS.API_TYPE)
                    ? <ApiTagCloud data={allTags} listType={listType} apiType={apiType} />
                    : (
                        <div className={classes.listContentWrapper}>
                            {listType === 'grid'
                            && (
                                <ApiContext.Provider value={{ apiType }}>
                                    <ApiTableView gridView query={search} />
                                </ApiContext.Provider>
                            )}
                            {listType === 'list'
                            && (
                                <ApiContext.Provider value={{ apiType }}>
                                    <ApiTableView gridView={false} query={search} />
                                </ApiContext.Provider>
                            )}
                        </div>
                    )
                }
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
