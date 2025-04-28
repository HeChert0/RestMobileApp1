// ui/src/components/UpdateOrderModal.js
import React, { useState, useEffect } from 'react';
import { Modal, Box, Typography, TextField, Button, Stack, MenuItem, Chip } from '@mui/material';
import Autocomplete from '@mui/material/Autocomplete';
import { useNavigate } from 'react-router-dom';
import { getAllOrders, updateOrder } from '../../services/orderService';
import { getAllUsers } from '../../services/userService';
import { getAllPhones } from '../../services/phoneService';
import { modalStyle } from '../modalStyle';

export default function UpdateOrderModal() {
    const navigate = useNavigate();
    const [orders, setOrders] = useState([]);
    const [users, setUsers] = useState([]);
    const [phones, setPhones] = useState([]);
    const [selectedId, setSelectedId] = useState('');
    const [userId, setUserId] = useState('');
    const [smartphoneIds, setSmartphoneIds] = useState([]);

    useEffect(() => {
        getAllOrders().then(setOrders);
        getAllUsers().then(setUsers);
        getAllPhones().then(setPhones);
    }, []);

    useEffect(() => {
        const o = orders.find(o => o.id === +selectedId);
        if (o) {
            setUserId(o.userId);
            setSmartphoneIds(o.smartphones.map(p => p.id));
        }
    }, [selectedId, orders]);

    const handleClose = () => navigate('/orders');
    const handleSubmit = async () => {
        await updateOrder(selectedId, { userId: +userId, smartphoneIds });
        handleClose();
        window.location.reload();
    };

    return (
        <Modal open onClose={handleClose}>
            <Box sx={modalStyle}>
                <Typography variant="h6">Обновить заказ</Typography>
                <Stack spacing={2} sx={{ mt: 2 }}>
                    <TextField
                        select
                        label="Выберите заказ"
                        value={selectedId}
                        onChange={e => setSelectedId(e.target.value)}
                        fullWidth
                    >
                        {orders.map(o => (
                            <MenuItem key={o.id} value={o.id}>
                                #{o.id}
                            </MenuItem>
                        ))}
                    </TextField>
                    <TextField
                        select
                        label="Пользователь"
                        value={userId}
                        onChange={e => setUserId(e.target.value)}
                        fullWidth
                    >
                        {users.map(u => (
                            <MenuItem key={u.id} value={u.id}>
                                {u.username}
                            </MenuItem>
                        ))}
                    </TextField>
                    <Autocomplete
                        multiple
                        options={phones}
                        getOptionLabel={option => `${option.brand} ${option.model}`}
                        value={phones.filter(p => smartphoneIds.includes(p.id))}
                        onChange={(e, value) => setSmartphoneIds(value.map(v => v.id))}
                        renderTags={(value, getTagProps) =>
                            value.map((option, index) => (
                                <Chip
                                    label={`${option.brand} ${option.model}`}
                                    {...getTagProps({ index })}
                                />
                            ))
                        }
                        renderInput={params => (
                            <TextField {...params} label="Телефоны" fullWidth />
                        )}
                    />
                    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                        <Button onClick={handleClose} sx={{ color: 'text.primary' }}>
                            Отмена
                        </Button>
                        <Button
                            variant="contained"
                            color="secondary"
                            onClick={handleSubmit}
                            disabled={!selectedId}
                        >
                            Сохранить
                        </Button>
                    </Box>
                </Stack>
            </Box>
        </Modal>
    );
}
