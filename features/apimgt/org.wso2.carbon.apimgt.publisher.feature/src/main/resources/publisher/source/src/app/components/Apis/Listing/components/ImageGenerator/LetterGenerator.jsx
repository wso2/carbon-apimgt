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
import { makeStyles } from '@material-ui/core/styles';
import Avatar from '@material-ui/core/Avatar';
import { capitalizeFirstLetter } from 'AppData/stringFormatter';
import Utils from 'AppData/Utils';

const getColorFromLetter = (letter, colorMap, offset) => {
    let charLightColor = colorMap[letter.toLowerCase()];

    if (!charLightColor) {
        const charNumber = parseInt(letter, 10);
        if (charNumber) {
            charLightColor = colorMap[String.fromCharCode(111 + charNumber)];
        } else {
            return [null, null];
        }
    }
    const { r, g, b } = Utils.hexToRGBHash(charLightColor);
    const dark = Utils.rgbToHex(r - Math.ceil(r * offset), g - Math.ceil(offset * g),
        b - Math.ceil(offset * b));
    return [charLightColor, dark];
};

const useStyles = makeStyles((theme) => {
    return {
        root: {
            display: 'flex',
        },
        thumbIcon: ({ width }) => {
            const { width: defaultWidth } = theme.custom.thumbnail;
            const fontSize = Math.ceil((width * 90) / defaultWidth);
            return {
                fontSize,
            };
        },
        square: ({
            char, width, height, bgColor,
        }) => {
            const {
                colorMap, offset, width: defaultWidth, textShadow,
            } = theme.custom.thumbnail;
            const [light, dark] = getColorFromLetter(bgColor === false ? '' : char, colorMap, offset);
            const fontSize = Math.ceil((width * 70) / defaultWidth);
            /* W3C, IE 10+/ Edge, Firefox 16+, Chrome 26+, Opera 12+, Safari 7+ */
            const background = light && `linear-gradient(to right, ${light}, ${dark})`;
            return {
                color: light && theme.palette.getContrastText(dark),
                background,
                fallbacks: [
                    { background: light }, /* fallback for old browsers */
                    {
                        background:
                        `-webkit-linear-gradient(to right, ${light}, ${dark})`, /* Chrome 10-25, Safari 5.1-6 */
                    },
                ],
                height,
                width,
                fontSize: `${fontSize}px`,
                textShadow,
            };
        },
    };
});

export default (props) => {
    const {
        artifact, width, height, charLength = 2, ThumbIcon, bgColor,
    } = props;
    const name = artifact.name.substring(0, charLength);
    const classes = useStyles({
        char: name.substring(0, 1), width, height, bgColor,
    });

    return (
        <div className={classes.root}>
            <Avatar variant='square' className={classes.square}>
                {ThumbIcon ? <ThumbIcon className={classes.thumbIcon} /> : capitalizeFirstLetter(name)}
            </Avatar>
        </div>
    );
};
