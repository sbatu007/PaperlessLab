import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getDocument, type DocumentDto } from '../api';

export default function Detail() {
    const { id } = useParams();
    const [doc, setDoc] = useState<DocumentDto>();
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState<string>();

    useEffect(() => {
        if (!id) return;
        getDocument(Number(id))
            .then(setDoc)
            .catch(e => setErr(String(e)))
            .finally(() => setLoading(false));
    }, [id]);

    if (loading) return <p>Lade…</p>;
    if (err) return <p style={{ color: 'crimson' }}>Fehler: {err}</p>;
    if (!doc) return <p>Nichts gefunden</p>;

    return (
        <div>
            <Link to="/">← Zurück</Link>
            <h1>{doc.filename}</h1>
            <p>{doc.description ?? '—'}</p>
        </div>
    );
}
