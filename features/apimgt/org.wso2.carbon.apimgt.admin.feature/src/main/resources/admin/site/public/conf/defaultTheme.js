/**
 * IMPORTANT: This file only contains theme JSS of the Admin portal app, Don't add other configuration parameters here.
 * This theme file is an extension of material-ui default theme https://material-ui.com/customization/default-theme/
 * Application related configurations are located in `<ADMIN_PORTAL_ROOT>site/public/theme/settings.js`
 */
const AppThemes = {
    light: {
        palette: {
            primary: {
                // light: will be calculated from palette.primary.main,
                main: '#15b8cf',
                // dark: will be calculated from palette.primary.main,
                // contrastText: will be calculated to contrast with palette.primary.main
            },
            secondary: {
                light: '#0066ff',
                main: '#a2ecf5',
                // dark: will be calculated from palette.secondary.main,
                contrastText: '#ffcc00',
            },
            background: {
                default: '#f6f6f6',
                paper: '#ffffff',
                appBar: '#1d344f',
            },
        },
        typography: {
            fontFamily: '"Open Sans", "Helvetica", "Arial", sans-serif',
            fontSize: 12,
            subtitle2: {
                fontWeight: 600,
                fontSize: '0.875rem',
            },
            h4: {
                fontSize: '1.3rem',
            },
        },
        custom: {
            logo: '/site/public/images/logo-inverse.svg', // todo: change logo here
            logoHeight: 30,
            logoWidth: 222,
        },
    },
};
if (typeof module !== 'undefined') {
    module.exports = AppThemes; // Added for tests
}
