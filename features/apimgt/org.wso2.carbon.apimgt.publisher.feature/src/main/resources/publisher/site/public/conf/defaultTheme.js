/**
 * IMPORTANT: This file only contains theme JSS of the Publisher app, Don't add other configuration parameters here.
 * This theme file is an extension of material-ui default theme https://material-ui.com/customization/default-theme/
 * Application related configurations are located in `<PUBLISHER_ROOT>site/public/theme/settings.js`
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
                appBarSelected: '#1d344f',
                leftMenu: '#1a1f2f',
                leftMenuActive: '#254061',
                drawer: '#1a1f2f',
                activeMenuItem: '#254061',
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
        zIndex: {
            apiCreateMenu: 1250,
            operationDeleteUndo: 1600,
            overviewArrow: 1,
            goToSearch: 2,
        },
        custom: {
            starColor: '#f2c73a',
            disableColor: '#D3D3D3',
            leftMenuWidth: 210,
            contentAreaWidth: 1240,
            drawerWidth: 200,
            logo: '/site/public/images/logo.svg',
            logoHeight: 40,
            logoWidth: 222,
            title: 'WSO2 APIM Publisher',
            defaultApiView: 'grid', // Sets the default view for the api listing page ( Other values available = 'list' )
            showApiHelp: false, // API detials page has a right hand side panel showing it's related help. Set this to false if you want to hide it.
            leftMenu: 'icon left', //  other values ('icon top', 'icon left', 'no icon', 'no text')
            leftMenuIconSize: 24,
            leftMenuIconMainSize: 52,
            leftMenuTextStyle: 'capitalize',
            resourceChipColors: { // https://github.com/swagger-api/swagger-ui/blob/master/src/style/_variables.scss#L45-L52
                get: '#61affe',
                post: '#49cc90',
                put: '#fca130',
                delete: '#f93e3e',
                options: '#0d5aa7',
                patch: '#50e3c2',
                head: '#9012fe',
                trace: '#785446',
                disabled: '#ebebeb',
            },
            operationChipColor: {
                query: '#b3e6fe',
                mutation: '#c1dea0',
                subscription: '#ffcc80',
            },
            thumbnail: {
                backgrounds: [ // These backgrounds are use to generate the thumbnails.
                    { prime: 0x8f6bcaff, sub: 0x4fc2f8ff },
                    { prime: 0xf47f16ff, sub: 0xcddc39ff },
                    { prime: 0xf44236ff, sub: 0xfec107ff },
                    { prime: 0x2196f3ff, sub: 0xaeea00ff },
                    { prime: 0xff9700ff, sub: 0xffeb3cff },
                    { prime: 0xff9700ff, sub: 0xfe5722ff },
                ],
                document: {
                    icon: 'library_books',
                    backgrounds: {
                        prime: 0xcff7ffff,
                        sub: 0xe2fff7ff,
                    },
                },
            },
            // lifeCycleImage: APP_CONTEXT + '/public/app/images/logo.png',
            // Uncomment above if you want to add a custom image to the lifecycle diagram.
            adminRole: 'admin',
            commentsLimit: 5,
            maxCommentLength: 1300,
            productSampleProgess: {
                backgroundMain: '#15b8cf',
                backgroundChip: '#5aebf9',
            },
            warningColor: '#ffc439',
        },
    },
};
if (typeof module !== 'undefined') {
    module.exports = AppThemes; // Added for tests
}
