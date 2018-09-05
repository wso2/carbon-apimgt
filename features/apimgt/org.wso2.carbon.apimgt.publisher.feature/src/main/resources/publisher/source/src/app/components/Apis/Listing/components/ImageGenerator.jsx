import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import * as icons from '@material-ui/icons/';
import { Link } from 'react-router-dom';

const styles = theme => ({
    svgImage: {
        cursor: 'pointer',
        // ⚠️ object-fit is not supported by IE11.
        objectFit: 'cover',
    },
    suppressLinkStyles: {
        textDecoration: 'none',
        color: theme.palette.text.disabled,
    },
});

/**
 * Generate dynamic API thumbnail image (SVG), Use PureComponent to avoid unnessasary re-rendering when hover ect
 *
 * @class ImageGenerator
 * @extends {PureComponent}
 */
class ImageGenerator extends PureComponent {
    /**
     *
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof ImageGenerator
     */
    render() {
        const { classes, apiName, id } = this.props;
        const str = apiName;
        const overviewPath = `apis/${id}/overview`;

        const colorPairs = [
            { prime: 0x8f6bcaff, sub: 0x4fc2f8ff },
            { prime: 0xf47f16ff, sub: 0xcddc39ff },
            { prime: 0xf44236ff, sub: 0xfec107ff },
            { prime: 0x2196f3ff, sub: 0xaeea00ff },
            { prime: 0xff9700ff, sub: 0xffeb3cff },
            { prime: 0xff9700ff, sub: 0xfe5722ff },
        ];
        const thumbnailBox = {
            width: 250,
            height: 200,
        };

        const thumbnailBoxChild = {
            width: 50,
            height: 50,
        };
        // Get a random color pair
        const allChars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_&!#$';
        let iconIndex = 0;
        let colorIndex = str.length;
        for (let i = 0; i < str.length; i++) {
            iconIndex += allChars.indexOf(str[i]);
        }
        while (colorIndex > 5) {
            colorIndex -= 6;
        }
        // let colorIndex = Math.floor(Math.random() * Math.floor(colorPairs.length)); //Get a random color combination
        const randomIndex = Math.floor(Math.random() * colorIndex);
        // returns a random integer from 0 to API name length
        const colorPair = colorPairs[randomIndex];
        // let iconIndex = Math.floor(Math.random() * Math.floor(Object.keys(icons).length)); // Get a random icon index
        let tmpIndex = 0;
        let icon = null;
        for (const i in icons) {
            if (Object.prototype.hasOwnProperty.call(icons, i)) {
                tmpIndex++;
                if (tmpIndex === iconIndex) {
                    icon = icons[i];
                }
            }
        }
        const rects = [];
        const Icon = icon;
        for (let i = 0; i <= 4; i++) {
            for (let j = 0; j <= 4; j++) {
                rects.push(<rect
                    key={i + '_' + j}
                    {...thumbnailBoxChild}
                    fill={'#' + (colorPair.sub - (0x00000025 * i) - (j * 0x00000015)).toString(16)}
                    x={200 - (i * 54)}
                    y={54 * j}
                />);
            }
        }
        return (
            <Link className={classes.suppressLinkStyles} to={overviewPath}>
                <svg width='250' height='190' className={classes.svgImage}>
                    <rect {...thumbnailBox} fill={'#' + colorPair.prime.toString(16)} />
                    {rects}
                    <Icon />
                </svg>
            </Link>
        );
    }
}
ImageGenerator.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(ImageGenerator);
