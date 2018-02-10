import  React from 'react'
import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import Button from 'material-ui/Button';
import Card from 'material-ui/Card';
import TextField from 'material-ui/TextField';
import Typography from 'material-ui/Typography'
import Api from '../../../data/api'
import {CircularProgress} from "material-ui/Progress";
// TODO: need to add alert library to store as well
// import Alert from '../../Shared/alert'

class Forum extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            commentList: null,
            comment: '',
            api: {}
        };
        this.api_uuid = this.props.match.params.api_uuid;
        this.updateCommentString = this.updateCommentString.bind(this);
        this.handleAddComment = this.handleAddComment.bind(this);
        this.getAllComments();
    }

    getAllComments() {
        let api = new Api();
        let promise_get = api.getAllComments(this.api_uuid);
        promise_get.then(
            response => {
                this.setState({commentList: response.obj.list});
            }).catch(
            error => {
                console.error(error);
                /* TODO: Uncomment below line when Alert library is added to store */
                // Alert.error("Error occurred while retrieving comments!");
            }
        );
    }

    updateCommentString(event) {
        this.setState({comment: event.target.value});
    }

    handleAddComment() {
        let api = new Api();
        let commentInfo = {"commentText": this.state.comment};
        let promise = api.addComment(this.api_uuid, commentInfo);
        promise.then(
            response => {
                this.getAllComments();
                this.setState({comment: ''});
                // TODO: uncomment below once the alert library is added to store
                // Alert.success("Comment added successfully");
            }).catch(
            error => {
                // TODO: uncomment below once the alert library is added to store ~tmkb
                // Alert.error("Error occurred while adding comments!");
            }
        );
    }

    render() {
        const {commentList, comment, api} = this.state;
        return (
            <Paper>
                <Grid container spacing={0}>
                    <Grid item xs={12}>
                        <div>
                            <Typography type="title" gutterBottom>
                                Comments
                            </Typography>
                            <Grid container justify="center" spacing={0}>
                                <Grid item xs={10}>
                                    <TextField
                                        fullWidth
                                        label="Type your comments here"
                                        placeholder="Type your comments here"
                                        multiline
                                        rows="4"
                                        helperText="Please proved your comments on this API"
                                        margin="normal"
                                        value={comment}
                                        onChange={this.updateCommentString}
                                    />
                                    <Button onClick={this.handleAddComment}>Add</Button>
                                </Grid>
                                <Grid item xs={10}>
                                    {commentList ? commentList.map((comment) => (
                                        <div>
                                            <Grid item>
                                                <Card bodyStyle={{padding: 5}} style={{background: "#e0d9d8"}}>
                                                    <p>{comment.commentText}</p></Card>
                                            </Grid>
                                            <Grid item>
                                                <p>Posted By {comment.createdBy} at {comment.createdTime}</p>
                                            </Grid>
                                        </div>))
                                        : <CircularProgress/>}
                                </Grid>
                            </Grid>
                        </div>
                    </Grid>
                </Grid>
            </Paper>
        );
    }
}
export default Forum