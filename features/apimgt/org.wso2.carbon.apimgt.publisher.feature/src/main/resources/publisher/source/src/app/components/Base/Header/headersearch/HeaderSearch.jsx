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
import { withRouter } from 'react-router-dom';
import PropTypes from 'prop-types';
import Autosuggest from 'react-autosuggest';
import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Box from '@material-ui/core/Box';
import InfoIcon from '@material-ui/icons/InfoOutlined';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import { FormattedMessage, injectIntl } from 'react-intl';

import {
    renderInput, renderSuggestion, getSuggestions, getSuggestionValue, buildSearchQuery,
} from './SearchUtils';

const styles = (theme) => ({
    container: {
        flexGrow: 0,
    },
    smContainer: {
        position: 'absolute',
        left: 0,
    },
    suggestionsContainerOpen: {
        marginTop: theme.spacing(1),
        display: 'block',
        position: 'absolute',
        width: '400px',
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
        borderRadius: theme.shape.borderRadius * 5,
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
    infoButton: {
        margin: theme.spacing(1),
        color: theme.palette.background.paper,
    },
    InfoToolTip: {
        backgroundColor: '#f5f5f9',
        color: 'rgba(0,0,0,0.87)',
        maxWidth: 350,
        fontSize: theme.typography.pxToRem(14),
        fontWeight: '400',
        border: '1px solid #dadde9',
        borderRadius: '5px',
        padding: '15px 10px 0 18px',
    },
});

/**
 * Render search bar in top AppBar
 *
 * @class HeaderSearch
 * @extends {React.Component}
 */
class HeaderSearch extends React.Component {
    suggestionSelected = false;

    /**
     *Creates an instance of HeaderSearch.
     * @param {Object} props @ignore
     * @memberof HeaderSearch
     */
    constructor(props) {
        super(props);
        this.state = {
            searchText: '',
            suggestions: [],
            isLoading: false,
        };
        this.handleSuggestionsFetchRequested = this.handleSuggestionsFetchRequested.bind(this);
        this.handleSuggestionsClearRequested = this.handleSuggestionsClearRequested.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.onKeyDown = this.onKeyDown.bind(this);
        this.clearOnBlur = this.clearOnBlur.bind(this);
        this.renderSuggestionsContainer = this.renderSuggestionsContainer.bind(this);
        this.onSuggestionSelected = this.onSuggestionSelected.bind(this);
    }

    /**
     * On enter pressed after giving a search text
     * @param event
     */
    onKeyDown(event) {
        if (event.key === 'Enter' && !this.suggestionSelected) {
            const { history } = this.props;
            history.push('/apis/search?query=' + buildSearchQuery(event.target.value));
        }
        this.suggestionSelected = false;
    }

    /**
     * To provide accessibility for Enter key upon suggestion selection
     * @param {React.SyntheticEvent} event event
     * @param {Object} suggestion This is either API object or document coming from search API call
     */
    onSuggestionSelected(event, { suggestion }) {
        const { history } = this.props;
        this.suggestionSelected = true;
        if (event.key === 'Enter') {
            const path = suggestion.type === 'API' ? `/apis/${suggestion.id}/overview`
                : `/apis/${suggestion.apiUUID}/documents/${suggestion.id}/details`;
            history.push(path);
        }
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
     * Fetch suggestions list for the user entered input value
     *
     * @param {String} { value }
     * @memberof HeaderSearch
     */
    handleSuggestionsFetchRequested({ value }) {
        this.setState({ isLoading: true });
        getSuggestions(value).then((body) => {
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
            this.setState({ searchText: '' });
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

        return isLoading ? (
            null
        ) : (
            // Disabling the eslint rule because the container props can't know beforehand
            // eslint-disable-next-line react/jsx-props-no-spreading
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
        const { intl } = this.props;
        const { classes, smSearch } = this.props;
        const { searchText, isLoading, suggestions } = this.state;
        let autoFocus = false;
        let responsiveContainer = classes.container;
        if (smSearch) {
            autoFocus = true;
            responsiveContainer = classes.smContainer;
        }
        return (
            <Box display='flex' alignItems='center' justifyContent='space-between' flexDirection='row' width={1}>
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
                            defaultMessage: 'Search',
                        }),
                        value: searchText,
                        onChange: this.handleChange,
                        onKeyDown: this.onKeyDown,
                        onBlur: this.clearOnBlur,
                        isLoading,
                        disableUnderline: true,
                    }}
                />
                <Tooltip
                    interactive
                    placement='top'
                    classes={{
                        tooltip: classes.InfoToolTip,
                    }}
                    title={(
                        <>
                            <FormattedMessage
                                id='Base.Header.headersearch.HeaderSearch.tooltip.title'
                                defaultMessage='Search Options for APIs and APIProducts'
                            />
                            <ol style={{ marginLeft: '-20px', marginTop: '8px' }}>
                                <li style={{ marginTop: '5px' }}>
                                    <FormattedMessage
                                        id='Base.Header.headersearch.HeaderSearch.tooltip.option0'
                                        defaultMessage='Content [ Default ]'
                                    />
                                </li>
                                <li style={{ marginTop: '5px' }}>
                                    <FormattedMessage
                                        id='Base.Header.headersearch.HeaderSearch.tooltip.option1'
                                        defaultMessage='Name [ Syntax - name:xxxx ]'
                                    />
                                </li>
                                <li style={{ marginTop: '5px' }}>
                                    <FormattedMessage
                                        id='Base.Header.headersearch.HeaderSearch.tooltip.option2'
                                        defaultMessage='Provider [ Syntax - provider:xxxx ]'
                                    />
                                </li>
                                <li style={{ marginTop: '5px' }}>
                                    <FormattedMessage
                                        id='Base.Header.headersearch.HeaderSearch.tooltip.option3'
                                        defaultMessage='Version [ Syntax - version:xxxx ]'
                                    />
                                </li>
                                <li style={{ marginTop: '5px' }}>
                                    <FormattedMessage
                                        id='Base.Header.headersearch.HeaderSearch.tooltip.option4'
                                        defaultMessage='Context [ Syntax - context:xxxx ]'
                                    />
                                </li>
                                <li style={{ marginTop: '5px' }}>
                                    <FormattedMessage
                                        id='Base.Header.headersearch.HeaderSearch.tooltip.option5'
                                        defaultMessage='Status [ Syntax - status:xxxx ]'
                                    />
                                </li>
                                <li style={{ marginTop: '5px' }}>
                                    <FormattedMessage
                                        id='Base.Header.headersearch.HeaderSearch.tooltip.option6'
                                        defaultMessage='Description [ Syntax - description:xxxx ]'
                                    />
                                </li>
                                <li style={{ marginTop: '5px' }}>
                                    <FormattedMessage
                                        id='Base.Header.headersearch.HeaderSearch.tooltip.option11'
                                        defaultMessage='Tags [ Syntax - tags:xxxx ]'
                                    />
                                </li>
                                <li style={{ marginTop: '5px' }}>
                                    <FormattedMessage
                                        id='Base.Header.headersearch.HeaderSearch.tooltip.option12'
                                        defaultMessage='Api Category [ Syntax - api-category:xxxx ]'
                                    />
                                </li>
                                <li style={{ marginTop: '5px' }}>
                                    <FormattedMessage
                                        id='Base.Header.headersearch.HeaderSearch.tooltip.option10'
                                        defaultMessage='Properties [Syntax - property_name:property_value]'
                                    />
                                </li>
                            </ol>
                        </>
                    )}
                >
                    <IconButton className={classes.infoButton}>
                        <InfoIcon />
                    </IconButton>
                </Tooltip>
            </Box>
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
