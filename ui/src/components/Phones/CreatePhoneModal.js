import React, { useState } from 'react';
import { Modal, Box, Typography, TextField, Button, Stack } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { createPhone } from '../../services/phoneService';
import { modalStyle } from '../modalStyle';

export default function CreatePhoneModal() {
    const navigate = useNavigate();
    const [brand, setBrand] = useState('');
    const [model, setModel] = useState('');
    const [price, setPrice] = useState('');
    const [errors, setErrors] = useState({});

    const handleClose = () => navigate('/phones');
    const handleSubmit = async () => {
        try {
            setErrors({});
            await createPhone({ brand, model, price: parseFloat(price) });
            handleClose();
            window.location.reload();
        } catch (e) {
            if (e.response && e.response.status === 400 && e.response.data) {
                setErrors(e.response.data);
            } else {
                console.error('Create user failed:', e);
            }
        } finally {
        }
    };

    return (
        <Modal open onClose={handleClose}>
            <Box sx={modalStyle}>
                <Typography variant="h6">Создать телефон</Typography>
                <Stack spacing={2} sx={{ mt: 2 }}>
                    <TextField label="Бренд*"
                               value={brand}
                               onChange={e => setBrand(e.target.value)}
                               error={Boolean(errors.username)}
                               helperText={errors.username}
                               fullWidth
                    />
                    <TextField label="Модель*"
                               value={model}
                               onChange={e => setModel(e.target.value)}
                               error={Boolean(errors.username)}
                               helperText={errors.username}
                               fullWidth
                    />
                    <TextField label="Цена*"
                               type="number"
                               value={price}
                               onChange={e => setPrice(e.target.value)}
                               error={Boolean(errors.username)}
                               helperText={errors.username}
                               fullWidth
                    />
                    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                        <Button onClick={handleClose} sx={{ color: 'text.primary' }}>Отмена</Button>
                        <Button variant="contained" color="secondary" onClick={handleSubmit} disabled={!brand || !model || !price}>
                            Создать
                        </Button>
                    </Box>
                </Stack>
            </Box>
        </Modal>
    );
}
