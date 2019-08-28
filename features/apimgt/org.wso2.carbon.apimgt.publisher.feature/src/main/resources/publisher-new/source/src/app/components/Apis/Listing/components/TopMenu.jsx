import React from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import List from '@material-ui/icons/List';
import GridOn from '@material-ui/icons/GridOn';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import APICreateMenu from '../components/APICreateMenu';

const styles = theme => ({
    rightIcon: {
        marginLeft: theme.spacing.unit,
    },
    button: {
        margin: theme.spacing.unit,
        marginBottom: 0,
    },
    buttonRight: {
        alignSelf: 'flex-end',
        display: 'flex',
    },
    ListingWrapper: {
        paddingTop: 10,
        paddingLeft: 35,
    },
    root: {
        height: 70,
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        display: 'flex',
    },
    mainIconWrapper: {
        paddingTop: 13,
        paddingLeft: 35,
        paddingRight: 20,
    },
    mainTitle: {
        paddingTop: 10,
    },
    mainTitleWrapper: {},
    APICreateMenu: {
        flexGrow: 1,
        display: 'flex',
        alignItems: 'center',
    },
    content: {
        flexGrow: 1,
    },
});
class TopMenu extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            listType: this.props.theme.custom.defaultApiView,
        };
    }
    render() {
        const {
            classes, apis, setListType, theme,
        } = this.props;
        const { listType } = this.state;
        const strokeColorMain = theme.palette.getContrastText(theme.palette.background.paper);
        return (
            <div className={classes.root}>
                <div className={classes.mainIconWrapper}>
                    <CustomIcon strokeColor={strokeColorMain} width={42} height={42} icon='api' />
                </div>
                <div className={classes.mainTitleWrapper}>
                    <Typography variant='display1' className={classes.mainTitle}>
                        APIs
                    </Typography>
                    {apis && (
                        <Typography variant='caption' gutterBottom align='left'>
                            Displaying {apis.count} API
                        </Typography>
                    )}
                </div>
                <VerticalDivider height={70} />
                <div className={classes.APICreateMenu}>
                    <APICreateMenu buttonProps={{ variant: 'contained', color: 'primary', className: classes.button }}>
                        <FormattedMessage id='create.an.api' defaultMessage='Create API' />
                    </APICreateMenu>
                </div>
                <div className={classes.buttonRight}>
                    <IconButton className={classes.button} onClick={() => setListType('list')}>
                        <List color={listType === 'list' ? 'primary' : 'default'} />
                    </IconButton>
                    <IconButton className={classes.button} onClick={() => setListType('grid')}>
                        <GridOn color={listType === 'grid' ? 'primary' : 'default'} />
                    </IconButton>
                </div>
            </div>
        );
    }
}

TopMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    setListType: PropTypes.func.isRequired,
    apis: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({
        custom: PropTypes.string,
    }).isRequired,
};

export default withStyles(styles, { withTheme: true })(TopMenu);
