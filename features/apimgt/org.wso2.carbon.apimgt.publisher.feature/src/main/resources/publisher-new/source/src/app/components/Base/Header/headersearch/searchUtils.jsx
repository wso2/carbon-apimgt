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

import API from 'AppData/api';
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
        classes, ref, isLoading, ...other
    } = inputProps; // `isLoading` has destructured here to prevent passing unintended prop to TextField
    return (
        <TextField
            InputProps={{
                inputRef: ref,
                className: classes.input,
                classes: { focused: classes.inputFocused },
                startAdornment: (
                    <InputAdornment position='start'>
                        <SearchOutlined />
                    </InputAdornment>
                ),
                ...other,
            }}
        />
    );
}

/**
 *
 * Use your imagination to define how suggestions are rendered.
 * @param {Object} suggestion This is an API object coming from APIS GET search API call
 * @param {Object} { query, isHighlighted } query : User entered value
 * @returns {React.Component} @inheritdoc
 */
function renderSuggestion(suggestion, { query, isHighlighted }) {
    const matches = match(suggestion.name, query);
    const parts = parse(suggestion.name, matches);
    const path = `/apis/${suggestion.id}/overview`;
    return (
        <React.Fragment>
            <Link to={path}>
                <MenuItem selected={isHighlighted} component='div'>
                    <APIsIcon />

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
 * Called for any input change to get the results set
 *
 * @param {String} value current value in input element
 * @returns {Promise} If no input text, return a promise which resolve to empty array, else return the API.all response
 */
function getSuggestions(value) {
    const inputValue = value.trim().toLowerCase();
    const inputLength = inputValue.length;
    const promisedAPIs = API.all({ query: { name: inputValue }, limit: 8 });
    return inputLength === 0 ? new Promise(resolve => resolve([])) : promisedAPIs;
}

export { renderInput, renderSuggestion, getSuggestions, getSuggestionValue };
