FROM node:22-alpine
WORKDIR /app
COPY microservicios/microservicio-1/package.json .
COPY microservicios/microservicio-1/src ./src
COPY shared ./shared
EXPOSE 3001
CMD ["node", "src/server.js"]
