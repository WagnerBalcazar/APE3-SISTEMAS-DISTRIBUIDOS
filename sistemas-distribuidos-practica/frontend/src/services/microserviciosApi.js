const gatewayUrl = '/api/microservicio-1'

async function request(path = '', options) {
  const response = await fetch(`${gatewayUrl}${path}`, options)
  if (!response.ok) throw new Error(`HTTP ${response.status}`)
  return response.json()
}

export const microserviciosApi = {
  health: number => fetch(`/api/microservicio-${number}/health`).then(response => {
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    return response.json()
  }),
  simular: () => request(),
  estado: () => request('/estacionamiento/estado'),
  llegar: id => request(`/estacionamiento/llegar/${id}`, { method: 'POST' }),
  salir: id => request(`/estacionamiento/salir/${id}`, { method: 'POST' }),
  reiniciar: () => request('/estacionamiento/reiniciar', { method: 'POST' }),
}
