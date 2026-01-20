import { useState, type FormEvent } from "react";
import { useNavigate, Link } from "react-router-dom";
import { uploadDocument } from "../api";

export default function Upload() {
    const [file, setFile] = useState<File | null>(null);
    const [description, setDescription] = useState("");
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const nav = useNavigate();

    async function onSubmit(e: FormEvent) {
        e.preventDefault();
        if (!file) return setError("Please choose a file.");
        setBusy(true);
        setError(null);

        try {
            await uploadDocument(file, description.trim() || undefined);
            nav("/");
        } catch (e) {
            setError((e as Error).message);
        } finally {
            setBusy(false);
        }
    }

    return (
        <form onSubmit={onSubmit} className="panel">
            <div className="panel-header">
                <h2>Upload</h2>
            </div>

            <div className="panel-body">
                {error && (
                    <div className="alert error" role="alert">
                        Error: {error}
                    </div>
                )}

                <div className="upload-grid">
                    <div className="field">
                        <label className="label">Datei</label>
                        <input
                            className="input"
                            type="file"
                            onChange={(e) => setFile(e.currentTarget.files?.[0] ?? null)}
                        />
                        {file && (
                            <div className="upload-file-meta muted">
                                <b>{file.name}</b> · {Math.round(file.size / 1024)} KB
                            </div>
                        )}
                    </div>

                    <div className="field">
                        <label className="label">Beschreibung</label>
                        <input
                            className="input"
                            type="text"
                            placeholder="Description (optional)"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                        />
                    </div>
                </div>

                <div className="actions">
                    <button type="submit" className="btn primary" disabled={busy}>
                        {busy ? "Uploading…" : "Upload"}
                    </button>
                    <Link to="/" className="btn">
                        Cancel
                    </Link>
                </div>
            </div>
        </form>
    );
}
