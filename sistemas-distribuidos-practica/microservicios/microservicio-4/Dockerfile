FROM node:22-alpine
WORKDIR /app
COPY microservicios/microservicio-4/package.json .
COPY microservicios/microservicio-4/src ./src
COPY shared ./shared
EXPOSE 3004
CMD ["node", "src/server.js"]
