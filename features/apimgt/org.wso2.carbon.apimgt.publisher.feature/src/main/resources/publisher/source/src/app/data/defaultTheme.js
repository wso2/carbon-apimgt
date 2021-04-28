/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * IMPORTANT: This file only contains theme JSS of the Publisher app, Don't add other configuration parameters here.
 * This theme file is an extension of material-ui default theme https://material-ui.com/customization/default-theme/
 * Application related configurations are located in `<PUBLISHER_ROOT>site/public/theme/settings.js`
 */
export default {
    overrides: {
        MuiDrawer: {
            paper: {
                backgroundColor: '#18202c',
            },
        },
        MuiButton: {
            label: {
                textTransform: 'none',
            },
            contained: {
                boxShadow: 'none',
                '&:active': {
                    boxShadow: 'none',
                },
            },
        },
        MuiIconButton: {
            root: {
                padding: 8,
            },
        },
        MuiTooltip: {
            tooltip: {
                borderRadius: 4,
            },
        },
        MuiDivider: {
            root: {
                backgroundColor: '#404854',
            },
        },
        MuiListItemIcon: {
            root: {
                color: 'inherit',
                marginRight: 0,
                '& svg': {
                    fontSize: 20,
                },
            },
        },
        MuiAvatar: {
            root: {
                width: 32,
                height: 32,
            },
        },
    },
    palette: {
        primary: {
            // light: will be calculated from palette.primary.main,
            main: '#006E9C',
            // dark: will be calculated from palette.primary.main,
            // contrastText: will be calculated to contrast with palette.primary.main
        },
        secondary: {
            light: '#0066ff',
            main: '#a2ecf5',
            highlight: '#e8fafd',
            // dark: will be calculated from palette.secondary.main,
            contrastText: '#ffcc00',
        },
        background: {
            default: '#f6f6f6',
            paper: '#ffffff',
            appBar: '#215088',
            appBarSelected: '#1d344f',
            leftMenu: '#1a1f2f',
            leftMenuActive: '#254061',
            drawer: '#1a1f2f',
            activeMenuItem: '#254061',
            divider: '#000000',
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
        wrapperBackground: '#f9f9f9',
        disableColor: '#D3D3D3',
        leftMenuWidth: 230,
        contentAreaWidth: 1240,
        logo: '/site/public/images/logo.svg',
        logoHeight: 40,
        logoWidth: 222,
        defaultApiView: 'grid', /* Sets the default view for the api listing page ( Other values available = 'list' )
                                                To disable one option for an example if you want to disable grid
                                                completely and get rid of the toggle buttons use ['list']. */
        leftMenu: 'icon left', //  other values ('icon top', 'icon left', 'no icon', 'no text')
        leftMenuIconSize: 20,
        leftMenuIconMainSize: 52,
        leftMenuTextStyle: 'capitalize',
        leftMenuAnalytics: {
            enable: true, // If `false`, External link to choreo cloud analytics icon will be removed/hidden in nav bar
            link: 'http://analytics.choreo.dev/setup',
        },
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
            sub: '#38a169',
            pub: '#4299e1',
        },
        operationChipColor: {
            query: '#b3e6fe',
            mutation: '#c1dea0',
            subscription: '#ffcc80',
        },
        overviewStepper: {
            backgrounds: {
                completed: '#eeeeee',
                active: '#fff',
                inactive: '#e0e0e0',
            },
            iconSize: 32,
        },
        thumbnailTemplates: {
            active: false,
            variant: 'letter', // Default template is `letter`, available templates are 'letter','image'.
        },
        footer: {
            height: 35, // In pixels
            background: '#e6e6e6', // MUI grey[100]
            text: '', // If empty will show default header text
            color: 'gray',
        },
        globalNavBar: {
            active: '#46a3ca',
            opened: {
                drawerWidth: 180,
            },
            collapsed: {
            },
        },
        thumbnail: {
            width: 240,
            height: 140,
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
            backgrounds: [ // These backgrounds are use to generate the thumbnails.
                { prime: 0x2196f3ff, sub: 0xaeea00ff },
                // { prime: 0x8f6bcaff, sub: 0x4fc2f8ff },
                // { prime: 0xf47f16ff, sub: 0xcddc39ff },
                // { prime: 0xf44236ff, sub: 0xfec107ff },
                // { prime: 0xff9700ff, sub: 0xffeb3cff },
                // { prime: 0xff9700ff, sub: 0xfe5722ff },
            ],
            document: {
                icon: 'library_books',
                backgrounds: {
                    prime: 0xcff7ffff,
                    sub: 0xe2fff7ff,
                },
            },
        },
        // lifeCycleImage: '/site/public/images/logo.svg',
        // Uncomment above if you want to add a custom image to the lifecycle diagram.
        commentsLimit: 5,
        maxCommentLength: 512,
        productSampleProgess: {
            backgroundMain: '#15b8cf',
            backgroundChip: '#5aebf9',
        },
        warningColor: '#ffc439',
        landingPage: {
            icons: {
                graphqlIcon: '/site/public/images/landing-icons/graphqlapi.svg',
                restApiIcon: '/site/public/images/landing-icons/restapi.svg',
                soapApiIcon: '/site/public/images/landing-icons/soapapi.svg',
                streamingApiIcon: '/site/public/images/landing-icons/streamingapi.svg',
                serviceCatalogApiIcon: '/site/public/images/landing-icons/servicecatalogapi.svg',
                scopesAddIcon: '/site/public/images/landing-icons/scopes.svg',
                apiproductAddIcon: '/site/public/images/landing-icons/apiproduct.svg',
            },
            menu: {
                primary: '#34679D',
            },
        },
        title: {
            prefix: '[Publisher]',
            suffix: '- WSO2 APIM',
        },
        apis: {
            topMenu: {
                height: 70,
            },
            overview: {
                stepper: {
                    active: '',
                    completed: '',
                },
            },
            listing: {
                deleteButtonColor: '#000',
            },
        },
        serviceCatalog: {
            onboarding: {
                buttonText: '#FF7300',
                buttonBorder: '#FCA574',
            },
            icons: {
                graphql: '/site/public/images/service_catalog/icons/graphql.svg',
                asyncapi: '/site/public/images/service_catalog/icons/async.svg',
                oas3: '/site/public/images/service_catalog/icons/oas3.png',
                swagger: '/site/public/images/service_catalog/icons/swagger.svg',
            },
        },
        revision: {
            activeRevision: {
                background: 'radial-gradient(#29bb89, #f7ea00)',
            },
        },
    },
};
