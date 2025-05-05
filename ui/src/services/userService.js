import axios from 'axios';
const BASE_URL = 'http://localhost:8081/api/users';

export const getAllUsers = () =>
    axios.get(BASE_URL).then(res => res.data);
export const createUser    = data => axios.post(BASE_URL, data).then(res => res.data);
export const updateUser    = (id, data) => axios.put(`${BASE_URL}/${id}`, data).then(res => res.data);
export const deleteUser    = id => axios.delete(`${BASE_URL}/${id}`);
export const createUsersBulk = data =>
    axios.post(`${BASE_URL}/bulk`, data).then(res => res.data);