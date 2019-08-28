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

import React from 'react';
import match from 'autosuggest-highlight/match';
import parse from 'autosuggest-highlight/parse';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import Divider from '@material-ui/core/Divider';
import InputAdornment from '@material-ui/core/InputAdornment';
import SearchOutlined from '@material-ui/icons/SearchOutlined';
import { Link } from 'react-router-dom';
import APIsIcon from '@material-ui/icons/SettingsApplicationsOutlined';
import DocumentsIcon from '@material-ui/icons/LibraryBooks';

import API from 'AppData/api';
import SearchParser from './SearchParser';
/* Utility methods defined here are described in
 * react-autosuggest documentation https://github.com/moroshko/react-autosuggest
 */

/**
 *
 * @param {Object} inputProps Props given for the underline input element
 * @returns {React.Component} @inheritdoc
 */
function renderInput(inputProps) {
    const {
        classes, ref, isLoading, onChange, ...other
    } = inputProps; // `isLoading` has destructured here to prevent passing unintended prop to TextField
    return (
        <TextField
            id='searchQuery'
            InputProps={{
                inputRef: ref,
                className: classes.input,
                classes: { focused: classes.inputFocused },
                startAdornment: (
                    <InputAdornment position='start'>
                        <SearchOutlined />
                    </InputAdornment>
                ),
                onChange,
                ...other,
            }}
        />
    );
}

/**
 *
 * Use your imagination to define how suggestions are rendered.
 * @param {Object} suggestion This is either API object or document coming from search API call
 * @param {Object} { query, isHighlighted } query : User entered value
 * @returns {React.Component} @inheritdoc
 */
function renderSuggestion(suggestion, { query, isHighlighted }) {
    const matches = match(suggestion.name, query);
    const parts = parse(suggestion.name, matches);
    const path =
        suggestion.type === 'API'
            ? `/apis/${suggestion.id}/overview`
            : `/apis/${suggestion.apiUUID}/documents/${suggestion.id}/details`;
    // TODO: Style the version ( and apiName if docs) apearing in the menu item
    const suffix = suggestion.type === 'API' ? suggestion.version : suggestion.apiName + ' ' + suggestion.apiVersion;
    return (
        <React.Fragment>
            <Link to={path}>
                <MenuItem selected={isHighlighted} component='div'>
                    {suggestion.type === 'API' ? <APIsIcon /> : <DocumentsIcon />}

                    {parts.map((part, index) => {
                        return part.highlight ? (
                            <span key={String(index)} style={{ fontWeight: 500 }}>
                                {part.text}
                            </span>
                        ) : (
                            <strong key={String(index)} style={{ fontWeight: 300 }}>
                                {part.text}
                            </strong>
                        );
                    })}
                    <pre />
                    <pre />
                    {suffix}
                </MenuItem>
            </Link>
            <Divider />
        </React.Fragment>
    );
}

/**
 * When suggestion is clicked, Autosuggest needs to populate the input
 * based on the clicked suggestion. Teach Autosuggest how to calculate the input value for every given suggestion.
 *
 * @param {Object} suggestion API Object returned from APIS search api.list[]
 * @returns {String} API Name
 */
function getSuggestionValue(suggestion) {
    return suggestion.name;
}

/**
 * Build the search query from the user input
 * @param searchText
 * @returns {string}
 */
function buildSearchQuery(searchText) {
    const inputValue = searchText.trim().toLowerCase();
    return SearchParser.parse(inputValue);
}

/**
 * Called for any input change to get the results set
 *
 * @param {String} value current value in input element
 * @returns {Promise} If no input text, return a promise which resolve to empty array, else return the API.all response
 */
function getSuggestions(value) {
    const modifiedSearchQuery = buildSearchQuery(value);

    if (value.trim().length === 0 || !modifiedSearchQuery) {
        return new Promise(resolve => resolve({ obj: { list: [] } }));
    } else {
        return API.search({ query: modifiedSearchQuery, limit: 8 });
    }
}

export { renderInput, renderSuggestion, getSuggestions, getSuggestionValue, buildSearchQuery };
