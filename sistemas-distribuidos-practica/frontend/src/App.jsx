import { useEffect, useMemo, useState } from 'react'
import { controlQuestions, emptyState, evaluationRows, exercises } from './data'

const storageKey = 'sistemas-distribuidos-practica'

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
    {index === 1 && <CajeroAutomaticoSimulacion />}
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
  const [serviceStatus, setServiceStatus] = useState(null)

  useEffect(() => {
    localStorage.setItem(storageKey, JSON.stringify(data))
    setSaved(true)
    const timer = setTimeout(() => setSaved(false), 1600)
    return () => clearTimeout(timer)
  }, [data])

  useEffect(() => {
    fetch('/api/services').then(response => response.json()).then(setServiceStatus).catch(() => setServiceStatus(null))
  }, [])

  const updateField = (field, value) => setData(current => ({ ...current, [field]: value }))
  const updateExercise = (index, field, value) => setData(current => ({ ...current, exercises: current.exercises.map((item, i) => i === index ? { ...item, [field]: value } : item) }))
  const updateAnswer = (exerciseIndex, answerIndex, value) => setData(current => ({ ...current, exercises: current.exercises.map((item, i) => i === exerciseIndex ? { ...item, answers: item.answers.map((answer, j) => j === answerIndex ? value : answer) } : item) }))
  const updateControl = (index, value) => setData(current => ({ ...current, control: current.control.map((answer, i) => i === index ? value : answer) }))

  const progress = useMemo(() => {
    const values = [data.title, data.subject, data.cycle, data.unit, data.members, data.date, ...data.exercises.flatMap(e => [e.code, ...e.answers, e.evidence]), ...data.control, data.bibliography, data.annexes]
    return Math.round(values.filter(value => value.trim()).length / values.length * 100)
  }, [data])

  return <>
    <header className="hero"><div className="hero-content"><div><span className="eyebrow">Plantilla académica editable</span><h1>{data.title || 'Trabajo práctico'}</h1><p>Documenta ejercicios, análisis, evidencias y evaluación en un solo lugar.</p></div><div className="actions"><button className="button light" onClick={() => window.print()}>Imprimir / PDF</button><button className="button subtle" onClick={() => { if (window.confirm('¿Limpiar todas las respuestas?')) setData(emptyState) }}>Limpiar</button></div></div></header>
    <div className="container layout"><nav className="sidebar">{exercises.map((exercise, index) => <button key={exercise.title} className={`tab ${activeTab === `exercise-${index}` ? 'active' : ''}`} onClick={() => setActiveTab(`exercise-${index}`)}>Ejercicio {index + 1}</button>)}<button className={`tab ${activeTab === 'control' ? 'active' : ''}`} onClick={() => setActiveTab('control')}>Preguntas de control</button><button className={`tab ${activeTab === 'evaluation' ? 'active' : ''}`} onClick={() => setActiveTab('evaluation')}>Evaluación</button></nav>
      <main><div className="progress-card"><div><strong>Progreso de la práctica</strong><small className="save-status">{saved ? 'Guardado automáticamente' : 'Los cambios se guardan solos'}</small></div><div className="progress-bar"><div style={{ width: `${progress}%` }} /></div><strong>{progress}%</strong></div>
        <section className="card"><h2>Datos de la práctica</h2><div className="meta-grid"><Field label="Título" value={data.title} onChange={v => updateField('title', v)} /><Field label="Asignatura" value={data.subject} onChange={v => updateField('subject', v)} /><Field label="Ciclo" value={data.cycle} onChange={v => updateField('cycle', v)} /><Field label="Unidad" value={data.unit} onChange={v => updateField('unit', v)} /><Field label="Integrantes" value={data.members} onChange={v => updateField('members', v)} /><Field label="Fecha" type="date" value={data.date} onChange={v => updateField('date', v)} /></div></section>
        <section className="card service-card"><div><span className="eyebrow">Integración</span><h2>Conexión con microservicios</h2><p>El frontend se comunica con el Gateway mediante rutas como <code>/api/microservicio-1</code>.</p></div><div className="service-list">{[1, 2, 3, 4, 5].map(number => <span className={serviceStatus?.services?.[number] ? 'service-pill online' : 'service-pill'} key={number}>MS {number}</span>)}</div></section>
        {activeTab.startsWith('exercise-') && <Exercise index={Number(activeTab.split('-')[1])} response={data.exercises[Number(activeTab.split('-')[1])]} updateExercise={updateExercise} updateAnswer={updateAnswer} />}
        {activeTab === 'control' && <Control answers={data.control} updateControl={updateControl} />}
        {activeTab === 'evaluation' && <Evaluation data={data} updateField={updateField} />}
      </main></div><footer>Plantilla React para Sistemas Distribuidos · Los datos se guardan en este navegador.</footer>
  </>
}

// Subcomponente para la lógica del Ejercicio 2 (Cajero y Mutex)
function CajeroAutomaticoSimulacion() {
    const [resultado, setResultado] = useState('');
    const [cargando, setCargando] = useState(false);

    const ejecutarSimulacion = async () => {
        setCargando(true);
        setResultado('');
        try {
            // Petición a tu microservicio en Spring Boot (Puerto 3002)
            const response = await fetch('http://localhost:3002/api/simulacion/ejecutar');
            if (!response.ok) throw new Error('Error en el servidor');
            
            const data = await response.text();
            setResultado(data);
        } catch (error) {
            setResultado('Error de conexión. ¿Está encendido el backend en el puerto 3002?');
            console.error(error);
        } finally {
            setCargando(false);
        }
    };

    return (
        <div style={{ marginTop: '20px', padding: '15px', border: '1px solid #e2e8f0', borderRadius: '8px' }}>
            <h4 style={{ margin: '0 0 10px 0', color: '#1e293b' }}>Demostración Práctica</h4>
            <button 
                onClick={ejecutarSimulacion} 
                disabled={cargando}
                style={{
                    padding: '8px 16px',
                    backgroundColor: '#4C3CE6',
                    color: 'white',
                    border: 'none',
                    borderRadius: '6px',
                    cursor: cargando ? 'not-allowed' : 'pointer',
                    fontWeight: 'bold'
                }}
            >
                {cargando ? 'Ejecutando procesos...' : 'Iniciar Simulación de Retiros y Depósitos'}
            </button>

            {resultado && (
                <div style={{ marginTop: '15px', padding: '10px', backgroundColor: '#f1f5f9', borderRadius: '4px', fontFamily: 'monospace' }}>
                    <p style={{ margin: 0, whiteSpace: 'pre-wrap' }}>{resultado}</p>
                </div>
            )}
        </div>
    );
}
