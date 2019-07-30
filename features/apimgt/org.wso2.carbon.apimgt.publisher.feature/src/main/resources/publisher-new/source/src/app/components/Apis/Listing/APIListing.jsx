/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';
import API from 'AppData/api.js';
import Listing from './Listing';

/**
 * Wrapper for API listing
 */
class APIListing extends Component {
    /**
     * Constructor
     * @param {*} props
     */
    constructor(props) {
        super(props);
        this.state = {
            apis: null,
        };
        this.updateAPIsList = this.updateAPIsList.bind(this);
        this.updateApi = this.updateApi.bind(this);
    }
    /**
     * @inheritDoc
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
                    doRedirectToLogin();
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
        const { apis } = this.state;
        for (const apiIndex in apis.list) {
            if (apis.list.apiIndex && apis.list[apiIndex].id === apiUUID) {
                apis.list.splice(apiIndex, 1);
                break;
            }
        }
        this.setState({ apis });
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
     * @inheritDoc
     */
    render() {
        const { apis, notFound, listType } = this.state;
        return (<Listing
            apis={apis}
            notFound={notFound}
            listType={listType}
            updateAPIsList={this.updateAPIsList}
        />);
    }
}

APIListing.propTypes = {
    history: PropTypes.shape({
        push: PropTypes.func,
    }).isRequired,
    location: PropTypes.shape({
        pathname: PropTypes.string,
    }).isRequired,
    classes: PropTypes.shape({
        content: PropTypes.string,
        contentInside: PropTypes.string,
    }).isRequired,
    theme: PropTypes.shape({
        custom: PropTypes.string,
    }).isRequired,
};

export default APIListing;
