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
import PropTypes from 'prop-types';
import Downshift from 'downshift';
import { withStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import Paper from '@material-ui/core/Paper';
import MenuItem from '@material-ui/core/MenuItem';
import InputAdornment from '@material-ui/core/InputAdornment';
import Icon from '@material-ui/core/Icon';
import { injectIntl } from 'react-intl';

const suggestions = [
    { label: '' },
];
/**
 *
 *
 * @param {*} inputProps
 * @returns
 */
function renderInput(inputProps) {
    const {
        InputProps, classes, ref, ...other
    } = inputProps;

    return (
        <TextField
            InputProps={{
                startAdornment: (
                    <InputAdornment position='start'>
                        <Icon className={classes.searchIcon}>search</Icon>
                    </InputAdornment>
                ),
                inputRef: ref,
                classes: {
                    root: classes.inputRoot,
                },
                ...InputProps,
            }}
            {...other}
        />
    );
}
/**
 *
 *
 * @param {*} { suggestion, index, itemProps, highlightedIndex, selectedItem }
 * @returns
 */
function renderSuggestion({
    suggestion, index, itemProps, highlightedIndex, selectedItem,
}) {
    const isHighlighted = highlightedIndex === index;
    const isSelected = (selectedItem || '').indexOf(suggestion.label) > -1;

    return (
        <MenuItem
            {...itemProps}
            key={suggestion.label}
            selected={isHighlighted}
            component='div'
            style={{
                fontWeight: isSelected ? 500 : 400,
            }}
        >
            {suggestion.label}
        </MenuItem>
    );
}
renderSuggestion.propTypes = {
    highlightedIndex: PropTypes.number,
    index: PropTypes.number,
    itemProps: PropTypes.object,
    selectedItem: PropTypes.string,
    suggestion: PropTypes.shape({ label: PropTypes.string }).isRequired,
};
/**
 *
 *
 * @param {*} inputValue
 * @returns
 */
function getSuggestions(inputValue) {
    let count = 0;

    return suggestions.filter((suggestion) => {
        const keep = (!inputValue || suggestion.label.toLowerCase().indexOf(inputValue.toLowerCase()) !== -1) && count < 5;

        if (keep) {
            count += 1;
        }

        return keep;
    });
}
/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    root: {
        flexGrow: 1,
        height: 'auto',
    },
    container: {
        flexGrow: 1,
        position: 'relative',
    },
    paper: {
        position: 'absolute',
        zIndex: 1,
        marginTop: theme.spacing(1),
        left: 0,
        right: 0,
    },
    chip: {
        margin: `${theme.spacing(1) / 2}px ${theme.spacing(1) / 4}px`,
    },
    inputRoot: {
        flexWrap: 'wrap',
        paddingLeft: 10,
        '&:before': {
            borderBottom: 'none',
        },
    },
    divider: {
        height: theme.spacing(2),
    },
    searchWrapper: {
        width: 300,
        flexGrow: 1,
    },
    searchIcon: {
        paddingLeft: 5,
        position: 'absolute',
        right: 0,
        top: 5,
        right: 12,
        cursor: 'pointer',
    },
});
/**
 *
 *
 * @class GenericSearch
 * @extends {React.Component}
 */
class GenericSearch extends React.Component {
    state = {
        inputValue: '',
        selectedItem: [],
    };

    handleKeyDown = (event) => {
        const { inputValue, selectedItem } = this.state;
        if (selectedItem.length && !inputValue.length && event.keyCode === 8) {
            this.setState({
                selectedItem: selectedItem.slice(0, selectedItem.length - 1),
            });
        }
    };

    handleInputChange = (event) => {
        this.setState({ inputValue: event.target.value });
    };

    handleChange = (item) => {
        let { selectedItem } = this.state;

        if (selectedItem.indexOf(item) === -1) {
            selectedItem = [...selectedItem, item];
        }

        this.setState({
            inputValue: '',
            selectedItem,
        });
    };

    handleDelete = item => () => {
        this.setState((state) => {
            const selectedItem = [...state.selectedItem];
            selectedItem.splice(selectedItem.indexOf(item), 1);
            return { selectedItem };
        });
    };

    render() {
        const { classes, intl } = this.props;
        const { inputValue, selectedItem } = this.state;

        return (
            <div className={classes.searchWrapper}>
                <Downshift id='downshift-simple'>
                    {({
                        getInputProps, getItemProps, isOpen, inputValue, selectedItem, highlightedIndex,
                    }) => (
                        <div className={classes.container}>
                            {renderInput({
                                fullWidth: true,
                                classes,
                                InputProps: getInputProps({
                                    placeholder: intl.formatMessage({
                                        id: 'Base.Generic.GenericSearch.search.apis',
                                        defaultMessage: 'Search APIs',
                                    }),
                                }),
                            })}
                            {isOpen ? (
                                <Paper className={classes.paper} square>
                                    {getSuggestions(inputValue).map((suggestion, index) => renderSuggestion({
                                        suggestion,
                                        index,
                                        itemProps: getItemProps({ item: suggestion.label }),
                                        highlightedIndex,
                                        selectedItem,
                                    }))}
                                </Paper>
                            ) : null}
                        </div>
                    )}
                </Downshift>
            </div>
        );
    }
}

GenericSearch.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
};
export default injectIntl(withStyles(styles)(GenericSearch));
