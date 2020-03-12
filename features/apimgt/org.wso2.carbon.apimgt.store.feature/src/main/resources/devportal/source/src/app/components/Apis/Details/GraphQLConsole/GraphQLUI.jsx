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

import React, {
    useState, useEffect, useRef, useContext,
} from 'react';
import GraphiQL from 'graphiql';
import fetch from 'isomorphic-fetch';
import 'graphiql/graphiql.css';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import Box from '@material-ui/core/Box';
import { FormattedMessage } from 'react-intl';
import GraphiQLExplorer from 'graphiql-explorer';
import { ApiContext } from '../ApiContext';
import Api from '../../../../data/api';

import Progress from '../../../Shared/Progress';

const { buildSchema } = require('graphql');

/**
 *
 * @param {*} props
 */
export default function GraphQLUI(props) {
    const {
        accessToken,
        authorizationHeader,
        URLs,
        securitySchemeType,
        username,
        password,
    } = props;
    const { api } = useContext(ApiContext);
    const [schema, setSchema] = useState(null);
    const [query, setQuery] = useState('');
    const [isExplorerOpen, setIsExplorerOpen] = useState(false);
    const graphiqlEl = useRef(null);
    useEffect(() => {
        const apiID = api.id;
        const apiClient = new Api();
        const promiseGraphQL = apiClient.getGraphQLSchemaByAPIId(apiID);

        promiseGraphQL
            .then((res) => {
                const graphqlSchemaObj = buildSchema(res.data);
                setSchema(graphqlSchemaObj);
            });
    }, []);

    const parameters = {};

    const handleToggleExplorer = () => {
        const newExplorerIsOpen = !isExplorerOpen;
        parameters.isExplorerOpen = newExplorerIsOpen;
        setIsExplorerOpen(newExplorerIsOpen);
    };

    /**
     *
     * @param {*} graphQLParams
     */
    function graphQLFetcher(graphQLParams) {
        let token;
        if (authorizationHeader === 'apikey') {
            token = accessToken;
        } else if (securitySchemeType === 'BASIC') {
            const credentials = username + ':' + password;
            token = 'Basic ' + btoa(credentials);
        } else {
            token = 'Bearer ' + accessToken;
        }
        return fetch((URLs.https), {
            method: 'post',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                [authorizationHeader]: token,
            },
            body: JSON.stringify(graphQLParams),
        }).then((response) => response.json());
    }
    if ({ schema } === null) {
        return <Progress />;
    } else {
        return (
            <>
                <Box width='30%' m={1}>
                    <TextField

                        label={(
                            <FormattedMessage
                                defaultMessage='Gateway URLs'
                                id='Apis.Details.GraphQLConsole.GraphQLUI.URLs'
                            />
                        )}
                        value={URLs.https}
                        name='selectedURL'
                        fullWidth
                        margin='normal'
                        variant='outlined'
                        InputProps={URLs.https}
                    />
                </Box>
                <div styles={{ width: '100%' }}>
                    <Box display='flex'>
                        <Box display='flex'>
                            <GraphiQLExplorer
                                schema={schema}
                                query={query}
                                onEdit={setQuery}
                                explorerIsOpen={isExplorerOpen}
                                onToggleExplorer={handleToggleExplorer}
                            />
                        </Box>
                        <Box display='flex' height='800px' flexGrow={1}>
                            <GraphiQL
                                ref={graphiqlEl}
                                fetcher={(graphQLFetcher)}
                                schema={schema}
                                query={query}
                                onEditQuery={setQuery}
                            >
                                <GraphiQL.Toolbar>
                                    <GraphiQL.Button
                                        onClick={() => graphiqlEl.current.handlePrettifyQuery()}
                                        label='Prettify'
                                        title='Prettify Query (Shift-Ctrl-P)'
                                    />
                                    <GraphiQL.Button
                                        onClick={() => graphiqlEl.current.handleToggleHistory()}
                                        label='History'
                                        title='Show History'
                                    />
                                    <GraphiQL.Button
                                        onClick={() => setIsExplorerOpen(!isExplorerOpen)}
                                        label='Explorer'
                                        title='Toggle Explorer'
                                    />
                                </GraphiQL.Toolbar>

                            </GraphiQL>
                        </Box>
                    </Box>
                </div>
            </>
        );
    }
}

GraphQLUI.propTypes = {
    classes: PropTypes.shape({
        paper: PropTypes.string.isRequired,
    }).isRequired,
};
