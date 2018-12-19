import { unwrap } from '@material-ui/core/test-utils';
import { Typography } from '@material-ui/core';
import ArrowDropDownCircleOutlined from '@material-ui/icons/ArrowDropDownCircleOutlined';
import Comments from '../../src/app/components/Apis/Details/Comments/Comments';
import Comment from '../../src/app/components/Apis/Details/Comments/Comment';
import CommentAdd from '../../src/app/components/Apis/Details/Comments/CommentAdd';


const CommentsUnwrapped = unwrap(Comments);

let wrapper;
const theme = {
    custom: {
        commentsLimit: 5,
    }
};
const api = {
    id: '6e770272-212b-404e-ab9c-333fdba02f2f',
    isDefaultVersion: true,
    lastUpdatedTime: '2018-11-27T03:08:22.569Z',
    lifeCycleStatus: 'Published',
    name: 'Swagger Petstore',
};

beforeEach(() => {
    wrapper = shallow(<CommentsUnwrapped api={api} apiId={api.id} classes={{}} theme={theme} /> );
});

beforeAll(() => {
    CommentsUnwrapped.prototype.componentDidMount = () => {
        console.log('componentDidMount method is called');
    };
});


describe('<Comments /> rendering', () => {
    it('should render a <ArrowDropDownCircleOutlined /> with the title of the section - Comments', () => {
        expect(wrapper.find(ArrowDropDownCircleOutlined)).toHaveLength(1);
    });

    it('should render a <Typography /> with the title of the section - Comments', () => {
        expect(wrapper.find(Typography)).toHaveLength(1);
    });

    it('should render a <CommentAdd /> component to add a new comment', () => {
        expect(wrapper.find(CommentAdd)).toHaveLength(1);
    });

    it('should render a <Comment /> component to display a comment', () => {
        expect(wrapper.find(Comment)).toHaveLength(1);
    });

    it('should render a <a /> to load more comments, if the number of comments is greater than the maximum limit to be displayed', () => {
        wrapper.setState({ startCommentsToDisplay: 1 });
        expect(wrapper.find('a')).toHaveLength(1);
    });

    it('should render 3 <Typography /> s, if the number of comments is greater than the maximum limit to be displayed', () => {
        wrapper.setState({ startCommentsToDisplay: 1 });
        expect(wrapper.find(Typography)).toHaveLength(3);
    });
});
