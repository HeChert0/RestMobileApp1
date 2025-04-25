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
