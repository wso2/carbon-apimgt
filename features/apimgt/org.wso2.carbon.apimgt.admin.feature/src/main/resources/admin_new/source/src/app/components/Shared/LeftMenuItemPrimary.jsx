/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React, { useState } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import Collapse from '@material-ui/core/Collapse';
import ExpandLess from '@material-ui/icons/ExpandLess';
import ExpandMore from '@material-ui/icons/ExpandMore';
import { withRouter } from 'react-router-dom';
import LeftMenuItemSecondary from './LeftMenuItemSecondary';

/* eslint-disable*/
// todo: format code
const styles = (theme) => ({
  expansionArrow: {
    color: theme.palette.getContrastText(theme.palette.background.leftMenu),
  },
  leftLInkText: {
    color: theme.palette.getContrastText(theme.palette.background.leftMenu),
    textTransform: theme.custom.leftMenuTextStyle,
    width: '100%',
    textAlign: 'left',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
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
    textDecoration: 'none',
  },
  leftLink_Icon: {
    color: theme.palette.getContrastText(theme.palette.background.leftMenu),
    fontSize: theme.custom.leftMenuIconSize + 'px',
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
  nested: {
    paddingLeft: theme.spacing(4),
  },
});

/**
 *
 *
 * @param {*} props
 * @returns
 */
function LeftMenuItemPrimary(props) {
  const [selected, setSelected] = useState(false);
  const [open, setOpen] = React.useState(false);
  const handleClick = () => {
    setOpen(!open);
  };

  const {
    classes, theme, Icon, text, secondaryMenuDetails
  } = props;

  const { leftMenu } = theme.custom;
  const strokeColor = theme.palette.getContrastText(theme.palette.background.leftMenu);
  const iconSize = theme.custom.leftMenuIconSize;

  const secondaryMenuItems = secondaryMenuDetails.map((item) =>
    <LeftMenuItemSecondary
      text={item.name}
      to={item.to}
    />
  );

  return (
    <List>
      <ListItem className={classes.expansionArrow} button onClick={handleClick}>
        <CustomIcon
          strokeColor={strokeColor}
          width={iconSize}
          height={iconSize}
          icon={text}
          className={classNames(
            classes.leftLInk,
            {
              [classes.noIcon]: leftMenu === 'no icon',
            },
            'leftLink_Icon',
          )}
        />
        <Typography
          className={classNames(
            classes.leftLInkText,
            {
              [classes.leftLInkText_IconLeft]: leftMenu === 'icon left',
              [classes.leftLInkText_NoText]: leftMenu === 'no text',
            },
            'leftLInkText',
          )}
        >
          {text}
        </Typography>
        {open ? <ExpandLess /> : <ExpandMore />}
      </ListItem>
      <Collapse in={open} timeout="auto" unmountOnExit>
        {secondaryMenuItems}
      </Collapse>
    </List>
  );
}
LeftMenuItemPrimary.defaultProps = {
  route: null,
};
LeftMenuItemPrimary.propTypes = {
  classes: PropTypes.shape({
    divider: PropTypes.string,
    leftLInk: PropTypes.string,
    leftLink_IconLeft: PropTypes.string,
    noIcon: PropTypes.string,
    leftLink_Icon: PropTypes.string,
    leftLInkText: PropTypes.string,
    leftLInkText_IconLeft: PropTypes.string,
    leftLInkText_NoText: PropTypes.string,
  }).isRequired,
  theme: PropTypes.shape({
    custom: PropTypes.shape({
      leftMenu: PropTypes.string,
      leftMenuIconSize: PropTypes.number,
    }),
    palette: PropTypes.shape({
      getContrastText: PropTypes.func,
      background: PropTypes.shape({
        leftMenu: PropTypes.string,
        appBar: PropTypes.string,
      }),
      leftMenu: PropTypes.string,
    }),
  }).isRequired,
  Icon: PropTypes.element.isRequired,
  text: PropTypes.string.isRequired,
  to: PropTypes.string.isRequired,
  route: PropTypes.string,
  history: PropTypes.shape({
    listen: PropTypes.func.isRequired,
    location: PropTypes.string.isRequired,
  }).isRequired,
};
export default withRouter(withStyles(styles, { withTheme: true })(LeftMenuItemPrimary));
