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
import StarRate from '@material-ui/icons/StarRate';
import Icon from '@material-ui/core/Icon';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';

const styles = (theme) => {
    const starColor = theme.custom.infoBar.starColor || theme.palette.getContrastText(theme.custom.infoBar.background);
    return {
        starRate: {
            marginRight: theme.spacing(),
            color: starColor,
            '&.material-icons': {
                fontSize: 30,
            },
        },
        userRating: {
            display: 'flex',
            justifyContent: 'flex-start',
            alignItems: 'center',
        },
    };
};

/**
 *
 * @param {JSON} props props passed from parent
 * @returns {JSX} summary of the rating
 */
function StarRatingSummary(props) {
    const {
        classes, theme, avgRating, reviewCount, returnCount,
    } = props;
    return (
        <>
            {returnCount > 0 ? (
                <>
                    <Icon className={classes.starRate}>star</Icon>
                    <div className={classes.ratingSummary}>
                        <div aria-label='User rating' className={classes.userRating}>
                            <Typography variant='body1'>{avgRating}</Typography>
                            <Typography aria-label='out of five' variant='body1'>/5.0</Typography>
                        </div>
                        <Typography aria-label='Number of users who has rated' variant='body1' gutterBottom align='left'>
                            {reviewCount}
                            {' '}
                            {reviewCount === 1 ? (
                                <FormattedMessage defaultMessage='user' id='Apis.Details.StarRatingSummary.user' />
                            ) : (
                                <FormattedMessage defaultMessage='users' id='Apis.Details.StarRatingSummary.users' />
                            )}
                        </Typography>
                    </div>
                </>
            ) : (
                <>
                    <StarRate className={classes.starRate} style={{ color: theme.palette.grey.A200 }} />
                    <div className={classes.ratingSummary}>
                        <Typography variant='caption' gutterBottom align='left'>
                            <FormattedMessage defaultMessage='Not Rated' id='Apis.Details.StarRatingSummary.not.rated' />
                        </Typography>
                    </div>
                </>
            )}
        </>
    );
}

export default withStyles(styles, { withTheme: true })(StarRatingSummary);
