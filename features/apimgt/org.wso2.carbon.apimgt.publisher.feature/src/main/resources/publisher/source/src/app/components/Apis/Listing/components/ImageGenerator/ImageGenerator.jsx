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
import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Icon from '@material-ui/core/Icon';
import Background from './Background';

import getIcon from './APICards/ImageUtils';

const styles = {
    icon: {},
    iconWrapper: {
        position: 'relative',
        '& span': {
            position: 'absolute',
            left: '50%',
        },
    },
};

/**
 * Generate dynamic API thumbnail image (SVG), Use PureComponent to avoid unnessasary re-rendering when hover ect
 *
 * @class ImageGenerator
 * @extends {PureComponent}
 */
class ImageGenerator extends PureComponent {
    /**
     *
     * @inheritdoc
     * @returns {React.PureComponent} @inheritdoc
     * @memberof ImageGenerator
     */
    render() {
        const {
            classes, api, width, height, theme, fixedIcon,
        } = this.props;

        const {
            category, key, color, backgroundIndex,
        } = fixedIcon;
        let str = api;
        if (typeof api === 'object') str = api.name;
        if (!str) str = 'default';
        let colorPair;
        let randomBackgroundIndex;
        const colorPairs = theme.custom.thumbnail.backgrounds;

        // Creating the icon
        const iconElement = getIcon(key, category, theme, api);

        if (api.type === 'DOC') {
            colorPair = theme.custom.thumbnail.document.backgrounds;
        } else if (typeof backgroundIndex === 'number' && colorPairs.length > backgroundIndex) {
            // Obtain or generate background color pair
            colorPair = colorPairs[backgroundIndex];
        } else {
            randomBackgroundIndex = (str.charCodeAt(0) + str.charCodeAt(str.length - 1)) % colorPairs.length;
            colorPair = colorPairs[randomBackgroundIndex];
        }
        return (
            <div className={classes.iconWrapper} style={{ width }}>
                <Icon className={classes.icon} style={{ fontSize: height, marginLeft: -height / 2, color }}>
                    {iconElement}
                </Icon>
                {(!theme.custom.thumbnailTemplates || !theme.custom.thumbnailTemplates.active) && (
                    <Background width={width} height={height} colorPair={colorPair} />
                )}
            </div>
        );
    }
}

ImageGenerator.defaultProps = {
    height: 190,
    width: 250,
    fixedIcon: {
        category: null,
        key: null,
        color: '',
        backgroundIndex: null,
    },
};

ImageGenerator.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    height: PropTypes.number,
    width: PropTypes.number,
    fixedIcon: PropTypes.shape({}),
    api: PropTypes.shape({}).isRequired,
    iconSettings: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(ImageGenerator);
