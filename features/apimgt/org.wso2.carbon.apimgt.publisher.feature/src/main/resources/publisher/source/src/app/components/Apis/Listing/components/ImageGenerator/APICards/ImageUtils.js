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

import MaterialIcons from 'MaterialIcons';

const getIcon = (key, category, theme, api) => {
    let IconElement;
    let count;

    // Creating the icon
    if (key && category) {
        IconElement = key;
    } else if (api.type === 'DOC') {
        IconElement = theme.custom.thumbnail.document.icon;
    } else {
        count = MaterialIcons.categories[1].icons.length;
        const randomIconIndex = (api.name.charCodeAt(0) + api.name.charCodeAt(api.name.length - 1)) % count;
        IconElement = MaterialIcons.categories[8].icons[randomIconIndex].id;
    }
    return IconElement;
};

export default getIcon;
