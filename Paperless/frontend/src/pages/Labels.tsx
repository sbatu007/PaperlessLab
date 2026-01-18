import { useEffect, useState } from "react";
import type { LabelDto } from "../api";
import { listLabels, createLabel, deleteLabel, updateLabel } from "../api";

export default function Labels() {
    const [labels, setLabels] = useState<LabelDto[]>([]);
    const [newName, setNewName] = useState("");
    const [edit, setEdit] = useState<Record<number, string>>({});
    const [error, setError] = useState<string | null>(null);

    async function reload() {
        const data = await listLabels();
        setLabels(data);
    }

    useEffect(() => { void reload(); }, []);

    return (
        <section className="panel">
            <div className="panel-header">
                <h2>Labels</h2>
            </div>

            <div className="panel-body">
                {error && <div className="alert error">{error}</div>}

                <div className="labels-admin-create">
                    <input
                        className="input"
                        value={newName}
                        onChange={(e) => setNewName(e.target.value)}
                        placeholder="Neues Label…"
                    />
                    <button
                        className="btn primary"
                        onClick={async () => {
                            try {
                                setError(null);
                                const n = newName.trim();
                                if (!n) return;
                                await createLabel(n);
                                setNewName("");
                                await reload();
                            } catch (e) {
                                setError((e as Error).message);
                            }
                        }}
                    >
                        Add
                    </button>
                </div>

                <div className="labels-admin-list">
                    {labels.map((l) => (
                        <div key={l.id} className="labels-admin-row">
                            <input
                                className="input"
                                value={edit[l.id] ?? l.name}
                                onChange={(e) => setEdit((p) => ({ ...p, [l.id]: e.target.value }))}
                            />
                            <button
                                className="btn"
                                onClick={async () => {
                                    try {
                                        setError(null);
                                        const name = (edit[l.id] ?? l.name).trim();
                                        await updateLabel(l.id, name);
                                        await reload();
                                    } catch (e) {
                                        setError((e as Error).message);
                                    }
                                }}
                            >
                                Save
                            </button>
                            <button
                                className="btn danger"
                                onClick={async () => {
                                    try {
                                        setError(null);
                                        await deleteLabel(l.id);
                                        await reload();
                                    } catch (e) {
                                        setError((e as Error).message);
                                    }
                                }}
                            >
                                Delete
                            </button>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
}
