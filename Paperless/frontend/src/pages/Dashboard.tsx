import { useEffect, useState } from 'react';
import { listDocuments, type DocumentDto } from '../api';
import { Link } from 'react-router-dom';

export default function Dashboard() {
    const [items, setItems] = useState<DocumentDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState<string>();

    useEffect(() => {
        listDocuments()
            .then(setItems)
            .catch(e => setErr(String(e)))
            .finally(() => setLoading(false));
    }, []);

    if (loading) return <p>Lade…</p>;
    if (err) return <p style={{ color: 'crimson' }}>Fehler: {err}</p>;

    return (
        <div>
            <h1>Dokumentenliste</h1>
            <Link to="/upload">+ Upload</Link>
            {items.length === 0 ? (
                <p>Keine Dokumente vorhanden</p>
            ) : (
                <ul>
                    {items.map(d => (
                        <li key={d.id}>
                            <Link to={`/detail/${d.id}`}>{d.filename}</Link>
                            {d.description ? ` – ${d.description}` : null}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}
