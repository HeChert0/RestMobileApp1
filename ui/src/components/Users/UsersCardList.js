import React, { useEffect, useState } from 'react';
import {
    Grid, Card, CardActionArea, CardContent,
    Typography, TextField, Button,
    Accordion, AccordionSummary, AccordionDetails,
    Stack
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { useNavigate } from 'react-router-dom';
import { getAllUsers } from '../../services/userService';
import { LocalizationProvider, DatePicker } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import BulkOperationsToolbar from '../BulkOperationsToolbar';

// @ts-nocheck
export default function UsersCardList() {
    const [allUsers, setAllUsers] = useState([]);
    const [filtered, setFiltered] = useState([]);
    const navigate = useNavigate();

    const [username,      setUsername]      = useState('');
    const [minOrderPrice, setMinOrderPrice] = useState('');
    const [maxOrderPrice, setMaxOrderPrice] = useState('');
    const [minOrders,     setMinOrders]     = useState('');
    const [maxOrders,     setMaxOrders]     = useState('');
    const [startDate,     setStartDate]     = useState(null);
    const [endDate,       setEndDate]       = useState(null);

    useEffect(() => {
        getAllUsers().then(users => {
            setAllUsers(users);
            setFiltered(users);
        });
    }, []);

    const handleSearch = () => {
        let tmp = allUsers;
        if (username)      tmp = tmp.filter(u => u.username.toLowerCase().includes(username.toLowerCase()));
        if (minOrderPrice) tmp = tmp.filter(u => u.orders.some(o => o.totalAmount >= +minOrderPrice));
        if (maxOrderPrice) tmp = tmp.filter(u => u.orders.some(o => o.totalAmount <= +maxOrderPrice));
        if (minOrders)     tmp = tmp.filter(u => u.orders.length >= +minOrders);
        if (maxOrders)     tmp = tmp.filter(u => u.orders.length <= +maxOrders);
        if (startDate) {
            const from = startDate.toISOString().slice(0,10);
            tmp = tmp.filter(u => u.orders.some(o => o.orderDate >= from));
        }
        if (endDate) {
            const to = endDate.toISOString().slice(0,10);
            tmp = tmp.filter(u => u.orders.some(o => o.orderDate <= to));
        }
        setFiltered(tmp);
    };

    const handleReset = () => {
        setUsername(''); setMinOrderPrice(''); setMaxOrderPrice('');
        setMinOrders(''); setMaxOrders('');
        setStartDate(null); setEndDate(null);
        setFiltered(allUsers);
    };

    return (
        <>
            <Accordion sx={{ mb: 4 }}>
                <AccordionSummary
                    expandIcon={<ExpandMoreIcon />}
                    sx={{
                        justifyContent: 'center',
                        '& .MuiAccordionSummary-content': { justifyContent: 'center' }
                    }}
                >
                    <Typography variant="h6">Фильтры</Typography>
                </AccordionSummary>

                <AccordionDetails>
                    <LocalizationProvider dateAdapter={AdapterDateFns}>
                        <Grid container spacing={2} alignItems="flex-start">
                            {/* Левая часть: филтры */}
                            <Grid item xs={12} md={9}>
                                <Grid container spacing={2}>
                                    <Grid item xs={12} sm={6} md={4} lg={3}>
                                        <TextField fullWidth label="Username"
                                                   value={username}
                                                   onChange={e => setUsername(e.target.value)}
                                        />
                                    </Grid>
                                    <Grid item xs={6} sm={4} md={3}>
                                        <TextField fullWidth label="Мин. цена заказа"
                                                   type="number"
                                                   value={minOrderPrice}
                                                   onChange={e => setMinOrderPrice(e.target.value)}
                                        />
                                    </Grid>
                                    <Grid item xs={6} sm={4} md={3}>
                                        <TextField fullWidth label="Макс. цена заказа"
                                                   type="number"
                                                   value={maxOrderPrice}
                                                   onChange={e => setMaxOrderPrice(e.target.value)}
                                        />
                                    </Grid>
                                    <Grid item xs={6} sm={4} md={3}>
                                        <TextField fullWidth label="Мин. число заказов"
                                                   type="number"
                                                   value={minOrders}
                                                   onChange={e => setMinOrders(e.target.value)}
                                        />
                                    </Grid>
                                    <Grid item xs={6} sm={4} md={3}>
                                        <TextField fullWidth label="Макс. число заказов"
                                                   type="number"
                                                   value={maxOrders}
                                                   onChange={e => setMaxOrders(e.target.value)}
                                        />
                                    </Grid>
                                    <Grid item xs={12} sm={6} md={4} lg={3}>
                                        <DatePicker
                                            label="Дата заказа (с)"
                                            value={startDate}
                                            onChange={setStartDate}
                                            renderInput={params => <TextField fullWidth {...params} />}
                                        />
                                    </Grid>
                                    <Grid item xs={12} sm={6} md={4} lg={3}>
                                        <DatePicker
                                            label="Дата заказа (по)"
                                            value={endDate}
                                            onChange={setEndDate}
                                            renderInput={params => <TextField fullWidth {...params} />}
                                        />
                                    </Grid>
                                </Grid>
                            </Grid>

                            <Grid item xs={12} md={3}>
                                <Grid container spacing={2} direction="row">
                                    <Grid item>
                                        <Button
                                            fullWidth size="large"
                                            variant="contained"
                                            color="secondary"
                                            onClick={handleSearch}
                                        >
                                            Поиск
                                        </Button>
                                    </Grid>
                                    <Grid item>
                                        <Button
                                            fullWidth size="large"
                                            variant="contained"
                                            color="secondary"
                                            onClick={handleReset}
                                        >
                                            Сброс
                                        </Button>
                                    </Grid>
                                </Grid>
                            </Grid>
                        </Grid>
                    </LocalizationProvider>
                </AccordionDetails>
            </Accordion>

            <BulkOperationsToolbar basePath="/users" />
            <Grid
                container
                spacing={3}
                sx={{ mt: 4, px: 3 }}
                justifyContent="flex-start"
            >
                {filtered.map(user => (
                    <Grid item xs="auto" key={user.id}>
                        <Card
                            sx={{
                                width: 350,
                                height: 240,
                                display: 'flex',
                                flexDirection: 'column',
                                justifyContent: 'center',
                                p: 2,
                            }}
                        >
                            <CardActionArea onClick={() => navigate(`/users/${user.id}`)}>
                                <CardContent>
                                    <Typography variant="h4" gutterBottom>
                                        {user.username}
                                    </Typography>
                                    <Typography variant="h6" color="text.secondary">
                                        Заказов: {user.orders.length}
                                    </Typography>
                                </CardContent>
                            </CardActionArea>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </>
    );
}
