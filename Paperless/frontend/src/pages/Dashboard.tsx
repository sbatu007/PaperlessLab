import { useEffect, useState } from "react";
import type { DocumentDto } from "../api";
import { listDocuments, deleteDocument } from "../api";
import { Link } from "react-router-dom";

export default function Dashboard() {
    const [docs, setDocs] = useState<DocumentDto[] | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [refresh, setRefresh] = useState(0);

    useEffect(() => {
        (async () => {
            try {
                const result = await listDocuments();
                setDocs(result);
            } catch (e) {
                setError((e as Error).message);
            }
        })();
    }, [refresh]);

    async function onDelete(id: number) {
        if (!confirm("Delete this document?")) return;
        try {
            await deleteDocument(id);
            setRefresh(r => r + 1);
        } catch (e) {
            alert((e as Error).message);
        }
    }

    if (error) return <div role="alert">Error: {error}</div>;
    if (docs === null) return <div>Loading…</div>;
    if (docs.length === 0) return <div>No documents yet.</div>;

    return (
        <section className="panel">
            <div className="panel-header">
                <h2>Documents</h2>
                <Link to="/upload" className="btn small">+ Upload new Document</Link>
            </div>
            <div className="panel-body">
                <div className="doc-head">
                    <div>Date</div>
                    <div>Name</div>
                    <div>Tags</div>
                    <div>Actions</div>
                </div>

                <ul className="doc-list">
                    {docs.map(d => (
                        <li key={d.id} className="doc-item">
                            <div className="col-date">—</div> {/* falls du ein Datum hast, hier einsetzen */}
                            <div className="col-name">
                                <Link to={`/detail/${d.id}`}>{d.filename}</Link>
                                {d.description ? <span className="muted"> — {d.description}</span> : null}
                            </div>
                            <div className="col-tags">
                                {/* Beispiel-Tags; falls keine Tags, Abschnitt leer lassen */}
                                {/* <span className="badge">Tech</span><span className="badge blue">Inbox</span> */}
                            </div>
                            <div className="col-actions">
                                <Link className="btn small" to={`/detail/${d.id}`}>Open</Link>
                                <button className="btn small danger" onClick={() => onDelete(d.id)}>Delete</button>
                            </div>
                        </li>
                    ))}
                </ul>
            </div>
        </section>
    );

}
