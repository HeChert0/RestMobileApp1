// ui/src/components/UpdatePhoneModal.js
import React, { useState, useEffect } from 'react';
import { Modal, Box, Typography, TextField, Button, Stack, MenuItem } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { getAllPhones, updatePhone } from '../services/phoneService';
import { modalStyle } from './modalStyle';

export default function UpdatePhoneModal() {
    const navigate = useNavigate();
    const [phones, setPhones] = useState([]);
    const [selectedId, setSelectedId] = useState('');
    const [brand, setBrand] = useState('');
    const [model, setModel] = useState('');
    const [price, setPrice] = useState('');

    useEffect(() => {
        getAllPhones().then(setPhones);
    }, []);

    useEffect(() => {
        const p = phones.find(p => p.id === +selectedId);
        if (p) {
            setBrand(p.brand);
            setModel(p.model);
            setPrice(p.price.toString());
        }
    }, [selectedId, phones]);

    const handleClose = () => navigate('/phones');
    const handleSubmit = async () => {
        await updatePhone(selectedId, { brand, model, price: parseFloat(price) });
        handleClose();
        window.location.reload();
    };

    return (
        <Modal open onClose={handleClose}>
            <Box sx={modalStyle}>
                <Typography variant="h6">Обновить телефон</Typography>
                <Stack spacing={2} sx={{ mt: 2 }}>
                    <TextField select label="Выберите телефон" value={selectedId} onChange={e => setSelectedId(e.target.value)} fullWidth>
                        {phones.map(p => (
                            <MenuItem key={p.id} value={p.id}>{`${p.brand} ${p.model}`}</MenuItem>
                        ))}
                    </TextField>
                    <TextField label="Бренд" value={brand} onChange={e => setBrand(e.target.value)} fullWidth />
                    <TextField label="Модель" value={model} onChange={e => setModel(e.target.value)} fullWidth />
                    <TextField label="Цена" type="number" value={price} onChange={e => setPrice(e.target.value)} fullWidth />
                    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                        <Button onClick={handleClose} sx={{ color: 'text.primary' }}>Отмена</Button>
                        <Button variant="contained" color="secondary" onClick={handleSubmit} disabled={!selectedId}>
                            Сохранить
                        </Button>
                    </Box>
                </Stack>
            </Box>
        </Modal>
    );
}

