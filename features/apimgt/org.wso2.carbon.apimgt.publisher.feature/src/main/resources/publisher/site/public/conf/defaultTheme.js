/**
 * IMPORTANT: This file only contains theme JSS of the Publisher app, Don't add other configuration parameters here.
 * This theme file is an extension of material-ui default theme https://material-ui.com/customization/default-theme/
 * Application related configurations are located in `<PUBLISHER_ROOT>site/public/theme/settings.js`
 */
// const context = '/publisher';

// const mediatorIcons = [
//     {
//         key: 'logmediator',
//         name: 'Log',
//         src1: context + '/site/public/images/mediatorIcons/LogMediator.png',
//         src2: context + '/site/public/images/mediatorIcons/log-mediator.png',
//     },
//     {
//         key: 'propertymediator',
//         name: 'Property',
//         src1: context + '/site/public/images/mediatorIcons/PropertyMediator.png',
//         src2: context + '/site/public/images/mediatorIcons/property-mediator.png',
//     },
//     {
//         key: 'dropmediator',
//         name: 'Drop',
//         src1: context + '/site/public/images/mediatorIcons/DropMediator.png',
//         src2: context + '/site/public/images/mediatorIcons/drop-mediator.png',
//     },
//     {
//         key: 'filtermediator',
//         name: 'Filter',
//         src1: context + '/site/public/images/mediatorIcons/FilterMediator.png',
//         src2: context + '/site/public/images/mediatorIcons/filter-mediator.png',
//     },
//     {
//         key: 'foreachmediator',
//         name: 'ForEach',
//         src1: context + '/site/public/images/mediatorIcons/ForEachMediator.png',
//         src2: context + '/site/public/images/mediatorIcons/foreach-mediator.png',
//     },
//     {
//         key: 'paloadfactorymediator',
//         name: 'PayloadFactory',
//         src1: context + '/site/public/images/mediatorIcons/PayloadFactoryMediator.png',
//         src2: context + '/site/public/images/mediatorIcons/payloadFactory-mediator.png',
//     },
//     {
//         key: 'propertygroupmediator',
//         name: 'PropertyGroup',
//         src1: context + '/site/public/images/mediatorIcons/PropertyGroupMediator.png',
//         src2: context + '/site/public/images/mediatorIcons/propertyGroup-mediator.png',
//     },
//     {
//         key: 'switchmediator',
//         name: 'Switch',
//         src1: context + '/site/public/images/mediatorIcons/SwitchMediator.png',
//         src2: context + '/site/public/images/mediatorIcons/switch-mediator.png',
//     },
//     {
//         key: 'validatemediator',
//         name: 'Validate',
//         src1: context + '/site/public/images/mediatorIcons/ValidateMediator.png',
//         src2: context + '/site/public/images/mediatorIcons/validate-mediator.png',
//     },
//     {
//         key: 'scriptmediator',
//         name: 'Script',
//         src1: context + '/site/public/images/mediatorIcons/ScriptMediator.png',
//         src2: context + '/site/public/images/mediatorIcons/script-mediator.png',
//     },
//     {
//         key: 'classmediator',
//         name: 'Class',
//         src1: context + '/site/public/images/mediatorIcons/ClassMediator.png',
//         src2: context + '/site/public/images/mediatorIcons/class-mediator.png',
//     },
// ];

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
            wrapperBackground: '#f9f9f9',
            starColor: '#f2c73a',
            disableColor: '#D3D3D3',
            leftMenuWidth: 210,
            contentAreaWidth: 1240,
            drawerWidth: 200,
            logo: '/site/public/images/logo.svg',
            logoHeight: 40,
            logoWidth: 222,
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
            overviewStepper: {
                backgrounds: {
                    completed: '#eeeeee',
                    active: '#fff',
                    inactive: '#e0e0e0',
                },
                iconSize: 32,
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
            title: {
                prefix: '[Devportal]',
                sufix: '- WSO2 APIM',
            },
        },
    },
    // mediatorIcons,
};
if (typeof module !== 'undefined') {
    module.exports = AppThemes; // Added for tests
}
