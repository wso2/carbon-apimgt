const getIcon = (key, category, theme, api) => {
    let IconElement;

    // Creating the icon
    if (key && category) {
        IconElement = key;
    } else if (api.type === 'DOC') {
        IconElement = theme.custom.thumbnail.document.icon;
    } else {
        IconElement = 'settings';
    }
    return IconElement;
};

export default getIcon;
