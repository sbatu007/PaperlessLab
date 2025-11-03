import { useEffect, useState } from 'react';
import type { DocumentDto } from '../api';
import { listDocuments } from '../api';
import { Link } from 'react-router-dom';

export default function Dashboard() {
    const [docs, setDocs] = useState<DocumentDto[] | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let active = true;
        (async () => {
            try {
                const result = await listDocuments();
                if (active) setDocs(result);
            } catch (e) {
                if (active) setError((e as Error).message);
            }
        })();
        return () => { active = false; };
    }, []);

    if (error) return <div role="alert">Error: {error}</div>;
    if (docs === null) return <div>Loading…</div>;
    if (docs.length === 0) return <div>No documents yet.</div>;

    return (
        <main>
            <h1>Documents</h1>
            <ul>
                {docs.map(d => (
                    <li key={d.id}>
                        <Link to={`/detail/${d.id}`}>{d.filename}</Link>
                        {d.description && ` — ${d.description}`}
                    </li>
                ))}
            </ul>
            <p><Link to="/upload">Upload new</Link></p>
        </main>
    );
}
