import  React from 'react'
import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import Button from 'material-ui/Button';
import Card from 'material-ui/Card';
import TextField from 'material-ui/TextField';
import Api from '../../../data/api'
// TODO: need to add alert library to store as well
// import Alert from '../../Shared/alert'

class Forum extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            commentList: [],
            comment: '',
        };
        this.api_uuid = this.props.match.params.api_uuid;
        this.updateCommentString = this.updateCommentString.bind(this);
        this.handleAddComment = this.handleAddComment.bind(this);

    }

    handleGetAllComments() {
        var api = new Api();
        let promise_get = api.getAllComments(this.api_uuid);
        promise_get.then(
            response => {
                var index = 0;
                var comments = [];
                this.setState({commentList: response.obj.list});
            }).catch(
            error => {
                message.error("Error occurred while retrieving comments!");
            }
        );
    }


    updateCommentString(event) {
        this.setState({comment: event.target.value});
    }

    handleAddComment() {
        var api = new Api();
        let commentInfo = {"commentText": this.state.comment};
        let promise = api.addComment(this.api_uuid, commentInfo);
        promise.then(
            response => {
                this.handleGetAllComments();
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
        const {commentList, comment} = this.state;
        return (
            <Paper>
                <Grid container className="tab-grid" spacing={0}>
                    <Grid item xs={12}>
                        <div>
                            <p>Comments</p>
                            <Grid item>
                                <TextField
                                    id="multiline-static"
                                    label="With placeholder multiline"
                                    placeholder="Placeholder"
                                    multiline
                                    rows="4"
                                    defaultValue="Default Value"
                                    helperText="Some important text"
                                    margin="normal"
                                    value={comment}
                                    onChange={this.updateCommentString}
                                />
                            </Grid>
                            <Grid item>
                                <Button onClick={this.handleAddComment}>Add</Button>
                            </Grid>
                        </div>
                        <div>
                            {commentList && commentList.map((comment) => (
                                    <div>
                                        <Grid item>
                                            <Card bodyStyle={{padding: 5}} style={{background: "#e0d9d8"}}>
                                                <p>{comment.commentText}</p></Card>
                                        </Grid>

                                        <Grid item>
                                            <p>Posted By {comment.createdBy} at {comment.createdTime}</p>
                                        </Grid>
                                    </div>))}
                        </div>
                    </Grid>
                </Grid>
            </Paper>
        );
    }
}
export default Forum