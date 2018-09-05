import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import {FileCopy } from '@material-ui/icons'
import Typography from '@material-ui/core/Typography'
import TextField from '@material-ui/core/TextField';


const styles = theme => ({
    topBar: {
        display: 'flex',
        paddingBottom: theme.spacing.unit * 2,
    },
    infoContent: {
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey['A200'],
        padding: theme.spacing.unit * 3,

    },
    infoItem: {
        marginRight: theme.spacing.unit * 4,
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
        width: 250,
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
    },
    prodLabel: {
        lineHeight: '30px',
        marginRight: 10,
        width: 100,
    },
    contentWrapper: {
        width: theme.palette.custom.contentAreaWidth - theme.palette.custom.leftMenuWidth,
    }
});

class APIDetial extends React.Component {
  state = {
  };

 
  render() {
    const { classes, theme } = this.props;

    return (
        <div className={classes.infoContent}>
            <div className={classes.contentWrapper}>
                <div className={classes.topBar}>
                    <div className={classes.infoItem}>
                        <Typography variant="subheading" gutterBottom>
                            1.0.0
                        </Typography>
                        <Typography variant="caption" gutterBottom align="left">
                            Version
                        </Typography>
                    </div>
                    <div className={classes.infoItem}>
                        <Typography variant="subheading" gutterBottom>
                            /v2
                        </Typography>
                        <Typography variant="caption" gutterBottom align="left">
                            Context
                        </Typography>
                    </div>
                    <div>
                        <div className={classes.epWrapper}>
                            <Typography className={classes.prodLabel}>
                                Production URL
                            </Typography>
                            <TextField
                                    defaultValue="http://192.168.1.2:8282/SwaggerPetstore/1.0.0"
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
                            <FileCopy color="secondary" />
                        </div>
                        <div className={classes.epWrapper}>
                            <Typography className={classes.prodLabel}>
                                Sandbox URL
                            </Typography>
                            <TextField
                                    defaultValue="http://192.168.1.2:8282/SwaggerPetstore/1.0.0"
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
                            <FileCopy  color="secondary" />
                        </div>
                    </div> 
                </div>
                <Typography>
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut dolor dui, fermentum in ipsum a, pharetra feugiat orci. Suspendisse at sem nunc. Integer in eros eget orci sollicitudin ultricies. Mauris vehicula mollis vulputate. Morbi sed velit vulputate nisl ullamcorper blandit. Quisque diam orci, ultrices at risus vel, auctor vulputate odio. Etiam vel iaculis massa, vel sollicitudin velit. Aenean facilisis vitae elit vitae iaculis. Nam vel tincidunt arcu.
                </Typography> 
            </div>
        </div>
        
    );
  }
}

APIDetial.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(APIDetial);
