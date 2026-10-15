import http from 'node:http'

export function createService(number, exercise, port) {
  const server = http.createServer((request, response) => {
    const body = request.url === '/health'
      ? { service: `microservicio-${number}`, status: 'ok', exercise }
      : { service: `microservicio-${number}`, exercise, message: 'Respuesta del microservicio' }
    response.writeHead(200, { 'content-type': 'application/json; charset=utf-8', 'access-control-allow-origin': '*' })
    response.end(JSON.stringify(body))
  })
  server.listen(port, () => console.log(`Microservicio ${number} en http://localhost:${port}`))
}
