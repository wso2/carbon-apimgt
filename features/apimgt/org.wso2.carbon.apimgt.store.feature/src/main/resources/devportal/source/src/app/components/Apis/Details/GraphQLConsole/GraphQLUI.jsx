import React, {
    useState, useEffect, useContext, useRef,
} from 'react';
import GraphiQL from 'graphiql';
import fetch from 'isomorphic-fetch';
import 'graphiql/graphiql.css';
import './explorer.css';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import { FormattedMessage } from 'react-intl';
import GraphiQLExplorer from 'graphiql-explorer';
import Api from 'AppData/api';
import Box from '@material-ui/core/Box';
import { ApiContext } from '../ApiContext';
import Progress from '../../../Shared/Progress';


const { buildSchema } = require('graphql');


export default function GraphQLUI(props) {
    const { accessToken, authorizationHeader, URLss } = props;

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
        } else {
            token = 'Bearer ' + accessToken;
        }
        return fetch((URLss.https), {
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
                        value={URLss.https}
                        name='selectedURL'
                        fullWidth
                        margin='normal'
                        variant='outlined'
                        InputProps={URLss.https}
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
                            // onRunOperation={operationName =>
                            //     this._graphiql.handleRunQuery(operationName)
                            // }
                            />
                        </Box>
                        <Box display='flex' height='800px' flexGrow={1}>
                            <GraphiQL
                                ref={graphiqlEl}
                                fetcher={(graphQLFetcher)}
                                schema={schema}
                                query={query}
                                // variables={variables}
                                onEditQuery={setQuery}
                                // onEditVariables={onEditVariables}
                                // onEditOperationName={onEditOperationName}

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
