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
        <main>
            <h1>Documents</h1>
            <ul>
                {docs.map(d => (
                    <li key={d.id}>
                        <Link to={`/detail/${d.id}`}>{d.filename}</Link>
                        {d.description && ` — ${d.description}`}{" "}
                        <button onClick={() => onDelete(d.id)}>🗑</button>
                    </li>
                ))}
            </ul>
            <p><Link to="/upload">Upload new</Link></p>
        </main>
    );
}
