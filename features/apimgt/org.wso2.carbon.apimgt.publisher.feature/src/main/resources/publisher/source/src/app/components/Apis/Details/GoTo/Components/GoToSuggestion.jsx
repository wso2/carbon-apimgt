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
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import { Link as RouterLink } from 'react-router-dom';
import Link from '@material-ui/core/Link';
import PropTypes from 'prop-types';

/**
 * Method to render the suggestions
 * @param {*} suggestionProps suggestionProps
 * @returns {*} ListItem list of suggestions
 */
function RenderSuggestion(suggestionProps) {
    const {
        suggestion, index, itemProps, highlightedIndex, isAPIProduct, handleClickAway, apiId,
    } = suggestionProps;
    const isHighlighted = highlightedIndex === index;

    const route = (isAPIProduct
        ? (`/api-products/${apiId}/${suggestion.route}`)
        : (`/apis/${apiId}/${suggestion.route}`));
    return (
        <Link
            component={RouterLink}
            to={route}
            underline='none'
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

RenderSuggestion.propTypes = {
    highlightedIndex: PropTypes.oneOfType([PropTypes.oneOf([null]), PropTypes.number]).isRequired,
    index: PropTypes.number.isRequired,
    itemProps: PropTypes.shape({}).isRequired,
    selectedItem: PropTypes.string.isRequired,
    suggestion: PropTypes.shape({
        label: PropTypes.string.isRequired,
    }).isRequired,
};

export default RenderSuggestion;
