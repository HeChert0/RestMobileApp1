// ui/src/components/NavBar.js
import React, { useState } from 'react';
import {
    AppBar, Toolbar, Typography, Button,
    Menu, MenuItem
} from '@mui/material';
import { Link as RouterLink, useNavigate, useLocation  } from 'react-router-dom';

export default function NavBar() {
    const navigate = useNavigate();
    const location = useLocation();
    const [anchorEl, setAnchorEl] = useState({});

    const handleOpen = (event, key) => {
        setAnchorEl(prev => ({ ...prev, [key]: event.currentTarget }));
    };
    const handleClose = (key) => {
        setAnchorEl(prev => ({ ...prev, [key]: null }));
    };

    // Опишем сущности для генерации меню
    const entities = [
        { key: 'users', label: 'Пользователи', base: '/users' },
        { key: 'phones', label: 'Телефоны',   base: '/phones' },
        { key: 'orders', label: 'Заказы',     base: '/orders' },
    ];

    return (
        <AppBar position="static">
            <Toolbar>
                <Typography variant="h6" sx={{ flexGrow: 1 }}>
                    MobileApp
                </Typography>

                {entities.map(({ key, label, base }) => {
                    const isActive = location.pathname.startsWith(base);
                    return (<React.Fragment key={key}>
                        <Button
                               color={isActive ? 'secondary' : 'inherit'}
                               sx={{
                                 fontWeight: isActive ? 'bold' : 'normal',
                                 textTransform: 'none'
                               }}
                            onClick={e => handleOpen(e, key)}
                        >
                            {label}
                        </Button>
                        <Menu
                            anchorEl={anchorEl[key]}
                            open={Boolean(anchorEl[key])}
                            onClose={() => handleClose(key)}
                        >
                            <MenuItem onClick={() => { navigate(base);             handleClose(key); }}>Список</MenuItem>
                            <MenuItem onClick={() => { navigate(`${base}/new`);    handleClose(key); }}>Добавить</MenuItem>
                            <MenuItem onClick={() => { navigate(`${base}/update`); handleClose(key); }}>Обновить</MenuItem>
                            <MenuItem onClick={() => { navigate(`${base}/delete`); handleClose(key); }}>Удалить</MenuItem>
                        </Menu>
                    </React.Fragment>)
            })}
            </Toolbar>
        </AppBar>
    );
}
