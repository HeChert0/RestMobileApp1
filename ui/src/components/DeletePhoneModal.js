// ui/src/components/DeletePhoneModal.js
import React, { useState, useEffect } from 'react';
import { Modal, Box, Typography, Button, Stack, MenuItem, TextField } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { getAllPhones, deletePhone } from '../services/phoneService';
import { modalStyle } from './modalStyle';

export default function DeletePhoneModal() {
    const navigate = useNavigate();
    const [phones, setPhones] = useState([]);
    const [selectedId, setSelectedId] = useState('');

    useEffect(() => {
        getAllPhones().then(setPhones);
    }, []);

    const handleClose = () => navigate('/phones');
    const handleDelete = async () => {
        await deletePhone(selectedId);
        handleClose();
        window.location.reload();
    };

    return (
        <Modal open onClose={handleClose}>
            <Box sx={modalStyle}>
                <Typography variant="h6">Удалить телефон</Typography>
                <Stack spacing={2} sx={{ mt: 2 }}>
                    <TextField select label="Выберите телефон" value={selectedId} onChange={e => setSelectedId(e.target.value)} fullWidth>
                        {phones.map(p => (
                            <MenuItem key={p.id} value={p.id}>{`${p.brand} ${p.model}`}</MenuItem>
                        ))}
                    </TextField>
                    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                        <Button onClick={handleClose} sx={{ color: 'text.primary' }}>Отмена</Button>
                        <Button variant="contained" color="secondary" onClick={handleDelete} disabled={!selectedId}>
                            Удалить
                        </Button>
                    </Box>
                </Stack>
            </Box>
        </Modal>
    );
}
