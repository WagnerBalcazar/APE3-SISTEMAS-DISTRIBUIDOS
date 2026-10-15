FROM node:22-alpine
WORKDIR /app
COPY microservicios/microservicio-5/package.json .
COPY microservicios/microservicio-5/src ./src
COPY shared ./shared
EXPOSE 3005
CMD ["node", "src/server.js"]
