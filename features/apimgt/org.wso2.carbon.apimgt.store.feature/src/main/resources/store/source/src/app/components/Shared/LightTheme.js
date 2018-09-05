export default {
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
          default: '#f6f6f6ff',
          paper: '#ffffff',
          appBar: '#1d344f',
          leftMenu: '#1a1f2f',
        },
        custom: {
          leftMenuText: '#8b8e95',
          leftMenuActive: '#1d344f',
          starColor: '#f2c73a',
          leftMenuWidth: 90,
          contentAreaWidth: 1240,
        },
    },
    typography: {
      fontFamily: '"Open Sans", "Helvetica", "Arial", sans-serif',
      fontSize: 12,
    }
};