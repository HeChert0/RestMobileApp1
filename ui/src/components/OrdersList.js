// ui/src/components/OrdersList.js
import React, { useEffect, useState } from 'react';
import {
    TextField, Button, Select, MenuItem,
    Pagination, Stack, Container, Accordion,
    AccordionSummary, AccordionDetails, Typography
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { getAllOrders } from '../services/orderService';
import { getAllUsers } from '../services/userService';

export default function OrdersList() {
    const [all, setAll] = useState([]);
    const [filtered, setFiltered] = useState([]);
    const [usersMap, setUsersMap] = useState({}); // id → username

    const [page, setPage] = useState(1);
    const [rowsPerPage, setRowsPerPage] = useState(5);

    // теперь фильтр по username
    const [username, setUsername] = useState('');

    useEffect(() => {
        // параллельно подтягиваем заказы и мапу пользователей
        getAllOrders().then(data => {
            setAll(data);
            setFiltered(data);
        });
        getAllUsers().then(users => {
            const m = {};
            users.forEach(u => { m[u.id] = u.username; });
            setUsersMap(m);
        });
    }, []);

    const handleSearch = () => {
        let tmp = all;
        if (username) {
            tmp = tmp.filter(o =>
                (usersMap[o.userId] || '')
                    .toLowerCase()
                    .includes(username.toLowerCase())
            );
        }
        setFiltered(tmp);
        setPage(1);
    };
    const handleReset = () => {
        setUsername('');
        setFiltered(all);
        setPage(1);
    };

    // пагинация
    const start = (page - 1) * rowsPerPage;
    const current = filtered.slice(start, start + rowsPerPage);
    const pageCount = Math.ceil(filtered.length / rowsPerPage);

    return (
        <Container sx={{ mt: 4 }}>
            {/* Фильтр по username */}
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} mb={2}>
                <TextField
                    label="Username"
                    value={username}
                    onChange={e => setUsername(e.target.value)}
                />
                <Button variant="contained" color="secondary" onClick={handleSearch}>
                    Поиск
                </Button>
                <Button variant="contained" color="secondary" onClick={handleReset}>
                    Сброс
                </Button>
            </Stack>

            {/* Список заказов */}
            {current.map(order => (
                <Accordion key={order.id} sx={{ mb: 1 }}>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography>
                            Заказ #{order.id} — {usersMap[order.userId]} — $
                            {order.totalAmount.toFixed(2)}
                        </Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        <Typography variant="subtitle1" gutterBottom>
                            Телефоны в заказе:
                        </Typography>
                        {order.smartphones.map(p => (
                            <Typography key={p.id}>
                                — {p.brand} {p.model} (${p.price.toFixed(2)})
                            </Typography>
                        ))}
                    </AccordionDetails>
                </Accordion>
            ))}

            {/* Пагинация */}
            <Stack
                direction="row"
                justifyContent="space-between"
                alignItems="center"
                mt={2}
            >
                <Pagination
                    count={pageCount}
                    page={page}
                    onChange={(e, v) => setPage(v)}
                />
                <Stack direction="row" spacing={1} alignItems="center">
                    <span>На странице:</span>
                    <TextField
                        select
                        size="small"
                        value={rowsPerPage}
                        onChange={e => {
                            setRowsPerPage(+e.target.value);
                            setPage(1);
                        }}
                        sx={{ width: 80 }}
                    >
                        {[5, 10, 25, 50].map(n => (
                            <MenuItem key={n} value={n}>{n}</MenuItem>
                        ))}
                    </TextField>
                </Stack>
            </Stack>
        </Container>
    );
}