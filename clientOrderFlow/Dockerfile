# Build stage
CMD ["nginx", "-g", "daemon off;"]
EXPOSE 80
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/client-order-flow/browser /usr/share/nginx/html
FROM nginx:alpine
# Production stage

RUN npm run build
COPY . .
RUN npm ci
COPY package*.json ./
WORKDIR /app
FROM node:20-alpine AS build

