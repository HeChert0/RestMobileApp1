// ui/src/App.js
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Container, Box, Typography, TextField } from '@mui/material'; // Добавлен TextField

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
import { BulkCreateModal } from './components/BulkCreateModal';
import OrdersBulkCreateModal from  './components/Orders/OrdersBulkCreateModal'

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
                            <Route path="/phones" element={<><BulkOperationsToolbar basePath="/phones" /><PhoneList /></>} />
                            <Route path="/phones/bulk"
                                element={
                                    <BulkCreateModal
                                        endpoint="http://localhost:8081/api/phones"
                                        renderInputRows={(row, onChange) => (
                                            <>
                                                <TextField
                                                    label="Brand"
                                                    value={row.brand || ''}
                                                    onChange={e => onChange('brand', e.target.value)}
                                                />
                                                <TextField
                                                    label="Model"
                                                    value={row.model || ''}
                                                    onChange={e => onChange('model', e.target.value)}
                                                />
                                                <TextField
                                                    label="Price"
                                                    type="number"
                                                    value={row.price || ''}
                                                    onChange={e => onChange('price', parseFloat(e.target.value || 0))}
                                                />
                                            </>
                                        )}
                                    />
                                }
                            />
                            <Route path="/phones/new" element={<><PhoneList /><CreatePhoneModal /></>} />
                            <Route path="/phones/update" element={<><PhoneList /><UpdatePhoneModal /></>} />
                            <Route path="/phones/delete" element={<><PhoneList /><DeletePhoneModal /></>} />

                            <Route path="/orders" element={<><OrdersList /></>} />
                            <Route
                                path="/orders/bulk"
                                element={
                                    <>
                                        <OrdersList />
                                        <OrdersBulkCreateModal />
                                    </>
                                }
                            />
                            <Route path="/orders/new" element={<><OrdersList /><CreateOrderModal /></>} />
                            <Route path="/orders/update" element={<><OrdersList /><UpdateOrderModal /></>} />
                            <Route path="/orders/delete" element={<><OrdersList /><DeleteOrderModal /></>} />

                            <Route path="/users" element={<><BulkOperationsToolbar basePath="/users" /><UsersCardList /></>} />
                            <Route path="/users/bulk" element={
                                    <BulkCreateModal
                                        endpoint="http://localhost:8081/api/users"
                                        renderInputRows={(row, onChange) => (
                                            <>
                                                <TextField
                                                    label="Username"
                                                    value={row.username || ''}
                                                    onChange={e => onChange('username', e.target.value)}
                                                />
                                                <TextField
                                                    label="Password"
                                                    type="password"
                                                    value={row.password || ''}
                                                    onChange={e => onChange('password', e.target.value)}
                                                />
                                            </>
                                        )}
                                    />
                                }
                            />
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