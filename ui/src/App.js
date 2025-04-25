// ui/src/App.js
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Container } from '@mui/material';

import NavBar from './components/NavBar';
import PhoneList from './components/PhoneList';
import AddPhone   from './components/AddPhone';
import OrdersList        from './components/OrdersList';
import UsersCardList    from './components/UsersCardList';
import AddUser          from './components/AddUser';
import UserDetail       from './components/UserDetail';

function App() {
    return (
        <Router>
            <NavBar />
            <Container>
                <Routes>
                    <Route path="/" element={<Navigate to="/phones" replace />} />
                    <Route path="/phones" element={<PhoneList />} />
                    <Route path="/orders" element={<OrdersList />} />
                    <Route path="/phones/new" element={<AddPhone />} />
                    <Route path="/users"      element={<UsersCardList />} />
                    <Route path="/users/new"  element={<AddUser />}  />
                    <Route path="/users/:id"  element={<UserDetail />} />
                </Routes>
            </Container>
        </Router>
    );
}

export default App;
