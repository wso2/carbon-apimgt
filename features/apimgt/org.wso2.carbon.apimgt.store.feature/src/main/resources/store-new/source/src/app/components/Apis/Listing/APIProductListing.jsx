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

import APIProduct from 'AppData/APIProduct';
import React from 'react';
import qs from 'qs';
import CommonListing from './CommonListing';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';


/**
 * API Product listing page
 *
 * @class APIProductListing
 * @extends {Component}
 */
class APIProductListing extends React.Component {
    /**
     * Constructor
     *
     * @param {*} props Properties
     */
    constructor(props) {
        super(props);
        this.state = {
            apis: null,
            path: props.match.path,
        };
    }

    /**
     * @inheritdoc
     * @memberof APIProductListing
     */
    componentDidMount() {
        const apiProduct = new APIProduct();
        const promisedApis = apiProduct.getAllAPIProducts();
        promisedApis
            .then((response) => {
                this.setState({ apis: response.obj });
            })
            .catch((error) => {
                const { status } = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    this.setState({ isAuthorize: false });
                    const params = qs.stringify({ reference: this.props.location.pathname });
                    this.props.history.push({ pathname: '/login', search: params });
                }
            });
    }

    /**
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof APIProductListing
     */
    render() {
        const { notFound } = this.state;

        if (notFound) {
            return <ResourceNotFound />;
        }

        const { apis, path } = this.state;

        return <CommonListing apis={apis} path={path} />;
    }
}

export default (APIProductListing);
