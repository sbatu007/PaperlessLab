import { useEffect, useRef, useState } from "react";
import { useParams, Link } from "react-router-dom";
import type { DocumentDto, LabelDto } from "../api";
import {
    getDocument,
    updateDocument,
    listLabels,
    createLabel,
    setDocumentLabels,
} from "../api";

export default function Detail() {
    const { id } = useParams<{ id: string }>();

    const [doc, setDoc] = useState<DocumentDto | null>(null);
    const [desc, setDesc] = useState("");
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    // Labels
    const [allLabels, setAllLabels] = useState<LabelDto[]>([]);
    const [selectedLabelIds, setSelectedLabelIds] = useState<number[]>([]);
    const [newLabelName, setNewLabelName] = useState("");

    // prevent overwriting user changes while polling
    const didInitDesc = useRef(false);
    const didInitLabels = useRef(false);
    const userTouchedLabels = useRef(false);

    const loadLabels = async () => {
        try {
            const labels = await listLabels();
            setAllLabels(labels);
        } catch (e) {
            console.error("Failed to load labels:", e);
        }
    };

    const loadDocument = async () => {
        if (!id) return;
        try {
            const data = await getDocument(Number(id));
            setDoc(data);

            if (!didInitDesc.current) {
                setDesc(data.description ?? "");
                didInitDesc.current = true;
            }

            if (!didInitLabels.current) {
                setSelectedLabelIds((data.labels ?? []).map((l) => l.id));
                didInitLabels.current = true;
            }
        } catch (e) {
            setError((e as Error).message);
        }
    };

    useEffect(() => {
        void loadLabels();
    }, []);

    useEffect(() => {
        if (!id) return;

        setError(null);
        setSuccess(null);

        didInitDesc.current = false;
        didInitLabels.current = false;
        userTouchedLabels.current = false;

        let cancelled = false;
        let pollInterval: number | null = null;

        const start = Date.now();
        const timeoutMs = 60_000;

        const tick = async () => {
            try {
                const data = await getDocument(Number(id));
                if (cancelled) return;

                setDoc(data);

                if (!userTouchedLabels.current) {
                    setSelectedLabelIds((data.labels ?? []).map((l) => l.id));
                }

                if (data.result && data.result.trim().length > 0) {
                    if (pollInterval) window.clearInterval(pollInterval);
                    pollInterval = null;
                    return;
                }

                if (Date.now() - start > timeoutMs) {
                    if (pollInterval) window.clearInterval(pollInterval);
                    pollInterval = null;
                }
            } catch (e) {
                console.error("Polling error:", e);
            }
        };

        void (async () => {
            await loadDocument();
            if (!cancelled) pollInterval = window.setInterval(tick, 5000);
        })();

        return () => {
            cancelled = true;
            if (pollInterval) window.clearInterval(pollInterval);
        };
    }, [id]);

    const selectedLabels: LabelDto[] = selectedLabelIds
        .map((lid) => allLabels.find((l) => l.id === lid))
        .filter(Boolean) as LabelDto[];

    async function onSave() {
        if (!id) return;
        setBusy(true);
        setError(null);
        setSuccess(null);

        try {
            await updateDocument(Number(id), desc);

            const updated = await setDocumentLabels(Number(id), selectedLabelIds);
            setDoc(updated);

            userTouchedLabels.current = false;

            setSuccess("Änderungen erfolgreich gespeichert!");
            setTimeout(() => setSuccess(null), 3000);
        } catch (e) {
            setError((e as Error).message);
        } finally {
            setBusy(false);
        }
    }

    if (error) {
        return (
            <div role="alert" className="alert error">
                Error: {error}
            </div>
        );
    }

    if (!doc) {
        return (
            <section className="panel">
                <div className="panel-body">Loading…</div>
            </section>
        );
    }

    return (
        <section className="panel">
            <div className="panel-header">
                <h2>Document-Detail</h2>
            </div>

            <div className="panel-body">
                {success && <div className="alert success">{success}</div>}
                {error && <div className="alert error">{error}</div>}

                <div className="field">
                    <label className="label">Titel:</label>
                    <div className="filename-box">{doc.filename}</div>
                </div>

                <div className="detail-grid">
                    <div className="detail-left">
                        <div className="card">
                            <div className="card-title">Beschreibung</div>
                            <div className="field">
                <textarea
                    value={desc}
                    onChange={(e) => setDesc(e.target.value)}
                    placeholder="Beschreibung eingeben"
                    className="input"
                    rows={2}
                />
                            </div>
                        </div>

                        <div className="card">
                            <div className="card-title">Labels</div>

                            <div className="labels-chips left">
                                {selectedLabels.map((l) => (
                                    <span key={l.id} className="chip">
                    <span className="chip-text">{l.name}</span>
                    <button
                        type="button"
                        className="chip-x"
                        title="Vom Dokument entfernen"
                        onClick={() => {
                            userTouchedLabels.current = true;
                            setSelectedLabelIds((prev) => prev.filter((x) => x !== l.id));
                        }}
                    >
                      ×
                    </button>
                  </span>
                                ))}
                                {selectedLabels.length === 0 && (
                                    <span className="muted">(keine Labels)</span>
                                )}
                            </div>

                            <div className="labels-add-row">
                                <select
                                    className="input"
                                    defaultValue=""
                                    onChange={(e) => {
                                        const v = e.target.value;
                                        if (!v) return;
                                        const labelId = Number(v);
                                        if (Number.isNaN(labelId)) return;

                                        userTouchedLabels.current = true;
                                        setSelectedLabelIds((prev) =>
                                            prev.includes(labelId) ? prev : [...prev, labelId]
                                        );
                                        e.currentTarget.value = "";
                                    }}
                                >
                                    <option value="">Vorhandenes Label hinzufügen…</option>
                                    {allLabels
                                        .slice()
                                        .sort((a, b) => a.name.localeCompare(b.name))
                                        .filter((l) => !selectedLabelIds.includes(l.id))
                                        .map((l) => (
                                            <option key={l.id} value={l.id}>
                                                {l.name}
                                            </option>
                                        ))}
                                </select>
                            </div>

                            <div className="labels-add-row two">
                                <input
                                    value={newLabelName}
                                    onChange={(e) => setNewLabelName(e.target.value)}
                                    placeholder="Neues Label (z.B. Rechnung)"
                                    className="input"
                                />
                                <button
                                    className="btn"
                                    type="button"
                                    disabled={busy}
                                    onClick={async () => {
                                        const n = newLabelName.trim();
                                        if (!n) return;

                                        const created = await createLabel(n);
                                        setAllLabels((prev) => {
                                            const next = [...prev, created];
                                            next.sort((a, b) => a.name.localeCompare(b.name));
                                            return next;
                                        });

                                        userTouchedLabels.current = true;
                                        setSelectedLabelIds((prev) =>
                                            prev.includes(created.id) ? prev : [...prev, created.id]
                                        );
                                        setNewLabelName("");
                                    }}
                                >
                                    + Add
                                </button>
                            </div>
                        </div>
                    </div>

                    <div className="detail-right">
                        <div className="card">
                            <div className="card-title">Zusammenfassung (GenAI)</div>
                            <textarea
                                className="input mono"
                                readOnly
                                value={
                                    doc?.result && doc.result.trim().length > 0
                                        ? doc.result
                                        : "(noch keine Zusammenfassung vorhanden – OCR/GenAI läuft …)"
                                }
                            />
                        </div>
                    </div>
                </div>

                <div className="actions">
                    <div className="actions-hint" aria-live="polite">
                        <span className="hint-pill">
                          Änderungen werden erst mit&nbsp;<b>Save</b>&nbsp;übernommen.
                        </span>


                        {success && <span className="hint-success">Gespeichert</span>}
                    </div>

                    <div className="actions-buttons">
                        <Link className="btn" to="/">
                            Back
                        </Link>
                        <button className="btn primary" onClick={onSave} disabled={busy}>
                            {busy ? "Saving…" : "Save"}
                        </button>
                    </div>
                </div>
            </div>
        </section>
    );
}
