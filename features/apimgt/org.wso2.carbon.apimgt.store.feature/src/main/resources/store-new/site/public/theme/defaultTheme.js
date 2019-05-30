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
                    main: '#99c573',
                    // dark: will be calculated from palette.secondary.main,
                    contrastText: '#ffcc00',
                },
                background: {
                    default: '#efefef',
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
            },
            custom: {
                starColor: '#f2c73a',
                disableColor: '#D3D3D3',
                leftMenuWidth: 220,
                contentAreaWidth: 1240,
                drawerWidth: 200,
                logo: '/store-new/site/public/images/logo.png',
                backgroundImage: '/store-new/site/public/images/back-light.png',
                title: 'WSO2 Developer Portal',
                defaultApiView: 'grid', // Sets the default view for the api listing page ( Other values available = 'list' )
                showApiHelp: false, // API detials page has a right hand side panel showing it's related help. Set this to false if you want to hide it.
                leftMenu: 'icon left', //  other values ('icon top', 'icon left', 'no icon', 'no text')
                leftMenuIconSize: 32,
                leftMenuIconMainSize: 52,
                leftMenuTextStyle: 'uppercase',
                adminRole: 'admin',
                commentsLimit: 5,
                maxCommentLength: 1300,
                resourceChipColors: {get: '#02a8f4', post: '#8ac149', put: '#ff9700', delete: '#fd5621', option: '#5f7c8a', patch: '#785446', head: '#785446'},
                imageThumbnail: {
                    width: 240, 
                    contentPictureOverlap: false, 
                    contentBackgroundColor: 'rgba(239, 239, 239, 0.5)',
                    defaultApiImage: false, //put false to render the system generated and user provided image.
                    //And put a string to render a custom image
                }
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
                    default: '#efefef',
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
            },
            custom: {
                starColor: '#f2c73a',
                disableColor: '#D3D3D3',
                leftMenuWidth: 120,
                contentAreaWidth: 1240,
                drawerWidth: 200,
                logo: '/store-new/site/public/images/logo.png',
                backgroundImage: '/store-new/site/public/images/back-light.png',
                title: 'WSO2 Developer Portal',
                defaultApiView: 'grid', // Sets the default view for the api listing page ( Other values available = 'list' )
                showApiHelp: false, // API detials page has a right hand side panel showing it's related help. Set this to false if you want to hide it.
                leftMenu: 'icon left', //  other values ('icon top', 'icon left', 'no icon', 'no text')
                leftMenuIconSize: 32,
                leftMenuIconMainSize: 52,
                leftMenuTextStyle: 'uppercase',
                adminRole: 'admin',
                commentsLimit: 5,
                maxCommentLength: 1300,
            },
        },
    },
};
