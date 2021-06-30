/* eslint-disable max-len */
const DefaultConfigurations = {
    direction: 'ltr',
    palette: {
        primary: {
            // light: will be calculated from palette.primary.main,
            main: '#006e9c',
            // dark: will be calculated from palette.primary.main,
            // contrastText: will be calculated to contrast with palette.primary.main
        },
        secondary: {
            light: '#347eff',
            main: '#415a85',
            // dark: will be calculated from palette.secondary.main,
            contrastText: '#ffcc00',
        },
        background: {
            default: '#ffffff',
            paper: '#ffffff',
            drawer: '#1a1f2f',
        },
    },
    typography: {
        fontFamily: '"Open Sans", "Helvetica", "Arial", sans-serif',
        fontSize: 12,
        body2: {
            lineHeight: 2,
        },
        h4: {
            fontWeight: 400,
            fontSize: '1.3rem',
        },
        caption: {
            fontSize: '0.70rem',
            color: '#666',
        },
    },
    custom: {
        contentAreaWidth: 1240,
        backgroundImage: '', // Add a watermark background to the content area of the page. Example ( '/devportal/site/public/images/back-light.png')
        defaultApiView: 'grid', // Sets the default view for the api listing page ( Other values available = 'list' ).
        // To disable one option for an example if you want to disable grid completely and get rid of the toggle buttons use ['list'].
        page: {
            style: 'fluid', // Set the page style ( Other values available 'fixed', 'fluid')
            width: 1240, // This value is effected only when the page.style = 'fixed'
            emptyAreadBackground: '#ffffff', // This value is effected only when the page.style = 'fixed' and window size is greater than page.width
            border: 'none', // It can be something like 'solid 1px #cccccc' for fixed layouts
        },
        appBar: {
            logo: '/site/public/images/logo.svg', // You can set the url to an external image also ( ex: https://dummyimage.com/208x19/66aad1/ffffff&text=testlogo)
            logoHeight: 34,
            logoWidth: 'auto',
            background: '#0fa2db',
            backgroundImage: '/site/public/images/appbarBack.png',
            searchInputBackground: '#fff',
            searchInputActiveBackground: '#fff',
            activeBackground: '#1c6584',
            showSearch: true,
            drawerWidth: 200,
        },
        leftMenu: {
            position: 'vertical-left', // Sets the position of the left menu ( 'horizontal', 'vertical-left', 'vertical-right')
            style: 'icon left', //  other values ('icon top', 'icon left', 'no icon', 'no text')
            iconSize: 24,
            leftMenuTextStyle: 'uppercase',
            width: 180,
            background: '#012534',
            backgroundImage: '/site/public/images/leftMenuBack.png',
            leftMenuActive: '#00597f',
            leftMenuActiveSubmenu: '#0d1723',
            activeBackground: '#191e46',
            rootIconVisible: false,
            rootIconSize: 42,
            rootIconTextVisible: false,
            rootBackground: '#000',
        },
        infoBar: {
            height: 70,
            background: '#ffffff',
            showThumbnail: true,
            starColor: '#f6bf21', // Rating star color
            starColorEmpty: '#cfcfcf', // Rating star color empty state
            sliderBackground: '#ffffff',
            iconOddColor: '#d3d3d3',
            iconEvenColor: '#c3c3c3',
            listGridSelectedColor: '#347eff', // Defines color of the selected icon ( grid/ list ) view of the api listing page
            tagChipBackground: '#7dd7f5',
        },
        listView: {
            tableHeadBackground: '#fff',
            tableBodyOddBackgrund: '#efefef',
            tableBodyEvenBackgrund: '#fff',
        },
        overview: {
            titleIconColor: '#89b4ff',
            titleIconSize: 16,
            noContentBackground: 'rgba(255, 255, 255, 0.2)',
        },
        adminRole: 'admin',
        commentsLimit: 5,
        maxCommentLength: 512,
        overviewPage: {
            commentsBackground: '/site/public/images/overview/comments.svg',
            documentsBackground: '/site/public/images/overview/documents.svg',
            credentialsBackground: '/site/public/images/overview/credentials.svg',
            keysBackground: '/site/public/images/overview/keys.svg',
        },
        resourceChipColors: {
            get: '#02a8f4',
            post: '#8ac149',
            put: '#ff9700',
            delete: '#de3702',
            options: '#5f7c8a',
            patch: '#785446',
            head: '#785446',
            sub: '#38a169',
            pub: '#4299e1',
        },
        operationChipColor: {
            query: '#b3e6fe',
            mutation: '#c1dea0',
            subscription: '#ffcc80',
        },
        thumbnailTemplates: {
            active: true,
            variant: 'text', // default templates available templates 'text'. Accepted values 'plain', 'text'
        },
        thumbnail: {
            width: 240,
            height: 140,
            contentPictureOverlap: false,
            iconColor: 'rgba(0, 0, 0, 0.38)',
            listViewIconSize: 20,
            contentBackgroundColor: 'rgba(239, 239, 239, 0.5)',
            defaultApiImage: false, // put false to render the system generated and user provided image.
            // And put a string to render a custom image
            textShadow: '0 1px 0 #ccc,0 1px 3px rgba(0,0,0,.1), 0 10px 10px rgba(0,0,0,.1), 0 20px 20px rgba(0,0,0,.1)',
            offset: 0.4, // Ratio between dark and light gradient, don't want a gradient make it 0
            colorMap: { // Any hashed color code or if you remove a mapping will fallback to default
                a: '#CC5262',
                b: '#605F42',
                c: '#474675',
                d: '#F39137',
                e: '#ffd454',
                f: '#308BB7',
                g: '#1B3FA3',
                h: '#559839',
                i: '#1A9615',
                j: '#9C5136',
                k: '#2B641C',
                l: '#35A580',
                m: '#CE7332',
                n: '#862EF1',
                o: '#3776F7',
                p: '#006E9C',
                q: '#AE726C',
                r: '#317AD2',
                s: '#B331D0',
                t: '#E46E86',
                u: '#7D257A',
                v: '#264F7F',
                w: '#1E5817',
                x: '#9FA554',
                y: '#7E2137',
                z: '#696428',
            },
            backgrounds: [
                // These backgrounds are use to generate the thumbnails.
                { prime: 0x8f6bcaff, sub: 0x4fc2f8ff },
                { prime: 0xf47f16ff, sub: 0xcddc39ff },
                { prime: 0xf44236ff, sub: 0xfec107ff },
                { prime: 0x2196f3ff, sub: 0xaeea00ff },
                { prime: 0xff9700ff, sub: 0xffeb3cff },
                { prime: 0xff9700ff, sub: 0xfe5722ff },
                { prime: 0xa676cdff, sub: 0x8a34a3ed },
                { prime: 0xe2293aed, sub: 0xdf935ded },
                { prime: 0xff7600ff, sub: 0x222222ed },
            ],
            document: {
                icon: 'library_books',
                backgrounds: {
                    prime: 0xcff7ffff,
                    sub: 0xe2fff7ff,
                },
            },
        },
        noApiImage: '/site/public/images/nodata.svg',
        landingPage: {
            active: false,
            activeForAnonymous: true,
            carousel: {
                active: true,
                slides: [
                    {
                        src: '/site/public/images/landing/01.jpg',
                        title: 'Lorem <span>ipsum</span> dolor sit amet',
                        content:
                            'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer felis lacus, placerat vel condimentum in, porta a urna. Suspendisse dolor diam, vestibulum at molestie dapibus, semper eget ex. Morbi sit amet euismod tortor.',
                    },
                    {
                        src: '/site/public/images/landing/02.jpg',
                        title: 'Curabitur <span>malesuada</span> arcu sapien',
                        content:
                            'Curabitur malesuada arcu sapien, suscipit egestas purus efficitur vitae. Etiam vulputate hendrerit venenatis. ',
                    },
                    {
                        src: '/site/public/images/landing/03.jpg',
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
                        src: '/site/public/images/landing/parallax1.jpg',
                        title: 'Lorem <span>ipsum</span> dolor sit amet',
                        content:
                            'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer felis lacus, placerat vel condimentum in, porta a urna. Suspendisse dolor diam, vestibulum at molestie dapibus, semper eget ex. Morbi sit amet euismod tortor.',
                    },
                    {
                        src: '/site/public/images/landing/parallax2.jpg',
                        title: 'Nam vel ex <span>feugiat</span> nunc laoreet',
                        content:
                            'Nam vel ex feugiat nunc laoreet elementum. Duis sed nibh condimentum, posuere risus a, mollis diam. Vivamus ultricies, augue id pulvinar semper, mauris lorem bibendum urna, eget tincidunt quam ex ut diam.',
                    },
                ],
            },
            contact: {
                active: true,
                contactHTML: '<div><strong>Mailing Address</strong><p><div>787 Castro Street</div><div>Mountain View,</div><div>CA 94041</div></p><div>Tel: +1 650 745 4499 </div><div>Fax: +1 650 584 3894</div></div>',
            },
        },
        tagWise: {
            active: false,
            style: 'fixed-left', // If 'page' it will show a different page. Else if 'fixed-left' will show a fixed menu with all the group tags on the left.
            thumbnail: { // These params will be applyed only if the style is 'page'
                width: 150,
                image: '/site/public/images/api/api-default.png',
            },
            key: '-group',
            showAllApis: true,
        },
        tagCloud: {
            active: true,
            colorOptions: { // This is the Options object passed to TagCloud component of https://www.npmjs.com/package/react-tagcloud
                luminosity: 'dark',
                hue: 'blue',
            },
            leftMenu: { // These params will be applyed only if the style is 'fixed-left'
                width: 200,
                height: 'calc(100vh - 222px)',
                background: '#d8e4e9',
                color: '#000',
                titleBackground: '#222',
                sliderBackground: '#222',
                sliderWidth: 25,
                hasIcon: false,
            },
        },
        social: {
            showRating: true,
            showSharing: {
                active: false,
                showFacebook: true,
                showReddit: true,
                showTwitter: true,
                showEmbed: true,
                showEmail: true,
            },
        },
        apiDetailPages: {
            showCredentials: true,
            showComments: true,
            showTryout: true,
            showDocuments: true,
            showSdks: true,
            showAsyncSpecification: true,
            onlyShowSdks: [], // You can put an array of strings to enable only a given set of sdks. Leave empty to show all. ex: ['java','javascript']
            sdkBackground: '#ffffff',
            swaggerUIBackground: '#efefef',
            documentBackground: '#efefef',
            tokenTextBoxBackground: '#efefef',
        },
        banner: {
            active: false, // make it true to display a banner image
            style: 'text', // 'can take 'image' or 'text'. If text it will display the 'banner.text' value else it will display the 'banner.image' value.
            image: '/site/public/images/landing/01.jpg',
            text: 'This is a very important announcement',
            color: '#ffffff',
            background: '#e08a00',
            padding: 20,
            margin: 0,
            fontSize: 18,
            textAlign: 'center',
        },
        footer: {
            active: true,
            footerHTML: '',
            text: '', // Leave empty to show the default WSO2 Text. Provide custom text to display your own thing.
            background: '#000',
            color: '#fff',
            height: 50,
        },
        title: {
            prefix: '[Devportal]',
            sufix: '- WSO2 APIM',
        },
        languageSwitch: {
            active: false,
            languages: [
                {
                    key: 'en',
                    image: '/site/public/images/flags/en.png',
                    imageWidth: 24, // in pixles
                    text: 'English',
                    direction: 'ltr',
                },
                {
                    key: 'es',
                    image: '/site/public/images/flags/sp.png',
                    imageWidth: 24, // in pixles
                    text: 'Spanish',
                    direction: 'ltr',
                },
                {
                    key: 'ar',
                    image: '/site/public/images/flags/ar.png',
                    imageWidth: 24, // in pixles
                    text: 'Arabic',
                    direction: 'rtl',
                },
                {
                    key: 'si',
                    image: '/site/public/images/flags/si.png',
                    imageWidth: 24, // in pixles
                    text: 'Sinhala',
                    direction: 'ltr',
                },
            ],
            showFlag: true,
            showText: true,
            minWidth: 60, // Width of the language switcher in pixles
        },
        info: {
            color: '#64b5f6',
        },
        /*
            This is the config for the public teant store.
            Set "active: false" to redirect the teant user to the url
            given by redirectToIfInactive. It will also hide the link
            SWITCH DEV PORTALS link at the header.
        */
        publicTenantStore: {
            active: true,
            redirectToIfInactive: 'https://wso2.com/api-management/cloud/',
        },
        curlGenerator: {
            backgroundColor: '#41444e',
            color: '#fff',
        },
        AsyncTryOut: {
            backgroundColor: '#89b7d1',
        },
        showSwaggerDescriptionOnOverview: false,
        overrides: {
            MuiOutlinedInput: {
                root: {
                    borderColor: '#444',
                },
                notchedOutline: {
                    borderColor: '#444',
                },
            },
        },
    },
};

export default DefaultConfigurations;
