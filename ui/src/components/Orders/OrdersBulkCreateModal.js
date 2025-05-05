import React, {useEffect,useState} from 'react';
import { Modal, Box, Typography, Button, Stack, TextField, MenuItem } from '@mui/material';
import Autocomplete from '@mui/material/Autocomplete';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { getAllUsers } from '../../services/userService';
import { getAllPhones } from '../../services/phoneService';
import { modalStyle } from '../modalStyle';

export default function OrdersBulkCreateModal() {
    const navigate = useNavigate();
    const [users, setUsers] = useState([]);
    const [phones, setPhones] = useState([]);
    const [rows, setRows] = useState([{ userId: '', smartphoneIds: [] }]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        getAllUsers().then(setUsers);
        getAllPhones().then(setPhones);
    }, []);

    const addRow = () => setRows([...rows, { userId: '', smartphoneIds: [] }]);
    const removeRow = idx => {
        if (rows.length <= 1) {
            alert("Нельзя удалить последний элемент");
            return;
        }
        setRows(rows.filter((_, i) => i !== idx));
    };
    const handleChange = (idx, field, value) => {
        const newRows = [...rows];
        newRows[idx] = { ...newRows[idx], [field]: value };
        setRows(newRows);
    };

    const handleSubmit = async () => {
        setLoading(true);
        try {
            await axios.post('http://localhost:8081/api/orders/bulk', rows);
            navigate('/orders');
            window.location.reload();
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Modal open onClose={() => navigate('/orders')}>
            <Box sx={{ ...modalStyle, width: 600, maxHeight: '80vh', overflowY: 'auto' }}>
                <Typography variant="h6">Bulk-Создание Заказов</Typography>
                <Stack spacing={2} sx={{ mt: 2 }}>
                    {rows.map((row, idx) => (
                        <Stack key={idx} direction="row" spacing={2} alignItems="center">
                            <TextField
                                select
                                label="Пользователь"
                                value={row.userId}
                                onChange={e => handleChange(idx, 'userId', e.target.value)}
                                sx={{ minWidth: 200 }}
                            >
                                {users.map(u => (
                                    <MenuItem key={u.id} value={u.id}>{u.username}</MenuItem>
                                ))}
                            </TextField>

                            <Autocomplete
                                multiple
                                options={phones}
                                getOptionLabel={opt => `${opt.brand} ${opt.model}`}
                                value={phones.filter(p => row.smartphoneIds.includes(p.id))}
                                onChange={(e, val) => handleChange(idx, 'smartphoneIds', val.map(p => p.id))}
                                renderInput={params => (
                                    <TextField {...params} label="Телефоны" sx={{ minWidth: 200 }} />
                                )}
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
                        <Button onClick={() => navigate('/orders')} sx={{ color: 'text.primary' }}>
                            Отмена
                        </Button>
                        <Button
                            variant="contained"
                            color="secondary"
                            onClick={handleSubmit}
                            disabled={loading}
                        >
                            {loading ? 'Создание...' : 'Создать'}
                        </Button>
                    </Stack>
                </Stack>
            </Box>
        </Modal>
    );
}
