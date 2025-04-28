// ui/src/components/BulkCreateModal.js
import React, { useState } from 'react';
import {
    Modal, Box, Typography, TextField,
    Button, Stack, Chip, CircularProgress
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { modalStyle } from './modalStyle';

export function BulkCreateModal({ endpoint, renderInputRows }) {
    const navigate = useNavigate();
    const [rows, setRows] = useState([{}]);
    const [loading, setLoading] = useState(false);

    const handleChange = (index, field, value) => {
        const newRows = [...rows]; newRows[index][field] = value;
        setRows(newRows);
    };
    const addRow = () => setRows([...rows, {}]);
    const removeRow = idx => setRows(rows.filter((_, i) => i !== idx));

    const handleSubmit = async () => {
        setLoading(true);
        try {
            await axios.post(endpoint + '/bulk', rows);
            navigate(-1);
            window.location.reload();
        } finally {
            setLoading(false);
        }
    };

    return (
        <Modal open onClose={() => navigate(-1)}>
            <Box sx={{ ...modalStyle, width: 600, maxHeight: '80vh', overflowY: 'auto' }}>
                <Typography variant="h6">Bulk Create</Typography>
                <Stack spacing={2} sx={{ mt: 2 }}>
                    {rows.map((row, idx) => (
                        <Box key={idx} sx={{ display: 'flex', gap: 1 }}>
                            {renderInputRows(row, (field, val) => handleChange(idx, field, val))}
                            <Button color="secondary" onClick={() => removeRow(idx)}>
                                Delete
                            </Button>
                        </Box>
                    ))}
                    <Button
                        variant="contained"
                        color="secondary"
                        onClick={handleSubmit}
                        disabled={loading}
                    >Добавить</Button>
                    <Box sx={{ mt: 2, display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
                        <Button onClick={() => navigate(-1)} sx={{ color: 'text.primary' }}>Cancel</Button>
                        <Button
                            variant="contained"
                            color="secondary"
                            onClick={handleSubmit}
                            disabled={loading}
                        >
                            {loading ? <CircularProgress size={20} color="inherit"/> : 'Submit'}
                        </Button>
                    </Box>
                </Stack>
            </Box>
        </Modal>
    );
}
