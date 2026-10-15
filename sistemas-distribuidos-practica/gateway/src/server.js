import http from 'node:http'

const port = Number(process.env.PORT || 3000)
const services = {
  1: process.env.SERVICE_1_URL || 'http://localhost:3001',
  2: process.env.SERVICE_2_URL || 'http://localhost:3002',
  3: process.env.SERVICE_3_URL || 'http://localhost:3003',
  4: process.env.SERVICE_4_URL || 'http://localhost:3004',
  5: process.env.SERVICE_5_URL || 'http://localhost:3005',
}

function sendJson(response, status, body) {
  response.writeHead(status, { 'content-type': 'application/json; charset=utf-8', 'access-control-allow-origin': '*' })
  response.end(JSON.stringify(body))
}

const server = http.createServer(async (request, response) => {
  const url = new URL(request.url, `http://${request.headers.host}`)

  if (url.pathname === '/health') return sendJson(response, 200, { service: 'gateway', status: 'ok' })
  if (url.pathname === '/api/services') return sendJson(response, 200, { gateway: 'ok', services })

  const match = url.pathname.match(/^\/api\/microservicio-([1-5])(?:\/(.*))?$/)
  if (!match) return sendJson(response, 404, { error: 'Ruta no encontrada en el gateway' })

  const target = `${services[match[1]]}/${match[2] || 'api'}`
  try {
    const upstream = await fetch(target, { method: request.method, headers: { 'content-type': request.headers['content-type'] || 'application/json' } })
    const body = await upstream.text()
    response.writeHead(upstream.status, { 'content-type': upstream.headers.get('content-type') || 'application/json', 'access-control-allow-origin': '*' })
    response.end(body)
  } catch (error) {
    sendJson(response, 502, { error: 'Microservicio no disponible', detail: error.message })
  }
})

server.listen(port, () => console.log(`Gateway escuchando en http://localhost:${port}`))
