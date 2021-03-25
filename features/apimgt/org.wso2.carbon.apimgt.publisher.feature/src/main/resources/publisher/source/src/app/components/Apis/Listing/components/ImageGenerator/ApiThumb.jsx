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
/* eslint-disable react/jsx-props-no-spreading */
import React from 'react';
import { useTheme } from '@material-ui/core/styles';
import ApiThumbClassic from 'AppComponents/Apis/Listing/components/ImageGenerator/APICards/ApiThumbClassic';
import APIThumbPlain from 'AppComponents/Apis/Listing/components/ImageGenerator/APICards/APIThumbPlain';

/**
 * Render a thumbnail
 * @param {JSON} props required pros.
 * @returns {JSX} Thumbnail rendered output.
 */
export default function APIThumb(props) {
    const theme = useTheme();
    const { custom } = theme;
    if (!custom.thumbnailTemplates || !custom.thumbnailTemplates.active) {
        return (
            <ApiThumbClassic {...props} />
        );
    }
    return (
        <APIThumbPlain {...props} />
    );
}
