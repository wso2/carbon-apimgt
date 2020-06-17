import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import { injectIntl } from 'react-intl';
import ReactSafeHtml from 'react-safe-html';

const styles = {
    root: {
        paddingTop: 20,
        paddingBottom: 20,
    }
};

class Contact extends React.Component {
    constructor(props) {
        super(props);
    }

    handleChange = name => (event) => {
        this.setState({ [name]: event.target.value });
    };

    render() {
        const { classes, theme } = this.props;
        const { custom: { landingPage: { contact: {contactHTML} } } } = theme;
        return (
            <div className={classes.root}>
                <ReactSafeHtml html={contactHTML} />
            </div>
        );
    }
}

Contact.propTypes = {
    classes: PropTypes.object.isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(Contact));
