const APP_CONTEXT = '/store-new';
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
                    leftMenuActive: '#254061',
                    drawer: '#1a1f2f',
                    activeMenuItem: '#254061',
                },
            },
            typography: {
                fontFamily: '"Open Sans", "Helvetica", "Arial", sans-serif',
                fontSize: 12,
            },
            custom: {
                starColor: '#f2c73a',
                disableColor: '#D3D3D3',
                leftMenuWidth: 200,
                contentAreaWidth: 1240,
                drawerWidth: 200,
                logo: '/store-new/site/public/images/logo.png',
                backgroundImage: '/store-new/site/public/images/back-light.png',
                title: 'WSO2 Developer Portal',
                defaultApiView: 'grid', // Sets the default view for the api listing page ( Other values available = 'list' )
                showApiHelp: false, // API detials page has a right hand side panel showing it's related help. Set this to false if you want to hide it.
                leftMenu: 'icon left', //  other values ('icon top', 'icon left', 'no icon', 'no text')
                leftMenuIconSize: 24,
                leftMenuIconMainSize: 52,
                leftMenuTextStyle: 'uppercase',
                adminRole: 'admin',
                commentsLimit: 5,
                maxCommentLength: 512,
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
                    width: 240,
                    contentPictureOverlap: false,
                    iconColor: '#ffffff',
                    listViewIconSize: 20,
                    contentBackgroundColor: 'rgba(239, 239, 239, 0.5)',
                    defaultApiImage: false, // put false to render the system generated and user provided image.
                    // And put a string to render a custom image
                    backgrounds: [
                        // These backgrounds are use to generate the thumbnails.
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
                landingPage: {
                    active: true,
                    carousel: {
                        active: true,
                        slides: [
                            {
                                src: '/store-new/site/public/images/landing/01.jpg',
                                title: 'Lorem <span>ipsum</span> dolor sit amet',
                                content:
                                    'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer felis lacus, placerat vel condimentum in, porta a urna. Suspendisse dolor diam, vestibulum at molestie dapibus, semper eget ex. Morbi sit amet euismod tortor.',
                            },
                            {
                                src: '/store-new/site/public/images/landing/02.jpg',
                                title: 'Curabitur <span>malesuada</span> arcu sapien',
                                content:
                                    'Curabitur malesuada arcu sapien, suscipit egestas purus efficitur vitae. Etiam vulputate hendrerit venenatis. ',
                            },
                            {
                                src: '/store-new/site/public/images/landing/03.jpg',
                                title: 'Nam vel ex <span>feugiat</span> nunc laoreet',
                                content:
                                    'Nam vel ex feugiat nunc laoreet elementum. Duis sed nibh condimentum, posuere risus a, mollis diam. Vivamus ultricies, augue id pulvinar semper, mauris lorem bibendum urna, eget tincidunt quam ex ut diam.',
                            },
                        ],
                    },
                    listByTag: {
                        active: true,
                        content: [
                            {
                                tag: 'finance',
                                title: 'Checkout our Finance APIs',
                                description:
                                    'We offers online payment solutions and has more than 123 million customers worldwide. The WSO2 Finane API makes powerful functionality available to developers by exposing various features of our platform. Functionality includes but is not limited to invoice management, transaction processing and account management.',
                                maxCount: 5,
                            },
                            {
                                tag: 'weather',
                                title: 'Checkout our Weather APIs',
                                description:
                                    'We offers online payment solutions and has more than 123 million customers worldwide. The WSO2 Finane API makes powerful functionality available to developers by exposing various features of our platform. Functionality includes but is not limited to invoice management, transaction processing and account management.',
                                maxCount: 5,
                            },
                        ],
                    },
                    parallax: {
                        active: true,
                        content: [
                            {
                                src: '/store-new/site/public/images/landing/parallax1.jpg',
                                title: 'Lorem <span>ipsum</span> dolor sit amet',
                                content:
                                    'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer felis lacus, placerat vel condimentum in, porta a urna. Suspendisse dolor diam, vestibulum at molestie dapibus, semper eget ex. Morbi sit amet euismod tortor.',
                            },
                            {
                                src: '/store-new/site/public/images/landing/parallax2.jpg',
                                title: 'Nam vel ex <span>feugiat</span> nunc laoreet',
                                content:
                                    'Nam vel ex feugiat nunc laoreet elementum. Duis sed nibh condimentum, posuere risus a, mollis diam. Vivamus ultricies, augue id pulvinar semper, mauris lorem bibendum urna, eget tincidunt quam ex ut diam.',
                            },
                        ],
                    },
                },
                tagWiseMode: false,
                tagThumbnail: {
                    width: 150,
                    defaultTagImage: '/store-new/site/public/images/api/api-default.png',
                },
                tagGroupKey: '-group',
            },
        },
    },
    app: {
        context: APP_CONTEXT,
    },
    grantTypes: {
        authorization_code: 'Code',
        implicit: 'Implicit',
        refresh_token: 'Refresh Token',
        password: 'Password',
        'iwa:ntlm': 'IWA-NTLM',
        client_credentials: 'Client Credentials',
        'urn:ietf:params:oauth:grant-type:saml2-bearer': 'SAML2',
        'urn:ietf:params:oauth:grant-type:jwt-bearer': 'JWT',
        kerberos: 'Kerberos',
    },
};
