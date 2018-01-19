import  React from 'react'
import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import Button from 'material-ui/Button';
import Card from 'material-ui/Card';
import {Row} from 'antd';


class Forum extends React.Component{
    constructor(props){
        super(props);
        this.state = {
            commentList: [],
            comment: '',
        };
        this.updateCommentString = this.updateCommentString.bind(this);
        this.handleAddComment = this.handleAddComment.bind(this);

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
                message.success("Comment added successfully");
            }).catch(
            error => {
                message.error("Error occurred while adding comments!");
            }
        );
    }

    render(){
        const {commentList, comment} = this.state;
        return (
            <Paper>
                <Grid container className="tab-grid" spacing={0} >
                    <Grid item xs={12}>
                        <div>
                            <p>Comments</p>
                            <Row>
                            <textarea cols="180" rows="4" value={comment}
                                      onChange={this.updateCommentString}> </textarea>
                            </Row>
                            <Row>
                                <Button onClick={this.handleAddComment}>Add</Button>
                            </Row>
                        </div>
                        <div>
                            {commentList && commentList.map((comment) => {
                                return <div><Row>
                                    <Card bodyStyle={{padding: 5}} style={{background: "#e0d9d8"}}>
                                        <p>{comment.commentText}</p></Card>
                                </Row>

                                    <Row>
                                        <p>Posted By {comment.createdBy} at {comment.createdTime}</p>
                                    </Row><br /></div>
                            })
                            }
                        </div>
                    </Grid>
                </Grid>
            </Paper>
        );
    }
}
export default Forum