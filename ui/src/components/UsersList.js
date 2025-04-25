// ui/src/components/UsersList.js
import React, { useEffect, useState } from 'react';
import { getAllUsers } from '../services/userService';
import {
    Table, TableBody, TableCell, TableContainer,
    TableHead, TableRow, Paper, Collapse, IconButton
} from '@mui/material';
import { KeyboardArrowDown, KeyboardArrowUp } from '@mui/icons-material';

export default function UsersList() {
    const [users, setUsers] = useState([]);

    useEffect(() => {
        getAllUsers().then(setUsers).catch(console.error);
    }, []);

    return (
        <TableContainer component={Paper} sx={{ mt: 4 }}>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell />
                        <TableCell>ID</TableCell>
                        <TableCell>Username</TableCell>
                        <TableCell>Заказов</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {users.map(user => (
                        <UserRow key={user.id} user={user} />
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );
}

function UserRow({ user }) {
    const [open, setOpen] = useState(false);
    return (
        <>
            <TableRow>
                <TableCell>
                    <IconButton size="small" onClick={() => setOpen(o => !o)}>
                        {open ? <KeyboardArrowUp /> : <KeyboardArrowDown />}
                    </IconButton>
                </TableCell>
                <TableCell>{user.id}</TableCell>
                <TableCell>{user.username}</TableCell>
                <TableCell>{user.orders.length}</TableCell>
            </TableRow>
            <TableRow>
                <TableCell colSpan={4} sx={{ p: 0, border: 0 }}>
                    <Collapse in={open}>
                        <Table size="small">
                            <TableHead>
                                <TableRow>
                                    <TableCell>Order ID</TableCell>
                                    <TableCell>Дата</TableCell>
                                    <TableCell>Сумма</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {user.orders.map(o => (
                                    <TableRow key={o.id}>
                                        <TableCell>{o.id}</TableCell>
                                        <TableCell>{o.orderDate}</TableCell>
                                        <TableCell>{o.totalAmount.toFixed(2)}</TableCell>
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
