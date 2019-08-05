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
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';
import PropTypes from 'prop-types';
import APIProduct from 'AppData/APIProduct';
import Listing from './Listing';

/**
 * Wrapper for API product listing
 */
class APIProductListing extends Component {
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
    }
    /**
     * @inheritDoc
     */
    componentDidMount() {
        APIProduct.all()
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

APIProductListing.propTypes = {
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

export default APIProductListing;
