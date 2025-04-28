// ui/src/components/CreateOrderModal.js
import React, { useState, useEffect } from 'react';
import { Modal, Box, Typography, TextField, Button, Stack, MenuItem, Chip } from '@mui/material';
import Autocomplete from '@mui/material/Autocomplete';
import { useNavigate } from 'react-router-dom';
import { createOrder } from '../../services/orderService';
import { modalStyle } from '../modalStyle';
import { getAllPhones } from '../../services/phoneService'
import { getAllUsers } from '../../services/userService'

export default function CreateOrderModal() {
    const navigate = useNavigate();
    const [users, setUsers] = useState([]);
    const [phones, setPhones] = useState([]);
    const [userId, setUserId] = useState('');
    const [smartphoneIds, setSmartphoneIds] = useState([]);

    useEffect(() => {
        getAllUsers().then(setUsers);
        getAllPhones().then(setPhones);
    }, []);

    const handleClose = () => navigate('/orders');
    const handleSubmit = async () => {
        await createOrder({ userId: +userId, smartphoneIds });
        handleClose();
        window.location.reload();
    };

    return (
        <Modal open onClose={handleClose}>
            <Box sx={modalStyle}>
                <Typography variant="h6">Создать заказ</Typography>
                <Stack spacing={2} sx={{ mt: 2 }}>
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
                            disabled={!userId || smartphoneIds.length === 0}
                        >
                            Создать
                        </Button>
                    </Box>
                </Stack>
            </Box>
        </Modal>
    );
}

