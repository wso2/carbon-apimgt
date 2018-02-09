import React from 'react'
import Dialog, {DialogActions, DialogContent, DialogContentText, DialogTitle,} from 'material-ui/Dialog';
import Button from 'material-ui/Button';

class Confirm extends React.Component {
    constructor(props) {
        super(props);
    }

    handleRequestClose(action) {
        const {callback} = this.props;
        action === Action.OK ? callback(true) : callback(false);
    }

    render(props) {
        const {title, message, labelCancel, labelOk, open} = this.props;

        return (
            <Dialog open={open} onClose={this.handleRequestClose}>
                <DialogTitle>
                    {title || 'Please Confirm'}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        {message || 'Are you sure?'}
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => this.handleRequestClose(Action.CANCEL)} color="primary">
                        {labelCancel || 'Cancel'}
                    </Button>
                    <Button onClick={() => this.handleRequestClose(Action.OK)} color="primary">
                        {labelOk || 'OK'}
                    </Button>
                </DialogActions>
            </Dialog>
        )
    }
}

const Action = {
    OK: 'ok',
    CANCEL: 'cancel'
};

export default Confirm;