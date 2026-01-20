import './App.css'
import Dashboard from './pages/Dashboard';
import Detail from './pages/Detail';
import Upload from './pages/Upload';
import Labels from "./pages/Labels";

import {BrowserRouter, Routes, Route, Link } from "react-router-dom";

function Home() {
    return (
        <section className="panel">
            <div className="panel-header">
                <h2>Home</h2>
            </div>

            <div className="panel-body">
                <h1>Willkommen bei Paperless</h1>
                <p className="muted">
                    Lade Dokumente hoch, verwalte Metadaten & Labels und finde Inhalte per Volltextsuche.
                </p>

                <div className="actions" style={{ borderTop: "none", paddingTop: 0 }}>
                    <div className="actions-hint">
            <span className="hint-pill">
              Tipp: Starte mit &nbsp;<b>Upload</b>&nbsp; oder öffne das &nbsp;<b>Dashboard</b>.
            </span>
                    </div>
                </div>
            </div>
        </section>
    );
}



export default function App() {
  // const [count, setCount] = useState(0)
  // return (
  //   <>
  //     <div>
  //       <a href="https://vite.dev" target="_blank">
  //         <img src={viteLogo} className="logo" alt="Vite logo" />
  //       </a>
  //       <a href="https://react.dev" target="_blank">
  //         <img src={reactLogo} className="logo react" alt="React logo" />
  //       </a>
  //     </div>
  //     <h1>Vite + React</h1>
  //     <div className="card">
  //       <button onClick={() => setCount((count) => count + 1)}>
  //         count is {count}
  //       </button>
  //       <p>
  //         Edit <code>src/App.tsx</code> and save to test HMR
  //       </p>
  //     </div>
  //     <p className="read-the-docs">
  //       Click on the Vite and React logos to learn more
  //     </p>
  //   </>
  // )
    return(
        <BrowserRouter>
            {/* ======= BLAUER HEADER ======= */}
            <header className="app-header bar">
                <div className="header-inner">
                    <h1>Paperless</h1>
                    <nav className="nav-links">
                        <Link to="/welcome">Home</Link>
                        <Link to="/">Dashboard</Link>
                        <Link to="/upload">Upload</Link>
                        <Link to="/labels">Labels</Link>
                    </nav>
                </div>
            </header>

            {/* ======= SEITENINHALT ======= */}
            <main>
                <Routes>
                    <Route path="/welcome" element={<Home />} />
                    <Route path="/" element={<Dashboard />} />
                    <Route path="/upload" element={<Upload />} />
                    <Route path="/detail/:id" element={<Detail />} />
                    <Route path="/labels" element={<Labels />} />
                </Routes>
            </main>
        </BrowserRouter>
    );
}

