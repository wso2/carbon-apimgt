import  React from 'react'
import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import Button from 'material-ui/Button';
import Card from 'material-ui/Card';
import TextField from 'material-ui/TextField';
import Typography from 'material-ui/Typography'
import Api from '../../../data/api'
import {CircularProgress} from "material-ui/Progress";
import {withStyles} from 'material-ui/styles';
import PropTypes from 'prop-types';
import List, { ListItem, ListItemText } from 'material-ui/List';
import Avatar from 'material-ui/Avatar';
import ImageIcon from 'material-ui-icons/Image';
// TODO: need to add alert library to store as well
// import Alert from '../../Shared/alert'

const styles = theme => ({
    titleBar: {
        display: 'flex',
        justifyContent: 'space-between',
        borderBottomWidth: '1px',
        borderBottomStyle: 'solid',
        borderColor: theme.palette.text.secondary,
        marginBottom: 20,
    }
});

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
        const { classes } = this.props;
        return (
            <Grid container>
                <Grid item xs={12} sm={12} md={12} lg={11} xl={10}  >
                    <TextField
                            label="Comment"
                            InputLabelProps={{
                                shrink: true,
                            }}
                            helperText="Please proved your comments on this API "
                            fullWidth
                            name="name"
                            multiline
                            rows="4"
                            onChange={this.updateCommentString}
                            placeholder="Type your comments here"
                            autoFocus={true}
                            className={classes.inputText}
                        />
                </Grid>
                <Grid item xs={12} sm={12} md={12} lg={11} xl={10}  >
                    <Button variant="raised" color="primary"  onClick={this.handleAddComment}>
                            Add New Comment
                    </Button>
                </Grid>
                <Grid item xs={12}>
                <Paper>
                    <List>
                        {commentList ? commentList.map((comment,index) => (
                            <ListItem key={index}>
                                <Avatar>
                                    <ImageIcon />
                                </Avatar>
                                <ListItemText primary={comment.commentText} 
                                                secondary={ "Posted By "  + comment.createdBy +  " at " + comment.createdTime } />
                            </ListItem>))
                                        : 
                            <CircularProgress/>
                        }
                    </List>
                    </Paper>
                </Grid>
            </Grid>


            
        );
    }
}

Forum.propTypes = {
    classes: PropTypes.object.isRequired,
};
  
export default withStyles(styles)(Forum);