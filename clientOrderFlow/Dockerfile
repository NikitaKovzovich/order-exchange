FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build -- --configuration production

FROM nginx:alpine
ENV API_GATEWAY_HOST=host.docker.internal
ENV API_GATEWAY_PORT=8765
COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/orderflow/browser /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

