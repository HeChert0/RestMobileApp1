// ui/src/components/BulkOperationsToolbar.js
import React from 'react';
import { Box, Button, Stack } from '@mui/material';
import { useNavigate } from 'react-router-dom';

export default function BulkOperationsToolbar({ basePath }) {
    const navigate = useNavigate();
    return (
        <Box sx={{ my: 2 }}>
            <Stack direction="row" spacing={2}>
                <Button
                    variant="outlined"
                    color="secondary"
                    onClick={() => navigate(`${basePath}/bulk`)}
                >
                    Bulk Create
                </Button>
            </Stack>
        </Box>
    );
}