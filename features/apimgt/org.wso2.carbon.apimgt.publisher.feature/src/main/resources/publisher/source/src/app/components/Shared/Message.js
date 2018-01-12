import React from 'react'
import Snackbar from 'material-ui/Snackbar';
import IconButton from 'material-ui/IconButton';
import CloseIcon from 'material-ui-icons/Close';
import Info from 'material-ui-icons/Info';
import Error from 'material-ui-icons/Error';
import Warning from 'material-ui-icons/Warning';

/**
 * @Deprecated use {Alert} class instead
 */
class Message extends React.Component{
    constructor(props){
        super(props);
        this.state = {
            open: false,
            message: '',
            type: ''
        }
    }
    info = (message) => {
        this.setState({ open: true, type: 'info' , message});
    };
    error = (message) => {
        this.setState({ open: true, type: 'error', message });
    };
    warning = (message) => {
        this.setState({ open: true, type: 'warning', message });
    };

    handleRequestClose = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        this.setState({ open: false });
    };

    render(){
        return <Snackbar
                anchorOrigin={{
                    vertical: 'top',
                    horizontal: 'center',
                }}
                open={this.state.open}
                autoHideDuration={12000}
                onClose={this.handleRequestClose}
                SnackbarContentProps={{
                    'aria-describedby': 'message-id',
                }}
                message={
                    <div id="message-id" className="message-content-box">
                        {this.state.type === 'info' && <Info />}
                        {this.state.type === 'error' && <Error />}
                        {this.state.type === 'warning' && <Warning />}
                        <span >
                    {this.state.message}</span></div>}
                action={[
                    <IconButton
                        key="close"
                        aria-label="Close"
                        color="inherit"
                        onClick={this.handleRequestClose}
                    >
                        <CloseIcon />
                    </IconButton>,
                ]}
            />

    }
}
export default Message;