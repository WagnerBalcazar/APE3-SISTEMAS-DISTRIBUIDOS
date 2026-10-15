FROM node:22-alpine
WORKDIR /app
COPY microservicios/microservicio-2/package.json .
COPY microservicios/microservicio-2/src ./src
COPY shared ./shared
EXPOSE 3002
CMD ["node", "src/server.js"]
