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
            main: '#15b8cf',
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
            appBar: '#1d344f',
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
        starColor: '#f2c73a',
        disableColor: '#D3D3D3',
        leftMenuWidth: 210,
        contentAreaWidth: 1240,
        drawerWidth: 250,
        logo: '/site/public/images/logo.svg',
        logoHeight: 40,
        logoWidth: 222,
        defaultApiView: 'grid', /* Sets the default view for the api listing page ( Other values available = 'list' )
                                                To disable one option for an example if you want to disable grid
                                                completely and get rid of the toggle buttons use ['list']. */
        showApiHelp: false, // API details page has a right hand side panel showing it's related help. Set this
        // to false if you want to hide it.
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
            subscribe: '#61affe',
            publish: '#49cc90',
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
        thumbnail: {
            width: 240,
            height: 140,
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
        // lifeCycleImage: '/site/public/images/logo.svg',
        // Uncomment above if you want to add a custom image to the lifecycle diagram.
        adminRole: 'admin',
        commentsLimit: 5,
        maxCommentLength: 256,
        productSampleProgess: {
            backgroundMain: '#15b8cf',
            backgroundChip: '#5aebf9',
        },
        warningColor: '#ffc439',
        landingPageIcons: {
            graphqlIcon: '/site/public/images/landing-icons/graphqlapi.svg',
            restApiIcon: '/site/public/images/landing-icons/restapi.svg',
            soapApiIcon: '/site/public/images/landing-icons/soapapi.svg',
            streamingApiIcon: '/site/public/images/landing-icons/streamingapi.svg',
            websocketApiIcon: '/site/public/images/landing-icons/websocketapi.svg',
        },
        title: {
            prefix: '[Publisher]',
            sufix: '- WSO2 APIM',
        },
        serviceCatalog: {
            onboarding: {
                buttonText: '#FF7300',
                buttonBorder: '#FCA574',
            },
        },
    },
};
