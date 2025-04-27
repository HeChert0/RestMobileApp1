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
        <Grid
            container
            spacing={3}
            sx={{ mt: 4, px: 3 }}
            justifyContent="flex-start"
        >
            {users.map(user => (
                <Grid item xs="auto" key={user.id}>
                    <Card
                        sx={{
                            width: 350,
                            height: 240,
                            display: 'flex',
                            flexDirection: 'column',
                            justifyContent: 'center',
                            p: 2,
                        }}
                    >
                        <CardActionArea onClick={() => navigate(`/users/${user.id}`)}>
                            <CardContent>
                                <Typography variant="h4" gutterBottom>
                                    {user.username}
                                </Typography>
                                <Typography variant="h6" color="text.secondary">
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
