import React, { useState } from 'react';
import { Modal, Box, Typography, TextField, Button, Stack } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { createUser } from '../../services/userService';
import { modalStyle } from '../modalStyle';

export default function CreateUserModal() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);

    const handleClose = () => navigate('/users');
    const handleSubmit = async () => {
        setLoading(true);
          try {
              setErrors({});await createUser({ username, password });
              handleClose();
              window.location.reload();
          } catch (e) {
              if (e.response && e.response.status === 400 && e.response.data) {
                  setErrors(e.response.data);
              } else {
                  console.error('Create user failed:', e);
              }
          } finally {
              setLoading(false);
          }
    };

    return (
        <Modal open onClose={handleClose}>
            <Box sx={modalStyle}>
                <Typography variant="h6">Добавить пользователя</Typography>
                <Stack spacing={2} sx={{ mt: 2 }}>
                    <TextField
                        label="Username*"
                        value={username}
                        onChange={e => setUsername(e.target.value)}
                        error={Boolean(errors.username)}
                        helperText={errors.username}
                        fullWidth
                    />
                    <TextField
                        label="Password*"
                        type="password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        error={Boolean(errors.password)}
                        helperText={errors.password}
                        fullWidth
                    />
                    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                        <Button onClick={handleClose} sx={{ color: 'text.primary' }}>Отмена</Button>
                        <Button variant="contained" color="secondary" onClick={handleSubmit} disabled={loading}>
                            {loading ? 'Создание…' : 'Создать'}
                        </Button>
                    </Box>
                </Stack>
            </Box>
        </Modal>
    );
}
