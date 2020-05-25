/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import CircularProgress from '@material-ui/core/CircularProgress';
import { FormattedMessage } from 'react-intl';

const useStyles = makeStyles((theme) => ({
    root: {
        display: 'flex',
        '& > * + *': {
            marginLeft: theme.spacing(2),
        },
        justifyContent: 'center',
        padding: 20,
    },
}));

/**
 * Render progress inside a container centering in the container.
 * @param {JSON} props component props.
 * @returns {JSX} Loading animation.
 */
function InlineProgress(props) {
    const classes = useStyles();
    const { message } = props;
    return (
        <div className={classes.root}>
            <CircularProgress />
            <Typography color='textSecondary' align='center'>
                {message || (
                    <FormattedMessage
                        id='AdminPages.Addons.InlineProgress.message'
                        defaultMessage='Loading...'
                    />
                )}
            </Typography>
        </div>
    );
}
InlineProgress.defaultProps = {
    message: null,
};
InlineProgress.propTypes = {
    message: PropTypes.string,
};
export default InlineProgress;
