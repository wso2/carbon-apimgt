import Utils from 'AppData/Utils';
import User from 'AppData/User';

const environmentName = Utils.getCurrentEnvironment().label;
const userStorageKey = `${User.CONST.LOCAL_STORAGE_USER}_${environmentName}`;

export const updateUserLocalStorage = (key, newValue) => {
    const userData = JSON.parse(localStorage.getItem(userStorageKey));
    userData[key] = newValue;
    localStorage.setItem(userStorageKey, JSON.stringify(userData));
};

export const getUserLocalStorage = (key) => {
    const userData = JSON.parse(localStorage.getItem(userStorageKey));
    return userData[key];
};
