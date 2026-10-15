import { useEffect, useMemo, useState } from 'react'
import { controlQuestions, emptyState, evaluationRows, exercises } from './data'

const storageKey = 'sistemas-distribuidos-practica'
const serviceNames = {
  1: 'Control de acceso al estacionamiento',
  2: 'Sincronizacion y coordinacion',
  3: 'Tolerancia a fallos',
  4: 'Consistencia de datos',
  5: 'Caso integrador',
}

function Field({ label, value, onChange, type = 'text' }) {
  return <label className="field"><span>{label}</span><input type={type} value={value} onChange={e => onChange(e.target.value)} /></label>
}

function TextBlock({ label, children }) {
  return <div><span className="field-label">{label}</span><div className="info-box">{children}</div></div>
}

function Exercise({ index, response, updateExercise, updateAnswer }) {
  const exercise = exercises[index]
  return <article className="card">
    <div className="exercise-heading"><div className="exercise-number">{index + 1}</div><div><span className="eyebrow">Ejercicio {index + 1}</span><h2>{exercise.title}</h2></div></div>
    <TextBlock label="Objetivo">{exercise.objective}</TextBlock>
    <TextBlock label="Situación o descripción del problema">{exercise.situation}</TextBlock>
    <label className="field-label" htmlFor={`code-${index}`}>Código o pseudocódigo</label>
    <textarea id={`code-${index}`} className="code-area" placeholder="// Escribe aquí tu solución..." value={response.code} onChange={e => updateExercise(index, 'code', e.target.value)} />
    <h3>Preguntas de análisis</h3>
    {exercise.questions.map((question, questionIndex) => <div className="question" key={question}>
      <strong>{question}</strong><textarea placeholder="Escribe tu respuesta..." value={response.answers[questionIndex]} onChange={e => updateAnswer(index, questionIndex, e.target.value)} />
    </div>)}
    <div className="two-columns"><TextBlock label="Resultados esperados">{exercise.results}</TextBlock><TextBlock label="Criterios de verificación">{exercise.criteria}</TextBlock></div>
    <label className="field-label" htmlFor={`evidence-${index}`}>Evidencias</label>
    <textarea id={`evidence-${index}`} placeholder="Capturas, enlaces, resultados de pruebas o notas..." value={response.evidence} onChange={e => updateExercise(index, 'evidence', e.target.value)} />
  </article>
}

function Control({ answers, updateControl }) {
  return <article className="card"><span className="eyebrow">Cierre conceptual</span><h2>Preguntas de control</h2><p>Responde utilizando conceptos, ejemplos y conclusiones propias.</p>
    {controlQuestions.map((question, index) => <div className="question" key={question}><strong>{index + 1}. {question}</strong><textarea placeholder="Escribe tu respuesta..." value={answers[index]} onChange={e => updateControl(index, e.target.value)} /></div>)}
  </article>
}

function Evaluation({ data, updateField }) {
  return <article className="card"><span className="eyebrow">Autoevaluación</span><h2>Evaluación</h2>
    <div className="table-wrap"><table><thead><tr><th>Criterio</th><th>Excelente</th><th>Bueno</th><th>Bajo</th></tr></thead><tbody>{evaluationRows.map(row => <tr key={row[0]}>{row.map((cell, index) => <td key={cell}>{index === 0 ? <strong>{cell}</strong> : cell}</td>)}</tr>)}</tbody></table></div>
    <label className="field-label">Bibliografía</label><textarea placeholder="Libros, artículos, documentación y enlaces..." value={data.bibliography} onChange={e => updateField('bibliography', e.target.value)} />
    <label className="field-label">Anexos</label><textarea placeholder="Enlaces a GitHub, videos, capturas o material adicional..." value={data.annexes} onChange={e => updateField('annexes', e.target.value)} />
  </article>
}

export default function App() {
  const [activeTab, setActiveTab] = useState('exercise-0')
  const [data, setData] = useState(() => {
    try { return JSON.parse(localStorage.getItem(storageKey)) || emptyState } catch { return emptyState }
  })
  const [saved, setSaved] = useState(false)
  const [serviceHealth, setServiceHealth] = useState({})
  const [parkingSimulation, setParkingSimulation] = useState(null)
  const [parkingState, setParkingState] = useState(null)
  const [nextVehicle, setNextVehicle] = useState(1)

  useEffect(() => {
    localStorage.setItem(storageKey, JSON.stringify(data))
    setSaved(true)
    const timer = setTimeout(() => setSaved(false), 1600)
    return () => clearTimeout(timer)
  }, [data])

  const checkService = number => fetch(`/api/microservicio-${number}/health`).then(response => response.ok ? response.json() : Promise.reject()).then(result => setServiceHealth(current => ({ ...current, [number]: result }))).catch(() => setServiceHealth(current => ({ ...current, [number]: null })))
  useEffect(() => { [1, 2, 3, 4, 5].forEach(checkService) }, [])

  const loadParkingSimulation = () => fetch('/api/microservicio-1').then(response => response.ok ? response.json() : Promise.reject()).then(result => { if (result?.capacidad === undefined) throw new Error(); setParkingSimulation(result); return loadParkingState().then(() => setParkingState(current => current ? { ...current, mensaje: `Simulación completada: ${result.totalVehiculos} vehículos procesados.` } : current)) }).catch(() => { setParkingSimulation(null); setParkingState(current => current ? { ...current, mensaje: 'No se pudo ejecutar. Verifica que el microservicio 1 y el Gateway estén encendidos.' } : current) })
  const loadParkingState = () => fetch('/api/microservicio-1/estacionamiento/estado').then(response => response.ok ? response.json() : Promise.reject()).then(result => Array.isArray(result?.vehiculosDentro) ? setParkingState(result) : setParkingState(null)).catch(() => setParkingState(null))
  const arriveVehicle = () => fetch(`/api/microservicio-1/estacionamiento/llegar/${nextVehicle}`, { method: 'POST' }).then(response => response.ok ? response.json() : Promise.reject()).then(result => { setNextVehicle(current => current + 1); setParkingState(result) }).catch(() => loadParkingState())
  const leaveVehicle = () => { const id = parkingState?.vehiculosDentro?.[0]; if (id) fetch(`/api/microservicio-1/estacionamiento/salir/${id}`, { method: 'POST' }).then(response => response.ok ? response.json() : Promise.reject()).then(setParkingState).catch(() => loadParkingState()) }
  const resetParking = () => fetch('/api/microservicio-1/estacionamiento/reiniciar', { method: 'POST' }).then(response => response.ok ? response.json() : Promise.reject()).then(result => { setParkingState(result); setNextVehicle(1) }).catch(() => loadParkingState())
  useEffect(() => { loadParkingSimulation(); loadParkingState() }, [])

  const updateField = (field, value) => setData(current => ({ ...current, [field]: value }))
  const updateExercise = (index, field, value) => setData(current => ({ ...current, exercises: current.exercises.map((item, i) => i === index ? { ...item, [field]: value } : item) }))
  const updateAnswer = (exerciseIndex, answerIndex, value) => setData(current => ({ ...current, exercises: current.exercises.map((item, i) => i === exerciseIndex ? { ...item, answers: item.answers.map((answer, j) => j === answerIndex ? value : answer) } : item) }))
  const updateControl = (index, value) => setData(current => ({ ...current, control: current.control.map((answer, i) => i === index ? value : answer) }))

  const progress = useMemo(() => {
    const values = [...data.exercises.flatMap(e => [e.code, ...e.answers, e.evidence]), ...data.control, data.bibliography, data.annexes]
    return Math.round(values.filter(value => value.trim()).length / values.length * 100)
  }, [data])

  return <>
     <header className="hero"><div className="hero-content"><div><span className="eyebrow">Panel de demostracion</span><h1>Sistema distribuido</h1><p>Frontend React conectado a un API Gateway y cinco microservicios Spring Boot.</p></div></div></header>
    <div className="container layout"><nav className="sidebar">{exercises.map((exercise, index) => <button key={exercise.title} className={`tab ${activeTab === `exercise-${index}` ? 'active' : ''}`} onClick={() => setActiveTab(`exercise-${index}`)}>Ejercicio {index + 1}</button>)}<button className={`tab ${activeTab === 'control' ? 'active' : ''}`} onClick={() => setActiveTab('control')}>Preguntas de control</button><button className={`tab ${activeTab === 'evaluation' ? 'active' : ''}`} onClick={() => setActiveTab('evaluation')}>Evaluación</button></nav>
      <main><div className="progress-card"><div><strong>Progreso de la práctica</strong><small className="save-status">{saved ? 'Guardado automáticamente' : 'Los cambios se guardan solos'}</small></div><div className="progress-bar"><div style={{ width: `${progress}%` }} /></div><strong>{progress}%</strong></div>
         <section className="card service-card"><div><span className="eyebrow">Arquitectura en funcionamiento</span><h2>Servicios disponibles</h2><p>React consulta al Gateway y el Gateway dirige cada solicitud al microservicio correspondiente.</p></div><div className="service-list">{[1, 2, 3, 4, 5].map(number => <span className={serviceHealth[number] ? 'service-pill online' : 'service-pill'} key={number}>MS {number} {serviceHealth[number] ? 'OK' : '...'}</span>)}</div></section>
         <section className="service-dashboard">{[1, 2, 3, 4, 5].map(number => <article className={`service-tile ${serviceHealth[number] ? 'ready' : ''}`} key={number}><div className="service-tile-top"><span className="service-number">0{number}</span><span className={serviceHealth[number] ? 'status-dot online' : 'status-dot'} /></div><h3>Microservicio {number}</h3><p>{serviceNames[number]}</p><button className="button outline" onClick={() => checkService(number)}>Probar servicio</button></article>)}</section>
         {activeTab === 'exercise-0' && <section className="card simulation-card"><div className="simulation-heading"><div><span className="eyebrow">Ejecución en vivo</span><h2>Estacionamiento con semáforo contador</h2><p>Los espacios y los autos cambian cuando interactúas con los botones.</p></div><button className="button primary" onClick={loadParkingSimulation}>Ejecutar simulación de 10 vehículos</button></div>{parkingSimulation ? <div className="simulation-grid"><div><strong>{parkingSimulation.capacidad}</strong><span>Espacios disponibles</span></div><div><strong>{parkingSimulation.totalVehiculos}</strong><span>Vehículos creados</span></div><div><strong>{parkingSimulation.maximoVehiculosDentro}</strong><span>Máximo dentro</span></div><div><strong>{parkingSimulation.vehiculosQueEsperaron}</strong><span>Vehículos que esperaron</span></div><div><strong>{parkingSimulation.permisosDisponiblesAlFinal}</strong><span>Permisos al final</span></div></div> : <p className="simulation-empty">Conecta el Gateway para ejecutar la simulación.</p>}<div className="interactive-parking"><div><strong>Demostración paso a paso</strong><span>{parkingState?.mensaje || 'Conecta el microservicio 1 para comenzar.'}</span></div><div className="parking-board">{[1, 2, 3, 4].map((slot, index) => { const vehicle = parkingState?.vehiculosDentro?.[index]; return <div className={`parking-slot ${vehicle ? 'occupied' : ''}`} key={slot}><span>Espacio {slot}</span>{vehicle ? <strong className="car">Auto {vehicle}</strong> : <small>Libre</small>}</div> })}</div><div className="waiting-lane"><strong>Cola de espera</strong>{parkingState?.vehiculosEsperando?.length ? parkingState.vehiculosEsperando.map(vehicle => <span className="waiting-car" key={vehicle}>Auto {vehicle}</span>) : <span className="empty-lane">No hay autos esperando</span>}</div><div className="parking-actions"><button className="button primary" onClick={arriveVehicle}>Llega vehículo {nextVehicle}</button><button className="button outline" onClick={leaveVehicle} disabled={!parkingState?.vehiculosDentro?.length}>Liberar un espacio</button><button className="button outline" onClick={resetParking}>Reiniciar</button></div>{parkingState && <div className="parking-status"><span><strong>{parkingState.espaciosOcupados ?? 0}/{parkingState.capacidad ?? 4}</strong> ocupados</span><span><strong>{parkingState.espaciosDisponibles ?? 0}</strong> permisos libres</span><span><strong>{parkingState.vehiculosEsperando?.length ?? 0}</strong> esperando</span><span>Dentro: {parkingState.vehiculosDentro?.join(', ') || 'ninguno'}</span></div>}</div></section>}
         {activeTab.startsWith('exercise-') && activeTab !== 'exercise-0' && <Exercise index={Number(activeTab.split('-')[1])} response={data.exercises[Number(activeTab.split('-')[1])]} updateExercise={updateExercise} updateAnswer={updateAnswer} />}
        {activeTab === 'control' && <Control answers={data.control} updateControl={updateControl} />}
        {activeTab === 'evaluation' && <Evaluation data={data} updateField={updateField} />}
      </main></div><footer>Plantilla React para Sistemas Distribuidos · Los datos se guardan en este navegador.</footer>
  </>
}
