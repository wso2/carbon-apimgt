import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { withStyles } from '@material-ui/core/styles';
import CopyToClipboard from 'react-copy-to-clipboard';
import Tooltip from '@material-ui/core/Tooltip';
import Typography from '@material-ui/core/Typography'
import TextField from '@material-ui/core/TextField';
import { FileCopy } from '@material-ui/icons';
import InlineMessage from '../../Shared/InlineMessage';
import FormHelperText from '@material-ui/core/FormHelperText';



const styles = (theme) => ({
    epWrapper: {
        display: 'flex',
    },
    bootstrapRoot: {
        padding: 0,
        'label + &': {
          marginTop: theme.spacing.unit * 3,
        },
    },
    bootstrapInput: {
        borderRadius: 4,
        backgroundColor: theme.palette.common.white,
        border: '1px solid #ced4da',
        padding: '5px 12px',
        width: 350,
        transition: theme.transitions.create(['border-color', 'box-shadow']),
        fontFamily: [
          '-apple-system',
          'BlinkMacSystemFont',
          '"Segoe UI"',
          'Roboto',
          '"Helvetica Neue"',
          'Arial',
          'sans-serif',
          '"Apple Color Emoji"',
          '"Segoe UI Emoji"',
          '"Segoe UI Symbol"',
        ].join(','),
        '&:focus': {
          borderColor: '#80bdff',
          boxShadow: '0 0 0 0.2rem rgba(0,123,255,.25)',
        },
    },
    epWrapper: {
        display: 'flex',
        marginTop: 20,
    },
    prodLabel: {
        lineHeight: '30px',
        marginRight: 10,
        width: 100,
    },
    contentWrapper: {
        width: theme.palette.custom.contentAreaWidth - theme.palette.custom.leftMenuWidth,
    },
    root: {
        marginTop: 20,
    }
});

class ViewToken extends React.Component {
    constructor(props){
        super(props);
    }
    state = {
        tokenCopied: false,
    }
    onCopy = name => event => {
        this.setState({
          [name]: true,
        });
        let that = this;
        let elementName = name;
        var caller = function(){
            that.setState({
                [elementName]: false,
              });   
        }
        setTimeout(caller,4000);
    };
    render(){
    const { classes, token } = this.props;
        return (
            <div className={classes.root}>
                <InlineMessage type="warn"/>
                <div className={classes.epWrapper}>
                    <Typography className={classes.prodLabel}>
                        Access Token
                    </Typography>
                    <TextField
                            defaultValue={token.accessToken}
                            id="bootstrap-input"
                            InputProps={{
                            disableUnderline: true,
                            classes: {
                                root: classes.bootstrapRoot,
                                input: classes.bootstrapInput,
                            },
                            }}
                            InputLabelProps={{
                            shrink: true,
                            className: classes.bootstrapFormLabel,
                            }}
        
                        />
                    <Tooltip title={this.state.tokenCopied ? "Copied" : "Copy to clipboard"} placement="right">
                        <CopyToClipboard text={token.accessToken} onCopy={this.onCopy("tokenCopied")} >
                            <FileCopy color="secondary" />
                        </CopyToClipboard>
                    </Tooltip>
                </div>
                <FormHelperText>Above token has a validity period of {this.state.validityTime} seconds.  And the token has ( {this.state.tokenScopes} ) scopes.</FormHelperText>
            </div>)
    }
}

ViewToken.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(ViewToken);
