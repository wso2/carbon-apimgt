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
import { deepOrange } from '@material-ui/core/colors';
import { capitalizeFirstLetter } from 'AppData/stringFormatter';
import Utils from 'AppData/Utils';

const useStyles = makeStyles((theme) => {
    return {
        root: {
            display: 'flex',
        },
        square: ({ char, width, height }) => {
            const { colorMap, offset, width: defaultWidth } = theme.custom.thumbnail;
            let charColor = colorMap[char.toLowerCase()];
            if (!charColor) {
                const charNumber = parseInt(char, 10);
                if (charNumber) {
                    charColor = colorMap[String.fromCharCode(111 + charNumber)];
                } else {
                    charColor = colorMap.x;
                }
            }
            const { r, g, b } = Utils.hexToRGBHash(charColor);
            const darkHex = Utils.rgbToHex(r - Math.ceil(r * offset), g - Math.ceil(offset * g),
                b - Math.ceil(offset * b));
            const fontSize = Math.ceil((width * 70) / defaultWidth);
            return {
                color: theme.palette.getContrastText(deepOrange[500]),
                background: `linear-gradient(to right, ${charColor}, ${darkHex})`,
                height,
                width,
                fontSize: `${fontSize}px`,
                textShadow: '0 1px 0 #ccc, '
                    + '0 2px 0 #c9c9c9,'
                    + ' 0 1px 3px rgba(0,0,0,.3),'
                    + ' 0 10px 10px rgba(0,0,0,.2),'
                    + ' 0 20px 20px rgba(0,0,0,.15)',
            };
        },
    };
});

export default (props) => {
    const { api, width, height } = props;
    const name = api.name.substring(0, 2);
    const classes = useStyles({ char: name.substring(0, 1), width, height });

    return (
        <div className={classes.root}>
            <Avatar variant='square' className={classes.square}>
                {capitalizeFirstLetter(name)}
            </Avatar>
        </div>
    );
};
