FROM node:22-alpine
WORKDIR /app
COPY microservicios/microservicio-3/package.json .
COPY microservicios/microservicio-3/src ./src
COPY shared ./shared
EXPOSE 3003
CMD ["node", "src/server.js"]
