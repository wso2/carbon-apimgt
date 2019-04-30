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
import { withStyles } from '@material-ui/core/styles';
import API from 'AppData/api.js';
import { Progress } from 'AppComponents/Shared';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import SampleAPI from './SampleAPI/SampleAPI';
import CardView from './CardView/CardView';
import TableView from './TableView/TableView';
import TopMenu from './components/TopMenu';

const styles = theme => ({
    content: {
        flexGrow: 1,
    },
    contentInside: {
        maxWidth: theme.custom.contentAreaWidth,
        paddingLeft: theme.spacing.unit * 3,
        paddingTop: theme.spacing.unit * 2,
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
            apis: null,
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
     *
     * Switch the view between grid and list view
     * @param {String} value UUID(ID) of the deleted API
     * @memberof Listing
     */
    setListType = (value) => {
        this.setState({ listType: value });
    };
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
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof Listing
     */
    render() {
        const { apis, notFound, listType } = this.state;
        const { classes } = this.props;
        if (notFound) {
            return (
                <main className={classes.content}>
                    <TopMenu setListType={this.setListType} apis={apis} />
                    <div className={classes.contentInside}>
                        <ResourceNotFound />
                    </div>
                </main>
            );
        }
        if (!apis) {
            return (
                <main className={classes.content}>
                    <TopMenu setListType={this.setListType} apis={apis} />
                    <div className={classes.contentInside}>
                        <Progress />
                    </div>
                </main>
            );
        }
        if (apis.list.length === 0) {
            return (
                <main className={classes.content}>
                    <TopMenu setListType={this.setListType} apis={apis} />
                    <div className={classes.contentInside}>
                        <SampleAPI />
                    </div>
                </main>
            );
        }

        return (
            <main className={classes.content}>
                <TopMenu setListType={this.setListType} apis={apis} />
                <div className={classes.contentInside}>
                    {listType === 'grid' ? (
                        <CardView updateAPIsList={this.updateAPIsList} apis={apis} />
                    ) : (
                        <TableView updateAPIsList={this.updateAPIsList} apis={apis} />
                    )}
                </div>
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
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({
        custom: PropTypes.string,
    }).isRequired,
};

export default withStyles(styles, { withTheme: true })(Listing);
