/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Paper from '@material-ui/core/Paper';
import Box from '@material-ui/core/Box';
import deburr from 'lodash/deburr';
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';

import suggestions from 'AppComponents/Apis/Details/GoTo/RouteMappings';
import GoToSuggestion from 'AppComponents/Apis/Details/GoTo/Components/GoToSuggestion';

const useStyles = makeStyles((theme) => ({
    paper: {
        position: 'absolute',
        zIndex: theme.zIndex.goToSearch,
        marginTop: theme.spacing(2),
        padding: theme.spacing(1),
        left: 0,
        right: 0,
    },
}));

/**
 * Method to retrieve suggestions
 * @param {*} value Value of suggestion
 * @param {*} isAPIProduct Boolean to check if it is an APIProduct
 * @param {*} param2 showEmpty
 * @returns {*} filter
 */
function getSuggestions(value, isAPIProduct, isGraphQL, { showEmpty = false } = {}) {
    const inputValue = deburr(value.trim()).toLowerCase();
    const inputLength = inputValue.length;
    let count = 0;
    const newSuggestions = [...suggestions.common];

    if (isAPIProduct) {
        newSuggestions.push(...suggestions.productOnly);
    } else if (isGraphQL) {
        newSuggestions.push(...suggestions.graphqlOnly);
    } else {
        newSuggestions.push(...suggestions.apiOnly);
    }

    return inputLength === 0 && !showEmpty
        ? []
        : newSuggestions.filter((suggestion) => {
            const keep = count < 5 && suggestion.label.slice(0, inputLength).toLowerCase() === inputValue;

            if (keep) {
                count += 1;
            }

            return keep;
        });
}


const GoToSuggestions = (props) => {
    const {
        inputValue, isAPIProduct, isGraphQL, getItemProps, highlightedIndex, selectedItem, handleClickAway, apiId,
    } = props;
    const classes = useStyles();
    const currentSuggestions = getSuggestions(inputValue, isAPIProduct, isGraphQL);
    return (
        <Paper className={classes.paper} square>
            {currentSuggestions.length > 0
                ? currentSuggestions.map((suggestion, index) => (
                    <GoToSuggestion
                        suggestion={suggestion}
                        index={index}
                        itemProps={getItemProps({ item: suggestion.label })}
                        highlightedIndex={highlightedIndex}
                        selectedItem={selectedItem}
                        handleClickAway={handleClickAway}
                        apiId={apiId}
                    />
                ))
                : (
                    <Box
                        m={3}
                        color='text.secondary'
                        fontSize='h5.fontSize'
                        fontFamily='fontFamily'
                        display='flex'
                    >
                        <FormattedMessage
                            id='Apis.Details.GoTo.Components.GoToSuggestions.no.result'
                            defaultMessage='There are no results for this query'
                        />
                    </Box>
                )}
        </Paper>
    );
};


export default GoToSuggestions;
