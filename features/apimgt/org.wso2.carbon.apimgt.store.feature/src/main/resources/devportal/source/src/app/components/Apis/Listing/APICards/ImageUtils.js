import MaterialIcons from 'MaterialIcons';

const getIcon = (key, category, theme, api) => {
    let IconElement;
    let count;

    // Creating the icon
    if (key && category) {
        IconElement = key;
    } else if (api.type === 'DOC') {
        IconElement = theme.custom.thumbnail.document.icon;
    } else {
        count = MaterialIcons.categories[1].icons.length;
        const randomIconIndex = (api.name.charCodeAt(0) + api.name.charCodeAt(api.name.length - 1)) % count;
        IconElement = MaterialIcons.categories[8].icons[randomIconIndex].id;
    }
    return IconElement;
};

export default getIcon;
