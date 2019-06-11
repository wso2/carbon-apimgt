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
import { withStyles } from '@material-ui/core/styles';

const styles = {
    svgImage: {
        cursor: 'pointer',
        // ⚠️ object-fit is not supported by IE11.
        objectFit: 'cover',
    },
};

function Background(props) {
    const {
        classes, colorPair, width, height,
    } = props;
    // Creating the background

    const thumbnailBox = {
        width: 250,
        height: 200,
    };

    const thumbnailBoxChild = {
        width: 50,
        height: 50,
    };

    const rects = [];
    for (let i = 0; i <= 4; i++) {
        for (let j = 0; j <= 4; j++) {
            rects.push(<rect
                key={i + '_' + j}
                {...thumbnailBoxChild}
                /* eslint no-mixed-operators: 0 */
                fill={'#' + (colorPair.sub - ((0x00000025 * i) - (j * 0x00000015))).toString(16)}
                x={200 - i * 54}
                y={54 * j}
            />);
        }
    }

    return (
        <svg width={width} height={height} className={classes.svgImage}>
            <rect {...thumbnailBox} fill={'#' + colorPair.prime.toString(16)} />
            {rects}
        </svg>
    );
}

Background.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    width: PropTypes.shape({}).isRequired,
    height: PropTypes.shape({}).isRequired,
    colorPair: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(Background);
