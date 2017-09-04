import React from 'react'
import Dialog, {
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
} from 'material-ui/Dialog';

class Confirm extends React.Component{
    constructor(props){
        super(props);
        this.state(
            {
                open: false
            }
        )
    }
    handleRequestClose(action){
        this.setState({ open: false });
        action === "ok" ? this.props.callback(true) : this.props.callback(false);
    }
    render(props){
        return(
            <Dialog open={this.state.open} onRequestClose={this.handleRequestClose}>
                <DialogTitle>
                    { props.title ? props.title : 'Please Confirm' }
                </DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        { props.message ? props.message : 'Are you sure?' }
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => this.handleRequestClose("cancel")} color="primary">
                        { props.labelCancel ? props.labelCancel : 'Cancel'}
                    </Button>
                    <Button onClick={() => this.handleRequestClose("ok")} color="primary">
                        { props.labelOk ? props.labelOk : 'OK'}
                    </Button>
                </DialogActions>
            </Dialog>
        )
    }
}

export default Confirm;