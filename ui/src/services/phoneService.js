// ui/src/services/phoneService.js
// @ts-nocheck
import axios from 'axios';

// временно на прямую
const BASE_URL = 'http://localhost:8081/api/phones';

export const getAllPhones = async () => {
    try {
        const response = await axios.get(BASE_URL);
        console.log('Response data:', response.data);
        return response.data;
    } catch (err) {
        console.error('Ошибка при запросе телефонов:', err);
        throw err;
    }
};
export const createPhone = data => axios.post(BASE_URL, data).then(res => res.data);
export const updatePhone = (id, data) => axios.put(`${BASE_URL}/${id}`, data).then(res => res.data);
export const deletePhone = id => axios.delete(`${BASE_URL}/${id}`);
export const createPhonesBulk = data =>
    axios.post(`${BASE_URL}/bulk`, data).then(res => res.data);
