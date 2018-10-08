/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import qs from 'qs';
import PropTypes from 'prop-types';
import API from 'AppData/api.js';
import PageContainer from 'AppComponents/Base/container/';
import { Progress } from 'AppComponents/Shared';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import CustomIcon from '../../Shared/CustomIcon';

import PageNavigation from '../APIsNavigation';
import SampleAPI from './SampleAPI/SampleAPI';
import CardView from './CardView/CardView';
import TableView from './TableView/TableView';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import List from '@material-ui/icons/List';
import GridOn from '@material-ui/icons/GridOn';
import { FormattedMessage } from 'react-intl';
import APICreateMenu from './components/APICreateMenu';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import TopMenu from './components/TopMenu';

const styles = theme => ({
    rightIcon: {
        marginLeft: theme.spacing.unit
    },
    button: {
        margin: theme.spacing.unit,
        marginBottom: 0
    },
    buttonRight: {
        alignSelf: 'flex-end',
        display: 'flex',
    },
    ListingWrapper: {
        paddingTop: 10,
        paddingLeft: 35,
    },
    root: {
        height: 70,
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey['A200'],
        display: 'flex',
    },
    mainIconWrapper: {
        paddingTop: 13,
        paddingLeft: 35,
        paddingRight: 20,
    },
    mainTitle:{
        paddingTop: 10,
    },
    mainTitleWrapper: {
    },
    APICreateMenu: {
        flexGrow: 1,
        display: 'flex',
        alignItems: 'center',
    },
    content: {
        flexGrow: 1,
    },
});
/**
 * Render the APIs Listing page, This is the Default Publisher Landing page as well
 *
 * @class Listing
 * @extends {React.Component}
 */
class Listing extends React.Component {
    /**
     *Creates an instance of Listing.
     * @param {*} props
     * @memberof Listing
     */
    constructor(props) {
        super(props);
        this.state = { 
            isCardView: true,
             apis: null 
            };
        this.updateAPIsList = this.updateAPIsList.bind(this);
        this.updateApi = this.updateApi.bind(this);
        this.state.listType = this.props.theme.custom.defaultApiView;
    }

    /**
     *
     * @inheritdoc
     * @memberof Listing
     */
    componentDidMount() {
        API.all()
            .then((response) => {
                this.setState({ apis: response.obj });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    const params = qs.stringify({ reference: this.props.location.pathname });
                    this.props.history.push({ pathname: '/login', search: params });
                }
            });
    }

    /**
     * Update Sample API
     *
     * @param {String} apiUUID
     * @memberof Listing
     */
    updateApi(apiUUID) {
        const api = this.state.apis;
        for (const apiIndex in api.list) {
            if (api.list.apiIndex && api.list[apiIndex].id === apiUUID) {
                api.list.splice(apiIndex, 1);
                break;
            }
        }
        this.setState({ apis: api });
    }

    /**
     *
     * Update APIs list if an API get deleted in card or table view
     * @param {String} apiUUID UUID(ID) of the deleted API
     * @memberof Listing
     */
    updateAPIsList(apiUUID) {
        this.setState((currentState) => {
            const { apis } = currentState;
            for (const apiIndex in apis.list) {
                if (apis.list[apiIndex].id === apiUUID) {
                    apis.list.splice(apiIndex, 1);
                    this.setState({ apis });
                    break;
                }
            }
        });
    }
    /**
     *
     * Switch the view between grid and list view
     * @param {String} value UUID(ID) of the deleted API
     * @memberof Listing
     */
    setListType = (value) => {
        this.setState({ listType: value });
    }

    /**
     *
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof Listing
     */
    render() {
        const { apis, notFound, listType } = this.state;
        const { classes, theme } = this.props;
        const strokeColorMain = theme.palette.getContrastText(theme.palette.background.paper);
        if (notFound) {
            return (
                <PageContainer pageNav={<PageNavigation />}>
                    <ResourceNotFound />
                </PageContainer>
            );
        }
        if (!apis) {
            return (
                <PageContainer pageNav={<PageNavigation />}>
                    <Progress />
                </PageContainer>
            );
        }
        if (apis.list.length === 0) {
            return (
                <PageContainer pageNav={<PageNavigation />}>
                    <SampleAPI />
                </PageContainer>
            );
        }

        return (
            <main className={classes.content}>
                <TopMenu setListType={this.setListType} apis={apis}  />    
                { (listType === "grid")  ? (
                    <CardView updateAPIsList={this.updateAPIsList} apis={apis} />
                ) : (
                    <TableView updateAPIsList={this.updateAPIsList} apis={apis} />
                )}
            </main>
        );
    }
}

Listing.propTypes = {
    history: PropTypes.shape({
        push: PropTypes.func,
    }).isRequired,
    location: PropTypes.shape({
        pathname: PropTypes.string,
    }).isRequired,
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(Listing);
