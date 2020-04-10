import React from 'react';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import Typography from '@material-ui/core/Typography';
import 'react-tagsinput/react-tagsinput.css';
import Button from '@material-ui/core/Button';
import withStyles from '@material-ui/core/styles/withStyles';

const styles = (theme) => ({
    root: {
        paddingTop: 0,
        paddingLeft: 0,
    },
    buttonProgress: {
        position: 'relative',
        margin: theme.spacing(1),
    },
    headline: { paddingTop: theme.spacing(1.25), paddingLeft: theme.spacing(2.5) },
    heading: {
        flexGrow: 1,
        marginTop: 10,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: theme.spacing(2),
    },
    mainTitle: {
        paddingLeft: 0,
    },
    button: {
        textDecoration: 'none',
        color: theme.palette.getContrastText(theme.palette.primary.main),
        marginLeft: theme.spacing(1),
    },
    buttonIcon: {
        marginRight: theme.spacing(1),
    },
    content: {
        margin: `${theme.spacing(2)}px 0 ${theme.spacing(2)}px 0`,
    },
    head: {
        fontWeight: 200,
    },
});

const CreateBanner = (props) => {
    const {
        classes, title, description, buttonText, onClick,
    } = props;
    return (
        <InlineMessage type='info' height={140}>
            <div className={classes.contentWrapper}>
                <Typography variant='h5' component='h3' className={classes.head}>
                    {title}
                </Typography>
                <Typography component='p' className={classes.content}>
                    {description}
                </Typography>
                <div className={classes.actions}>
                    <Button
                        variant='contained'
                        color='primary'
                        className={classes.button}
                        onClick={onClick}
                    >
                        {buttonText}
                    </Button>
                </div>
            </div>
        </InlineMessage>
    );
};

export default withStyles(styles)(CreateBanner);
