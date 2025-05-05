import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Container, Box, Typography, TextField } from '@mui/material';

import NavBar from './components/NavBar';
import PhoneList from './components/Phones/PhoneList';
import OrdersList from './components/Orders/OrdersList';
import UsersCardList from './components/Users/UsersCardList';
import CreateUserModal from './components/Users/CreateUserModal';
import UpdateUserModal from './components/Users/UpdateUserModal';
import DeleteUserModal from './components/Users/DeleteUserModal';
import UserDetail from './components/Users/UserDetail';
import CreateOrderModal from './components/Orders/CreateOrderModal';
import UpdateOrderModal from './components/Orders/UpdateOrderModal';
import DeleteOrderModal from './components/Orders/DeleteOrderModal';
import CreatePhoneModal from './components/Phones/CreatePhoneModal';
import UpdatePhoneModal from './components/Phones/UpdatePhoneModal';
import DeletePhoneModal from './components/Phones/DeletePhoneModal';
import BulkOperationsToolbar from './components/BulkOperationsToolbar';
import OrdersBulkCreateModal from  './components/Orders/OrdersBulkCreateModal'
import PhonesBulkCreateModal from './components/Phones/PhonesBulkCreateModal';
import UsersBulkCreateModal from './components/Users/UsersBulkCreateModal';

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
                <Box component="main" flexGrow={1} sx={{ py: 2 }}>
                    <Container>
                        <Routes>
                            <Route path="/" element={<Navigate to="/phones" replace />} />
                            <Route path="/phones"      element={<PhoneList />} />
                            <Route path="/phones/bulk" element={<><PhoneList /><PhonesBulkCreateModal/></>} />
                            <Route path="/phones/new" element={<><PhoneList /><CreatePhoneModal /></>} />
                            <Route path="/phones/update" element={<><PhoneList /><UpdatePhoneModal /></>} />
                            <Route path="/phones/delete" element={<><PhoneList /><DeletePhoneModal /></>} />

                            <Route path="/orders" element={<><OrdersList /></>} />
                            <Route path="/orders/bulk" element={<><OrdersList /><OrdersBulkCreateModal /> </>} />
                            <Route path="/orders/new" element={<><OrdersList /><CreateOrderModal /></>} />
                            <Route path="/orders/update" element={<><OrdersList /><UpdateOrderModal /></>} />
                            <Route path="/orders/delete" element={<><OrdersList /><DeleteOrderModal /></>} />

                            <Route path="/users"       element={<UsersCardList/>} />
                            <Route path="/users/bulk"  element={<><UsersCardList /><UsersBulkCreateModal/></>} />
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