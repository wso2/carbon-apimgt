import grey from '@material-ui/core/colors/grey';

export default {
    palette: {
        text: {
            primary: 'rgba(178, 223, 219, 1)',
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
            default: 'rgba(96, 125, 139, 1)',
            paper: 'rgba(69, 90, 100, 1)',
            appBar: 'rgba(38, 50, 56, 1)',
            contentFrame: grey[900],
            active: 'rgba(67, 160, 71, 1)',
        },
    },
};
