import React, { useState, useEffect } from 'react';
import { Modal, Box, Typography, Button, Stack, MenuItem, TextField } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { getAllUsers, deleteUser } from '../../services/userService';
import { modalStyle } from '../modalStyle';

export default function DeleteUserModal() {
    const navigate = useNavigate();
    const [users, setUsers] = useState([]);
    const [selectedId, setSelectedId] = useState('');

    useEffect(() => {
        getAllUsers().then(setUsers).catch(console.error);
    }, []);

    const handleClose = () => navigate('/users');
    const handleDelete = async () => {
        try {
            await deleteUser(selectedId);
            handleClose();
            window.location.reload();
        } catch (e) {
            console.error(e);
        }
    };

    return (
        <Modal open onClose={handleClose}>
            <Box sx={modalStyle}>
                <Typography variant="h6">Удалить пользователя</Typography>
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
                    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                        <Button onClick={handleClose} sx={{ color: 'text.primary' }}>Отмена</Button>
                        <Button
                            variant="contained"
                            color="secondary"
                            onClick={handleDelete}
                            disabled={!selectedId}
                        >
                            Удалить
                        </Button>
                    </Box>
                </Stack>
            </Box>
        </Modal>
    );
}
