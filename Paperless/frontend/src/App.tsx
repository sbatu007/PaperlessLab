import './App.css'
import Dashboard from './pages/Dashboard';
import Detail from './pages/Detail';
import Upload from './pages/Upload';

import {BrowserRouter, Routes, Route, Link } from "react-router-dom";

function Home(){
    return <p>Paperless App</p>;
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
                        <Link to="/">Dashboard</Link>
                        <Link to="/upload">Upload</Link>
                        <Link to="/welcome">Home</Link>
                    </nav>
                </div>
            </header>

            {/* ======= SEITENINHALT ======= */}
            <main>
                <Routes>
                    <Route path="/" element={<Dashboard />} />
                    <Route path="/upload" element={<Upload />} />
                    <Route path="/detail/:id" element={<Detail />} />
                    <Route path="/welcome" element={<Home />} />
                </Routes>
            </main>
        </BrowserRouter>
    );
}

