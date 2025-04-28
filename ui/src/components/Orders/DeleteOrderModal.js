// ui/src/components/DeleteOrderModal.js
import React, { useState, useEffect } from 'react';
import { Modal, Box, Typography, Button, Stack, MenuItem, TextField } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { getAllOrders, deleteOrder } from '../../services/orderService';
import { modalStyle } from '../modalStyle';

export default function DeleteOrderModal() {
    const navigate = useNavigate();
    const [orders, setOrders] = useState([]);
    const [selectedId, setSelectedId] = useState('');

    useEffect(() => {
        getAllOrders().then(setOrders);
    }, []);

    const handleClose = () => navigate('/orders');
    const handleDelete = async () => {
        const idNum = parseInt(selectedId, 10);
        if (isNaN(idNum)) {
            console.error('Неверный ID для удаления заказа:', selectedId);
            return;
        }
        await deleteOrder(idNum);
        handleClose();
        window.location.reload();
    };

    return (
        <Modal open onClose={handleClose}>
            <Box sx={modalStyle}>
                <Typography variant="h6">Удалить заказ</Typography>
                <Stack spacing={2} sx={{ mt: 2 }}>
                    <TextField select label="Выберите заказ" value={selectedId}
                               onChange={e => setSelectedId(e.target.value)} fullWidth>
                        {orders.map(o => <MenuItem key={o.id} value={o.id}>#{o.id}</MenuItem>)}
                    </TextField>
                    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                        <Button onClick={handleClose} sx={{ color: 'text.primary' }}>Отмена</Button>
                        <Button variant="contained" color="secondary" onClick={handleDelete} disabled={!selectedId}>Удалить</Button>
                    </Box>
                </Stack>
            </Box>
        </Modal>
    );
}