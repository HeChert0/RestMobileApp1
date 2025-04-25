// ui/src/services/orderService.js
import axios from 'axios';
const BASE_URL = 'http://localhost:8081/api/orders';

export const getAllOrders = () =>
    axios.get(BASE_URL).then(res => res.data);

// при необходимости добавим createOrder, updateOrder, deleteOrder…
