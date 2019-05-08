import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import InboxIcon from '@material-ui/icons/Inbox';
import FaceIcon from '@material-ui/icons/Face';
import PlaceIcon from '@material-ui/icons/Place';
import MailIcon from '@material-ui/icons/Mail';

const icons = {
    InboxIcon,
    FaceIcon,
    PlaceIcon,
    MailIcon,
};

const iconNames = Object.keys(icons);
const styles = {
    svgImage: {
        cursor: 'pointer',
        // ⚠️ object-fit is not supported by IE11.
        objectFit: 'cover',
    },
};

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
     * @returns {React.PureComponent} @inheritdoc
     * @memberof ImageGenerator
     */
    render() {
        const {
            classes, api, width, height,
        } = this.props;
        const str = api.name;

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
        const randomIndex = (str.charCodeAt(0) + str.charCodeAt(str.length - 1)) % 5;
        const randomIcon = (str.charCodeAt(0) + str.charCodeAt(str.length - 1)) % iconNames.length;
        const colorPair = colorPairs[randomIndex];
        const rects = [];
        const Icon = icons[iconNames[randomIcon]];
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
            <svg width={width} height={height} className={classes.svgImage}>
                <rect {...thumbnailBox} fill={'#' + colorPair.prime.toString(16)} />
                {rects}
                <Icon />
            </svg>
        );
    }
}

ImageGenerator.defaultProps = {
    height: 190,
    width: 250,
};

ImageGenerator.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    height: PropTypes.number,
    width: PropTypes.number,
    api: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(ImageGenerator);
