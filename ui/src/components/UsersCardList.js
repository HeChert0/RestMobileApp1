// ui/src/components/UsersCardList.js
import React, { useEffect, useState } from 'react';
import {
    Grid, Card, CardActionArea, CardContent,
    Typography
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { getAllUsers } from '../services/userService';

// @ts-nocheck
export default function UsersCardList() {
    const [users, setUsers] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        getAllUsers()
            .then(setUsers)
            .catch(console.error);
    }, []);

    return (
        <Grid container spacing={2} sx={{ mt: 4 }}>
            {users.map(user => (
                <Grid item xs={12} sm={6} md={4} lg={3} key={user.id}>
                    <Card>
                        <CardActionArea onClick={() => navigate(`/users/${user.id}`)}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom>
                                    {user.username}
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    Заказов: {user.orders.length}
                                </Typography>
                            </CardContent>
                        </CardActionArea>
                    </Card>
                </Grid>
            ))}
        </Grid>
    );
}
