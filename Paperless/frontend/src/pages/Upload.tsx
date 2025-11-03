import { useState, type FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { uploadDocument } from '../api';

export default function Upload() {
    const [file, setFile] = useState<File | null>(null);
    const [description, setDescription] = useState('');
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const nav = useNavigate();

    async function onSubmit(e: FormEvent) {
        e.preventDefault();
        if (!file) return setError('Please choose a file.');
        setBusy(true);
        setError(null);
        try {
            await uploadDocument(file, description.trim() || undefined);
            nav('/');
        } catch (e) {
            setError((e as Error).message);
        } finally {
            setBusy(false);
        }
    }

    return (
        <form onSubmit={onSubmit} className="panel">
            <div className="panel-header"><h2>Upload</h2></div>
            <div className="panel-body">
                {error && <div role="alert">Error: {error}</div>}
                <input type="file" onChange={e => setFile(e.currentTarget.files?.[0] ?? null)} />
                <input type="text" placeholder="Description (optional)" value={description} onChange={e => setDescription(e.target.value)} />
                <div className="actions">
                    <button type="submit" className="btn primary" disabled={busy}>{busy ? "Uploading…" : "Upload"}</button>
                    <Link to="/" className="btn">Cancel</Link>
                </div>
            </div>
        </form>
    );
}
