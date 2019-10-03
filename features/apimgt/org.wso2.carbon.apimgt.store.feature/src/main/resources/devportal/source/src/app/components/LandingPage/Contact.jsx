import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import { FormattedMessage, injectIntl } from 'react-intl';

const styles = {
    card: {
        minWidth: 275,
    },
    bullet: {
        display: 'inline-block',
        margin: '0 2px',
        transform: 'scale(0.8)',
    },
    title: {
        fontSize: 14,
    },
    pos: {
        marginBottom: 12,
    },
};

class Contact extends React.Component {
    constructor(props) {
        super(props);
    }

    handleChange = name => (event) => {
        this.setState({ [name]: event.target.value });
    };

    render() {
        const { classes, intl } = this.props;

        return (
            <Card className={classes.card}>
                <CardContent>
                    <TextField
                        id='standard-full-width'
                        label={intl.formatMessage({
                            defaultMessage: 'Name',
                            id: 'LandingPage.Contact.name',
                        })}
                        style={{ margin: 8 }}
                        placeholder={intl.formatMessage({
                            defaultMessage: 'Enter your name',
                            id: 'LandingPage.Contact.name.placeholder',
                        })}
                        helperText={intl.formatMessage({
                            defaultMessage: 'Let us know who you are.',
                            id: 'LandingPage.Contact.name.helperText',
                        })}
                        fullWidth
                        margin='normal'
                        InputLabelProps={{
                            shrink: true,
                        }}
                    />
                    <TextField
                        id='standard-full-width'
                        label={intl.formatMessage({
                            defaultMessage: 'Email',
                            id: 'LandingPage.Contact.email',
                        })}
                        style={{ margin: 8 }}
                        placeholder={intl.formatMessage({
                            defaultMessage: 'Enter your email',
                            id: 'LandingPage.Contact.email.placeholder',
                        })}
                        helperText={intl.formatMessage({
                            defaultMessage: 'Let us know your email address.',
                            id: 'LandingPage.Contact.email.helperText',
                        })}
                        fullWidth
                        margin='normal'
                        InputLabelProps={{
                            shrink: true,
                        }}
                    />
                    <TextField
                        id='standard-full-width'
                        label={intl.formatMessage({
                            defaultMessage: 'Message',
                            id: 'LandingPage.Contact.message',
                        })}
                        style={{ margin: 8 }}
                        placeholder={intl.formatMessage({
                            defaultMessage: 'Briefly write your message.',
                            id: 'LandingPage.Contact.message.placeholder',
                        })}
                        helperText={intl.formatMessage({
                            defaultMessage: 'Let us know what you think',
                            id: 'LandingPage.Contact.message.helperText',
                        })}
                        fullWidth
                        multiline
                        margin='normal'
                        InputLabelProps={{
                            shrink: true,
                        }}
                    />
                </CardContent>
                <CardActions>
                    <Button variant='contained' color='primary'>
                        <FormattedMessage id='LandingPage.Contact.submit' defaultMessage='Submit' />
                    </Button>
                </CardActions>
            </Card>
        );
    }
}

Contact.propTypes = {
    classes: PropTypes.object.isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default injectIntl(withStyles(styles)(Contact));
