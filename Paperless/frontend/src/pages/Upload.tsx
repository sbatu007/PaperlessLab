import { useState } from 'react';
import { uploadDocument } from '../api';
import { useNavigate, Link } from 'react-router-dom';

export default function Upload() {
    const [file, setFile] = useState<File>();
    const [desc, setDesc] = useState('');
    const [err, setErr] = useState<string>();
    const [busy, setBusy] = useState(false);
    const nav = useNavigate();

    async function onSubmit(e: React.FormEvent) {
        e.preventDefault();
        if (!file) return setErr('Bitte Datei auswählen');
        setBusy(true); setErr(undefined);
        try {
            await uploadDocument(file, desc);
            nav('/');
        } catch (e:any) {
            setErr(String(e?.message ?? e));
        } finally { setBusy(false); }
    }

    return (
        <div>
            <Link to="/">← Zurück</Link>
            <h1>Upload</h1>
            {err && <p style={{ color: 'crimson' }}>{err}</p>}
            <form onSubmit={onSubmit}>
                <div>
                    <input type="file" accept="application/pdf"
                           onChange={e => setFile(e.target.files?.[0] ?? undefined)} />
                </div>
                <div>
                    <input placeholder="Beschreibung (optional)"
                           value={desc} onChange={e => setDesc(e.target.value)} />
                </div>
                <button disabled={busy} type="submit">
                    {busy ? 'Lade…' : 'Hochladen'}
                </button>
            </form>
        </div>
    );
}
