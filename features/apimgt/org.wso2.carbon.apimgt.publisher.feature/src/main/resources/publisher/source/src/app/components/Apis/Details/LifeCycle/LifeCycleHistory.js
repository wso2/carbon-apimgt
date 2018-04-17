/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React from 'react'
import List, {
    ListItem,
    ListItemText,
} from 'material-ui/List';
import Avatar from 'material-ui/Avatar'
import Person from '@material-ui/icons/Person';

const LifeCycleHistory = props => {
    return (
    <List>
        { props.lcHistory.map(entry =>
            entry.previousState &&
            <ListItem button key={entry.postState}>
                <div>
                    <Avatar>
                        <Person />
                    </Avatar>

                    <div>{entry.user}</div>
                </div>
                <ListItemText
                    primary={"LC has changed from " + entry.previousState + " to " + entry.postState}
                    secondary={entry.updatedTime} />
            </ListItem>
        )}
    </List>


    );
};

export default LifeCycleHistory