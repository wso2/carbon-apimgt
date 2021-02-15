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

import React, { useContext } from 'react';

const APIContext = React.createContext({
    api: null,
    tenantList: [],
});

const { Provider: APIProvider } = APIContext;

/**
 * withAPI HOC can be used with class style components, To get the context with hooks useContext,
 * use the default export. Using hooks is preferred over class components due to its contribution to wrapper hell
 *
 * @param {*} WrappedComponent
 * @returns {React.Component} withAPI HOC
 */
function withAPI(WrappedComponent) {
    /**
     *
     * Higher order component which passes the API context to its child component
     * @param {*} props
     * @returns
     */
    function HOCWithAPI(props) {
        return <APIContext.Consumer>{(context) => <WrappedComponent {...context} {...props} />}</APIContext.Consumer>;
    }
    HOCWithAPI.displayName = `withAPI(${WrappedComponent.displayName})`;
    return HOCWithAPI;
}

/**
 * Provide current api object and method updateAPI function to update it. To be used with hooks
 *
 * @returns {Array} Multiple return values
 */
function useAPI() {
    const { api, updateAPI } = useContext(APIContext);
    return [api, updateAPI];
}

export default APIContext;

export {
    withAPI, APIProvider, APIContext, useAPI,
};
