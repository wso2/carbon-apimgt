/**
 * IMPORTANT: This file only contains theme JSS of the Publisher app, Don't add other configuration parameters here.
 * This theme file is an extension of material-ui default theme https://material-ui.com/customization/default-theme/
 * Application related configurations are located in `<PUBLISHER_ROOT>site/public/theme/settings.js`
 */
const userThemes = {
    light(theme) {
        return (
            {
                overrides: {
                    MuiRadio: {
                        colorSecondary: {
                            '&$checked': { color: theme.palette.primary.main },
                            '&$disabled': {
                                color: theme.palette.action.disabled,
                            },
                        },
                    },
                },
            }
        );
    },
};
if (typeof module !== 'undefined') {
    module.exports = userThemes; // Added for tests
}
