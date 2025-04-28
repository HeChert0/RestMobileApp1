// ui/src/components/PhoneList.js
import React, { useEffect, useState } from 'react';
import { getAllPhones } from '../../services/phoneService';
import {
    Table, TableBody, TableCell, TableContainer,
    TableHead, TableRow, Paper, TextField,
    Button, Select, MenuItem, Pagination, Stack, Container
} from '@mui/material';
import BulkOperationsToolbar from '../BulkOperationsToolbar';

export default function PhoneList() {
    const [allPhones, setAll] = useState([]);
    const [filtered, setFiltered] = useState([]);
    const [page, setPage] = useState(1);
    const [rowsPerPage, setRowsPerPage] = useState(5);
    // фильтры
    const [brand, setBrand] = useState('');
    const [model, setModel] = useState('');
    const [minPrice, setMinPrice] = useState('');
    const [maxPrice, setMaxPrice] = useState('');

    useEffect(() => {
        getAllPhones().then(data => {
            setAll(data);
            setFiltered(data);
        });
    }, []);

    const handleSearch = () => {
        let tmp = allPhones;
        if (brand)   tmp = tmp.filter(p => p.brand.toLowerCase().includes(brand.toLowerCase()));
        if (model)   tmp = tmp.filter(p => p.model.toLowerCase().includes(model.toLowerCase()));
        if (minPrice) tmp = tmp.filter(p => p.price >= parseFloat(minPrice));
        if (maxPrice) tmp = tmp.filter(p => p.price <= parseFloat(maxPrice));
        setFiltered(tmp);
        setPage(1);
    };
    const handleReset = () => {
        setBrand(''); setModel(''); setMinPrice(''); setMaxPrice('');
        setFiltered(allPhones);
        setPage(1);
    };

    // данные для текущей страницы
    const start = (page - 1) * rowsPerPage;
    const current = filtered.slice(start, start + rowsPerPage);
    const pageCount = Math.ceil(filtered.length / rowsPerPage);

    return (
        <Container>
            <BulkOperationsToolbar basePath="/phones" sx={{ mt: 4 }} />
        <Paper sx={{ p: 2, mt: 4 }}>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} mb={2}>
                <TextField label="Бренд"   value={brand}    onChange={e => setBrand(e.target.value)} />
                <TextField label="Модель"  value={model}    onChange={e => setModel(e.target.value)} />
                <TextField label="Мин цена" value={minPrice} onChange={e => setMinPrice(e.target.value)} />
                <TextField label="Макс цена" value={maxPrice} onChange={e => setMaxPrice(e.target.value)} />
                 <Button
                   variant="contained"
                   color="secondary"
                   onClick={handleSearch}
                 >
                   Поиск
                 </Button>
                 <Button
                   variant="contained"
                   color="secondary"
                   onClick={handleReset}
                 >
                   Сброс
                 </Button>
            </Stack>

            <TableContainer>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>ID</TableCell>
                            <TableCell>Бренд</TableCell>
                            <TableCell>Модель</TableCell>
                            <TableCell align="right">Цена</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {current.map(p => (
                            <TableRow key={p.id}>
                                <TableCell>{p.id}</TableCell>
                                <TableCell>{p.brand}</TableCell>
                                <TableCell>{p.model}</TableCell>
                                <TableCell align="right">${p.price.toFixed(2)}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            {/* Пагинация и выбор размера страницы */}
            <Stack direction="row" justifyContent="space-between" alignItems="center" mt={2}>
                <Pagination
                    count={pageCount}
                    page={page}
                    onChange={(e, v) => setPage(v)}
                />
                <Stack direction="row" spacing={1} alignItems="center">
                    <span>На странице:</span>
                    <Select
                        value={rowsPerPage}
                        onChange={e => { setRowsPerPage(+e.target.value); setPage(1); }}
                        size="small"
                    >
                        {[5,10,25,50].map(n => (
                            <MenuItem key={n} value={n}>{n}</MenuItem>
                        ))}
                    </Select>
                </Stack>
            </Stack>
        </Paper>
        </Container>
    );
}
