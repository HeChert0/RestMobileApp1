import React, { useState } from 'react';
import { Modal, Box, Typography, TextField, Button, Stack } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { createUsersBulk  } from '../../services/userService';
import { modalStyle } from '../modalStyle';

export default function UsersBulkCreateModal() {
    const navigate = useNavigate();
    const [rows, setRows] = useState([{ username: '', password: '' }]);
    const [loading, setLoading] = useState(false);

    const addRow = () => setRows([...rows, { username: '', password: '' }]);
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
            await createUsersBulk(rows);
            navigate('/users');
            window.location.reload();
        } catch(e) {
            console.error('Bulk create users failed:', e);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Modal open onClose={() => navigate('/users')}>
            <Box sx={{ ...modalStyle, width: 600, maxHeight: '80vh', overflowY: 'auto' }}>
                <Typography variant="h6">Bulk-Создание Пользователей</Typography>
                <Stack spacing={2} sx={{ mt: 2 }}>
                    {rows.map((row, idx) => (
                        <Stack key={idx} direction="row" spacing={2} alignItems="center">
                            <TextField
                                label="Username"
                                value={row.username}
                                onChange={e => handleChange(idx, 'username', e.target.value)}
                                sx={{ minWidth: 200 }}
                            />
                            <TextField
                                label="Password"
                                type="password"
                                value={row.password}
                                onChange={e => handleChange(idx, 'password', e.target.value)}
                                sx={{ minWidth: 200 }}
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
                        <Button onClick={() => navigate('/users')} sx={{ color: 'text.primary' }}>Отмена</Button>
                        <Button variant="contained" color="secondary" onClick={handleSubmit} disabled={loading}>
                            {loading ? 'Создание...' : 'Создать'}
                        </Button>
                    </Stack>
                </Stack>
            </Box>
        </Modal>
    );
}
