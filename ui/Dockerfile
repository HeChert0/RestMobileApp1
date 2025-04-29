# Stage 1: сборка через Node.js
FROM node:16-alpine AS build
WORKDIR /app
# Копируем манифесты и устанавливаем зависимости
COPY package.json package-lock.json* ./
RUN npm install
# Копируем весь код и собираем
COPY . .
RUN npm run build

# Stage 2: Nginx для статики
FROM nginx:stable-alpine
# Удаляем дефолтный конфиг
RUN rm /etc/nginx/conf.d/default.conf
# Копируем свой
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Копируем статику
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]