import { useEffect, useMemo, useState } from 'react'
import { emptyState, exercises } from './data'
import Control from './components/Control'
import EstacionamientoPanel from './microservicios/microservicio-1/EstacionamientoPanel'
import Evaluation from './components/Evaluation'
import Exercise from './components/Exercise'
import MicroservicesPanel from './components/MicroservicesPanel'
import { microserviciosApi } from './services/microserviciosApi'

const storageKey = 'sistemas-distribuidos-practica'

export default function App() {
  const [activeTab, setActiveTab] = useState('exercise-0')
  const [data, setData] = useState(() => { try { return JSON.parse(localStorage.getItem(storageKey)) || emptyState } catch { return emptyState } })
  const [saved, setSaved] = useState(false)
  const [serviceHealth, setServiceHealth] = useState({})
  const [parkingSimulation, setParkingSimulation] = useState(null)
  const [parkingState, setParkingState] = useState(null)
  const [nextVehicle, setNextVehicle] = useState(1)

  useEffect(() => { localStorage.setItem(storageKey, JSON.stringify(data)); setSaved(true); const timer = setTimeout(() => setSaved(false), 1600); return () => clearTimeout(timer) }, [data])
  const checkService = number => microserviciosApi.health(number).then(result => setServiceHealth(current => ({ ...current, [number]: result }))).catch(() => setServiceHealth(current => ({ ...current, [number]: null })))
  useEffect(() => { [1, 2, 3, 4, 5].forEach(checkService) }, [])
  const loadParkingState = () => microserviciosApi.estado().then(result => setParkingState(result)).catch(() => setParkingState(null))
  const loadParkingSimulation = () => microserviciosApi.simular().then(result => { setParkingSimulation(result); return loadParkingState() }).catch(() => setParkingSimulation(null))
  const arriveVehicle = () => microserviciosApi.llegar(nextVehicle).then(result => { setNextVehicle(current => current + 1); setParkingState(result) }).catch(loadParkingState)
  const leaveVehicle = () => { const id = parkingState?.vehiculosDentro?.[0]; if (id) microserviciosApi.salir(id).then(setParkingState).catch(loadParkingState) }
  const resetParking = () => microserviciosApi.reiniciar().then(result => { setParkingState(result); setNextVehicle(1) }).catch(loadParkingState)
  useEffect(() => { loadParkingSimulation(); loadParkingState() }, [])
  const updateField = (field, value) => setData(current => ({ ...current, [field]: value }))
  const updateExercise = (index, field, value) => setData(current => ({ ...current, exercises: current.exercises.map((item, i) => i === index ? { ...item, [field]: value } : item) }))
  const updateAnswer = (exerciseIndex, answerIndex, value) => setData(current => ({ ...current, exercises: current.exercises.map((item, i) => i === exerciseIndex ? { ...item, answers: item.answers.map((answer, j) => j === answerIndex ? value : answer) } : item) }))
  const updateControl = (index, value) => setData(current => ({ ...current, control: current.control.map((answer, i) => i === index ? value : answer) }))
  const progress = useMemo(() => { const values = [...data.exercises.flatMap(e => [e.code, ...e.answers, e.evidence]), ...data.control, data.bibliography, data.annexes]; return Math.round(values.filter(value => value.trim()).length / values.length * 100) }, [data])

  return <><header className="hero"><div className="hero-content"><div><span className="eyebrow">Panel de demostracion</span><h1>Sistema distribuido</h1><p>Frontend React conectado a un API Gateway y cinco microservicios Spring Boot.</p></div></div></header><div className="container layout"><nav className="sidebar">{exercises.map((exercise, index) => <button key={exercise.title} className={`tab ${activeTab === `exercise-${index}` ? 'active' : ''}`} onClick={() => setActiveTab(`exercise-${index}`)}>Ejercicio {index + 1}</button>)}<button className={`tab ${activeTab === 'control' ? 'active' : ''}`} onClick={() => setActiveTab('control')}>Preguntas de control</button><button className={`tab ${activeTab === 'evaluation' ? 'active' : ''}`} onClick={() => setActiveTab('evaluation')}>Evaluación</button></nav><main className="main-column"><div className="progress-card"><div><strong>Progreso de la práctica</strong><small className="save-status">{saved ? 'Guardado automáticamente' : 'Los cambios se guardan solos'}</small></div><div className="progress-bar"><div style={{ width: `${progress}%` }} /></div><strong>{progress}%</strong></div>{activeTab === 'exercise-0' && <EstacionamientoPanel simulation={parkingSimulation} state={parkingState} nextVehicle={nextVehicle} onSimulate={loadParkingSimulation} onArrive={arriveVehicle} onLeave={leaveVehicle} onReset={resetParking} />}{activeTab.startsWith('exercise-') && activeTab !== 'exercise-0' && <Exercise index={Number(activeTab.split('-')[1])} response={data.exercises[Number(activeTab.split('-')[1])]} updateExercise={updateExercise} updateAnswer={updateAnswer} />}{activeTab === 'control' && <Control answers={data.control} updateControl={updateControl} />}{activeTab === 'evaluation' && <Evaluation data={data} updateField={updateField} />}</main><MicroservicesPanel serviceHealth={serviceHealth} checkService={checkService} /></div><footer>Panel de Sistemas Distribuidos · React + Spring Boot</footer></>
}
