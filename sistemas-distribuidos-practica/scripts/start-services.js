import { spawn } from 'node:child_process'
import { readdirSync } from 'node:fs'
import { join } from 'node:path'

const root = join(process.cwd(), 'microservicios')
const services = readdirSync(root).filter(name => name.startsWith('microservicio-'))
services.forEach(name => {
  const child = spawn(process.execPath, [join(root, name, 'src', 'server.js')], { stdio: 'inherit', env: { ...process.env } })
  child.on('error', error => console.error(`${name}: ${error.message}`))
})
