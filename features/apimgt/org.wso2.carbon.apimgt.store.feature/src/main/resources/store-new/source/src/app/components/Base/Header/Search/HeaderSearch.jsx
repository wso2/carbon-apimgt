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

import React from 'react';
import { Redirect, withRouter } from 'react-router-dom';
import PropTypes from 'prop-types';
import Autosuggest from 'react-autosuggest';
import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import CircularProgress from '@material-ui/core/CircularProgress';
import { injectIntl } from 'react-intl';

import {
    renderInput, renderSuggestion, getSuggestions, getSuggestionValue, buildSearchQuery,
} from './SearchUtils';

const styles = theme => ({
    container: {
        flexGrow: 2,
    },
    smContainer: {
        position: 'absolute',
    },
    suggestionsContainerOpen: {
        display: 'block',
        position: 'absolute',
        width: '535px',
        zIndex: theme.zIndex.modal + 1,
    },
    suggestion: {
        display: 'block',
    },
    suggestionsList: {
        margin: 0,
        padding: 0,
        listStyleType: 'none',
    },
    input: {
        width: '300px',
        background: theme.palette.getContrastText(theme.palette.background.appBar),
        '-webkit-transition': 'all .35s ease-in-out',
        transition: 'all .35s ease-in-out',
        padding: '5px 5px 5px 5px',
    },
    inputFocused: {
        width: '400px',
        background: theme.palette.getContrastText(theme.palette.background.appBar),
        padding: '5px 5px 5px 5px',
    },
    searchBox: {
        padding: '5px 5px 5px 5px',
    },
    buttonProgress: {
        color: theme.palette.secondary.main,
        marginLeft: -50,
    },
    selectRoot: {
        background: '#fff',
        borderBottom: '1px solid rgba(0, 0, 0, 0.42)',
        borderRight: '1px solid rgba(0, 0, 0, 0.42)',
        height: '39px',
        padding: '0 10px',
    },
});

/**
 * Render search bar in top AppBar
 *
 * @class HeaderSearch
 * @extends {React.Component}
 */
class HeaderSearch extends React.Component {
    /**
     *Creates an instance of HeaderSearch.
     * @param {Object} props @ignore
     * @memberof HeaderSearch
     */
    constructor(props) {
        super(props);
        this.state = {
            searchText: '',
            lcState: '',
            suggestions: [],
            isLoading: false,
        };
        this.handleSuggestionsFetchRequested = this.handleSuggestionsFetchRequested.bind(this);
        this.handleSuggestionsClearRequested = this.handleSuggestionsClearRequested.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.handleDropDownChange = this.handleDropDownChange.bind(this);
        this.onKeyDown = this.onKeyDown.bind(this);
        this.clearOnBlur = this.clearOnBlur.bind(this);
        this.renderSuggestionsContainer = this.renderSuggestionsContainer.bind(this);
        this.onSuggestionSelected = this.onSuggestionSelected.bind(this);
    }

    /**
     * To provide accessibility for Enter key upon suggestion selection
     * @param {React.SyntheticEvent} event event
     * @param {Object} suggestion This is either API object or document coming from search API call
     */
    onSuggestionSelected(event, { suggestion }) {
        this.suggestionSelected = true;
        const { history } = this.props;
        if (event.key === 'Enter') {
            const path = suggestion.type === 'API' ? `/apis/${suggestion.id}/overview`
                : `/apis/${suggestion.apiUUID}/documents/${suggestion.id}/details`;
            history.push(path);
        }
    }

    /**
     * On enter pressed after giving a search text
     * @param event
     */
    onKeyDown(event) {
        if (event.key === 'Enter' && !this.suggestionSelected) {
            const { history } = this.props;
            const { lcState } = this.state;
            history.push('/apis/search?query=' + buildSearchQuery(event.target.value, lcState));
        }
        this.suggestionSelected = false;
    }

    suggestionSelected = false;

    /**
     * Fetch suggestions list for the user entered input value
     *
     * @param {String} { value }
     * @memberof HeaderSearch
     */
    handleSuggestionsFetchRequested({ value }) {
        const { lcState } = this.state;
        this.setState({ isLoading: true });
        getSuggestions(value, lcState).then((body) => {
            this.setState({ isLoading: false, suggestions: body.obj.list });
        });
    }


    /**
     * Handle the suggestions clear Synthetic event
     *
     * @memberof HeaderSearch
     */
    handleSuggestionsClearRequested() {
        this.setState({
            suggestions: [],
        });
    }

    /**
     * On change search input element
     *
     * @param {React.SyntheticEvent} event ReactDOM event
     * @param {String} { newValue } Changed value
     * @memberof HeaderSearch
     */
    handleChange(event, { newValue }) {
        this.setState({
            searchText: newValue,
        });
    }

    /**
     * On change of lcState drop down
     *
     * @param {React.SyntheticEvent} event ReactDOM event
     * @param {String} { newValue } Changed value
     * @memberof HeaderSearch
     */
    handleDropDownChange(event) {
        const { searchText, lcState } = this.state;
        this.setState({
            lcState: event.target.value,
        });
        const {history} = this.props;
        if (event.target.value) {
            history.push('/apis/search?query=' + buildSearchQuery(searchText, event.target.value));
        } else {
            history.push('/apis/');
        }

    }

    /**
     *
     * When search input is focus out (Blur), Clear the input text to accept brand new search
     * If Search input is show in responsive mode, On blur search input, hide the input element and show the search icon
     * @memberof HeaderSearch
     */
    clearOnBlur() {
        const { smSearch, toggleSmSearch } = this.props;
        if (smSearch) {
            toggleSmSearch();
        } else {
            this.setState({ lcState: '', searchText: '' });
        }
    }

    /**
     *
     *
     * @param {*} options
     * @returns
     * @memberof HeaderSearch
     */
    renderSuggestionsContainer(options) {
        const { containerProps, children } = options;
        const { isLoading } = this.state;
        const { classes } = this.props;

        return isLoading ? (
            <CircularProgress size={24} className={classes.buttonProgress} />
        ) : (
            <Paper {...containerProps} square>
                {children}
            </Paper>
        );
    }

    /**
     *
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof HeaderSearch
     */
    render() {
        const { intl, classes, smSearch } = this.props;
        const {
            searchText, lcState, isLoading, suggestions,
        } = this.state;
        let autoFocus = false;
        let responsiveContainer = classes.container;
        if (smSearch) {
            autoFocus = true;
            responsiveContainer = classes.smContainer;
        }
        return (
            <Autosuggest
                theme={{
                    container: responsiveContainer,
                    suggestionsContainerOpen: classes.suggestionsContainerOpen,
                    suggestionsList: classes.suggestionsList,
                    suggestion: classes.suggestion,
                }}
                suggestions={suggestions}
                renderInputComponent={renderInput}
                onSuggestionsFetchRequested={this.handleSuggestionsFetchRequested}
                onSuggestionsClearRequested={this.handleSuggestionsClearRequested}
                getSuggestionValue={getSuggestionValue}
                renderSuggestion={renderSuggestion}
                renderSuggestionsContainer={this.renderSuggestionsContainer}
                onSuggestionSelected={this.onSuggestionSelected}
                inputProps={{
                    autoFocus,
                    classes,
                    placeholder: intl.formatMessage({
                        id: 'Base.Header.headersearch.HeaderSearch.search_api.tooltip',
                        defaultMessage: 'Search APIs',
                    }),
                    value: searchText,
                    lcState,
                    onChange: this.handleChange,
                    onDropDownChange: this.handleDropDownChange,
                    onKeyDown: this.onKeyDown,
                    onBlur: this.clearOnBlur,
                    isLoading,
                }}
            />
        );
    }
}

HeaderSearch.defaultProps = {
    smSearch: false,
    toggleSmSearch: undefined,
};
HeaderSearch.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    smSearch: PropTypes.bool,
    toggleSmSearch: PropTypes.func,
    history: PropTypes.shape({
        push: PropTypes.func,
    }).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default injectIntl(withRouter(withStyles(styles)(HeaderSearch)));
