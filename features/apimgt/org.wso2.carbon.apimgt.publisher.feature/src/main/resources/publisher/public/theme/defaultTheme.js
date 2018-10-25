var Configurations = {
    themes: {
      light: {
        palette: {
          primary: {
            // light: will be calculated from palette.primary.main,
            main: "#15b8cf"
            // dark: will be calculated from palette.primary.main,
            // contrastText: will be calculated to contrast with palette.primary.main
          },
          secondary: {
            light: "#0066ff",
            main: "#a2ecf5",
            // dark: will be calculated from palette.secondary.main,
            contrastText: "#ffcc00"
          },
          background: {
            default: "#f6f6f6",
            paper: "#ffffff",
            appBar: "#1d344f",
            leftMenu: "#1a1f2f",
            leftMenuActive: "#000000",
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
            fontSize: "1.3rem",
          },
        },
        custom: {
          starColor: "#f2c73a",
          leftMenuWidth: 120,
          contentAreaWidth: 1240,
          drawerWidth: 200,
          logo: '/publisher/public/app/images/logo.png',
          backgroundImage: '/publisher/public/app/images/back-light.png',
          title: 'WSO2 APIM Publisher',
          defaultApiView: 'grid', // Sets the default view for the api listing page ( Other values available = 'list' )
          showApiHelp: false, // API detials page has a right hand side panel showing it's related help. Set this to false if you want to hide it.
          leftMenu: 'icon top', //  other values ('icon top', 'icon left', 'no icon', 'no text')
          leftMenuIconSize: 32,
          leftMenuIconMainSize: 52,
          leftMenuTextStyle: 'uppercase',
          resourceChipColors: {get: '#02a8f4', post: '#8ac149', put: '#ff9700', delete: '#fd5621', option: '#5f7c8a', patch: '#785446', head: '#785446'},
        },
      },
      dark: {
        palette: {
          primary: {
            // light: will be calculated from palette.primary.main,
            main: "#15b8cf"
            // dark: will be calculated from palette.primary.main,
            // contrastText: will be calculated to contrast with palette.primary.main
          },
          secondary: {
            light: "#0066ff",
            main: "#a2ecf5",
            // dark: will be calculated from palette.secondary.main,
            contrastText: "#ffcc00"
          },
          background: {
            default: "#f6f6f6",
            paper: "#ffffff",
            appBar: "#1d344f",
            leftMenu: "#1a1f2f",
            leftMenuActive: "#000000",
            drawer: '#1a1f2f',
          },
        },
        typography: {
          fontFamily: '"Open Sans", "Helvetica", "Arial", sans-serif',
          fontSize: 12,
          subtitle2: {
            fontWeight: 600,
          }
        },
        custom: {
          starColor: "#f2c73a",
          leftMenuWidth: 120,
          contentAreaWidth: 1240,
          drawerWidth: 200,
          logo: '/publisher/public/app/images/logo.png',
          backgroundImage: '/publisher/public/app/images/back-light.png',
          title: 'WSO2 APIM Publisher',
          defaultApiView: 'grid', // Sets the default view for the api listing page ( Other values available = 'list' )
          showApiHelp: false, // API detials page has a right hand side panel showing it's related help. Set this to false if you want to hide it.
          leftMenu: 'icon top', //  other values ('icon top', 'icon left', 'no icon', 'no text')
          leftMenuIconSize: 32,
          leftMenuIconMainSize: 52,
          leftMenuTextStyle: 'uppercase',
          resourceChipColors: {get: '#02a8f4', post: '#8ac149', put: '#ff9700', delete: '#fd5621', option: '#5f7c8a', patch: '#785446', head: '#785446'},
        },
      },
    },
  };
  