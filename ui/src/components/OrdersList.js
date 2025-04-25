// ui/src/components/OrdersList.js
import React, { useEffect, useState } from 'react';
import { getAllOrders } from '../services/orderService';
import {
    Table, TableBody, TableCell, TableContainer,
    TableHead, TableRow, Paper, Collapse, IconButton
} from '@mui/material';
import { KeyboardArrowDown, KeyboardArrowUp } from '@mui/icons-material';

export default function OrdersList() {
    const [orders, setOrders] = useState([]);

    useEffect(() => {
        getAllOrders().then(setOrders).catch(console.error);
    }, []);

    return (
        <TableContainer component={Paper} sx={{ mt: 4 }}>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell />
                        <TableCell>ID</TableCell>
                        <TableCell>User ID</TableCell>
                        <TableCell>Дата</TableCell>
                        <TableCell align="right">Сумма</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {orders.map(o => (
                        <OrderRow key={o.id} order={o} />
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );
}

function OrderRow({ order }) {
    const [open, setOpen] = useState(false);
    return (
        <>
            <TableRow>
                <TableCell>
                    <IconButton size="small" onClick={() => setOpen(o => !o)}>
                        {open ? <KeyboardArrowUp /> : <KeyboardArrowDown />}
                    </IconButton>
                </TableCell>
                <TableCell>{order.id}</TableCell>
                <TableCell>{order.userId}</TableCell>
                <TableCell>{order.orderDate}</TableCell>
                <TableCell align="right">
                    {order.totalAmount.toFixed(2)}
                </TableCell>
            </TableRow>
            <TableRow>
                <TableCell colSpan={5} sx={{ p: 0, border: 0 }}>
                    <Collapse in={open}>
                        <Table size="small">
                            <TableHead>
                                <TableRow>
                                    <TableCell>Phone ID</TableCell>
                                    <TableCell>Brand</TableCell>
                                    <TableCell>Model</TableCell>
                                    <TableCell align="right">Price</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {order.smartphones.map(p => (
                                    <TableRow key={p.id}>
                                        <TableCell>{p.id}</TableCell>
                                        <TableCell>{p.brand}</TableCell>
                                        <TableCell>{p.model}</TableCell>
                                        <TableCell align="right">
                                            {p.price.toFixed(2)}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </Collapse>
                </TableCell>
            </TableRow>
        </>
    );
}
