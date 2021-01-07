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
/* eslint object-shorthand: 0 */
/* eslint jsx-a11y/click-events-have-key-events: 0 */

/* eslint no-unused-expressions: 0 */

import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import deburr from 'lodash/deburr';
import Downshift from 'downshift';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Icon from '@material-ui/core/Icon';
import { FormattedMessage } from 'react-intl';
import ClickAwayListener from '@material-ui/core/ClickAwayListener';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import suggestions from './RouteMappings';

const useStyles = makeStyles((theme) => ({
    root: {
        flexGrow: 1,
        height: 250,
    },
    container: {
        flexGrow: 1,
        position: 'relative',
    },
    paper: {
        position: 'absolute',
        zIndex: theme.zIndex.goToSearch,
        marginTop: theme.spacing(1),
        left: 0,
        right: 0,
    },
    chip: {
        margin: theme.spacing(0.5, 0.25),
    },
    inputRoot: {
        flexWrap: 'wrap',
    },
    inputInput: {
        width: 'auto',
        flexGrow: 1,
    },
    divider: {
        height: theme.spacing(2),
    },
    linkButton: {
        display: 'flex',
        alignItems: 'center',
        flexDirection: 'column',
        padding: 10,
        cursor: 'pointer',
        minWidth: 30,
    },
    goToWrapper: {
        position: 'relative',
    },
    downshiftWrapper: {
        position: 'absolute',
        padding: theme.spacing(1),
        right: 60,
        top: 0,
        width: 300,
        background: 'white',
        border: 'solid 1px #ccc',
        borderRadius: 5,
    },
}));

/**
 * Method to render the input of the user
 * @param {*} inputProps inputProps
 * @returns {*} TextField
 */
function renderInput(inputProps) {
    const {
        InputProps, classes, ref, ...other
    } = inputProps;

    return (
        <TextField
            autoFocus
            InputProps={{
                inputRef: ref,
                classes: {
                    root: classes.inputRoot,
                    input: classes.inputInput,
                },
                ...InputProps,
            }}
            {...other}
        />
    );
}

renderInput.propTypes = {
    /**
     * Override or extend the styles applied to the component.
     */
    classes: PropTypes.shape({}).isRequired,
    InputProps: PropTypes.shape({}).isRequired,
};

/**
 * Method to render the suggestions
 * @param {*} suggestionProps suggestionProps
 * @returns {*} ListItem list of suggestions
 */
function renderSuggestion(suggestionProps) {
    const {
        suggestion, index, itemProps, highlightedIndex, api, isAPIProduct, handleClickAway,
    } = suggestionProps;
    const isHighlighted = highlightedIndex === index;

    const route = (isAPIProduct
        ? (`/api-products/${api.id}/${suggestion.route}`)
        : (`/apis/${api.id}/${suggestion.route}`));
    return (
        <Link
            to={route}
            onClick={handleClickAway}
        >
            <ListItem
                key={suggestion.label}
                {...itemProps}
                selected={isHighlighted}
                button
                aria-haspopup='true'
                aria-controls='lock-menu'
                aria-label='when device is locked'
            >
                <ListItemText primary={suggestion.label} secondary={suggestion.route} />
            </ListItem>
        </Link>
    );
}

renderSuggestion.propTypes = {
    highlightedIndex: PropTypes.oneOfType([PropTypes.oneOf([null]), PropTypes.number]).isRequired,
    index: PropTypes.number.isRequired,
    itemProps: PropTypes.shape({}).isRequired,
    selectedItem: PropTypes.string.isRequired,
    suggestion: PropTypes.shape({
        label: PropTypes.string.isRequired,
    }).isRequired,
};

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


/**
 * Method to render the GoTo search feature
 * @param {*} props props param
 * @returns {*} Downshift element
 */
function GoTo(props) {
    const { isAPIProduct, api } = props;
    const classes = useStyles();
    const [showSearch, setShowSearch] = useState(false);
    let isGraphQL = false;

    if (api.type === 'GRAPHQL') {
        isGraphQL = true;
    }
    const toggleSearch = () => {
        setShowSearch(!showSearch);
    };
    const handleClickAway = () => {
        setShowSearch(false);
    };

    return (
        <ClickAwayListener onClickAway={handleClickAway}>
            <div className={classes.goToWrapper}>
                <a className={classes.linkButton} onClick={toggleSearch}>
                    <Icon>find_in_page</Icon>
                    <Typography variant='caption'>
                        <FormattedMessage id='Apis.Details.GoTo.GoTo.btn' defaultMessage='Go To' />
                    </Typography>
                </a>
                {showSearch && (
                    <div className={classes.downshiftWrapper}>
                        <Downshift id='downshift-simple'>
                            {({
                                getInputProps,
                                getItemProps,
                                getLabelProps,
                                getMenuProps,
                                highlightedIndex,
                                inputValue,
                                isOpen,
                                selectedItem,
                            }) => {
                                const { onBlur, onFocus, ...inputProps } = getInputProps({
                                    placeholder: 'Type what you want to do..',
                                });

                                return (
                                    <div className={classes.container}>
                                        {renderInput({
                                            fullWidth: true,
                                            classes,
                                            label: 'Go to menu item',
                                            InputLabelProps: getLabelProps({ shrink: true }),
                                            InputProps: { onBlur, onFocus },
                                            inputProps,
                                        })}

                                        <div {...getMenuProps()}>
                                            {isOpen ? (
                                                <Paper className={classes.paper} square>
                                                    {getSuggestions(inputValue, isAPIProduct, isGraphQL)
                                                        .map((suggestion, index) => renderSuggestion({
                                                            suggestion,
                                                            index,
                                                            itemProps: getItemProps({ item: suggestion.label }),
                                                            highlightedIndex,
                                                            selectedItem,
                                                            handleClickAway: handleClickAway,
                                                            ...props,
                                                        }))}
                                                </Paper>
                                            ) : null}
                                        </div>
                                    </div>
                                );
                            }}
                        </Downshift>
                    </div>
                )}
            </div>
        </ClickAwayListener>
    );
}

GoTo.propTypes = {
    api: PropTypes.shape({}).isRequired,
    isAPIProduct: PropTypes.bool.isRequired,
};

export default GoTo;
