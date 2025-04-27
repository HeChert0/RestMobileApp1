// ui/src/App.js
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Container, Box, Typography } from '@mui/material';

import NavBar from './components/NavBar';
import PhoneList from './components/PhoneList';
import OrdersList        from './components/OrdersList';
import UsersCardList    from './components/UsersCardList';
import CreateUserModal  from './components/CreateUserModal';
import UpdateUserModal  from './components/UpdateUserModal';
import DeleteUserModal  from './components/DeleteUserModal';
import UserDetail       from './components/UserDetail';
import CreateOrderModal from './components/CreateOrderModal';
import UpdateOrderModal from './components/UpdateOrderModal';
import DeleteOrderModal from './components/DeleteOrderModal';
import CreatePhoneModal  from './components/CreatePhoneModal';
import UpdatePhoneModal  from './components/UpdatePhoneModal';
import DeletePhoneModal  from './components/DeletePhoneModal';


function Footer() {
    return (
        <Box
            component="footer"
            sx={{
                py: 2,
                textAlign: 'center',
                bgcolor: 'background.paper'
            }}
        >
            <Typography variant="body2" color="text.secondary">
                © 2025 MobileApp. Все права защищены.
            </Typography>
        </Box>
    );
}

function App() {
    return (
        <Router>
            <Box display="flex" flexDirection="column" minHeight="100vh">
                <NavBar />

                {/* Основное тело растягиваем */}
                <Box component="main" flexGrow={1} sx={{ py: 2 }}>
                    <Container>
                        <Routes>
                            <Route path="/" element={<Navigate to="/phones" replace />} />
                            <Route path="/phones" element={<PhoneList />} />
                            <Route path="/phones/new" element={<><PhoneList /><CreatePhoneModal /></>} />
                            <Route path="/phones/update" element={<><PhoneList /><UpdatePhoneModal /></>} />
                            <Route path="/phones/delete" element={<><PhoneList /><DeletePhoneModal /></>} />
                            <Route path="/orders" element={<OrdersList />} />
                            <Route path="/orders/new" element={<><OrdersList /><CreateOrderModal /></>} />
                            <Route path="/orders/update" element={<><OrdersList /><UpdateOrderModal /></>} />
                            <Route path="/orders/delete" element={<><OrdersList /><DeleteOrderModal /></>} />
                            <Route path="/users" element={<UsersCardList />} />
                            <Route path="/users/new" element={<><UsersCardList /><CreateUserModal /></>} />
                            <Route path="/users/update" element={<><UsersCardList /><UpdateUserModal /></>} />
                            <Route path="/users/delete" element={<><UsersCardList /><DeleteUserModal /></>} />
                            <Route path="/users/:id" element={<UserDetail />} />
                        </Routes>
                    </Container>
                </Box>
                <Footer />
            </Box>
        </Router>
    );
}

export default App;
