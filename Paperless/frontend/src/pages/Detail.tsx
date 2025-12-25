import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import type { DocumentDto } from "../api";
import { getDocument, updateDocument } from "../api";

export default function Detail() {
    const { id } = useParams<{ id: string }>();
    const [doc, setDoc] = useState<DocumentDto | null>(null);
    const [desc, setDesc] = useState("");
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    const loadDocument = async () => {
        try {
            const data = await getDocument(Number(id));
            setDoc(data);
            setDesc(data.description ?? "");
        } catch (e) {
            setError((e as Error).message);
        }
    };

    useEffect(() => {
        if (!id) return;

        loadDocument();

        const interval = setInterval(async () => {
            try {
                const data = await getDocument(Number(id));
                if (data.result) {
                    setDoc(data);
                    setDesc(data.description ?? "");
                    clearInterval(interval);
                }
            } catch (e) {
                console.error("Polling error:", e);
            }
        }, 5000);

        return () => clearInterval(interval);
    }, [id]);

    async function onSave() {
        if (!id) return;
        setBusy(true);
        setError(null);
        setSuccess(null);

        try {
            const updated = await updateDocument(Number(id), desc);
            setDoc(updated);

            setSuccess("Änderungen erfolgreich gespeichert!");
            setTimeout(() => setSuccess(null), 3000);

        } catch (e) {
            setError((e as Error).message);
        } finally {
            setBusy(false);
        }
    }

    if (error) return <div role="alert" className="alert error">Error: {error}</div>;
    if (!doc) return <section className="panel"><div className="panel-body">Loading…</div></section>;

    return (
        <section className="panel">
            <div className="panel-header"><h2>Document</h2></div>

            <div className="panel-body">

                {success && <div className="alert success">{success}</div>}
                {error && <div className="alert error">{error}</div>}

                <div className="field">
                    <label className="label">Titel:</label>
                    <div className="filename-box">{doc.filename}</div>
                </div>

                <div className="field">
                    <label className="label">Beschreibung:</label>
                    <input
                        value={desc}
                        onChange={e => setDesc(e.target.value)}
                        placeholder="Beschreibung eingeben"
                        className="input"
                    />
                </div>

                <div className="field">
                    <label className="label">Summary:</label>
                    <div className="filename-box" style={{whiteSpace: "pre-wrap"}}>
                        {doc.result ? doc.result : "(noch keine Zusammenfassung – OCR/GenAI läuft …)"}
                    </div>
                </div>

                <div className="actions">
                    <button className="btn primary" onClick={onSave} disabled={busy}>
                        {busy ? "Saving…" : "Save"}
                    </button>
                    <Link className="btn" to="/">Back</Link>
                </div>
            </div>
        </section>
    );
}
