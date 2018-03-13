import grey from 'material-ui/colors/grey';
import common from 'material-ui/colors/common';

export default {
    palette: {
        text: {
            primary: 'rgba(255, 255, 255, 1)',
            secondary: 'rgba(255, 255, 255, 0.7)',
            disabled: 'rgba(255, 255, 255, 0.5)',
            hint: 'rgba(255, 255, 255, 0.5)',
            icon: 'rgba(255, 255, 255, 0.5)',
            divider: 'rgba(255, 255, 255, 0.12)',
            lightDivider: 'rgba(255, 255, 255, 0.075)',
        },
        input: {
            bottomLine: 'rgba(255, 255, 255, 0.7)',
            helperText: 'rgba(255, 255, 255, 0.7)',
            labelText: 'rgba(255, 255, 255, 0.7)',
            inputText: 'rgba(255, 255, 255, 1)',
            disabled: 'rgba(255, 255, 255, 0.5)',
        },
        action: {
            active: 'rgba(255, 255, 255, 1)',
            disabled: 'rgba(255, 255, 255, 0.3)',
        },
        background: {
            default: '#303030',
            paper: grey[800],
            appBar: grey[900],
            contentFrame: grey[900],
            status: common.black,
        },
        custom: {
            imageIconColor: grey[50]
        }
    }
};
