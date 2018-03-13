import grey from 'material-ui/colors/grey';
import common from 'material-ui/colors/common';

export default {
    palette: {
        text: {
            primary: 'rgba(0, 0, 0, 0.87)',
            secondary: 'rgba(0, 0, 0, 0.54)',
            disabled: 'rgba(0, 0, 0, 0.38)',
            hint: 'rgba(0, 0, 0, 0.38)',
            icon: 'rgba(0, 0, 0, 0.38)',
            divider: 'rgba(0, 0, 0, 0.12)',
            lightDivider: 'rgba(0, 0, 0, 0.075)',
            banner: 'rgba(255, 255, 255, 0.9)',
        },
        input: {
            bottomLine: 'rgba(0, 0, 0, 0.42)',
            helperText: 'rgba(0, 0, 0, 0.54)',
            labelText: 'rgba(0, 0, 0, 0.54)',
            inputText: 'rgba(0, 0, 0, 0.87)',
            disabled: 'rgba(0, 0, 0, 0.42)',
        },
        action: {
            active: 'rgba(0, 0, 0, 0.54)',
            disabled: 'rgba(0, 0, 0, 0.26)',
        },
        background: {
            default: grey[50],
            paper: common.white,
            appBar: grey[100],
            contentFrame: grey[200],
        },
        custom: {
            imageIconColor: grey[50]
        }
    }
};
