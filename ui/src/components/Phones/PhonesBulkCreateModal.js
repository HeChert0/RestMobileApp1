import React, { useState } from 'react';
import { Modal, Box, Typography, TextField, Button, Stack } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import Autocomplete from '@mui/material/Autocomplete';
import { getAllPhones, createPhonesBulk } from '../../services/phoneService'; // adjust imports if needed
import { modalStyle } from '../modalStyle';

export default function PhonesBulkCreateModal() {
    const navigate = useNavigate();
    const [phonesList, setPhonesList] = useState([]);
    const [rows, setRows] = useState([{ brand: '', model: '', price: '' }]);
    const [loading, setLoading] = useState(false);

    const addRow = () => setRows([...rows, { brand: '', model: '', price: '' }]);
    const removeRow = idx => {
        if (rows.length <= 1) {
            alert("Нельзя удалить последний элемент");
            return;
        }
        setRows(rows.filter((_, i) => i !== idx));
    };
    const handleChange = (idx, field, value) => {
        const newRows = [...rows];
        newRows[idx][field] = value;
        setRows(newRows);
    };

    const handleSubmit = async () => {
        setLoading(true);
        try {
            await createPhonesBulk(rows.map(r => ({ brand: r.brand, model: r.model, price: parseFloat(r.price) })));
            navigate('/phones');
            window.location.reload();
        } catch(e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Modal open onClose={() => navigate('/phones')}>
            <Box sx={{ ...modalStyle, width: 600, maxHeight: '80vh', overflowY: 'auto' }}>
                <Typography variant="h6">Bulk-Создание Телефонов</Typography>
                <Stack spacing={2} sx={{ mt: 2 }}>
                    {rows.map((row, idx) => (
                        <Stack key={idx} direction="row" spacing={2} alignItems="center">
                            <TextField
                                label="Brand"
                                value={row.brand}
                                onChange={e => handleChange(idx, 'brand', e.target.value)}
                                sx={{ minWidth: 150 }}
                            />
                            <TextField
                                label="Model"
                                value={row.model}
                                onChange={e => handleChange(idx, 'model', e.target.value)}
                                sx={{ minWidth: 150 }}
                            />
                            <TextField
                                label="Price"
                                type="number"
                                value={row.price}
                                onChange={e => handleChange(idx, 'price', e.target.value)}
                                sx={{ minWidth: 100 }}
                            />
                            <Button color="secondary" onClick={() => removeRow(idx)}>Удалить</Button>
                        </Stack>
                    ))}
                    <Button
                        variant="contained"
                        color="secondary"
                        onClick={addRow}
                    >Добавить</Button>
                    <Stack direction="row" spacing={2} justifyContent="flex-end">
                        <Button onClick={() => navigate('/phones')} sx={{ color: 'text.primary' }}>Отмена</Button>
                        <Button variant="contained" color="secondary" onClick={handleSubmit} disabled={loading}>
                            {loading ? 'Создание...' : 'Создать'}
                        </Button>
                    </Stack>
                </Stack>
            </Box>
        </Modal>
    );
}

