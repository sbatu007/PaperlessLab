import { useEffect, useRef, useState } from "react";
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

    const didInitDesc = useRef(false);

    const loadDocument = async () => {
        if (!id) return;
        try {
            const data = await getDocument(Number(id));
            setDoc(data);

            // only initialize description once
            if (!didInitDesc.current) {
                setDesc(data.description ?? "");
                didInitDesc.current = true;
            }
        } catch (e) {
            setError((e as Error).message);
        }
    };

    useEffect(() => {
        if (!id) return;

        setError(null);
        setSuccess(null);
        didInitDesc.current = false;

        let cancelled = false;
        let pollInterval: number | null = null;

        const start = Date.now();
        const timeoutMs = 60_000; // stop polling after 60s

        const tick = async () => {
            try {
                const data = await getDocument(Number(id));
                if (cancelled) return;

                // ALWAYS update doc so UI reflects the newest state
                setDoc(data);

                // Stop polling once result exists and is not empty
                if (data.result && data.result.trim().length > 0) {
                    if (pollInterval) window.clearInterval(pollInterval);
                    pollInterval = null;
                    return;
                }

                // Stop polling after timeout
                if (Date.now() - start > timeoutMs) {
                    if (pollInterval) window.clearInterval(pollInterval);
                    pollInterval = null;
                }
            } catch (e) {
                // polling errors shouldn't break the UI
                console.error("Polling error:", e);
            }
        };

        // initial load
        void (async () => {
            await loadDocument();
            if (!cancelled) {
                // start polling (every 5s)
                pollInterval = window.setInterval(tick, 5000);
            }
        })();

        return () => {
            cancelled = true;
            if (pollInterval) window.clearInterval(pollInterval);
        };
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
                    <label className="label">Zusammenfassung (GenAI):</label>

                    <textarea
                        className="input"
                        readOnly
                        value={doc?.result && doc.result.trim().length > 0
                            ? doc.result
                            : "(noch keine Zusammenfassung vorhanden – OCR/GenAI läuft …)"}
                        rows={10}
                        style={{resize: "vertical", whiteSpace: "pre-wrap"}}
                    />
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
