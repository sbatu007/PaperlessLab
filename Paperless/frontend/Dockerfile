# --- Build stage ---
FROM node:20.19-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci || npm install
COPY . .
RUN npm run build

# --- Runtime stage (nginx) ---
FROM nginx:1.29.2-alpine
# Vollständige nginx.conf ersetzen
COPY nginx.conf /etc/nginx/nginx.conf
# gebaute Assets
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
