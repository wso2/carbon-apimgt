const APP_CONTEXT = '/publisher-new';
const Configurations = {
    themes: {
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
                    leftMenu: '#1a1f2f',
                    leftMenuActive: '#000000',
                    drawer: '#1a1f2f',
                },
            },
            typography: {
                fontFamily: '"Open Sans", "Helvetica", "Arial", sans-serif',
                fontSize: 12,
                subtitle2: {
                    fontWeight: 600,
                },
                h4: {
                    fontSize: '1.3rem',
                },
            },
            custom: {
                starColor: '#f2c73a',
                disableColor: '#D3D3D3',
                leftMenuWidth: 200,
                contentAreaWidth: 1240,
                drawerWidth: 200,
                logo: APP_CONTEXT + '/site/public/images/logo.png',
                backgroundImage: APP_CONTEXT + '/site/public/images/back-light.png',
                title: 'WSO2 APIM Publisher',
                defaultApiView: 'grid', // Sets the default view for the api listing page ( Other values available = 'list' )
                showApiHelp: false, // API detials page has a right hand side panel showing it's related help. Set this to false if you want to hide it.
                leftMenu: 'icon left', //  other values ('icon top', 'icon left', 'no icon', 'no text')
                leftMenuIconSize: 24,
                leftMenuIconMainSize: 52,
                leftMenuTextStyle: 'uppercase',
                resourceChipColors: {
                    get: '#02a8f4',
                    post: '#8ac149',
                    put: '#ff9700',
                    delete: '#fd5621',
                    option: '#5f7c8a',
                    patch: '#785446',
                    head: '#785446',
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
                },
                // lifeCycleImage: APP_CONTEXT + '/public/app/images/logo.png',
                // Uncomment above if you want to add a custom image to the lifecycle diagram.
                adminRole: 'admin',
                commentsLimit: 5,
                maxCommentLength: 1300,
            },
        },
        dark: {
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
                    leftMenu: '#1a1f2f',
                    leftMenuActive: '#000000',
                    drawer: '#1a1f2f',
                },
            },
            typography: {
                fontFamily: '"Open Sans", "Helvetica", "Arial", sans-serif',
                fontSize: 12,
                subtitle2: {
                    fontWeight: 600,
                },
                h4: {
                    fontSize: '1.3rem',
                },
            },
            custom: {
                starColor: '#f2c73a',
                disableColor: '#D3D3D3',
                leftMenuWidth: 200,
                contentAreaWidth: 1240,
                drawerWidth: 200,
                logo: APP_CONTEXT + '/site/public/images/logo.png',
                backgroundImage: APP_CONTEXT + '/site/public/images/back-light.png',
                title: 'WSO2 APIM Publisher',
                defaultApiView: 'grid', // Sets the default view for the api listing page ( Other values available = 'list' )
                showApiHelp: false, // API detials page has a right hand side panel showing it's related help. Set this to false if you want to hide it.
                leftMenu: 'icon left', //  other values ('icon top', 'icon left', 'no icon', 'no text')
                leftMenuIconSize: 24,
                leftMenuIconMainSize: 52,
                leftMenuTextStyle: 'uppercase',
                resourceChipColors: {
                    get: '#02a8f4',
                    post: '#8ac149',
                    put: '#ff9700',
                    delete: '#fd5621',
                    option: '#5f7c8a',
                    patch: '#785446',
                    head: '#785446',
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
                },
                // lifeCycleImage: APP_CONTEXT + '/public/app/images/logo.png',
                // Uncomment above if you want to add a custom image to the lifecycle diagram.
                adminRole: 'admin',
                commentsLimit: 5,
                maxCommentLength: 1300,
            },
        },
    },
    app: {
        context: APP_CONTEXT,
        walkThrough: false,
    },
};
if (typeof module !== 'undefined') {
    module.exports = Configurations; // Added for tests
}
