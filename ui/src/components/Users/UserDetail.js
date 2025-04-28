// ui/src/components/UserDetail.js
import React, { useEffect, useState } from 'react';
import {
    Card, CardContent, Typography, Accordion,
    AccordionSummary, AccordionDetails, List,
    ListItem, ListItemText, Container, CircularProgress
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { useParams } from 'react-router-dom';
import axios from 'axios';

export default function UserDetail() {
    const { id } = useParams();
    const [user, setUser] = useState(null);

    useEffect(() => {
        axios.get(`http://localhost:8081/api/users/${id}`)
            .then(res => setUser(res.data))
            .catch(console.error);
    }, [id]);

    if (!user) {
        return (
            <Container sx={{ textAlign: 'center', mt: 6 }}>
                <CircularProgress />
            </Container>
        );
    }

    return (
        <Container sx={{ mt: 4 }}>
            <Card>
                <CardContent>
                    <Typography variant="h5">Пользователь: {user.username}</Typography>
                    <Typography color="text.secondary">ID: {user.id}</Typography>
                    <Typography color="text.secondary">
                        Всего заказов: {user.orders.length}
                    </Typography>
                </CardContent>

                <CardContent>
                    <Typography variant="h6" gutterBottom>
                        Заказы
                    </Typography>
                    {user.orders.map(order => (
                        <Accordion key={order.id}>
                            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                                <Typography>
                                    Заказ #{order.id} — {order.orderDate} — ${order.totalAmount.toFixed(2)}
                                </Typography>
                            </AccordionSummary>
                            <AccordionDetails>
                                <Typography variant="subtitle1">Телефоны в заказе:</Typography>
                                <List>
                                    {order.smartphones.map(p => (
                                        <ListItem key={p.id}>
                                            <ListItemText
                                                primary={`${p.brand} ${p.model}`}
                                                secondary={`Цена: $${p.price.toFixed(2)}`}
                                            />
                                        </ListItem>
                                    ))}
                                </List>
                            </AccordionDetails>
                        </Accordion>
                    ))}
                </CardContent>
            </Card>
        </Container>
    );
}
