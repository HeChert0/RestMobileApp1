// ui/src/services/userService.js
import axios from 'axios';
const BASE_URL = 'http://localhost:8081/api/users';

export const getAllUsers = () =>
    axios.get(BASE_URL).then(res => res.data);

// и методы createUser, updateUser, deleteUser…
