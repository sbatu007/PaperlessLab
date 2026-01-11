import { useEffect, useState } from "react";
import type { DocumentDto, SearchHitDto } from "../api";
import { listDocuments, deleteDocument, searchDocuments } from "../api";
import { Link } from "react-router-dom";

export default function Dashboard() {
    const [docs, setDocs] = useState<DocumentDto[] | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [refresh, setRefresh] = useState(0);

    // Search state
    const [query, setQuery] = useState("");
    const [hits, setHits] = useState<SearchHitDto[] | null>(null);
    const [searchError, setSearchError] = useState<string | null>(null);
    const [searching, setSearching] = useState(false);

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

    async function onSearch() {
        const q = query.trim();
        setSearchError(null);

        if (!q) {
            setHits(null); // search cleared -> show normal list only
            return;
        }

        setSearching(true);
        try {
            const res = await searchDocuments(q);
            setHits(res);
        } catch (e) {
            setSearchError((e as Error).message);
        } finally {
            setSearching(false);
        }
    }

    function onClearSearch() {
        setQuery("");
        setHits(null);
        setSearchError(null);
    }

    if (error) return <div role="alert">Error: {error}</div>;
    if (docs === null) return <div>Loading…</div>;

    return (
        <section className="panel">
            <div className="panel-header">
                <h2>Documents</h2>
                <Link to="/upload" className="btn small">+ Upload new Document</Link>
            </div>

            <div className="panel-body">
                <div style={{ display: "flex", gap: 8, alignItems: "center", marginBottom: 12 }}>
                    <input
                        value={query}
                        onChange={e => setQuery(e.target.value)}
                        placeholder='Search (e.g. "Hello")'
                        className="input"
                        style={{ flex: 1 }}
                        onKeyDown={(e) => {
                            if (e.key === "Enter") void onSearch();
                        }}
                    />
                    <button className="btn small" onClick={() => void onSearch()} disabled={searching}>
                        {searching ? "Searching…" : "Search"}
                    </button>
                    <button className="btn small" onClick={onClearSearch} disabled={searching && !query.trim()}>
                        Clear
                    </button>
                </div>

                {searchError && <div role="alert" className="alert error">Search error: {searchError}</div>}

                {hits !== null && (
                    <div style={{ marginBottom: 16 }}>
                        <h3 style={{ marginTop: 0 }}>Search results</h3>
                        {hits.length === 0 ? (
                            <div>No results.</div>
                        ) : (
                            <ul className="doc-list">
                                {hits.map(h => (
                                    <li key={h.documentId} className="doc-item">
                                        <div className="col-date">—</div>
                                        <div className="col-name">
                                            <Link to={`/detail/${h.documentId}`}>{h.filename}</Link>
                                            <span className="muted"> — score: {h.score.toFixed(2)}</span>
                                        </div>
                                        <div className="col-tags"></div>
                                        <div className="col-actions">
                                            <Link className="btn small" to={`/detail/${h.documentId}`}>Open</Link>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>
                )}

                {docs.length === 0 ? (
                    <div>No documents yet.</div>
                ) : (
                    <>
                        <div className="doc-head">
                            <div>Date</div>
                            <div>Name</div>
                            <div>Tags</div>
                            <div>Actions</div>
                        </div>

                        <ul className="doc-list">
                            {docs.map(d => (
                                <li key={d.id} className="doc-item">
                                    <div className="col-date">—</div>
                                    <div className="col-name">
                                        <Link to={`/detail/${d.id}`}>{d.filename}</Link>
                                        {d.description ? <span className="muted"> — {d.description}</span> : null}
                                    </div>
                                    <div className="col-tags">
                                        {(d.labels ?? []).map(l => (
                                            <span key={l.id} className="tag">{l.name}</span>
                                        ))}
                                    </div>                                    <div className="col-actions">
                                        <Link className="btn small" to={`/detail/${d.id}`}>Open</Link>
                                        <button className="btn small danger" onClick={() => void onDelete(d.id)}>Delete</button>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    </>
                )}
            </div>
        </section>
    );
}
