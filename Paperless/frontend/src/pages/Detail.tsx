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

    useEffect(() => {
        (async () => {
            try {
                const data = await getDocument(Number(id));
                setDoc(data);
                setDesc(data.description ?? "");
            } catch (e) {
                setError((e as Error).message);
            }
        })();
    }, [id]);

    async function onSave() {
        if (!id) return;
        setBusy(true);
        try {
            const updated = await updateDocument(Number(id), desc);
            setDoc(updated);
        } catch (e) {
            setError((e as Error).message);
        } finally {
            setBusy(false);
        }
    }

    if (error) return <div role="alert">Error: {error}</div>;
    if (!doc) return <div>Loading…</div>;

    return (
        <main>
            <h1>{doc.filename}</h1>
            <p><strong>ID:</strong> {doc.id}</p>
            <p>
                <input
                    value={desc}
                    onChange={(e) => setDesc(e.target.value)}
                    placeholder="Description"
                />
                <button onClick={onSave} disabled={busy}>
                    {busy ? "Saving…" : "Save"}
                </button>
            </p>
            <p><Link to="/">Back</Link></p>
        </main>
    );
}
