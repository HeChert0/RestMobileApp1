// ui/src/App.js
import React from 'react';
import PhoneList from './components/PhoneList';
import { Container, Typography } from '@mui/material';

function App() {
    return (
        <Container>
            <Typography variant="h4" align="center" sx={{ mt: 4 }}>
                Каталог телефонов
            </Typography>
            <PhoneList />
        </Container>
    );
}

export default App;
