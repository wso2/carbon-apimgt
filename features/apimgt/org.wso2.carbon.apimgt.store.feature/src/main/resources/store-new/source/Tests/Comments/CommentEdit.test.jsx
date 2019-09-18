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
import { TextField, Button, Typography, Select, MenuItem } from '@material-ui/core';
import CommentEdit from '../../src/app/components/Apis/Details/Comments/CommentEdit';

const CommentEditUnwrapped = unwrap(CommentEdit);

/**
 * Initialize common properties to be passed
 * @param {*} props properies to be override
 */
function createTestProps(props) {
    return {
        classes: {},
        apiId: '6e770272-212b-404e-ab9c-333fdba02f2f',
        cancelButton: true,
        allComments: [],
        theme: { custom: { maxCommentLength: 512 } },
        comment:  {
            commentId: 'ebf03093-74a3-4cd3-b5d0-a30d32a90f4b',
            category: 'General',
            parentCommentId: null,
            username: 'admin',
            commentText: 'My new comment updated',
            createdTime: '2018-09-27T10:16:44.444Z',
            createdBy: 'admin',
            lastUpdatedTime: '2018-09-27T10:37:03.570Z',
            lastUpdatedBy: 'admin',
        },
        commentsUpdate: jest.fn(),
        toggleShowEdit: jest.fn(),
        ...props,
    };
}

let wrapper;
const commentText = 'New comment';
const category = 'Another category';
const props = createTestProps();

beforeEach(() => {
    wrapper = shallow(<CommentEditUnwrapped {...props} />);
});

describe('<CommentEdit /> rendering', () => {
    it('renders correctly', () => {
        expect(wrapper).toMatchSnapshot();
    });

    it('should render a <TextField /> to type the comment', () => {
        expect(wrapper.find(TextField)).toHaveLength(1);
    });

    it('should render a <Select /> to select the category', () => {
        expect(wrapper.find(Select)).toHaveLength(1);
    });

    it('should render a 3 <MenuItem /> s to select the categories', () => {
        expect(wrapper.find(Select).dive().find(MenuItem)).toHaveLength(3);
    });

    it('should render a <Typography /> to display the maximum length of the comment', () => {
        expect(wrapper.find(Typography)).toHaveLength(1);
    });

    it('should render 2 <Button /> s to display the save and cancel options', () => {
        expect(wrapper.find(Button)).toHaveLength(2);
    });
});


describe('<CommentEdit /> interactions', () => {
    it('should call the onClick function when \'Save\' button is clicked', () => {
        const mockedHandleClickUpdateComment = jest.fn();
        wrapper.instance().handleClickUpdateComment = mockedHandleClickUpdateComment;
        wrapper.find(Button).first().props().onClick();
        expect(mockedHandleClickUpdateComment).toHaveBeenCalledTimes(1);
    });

    it('should call the onClick function when \'Cancel\' button is clicked', () => {
        const mockedHandleClickCancel = jest.fn();
        wrapper.instance().handleClickCancel = mockedHandleClickCancel;
        wrapper.find(Button).last().props().onClick();
        expect(mockedHandleClickCancel).toHaveBeenCalledTimes(1);
    });

    it('should change the state commentText and currentlength when the onChange function of the TextField is invoked', () => {
        wrapper.find(TextField).simulate('change',
            { target: { value: commentText } }
        );
        expect(wrapper.state('commentText')).toEqual(commentText);
        expect(wrapper.state('currentLength')).toEqual(commentText.length);
    });

    it('should change the state commentText and currentlength when the onChange function of the Select is invoked', () => {
        wrapper.find(Select).simulate('change',
            { target: { value: category } }
        );
        expect(wrapper.state('category')).toEqual(category);
    });
});
