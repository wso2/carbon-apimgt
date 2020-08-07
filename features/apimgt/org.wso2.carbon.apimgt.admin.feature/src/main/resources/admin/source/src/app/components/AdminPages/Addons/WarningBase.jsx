/* eslint-disable react/jsx-props-no-spreading */
import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import WarningIcon from '@material-ui/icons/Warning';

const useStyles = makeStyles((theme) => ({
    root: {},
    warningIcon: {
        color: theme.palette.warning.dark,
        fontSize: 44,
    },
}));
/**
 * Adds two numbers together.
 * @param {JSON} props The first number.
 * @returns {JSX} Render the inline warning message
 */
export default function SimplePaper(props) {
    const classes = useStyles();
    const { content, title, pageProps } = props;

    return (
        <ContentBase
            {...pageProps}
            pageStyle='small'
        >
            <Card className={classes.root}>
                <CardContent>
                    <Box display='flex' flexDirection='row'>
                        <WarningIcon className={classes.warningIcon} />
                        <Typography variant='h5' component='h2'>
                            {title}
                        </Typography>
                    </Box>

                    <Typography variant='body2' color='textSecondary' component='p'>
                        {content}
                    </Typography>
                </CardContent>
            </Card>
        </ContentBase>
    );
}
