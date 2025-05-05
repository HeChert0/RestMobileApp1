import axios from 'axios';
const BASE_URL = 'http://localhost:8081/api/orders';

export const getAllOrders = () =>
    axios.get(BASE_URL).then(res => res.data);
export const createOrder    = data => axios.post(BASE_URL, data).then(res => res.data);
export const updateOrder    = (id, data) => axios.put(`${BASE_URL}/${id}`, data).then(res => res.data);
export const deleteOrder    = id => axios.delete(`${BASE_URL}/${id}`);