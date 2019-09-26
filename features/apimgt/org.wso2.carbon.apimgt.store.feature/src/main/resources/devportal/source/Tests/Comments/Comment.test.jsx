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
import { unwrap } from '@material-ui/core/test-utils';
import { Typography } from '@material-ui/core';
import Comment from '../../src/app/components/Apis/Details/Comments/Comment';
import CommentEdit from '../../src/app/components/Apis/Details/Comments/CommentEdit';
import CommentOptions from '../../src/app/components/Apis/Details/Comments/CommentOptions';
import CommentReply from '../../src/app/components/Apis/Details/Comments/CommentReply';
import ConfirmDialog from '../../src/app/components/Shared/ConfirmDialog';

const CommentUnwrapped = unwrap(Comment);

let comment, reply;

/**
 * Initialize common properties to be passed
 * @param {*} props properies to be override
 */
function createTestProps(props) {
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
        replies: [],
    };

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

    return {
        classes: {},
        apiId: '6e770272-212b-404e-ab9c-333fdba02f2f',
        allComments: [
            comment
        ],
        theme: { custom: { maxCommentLength: 512 } },
        comments:  [
            comment,
        ],
        commentsUpdate: jest.fn(),
        ...props,
    };
}

let wrapper;
const props = createTestProps();

beforeEach(() => {
    wrapper = shallow(<CommentUnwrapped {...props} /> );
});

describe('<Comment /> rendering', () => {
    it('renders correctly', () => {
        expect(wrapper).toMatchSnapshot();
    });

    it('should render 2 <Typography /> s to display the username and the comment text', () => {
        expect(wrapper.find(Typography)).toHaveLength(2);
    });

    it('should not render a <CommentEdit /> component to edit the comment if the edit index is not equal to the index of the currenly displayed comnment', () => {
        expect(wrapper.find(CommentEdit)).toHaveLength(0);
    });

    it('should render a <CommentOptions /> component', () => {
        expect(wrapper.find(CommentOptions)).toHaveLength(1);
    });

    it('should render a <ConfirmDialog /> component', () => {
        expect(wrapper.find(ConfirmDialog)).toHaveLength(1);
    });

    it('should not render a <CommentReply /> component when there are no replies', () => {
        expect(wrapper.find(CommentReply)).toHaveLength(0);
    });

    it('should render a <CommentReply /> component', () => {
        comment.replies.push(reply);
        wrapper = shallow(<CommentUnwrapped {...props} comments={[comment]} allComments={[comment]} /> );
        expect(wrapper.find(CommentReply)).toHaveLength(1);
    });
});
