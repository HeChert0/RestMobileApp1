// ui/src/components/PhoneList.js
// @ts-nocheck
import React, { useEffect, useState } from 'react';
import { getAllPhones } from '../services/phoneService';
import {
    Table, TableBody, TableCell, TableContainer,
    TableHead, TableRow, Paper,
} from '@mui/material';

export default function PhoneList() {
    const [phones, setPhones] = useState([]);

    useEffect(() => {
        getAllPhones()
            .then(data => setPhones(data))
            .catch(err => console.error(err));
    }, []);


    return (
        <TableContainer component={Paper} sx={{ mt: 4 }}>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>ID</TableCell>
                        <TableCell>Бренд</TableCell>
                        <TableCell>Модель</TableCell>
                        <TableCell align="right">Цена, $</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {phones.map(phone => (
                        <TableRow key={phone.id}>
                            <TableCell>{phone.id}</TableCell>
                            <TableCell>{phone.brand}</TableCell>
                            <TableCell>{phone.model}</TableCell>
                            <TableCell align="right">
                                {phone.price.toFixed(2)}
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );
}
