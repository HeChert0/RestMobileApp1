import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';

const theme = createTheme({
        palette: {
            mode: 'dark',
            primary: { main: '#000000' },
            background: { default: '#121212', paper: '#1F1F1F' },
            text: { primary: '#FFFFFF', secondary: '#BBBBBB' },
            secondary: { main: '#4CAF50', contrastText: '#000000' },
        },
      components: {
            MuiOutlinedInput: {
                styleOverrides: {
                    input: {
                       // откл спиннер в Chrome/Safari
                           '&::-webkit-outer-spin-button, &::-webkit-inner-spin-button': {
                             '-webkit-appearance': 'none',
                                 margin: 0,
                               },
                       //в Firefox
                           '&[type=number]': {
                             '-moz-appearance': 'textfield',
                               },
                    },
                },
            },
        MuiInputLabel: {
              styleOverrides: {
                    root: {
                          '&.Mui-focused': {
                                color: '#4CAF50',
                                  },
                        },
                  },
            },
        MuiButton: {
              styleOverrides: {
                    containedSecondary: {
                          color: '#000000',
                            },
                   },
            },
      },
});

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
          <ThemeProvider theme={theme}>
            <CssBaseline />
      <App />
          </ThemeProvider>
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
