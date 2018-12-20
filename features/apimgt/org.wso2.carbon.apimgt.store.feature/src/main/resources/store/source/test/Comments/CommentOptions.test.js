/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { unwrap } from '@material-ui/core/test-utils';
import { Typography } from '@material-ui/core';
import CommentOptions from '../../src/app/components/Apis/Details/Comments/CommentOptions';
import AuthManager from '../../src/app/data/AuthManager';

const CommentOptionsUnwrapped = unwrap(CommentOptions);
let reply; let
    comment;
const user = {
    expiryTime: '2018-12-16T17:38:24.193Z',
    name: 'admin',
    remember: false,
    scopes: ['openid', 'apim:api_delete', 'apim:api_publish', 'apim:external_services_discover'],
};

/**
 * Initialize common properties to be passed
 * @param {*} props properies to be override
 */
function createTestProps(props) {
    reply = {
        commentId: 'adf03093-74a3-4cd3-b5d0-a30d32a90f4b',
        category: 'General',
        parentCommentId: 'ebf03093-74a3-4cd3-b5d0-a30d32a90f4b',
        username: 'admin',
        commentText: 'My new comment reply',
        createdTime: '2018-09-27T10:17:44.444Z',
        createdBy: 'admin',
        lastUpdatedTime: '2018-09-27T11:37:03.570Z',
        lastUpdatedBy: 'admin',
        replies: [],
    };

    comment = {
        commentId: 'ebf03093-74a3-4cd3-b5d0-a30d32a90f4b',
        category: 'General',
        parentCommentId: null,
        username: 'admin',
        commentText: 'My new comment',
        createdTime: '2018-09-27T10:16:44.444Z',
        createdBy: 'admin',
        lastUpdatedTime: '2018-09-27T10:37:03.570Z',
        lastUpdatedBy: 'admin',
        replies: [reply],
    };

    return {
        classes: {},
        index: 0,
        comment,
        editIndex: -1,
        handleClickOpen: jest.fn(),
        showEditComment: jest.fn(),
        theme: { custom: { maxCommentLength: 1300 } },
        ...props,
    };
}

let wrapper;
const props = createTestProps();

beforeEach(() => {
    wrapper = shallow(<CommentOptionsUnwrapped {...props} />);
});

describe('<CommentOptions /> rendering', () => {
    it('renders correctly', () => {
        expect(wrapper).toMatchSnapshot();
    });

    it('should render a <Typography /> to type the comment', () => {
        AuthManager.setUser(user, 'environment');
        console.log(AuthManager.getUser());
        expect(wrapper.find(Typography)).toHaveLength(1);
    });
});
