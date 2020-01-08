import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/';
import Typography from '@material-ui/core/Typography';
import { Link } from 'react-router-dom';
import classNames from 'classnames';
import CustomIcon from '../../../Shared/CustomIcon';

const styles = (theme) => ({
    leftLInkText: {
        color: theme.palette.getContrastText(theme.palette.background.leftMenu),
    },
    leftLInkText_IconLeft: {
        paddingLeft: 10,
    },
    LeftMenu: {
        backgroundColor: theme.palette.background.leftMenu,
        width: theme.custom.leftMenuWidth,
        textAlign: 'center',
        fontFamily: theme.typography.fontFamily,
        position: 'absolute',
        bottom: 0,
        left: 0,
        top: 0,
    },
    leftLInk: {
        paddingTop: theme.spacing(1),
        paddingBottom: theme.spacing(1),
        paddingLeft: theme.spacing(1),
        paddingRight: theme.spacing(1),
        fontSize: theme.typography.caption.fontSize,
        cursor: 'pointer',
    },
    leftLink_IconLeft: {
        display: 'flex',
        alignItems: 'center',
    },
    noIcon: {
        display: 'none',
    },
    leftLInkText_NoText: {
        diplay: 'none',
    },
    leftLinkAnchor: {
        display: 'block',
    },
    leftLInkMain: {
        borderRight: 'solid 1px ' + theme.palette.background.leftMenu,
        paddingBottom: theme.spacing(1),
        paddingTop: theme.spacing(1),
        cursor: 'pointer',
        backgroundColor: theme.palette.background.leftMenuActive,
        color: theme.palette.getContrastText(theme.palette.background.leftMenuActive),
        textDecoration: 'none',
    },
});

const NavItem = (props) => {
    const {
        listItemProps, listItemTextProps, iconProps, classes, theme, ...other
    } = props;
    const {
        selected, name, linkTo, NavIcon,
    } = other;
    const { leftMenu, leftMenuIconMainSize } = theme.custom;

    return linkTo ? (
        <div
            className={classNames(
                classes.leftLInk,
                {
                    [classes.leftLink_IconLeft]: leftMenu === 'icon left',
                },
                'leftLInk',
            )}
            style={{ backgroundColor: selected ? theme.palette.background.appBar : '' }}
        >
            <Link to={linkTo} className={classNames(classes.leftLinkAnchor, 'leftLinkAnchor')}>
                {NavIcon}
                <Typography
                    className={classNames(
                        classes.leftLInkText,
                        {
                            [classes.leftLInkText_IconLeft]: leftMenu === 'icon left',
                            [classes.leftLInkText_NoText]: leftMenu === 'no text',
                        },
                        'leftLInkText',
                    )}
                    style={{ textTransform: 'uppercase' }}
                >
                    {name}
                </Typography>
            </Link>
        </div>
    ) : (
        <Link to='/apis'>
            <div className={classes.leftLInkMain}>
                <CustomIcon width={leftMenuIconMainSize} height={leftMenuIconMainSize} icon='api' />
            </div>
        </Link>
    );
};

NavItem.defaultProps = {
    listItemProps: {},
    listItemTextProps: {},
    iconProps: {},
    onClick: () => {},
    linkTo: undefined,
    selected: false,
};

NavItem.propTypes = {
    listItemProps: PropTypes.shape({}),
    listItemTextProps: PropTypes.shape({}),
    iconProps: PropTypes.shape({}),
    name: PropTypes.string.isRequired,
    onClick: PropTypes.func,
    linkTo: PropTypes.string,
    classes: PropTypes.shape({}).isRequired,
    selected: PropTypes.bool,
    theme: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(NavItem);
