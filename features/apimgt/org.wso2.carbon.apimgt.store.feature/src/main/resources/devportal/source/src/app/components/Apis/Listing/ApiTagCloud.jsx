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
import { useTheme, makeStyles } from '@material-ui/core/styles';
import Divider from '@material-ui/core/Divider';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import { TagCloud } from 'react-tagcloud';
import PropTypes from 'prop-types';
import { useHistory } from 'react-router-dom';

const useStyles = makeStyles(theme => ({
    clickablePointer: {
        cursor: 'pointer',
        padding: theme.spacing(1),
    },
    filterTitle: {
        fontWeight: 200,
        paddingLeft: theme.spacing(2),
        background: theme.custom.tagCloud.leftMenu.titleBackground,
        color: theme.palette.getContrastText(theme.custom.tagCloud.leftMenu.titleBackground),
        height: theme.custom.infoBar.height,
        alignItems: 'center',
        display: 'flex',
    },
}));

/**
 * Component used to handle API Tag Cloud
 * @class ApiTagCloud
 * @extends {React.Component}
 * @param {any} value @inheritDoc
 */
function ApiTagCloud(props) {
    const classes = useStyles();
    const theme = useTheme();
    const {
        custom: {
            tagWise: { key, active },
            tagCloud: { colorOptions },
        },
    } = theme;
    const history = useHistory();

    const { allTags } = props;
    let apisTagWithoutGroups = null;
    if (allTags.count !== 0) {
        // Remve the tags with a sufix '-group' to ignore the
        if (active) {
            apisTagWithoutGroups = allTags.filter(item => item.value.search(key) === -1);
        } else {
            apisTagWithoutGroups = allTags;
        }
    }

    /**
     *
     * @param {String} tag selected tag
     * @memberof ApiTagCloud
     */
    const handleOnClick = (tag) => {
        const tagSearchURL = `/apis?offset=0&query=tag:${tag.value}`;
        history.push(tagSearchURL);
    };

    return (
        apisTagWithoutGroups && (
            <React.Fragment>
                <Typography variant='h6' gutterBottom className={classes.filterTitle}>
                    <FormattedMessage defaultMessage='Tag Cloud' id='Apis.Listing.ApiTagCloud.title' />
                </Typography>
                <TagCloud
                    minSize={14}
                    maxSize={25}
                    colorOptions={colorOptions}
                    tags={allTags}
                    shuffle={false}
                    className={classes.clickablePointer}
                    onClick={tag => handleOnClick(tag)}
                />
            </React.Fragment>
        )
    );
}

ApiTagCloud.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    tag: PropTypes.shape({}).isRequired,
    listType: PropTypes.string.isRequired,
    apiType: PropTypes.string.isRequired,
    allTags: PropTypes.shape({}).isRequired,
};

export default ApiTagCloud;
