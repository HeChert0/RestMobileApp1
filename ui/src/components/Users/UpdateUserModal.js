import React, { useState, useEffect } from 'react';
import { Modal, Box, Typography, TextField, Button, Stack, MenuItem } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { getAllUsers, updateUser } from '../../services/userService';
import { modalStyle } from '../modalStyle';

export default function UpdateUserModal() {
    const navigate = useNavigate();
    const [users, setUsers] = useState([]);
    const [selectedId, setSelectedId] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false)

    useEffect(() => {
        getAllUsers().then(setUsers).catch(console.error);
    }, []);

    useEffect(() => {
        const u = users.find(u => u.id === +selectedId);
        if (u) {
            setUsername(u.username);
            setPassword('');
        }
    }, [selectedId, users]);

    const handleClose = () => navigate('/users');
     const handleSubmit = async () => {
         setLoading(true);
           try {
               setErrors({});
               const payload = { username, password };
               await updateUser(selectedId, payload);
               handleClose();
               window.location.reload();
           } catch (e) {
               if (e.response && e.response.status === 400 && e.response.data) {
                       setErrors(e.response.data);
               } else {
                   console.error('Create user failed:', e);
               }
           }
           finally {
               setLoading(false);
           }
     };

    return (
        <Modal open onClose={handleClose}>
            <Box sx={modalStyle}>
                <Typography variant="h6">Обновить пользователя</Typography>
                <Stack spacing={2} sx={{ mt: 2 }}>
                    <TextField
                        select
                        label="Выберите пользователя"
                        value={selectedId}
                        onChange={e => setSelectedId(e.target.value)}
                        fullWidth
                    >
                        {users.map(u => (
                            <MenuItem key={u.id} value={u.id}>
                                {u.username}
                            </MenuItem>
                        ))}
                    </TextField>
                    <TextField
                        label="Username*"
                        value={username}
                        onChange={e => setUsername(e.target.value)}
                        error={Boolean(errors.username)}
                        helperText={errors.username}
                        fullWidth
                    />
                    <TextField
                        label="Password"
                        type="password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        fullWidth
                    />
                    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                        <Button onClick={handleClose} sx={{ color: 'text.primary' }}>Отмена</Button>
                        <Button
                            variant="contained"
                            color="secondary"
                            onClick={handleSubmit}
                            disabled={!selectedId}
                        >
                            {loading ? 'Сохранение…' : 'Сохранить'}
                        </Button>
                    </Box>
                </Stack>
            </Box>
        </Modal>
    );
}
