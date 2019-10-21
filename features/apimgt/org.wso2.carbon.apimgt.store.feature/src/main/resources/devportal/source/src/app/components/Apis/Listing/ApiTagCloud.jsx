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

import React, { useState, useEffect } from 'react';
import { useTheme, makeStyles } from '@material-ui/core/styles';
import Divider from '@material-ui/core/Divider';
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import { FormattedMessage } from 'react-intl';
import { TagCloud } from 'react-tagcloud';
import PropTypes from 'prop-types';
import { useHistory } from 'react-router-dom';
import API from 'AppData/api';

const useStyles = makeStyles(theme => ({
    clickablePointer: {
        cursor: 'pointer',
        padding: theme.spacing(1),
    },
    filterTitle: {
        fontWeight: 400,
        padding: theme.spacing(1, 2),
    },
    paper: {
        minWidth: theme.custom.tagWise.fixedStyles.width,
        width: theme.custom.tagWise.fixedStyles.width,
        background: theme.custom.tagWise.fixedStyles.background,
        color: theme.palette.getContrastText(theme.custom.tagWise.fixedStyles.background),
        margin: `${theme.spacing(2)}px ${theme.spacing(2)}px 0 0`,
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
    const [allTags, setAllTags] = useState(null);

    /**
     * @memberof ApiTagCloud
     */
    useEffect(() => {
        const api = new API();
        const promisedTags = api.getAllTags();

        promisedTags
            .then((response) => {
                if (response.body.count !== 0) {
                    // Remve the tags with a sufix '-group' to ignore the
                    if (active) {
                        const apisTagWithoutGroups = response.body.list.filter(item => item.value.search(key) === -1);
                        setAllTags(apisTagWithoutGroups);
                    } else {
                        setAllTags(response.body.list);
                    }
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            });
    }, []);

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
        allTags && (
            <React.Fragment>
                <Paper className={classes.paper}>
                    <Divider />
                    <Typography variant='h6' gutterBottom className={classes.filterTitle}>
                        <FormattedMessage defaultMessage='Tag Cloud' id='Apis.Listing.ApiTagCloud.title' />
                    </Typography>
                    <Divider />
                    <TagCloud
                        minSize={18}
                        maxSize={40}
                        colorOptions={colorOptions}
                        tags={allTags}
                        shuffle={false}
                        className={classes.clickablePointer}
                        onClick={tag => handleOnClick(tag)}
                    />
                </Paper>
            </React.Fragment>
        )
    );
}

ApiTagCloud.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    tag: PropTypes.shape({}).isRequired,
    listType: PropTypes.string.isRequired,
    apiType: PropTypes.string.isRequired,
};

export default ApiTagCloud;
