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
import { StarRate } from '@material-ui/icons';
import Icon from '@material-ui/core/Icon';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';

/**
 *
 *
 * @param {*} theme
 */
const styles = (theme) => {
    const starColor = theme.custom.infoBar.starColor || theme.palette.getContrastText(theme.custom.infoBar.background);
    return {
        starRate: {
            color: starColor,
            '&.material-icons': {
                fontSize: 40,
            },
        },
        userRating: {
            display: 'flex',
            justifyContent: 'flex-start',
            alignItems: 'center',
        },
    };
};

function StarRatingSummary(props) {
    const {
        classes, theme, avgRating, reviewCount, returnCount,
    } = props;
    return (
        <React.Fragment>
            {returnCount > 0 ? (
                <React.Fragment>
                    <Icon className={classes.starRate}>star_border</Icon>
                    <div className={classes.ratingSummary}>
                        <div className={classes.userRating}>
                            <Typography variant='body1'>{avgRating}</Typography>
                            <Typography variant='body1'>/5.0</Typography>
                        </div>
                        <Typography variant='body1' gutterBottom align='left'>
                            {reviewCount}{' '}
                            {reviewCount === 1 ? (
                                <FormattedMessage defaultMessage='user' id='Apis.Listing.StarRatingBar.user' />
                            ) : (
                                <FormattedMessage defaultMessage='users' id='Apis.Listing.StarRatingBar.users' />
                            )}
                        </Typography>
                    </div>
                </React.Fragment>
            ) : (
                <React.Fragment>
                    <StarRate className={classes.starRate} style={{ color: theme.palette.grey.A200 }} />
                    <div className={classes.ratingSummary}>
                        <Typography variant='caption' gutterBottom align='left'>
                            <FormattedMessage defaultMessage='Not Rated' id='Apis.Listing.StarRatingBar.not.rated' />
                        </Typography>
                    </div>
                </React.Fragment>
            )}
        </React.Fragment>
    );
}

export default withStyles(styles, { withTheme: true })(StarRatingSummary);
