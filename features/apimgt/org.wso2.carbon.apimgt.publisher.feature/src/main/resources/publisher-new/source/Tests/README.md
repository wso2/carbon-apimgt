# Testing Implementation

We run two types of testing
mainly

-   Unit tests
-   Integration test

# Troubleshooting

> Feel free to update this guide , If you able to find better alternatives

## Mounted wrapper does not contain the expected rendered react elements

This could happen due to failures in mounting child component, Or your test might take longer time to re-render after a state change.
In this case , We could do the suggested workaround in [here](https://github.com/airbnb/enzyme/issues/1587#issuecomment-416610674) Or [this workaround](https://github.com/facebook/jest/issues/2157#issuecomment-279171856)

## Wrapper state returns null

In this case, You might probably looking at wrong component instance (element), Use `wrapper.debug()` to find out which component is wrapped in

## setState does not update the rendered output in wrapper

You probably missed some `wrapper.update()` calls there. This is an asynchronous call, So make sure to use `await`.

For more info refer [this issue](https://github.com/airbnb/enzyme/issues/450#issuecomment-225075145)



## Mounting and testing components return by higher order components (HOC)

Most of the components int he Publisher app are decorated by one or more higher order components provided by various libraries, i:e most of the components are wrapped with `withStyle` higher order components to get the theming support from Material-UI, Similarly we have user `withRoute` to get routing details from react-router and `injectIntl` to get internationalization (i18n) support to the components.

for example in `Listing.jsx` we have used both `injectIntl` and `withStyles` higher order functions

```javascript
export default injectIntl(withStyles(styles)(Listing));
```

In this case, Enzyme shallow rendering will just render the outer most component , which is intl wrapper and will not actually render the intended Listing component, So we need to unwrap the HOC, before shallow rendering the component.

When doing Enzyme mount, These HOCs would require some values to be there in the context.
Do the following as required

-   For [React Intl](https://github.com/formatjs/react-intl/blob/master/docs/Testing-with-React-Intl.md) wrapper


    Use the [IntlHelper.js](source/Tests/Utils/IntlHelper.js) If we could not servive with this Util we might need to add [this package](https://github.com/joetidee/enzyme-react-intl) to get the full support

-   React router

Use `MemoryRouter` for wrapping the component, for example in [Listing.test.jsx](source/src/app/components/Apis/Listing/Listing.test.jsx)

```javascript
<MuiThemeProvider theme={createMuiTheme(light)}>
    <MemoryRouter>
        <Listing />
    </MemoryRouter>
</MuiThemeProvider>
```

- Material-UI [testing](https://material-ui.com/guides/testing/)

Use `unwrap` util from material ui test utils to unwrap the `withStyle` decorations,

```javascript
import { unwrap } from '@material-ui/core/test-utils';

const UnwrappedMenuButton = unwrap(MenuButton);
```

When enzyme mounting use actual theme object and `MuiThemeProvider` component to wrap the target component.Like in the above (intl) example


## Clearing , Resting or Restoring mock implementations

If you want to change the mocked function behavior between different test cases within one test description(file), Use `mockReset` , `mockRestore` or `mockClear` accordingly. FOr more info refer this [issue](https://github.com/facebook/jest/issues/5143)

# For quick lookup [Jest cheat sheet](https://github.com/sapegin/jest-cheat-sheet)
