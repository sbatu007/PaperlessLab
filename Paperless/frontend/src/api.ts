export type LabelDto = {
    id: number;
    name: string;
};

export type DocumentDto = {
    id: number;
    filename: string;
    description?: string | null;
    ocrText?: string | null;
    result?: string | null;
    labels?: LabelDto[];
};



const base = "/api";

async function handle<T>(res: Response): Promise<T> {
    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText}: ${text}`);
    }
    return (await res.json()) as T;
}

// GET /documents
export async function listDocuments(): Promise<DocumentDto[]> {
    const res = await fetch(`${base}/documents`, { method: "GET" });
    return handle<DocumentDto[]>(res);
}

// GET /documents/{id}
export async function getDocument(id: number): Promise<DocumentDto> {
    const res = await fetch(`${base}/documents/${id}`, { method: "GET" });
    return handle<DocumentDto>(res);
}

// POST /documents/upload?description=...
export async function uploadDocument(file: File, description?: string): Promise<DocumentDto> {
    const form = new FormData();
    form.append("file", file);
    const qs = description && description.trim()
        ? `?${new URLSearchParams({ description: description.trim() }).toString()}`
        : "";
    const res = await fetch(`${base}/documents/upload${qs}`, {
        method: "POST",
        body: form,
    });
    return handle<DocumentDto>(res);
}

// DELETE /documents/{id}
export async function deleteDocument(id: number): Promise<void> {
    const res = await fetch(`${base}/documents/${id}`, { method: "DELETE" });
    if (!res.ok) throw new Error(`HTTP ${res.status} ${res.statusText}`);
}

// PUT /documents/{id}
export async function updateDocument(id: number, description: string): Promise<DocumentDto> {
    const res = await fetch(`${base}/documents/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ description }),
    });
    return handle<DocumentDto>(res);
}

export type SearchHitDto = {
    documentId: number;
    filename: string;
    score: number;
};

export async function searchDocuments(q: string): Promise<SearchHitDto[]> {
    const res = await fetch(`${base}/documents/search?q=${encodeURIComponent(q)}`, { method: "GET" });
    return handle<SearchHitDto[]>(res);
}
export async function listLabels(): Promise<LabelDto[]> {
    const res = await fetch(`${base}/labels`, {
        method: "GET",
        cache: "no-store",
        headers: { "Accept": "application/json" },
    });
    return handle<LabelDto[]>(res);
}

export async function updateLabel(id: number, name: string): Promise<LabelDto> {
    const res = await fetch(`${base}/labels/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name }),
    });
    return handle<LabelDto>(res);
}

export async function createLabel(name: string): Promise<LabelDto> {
    const res = await fetch(`${base}/labels`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name }),
    });
    return handle<LabelDto>(res);
}

export async function deleteLabel(id: number): Promise<void> {
    const res = await fetch(`${base}/labels/${id}`, { method: "DELETE" });
    if (!res.ok) throw new Error(`HTTP ${res.status} ${res.statusText}`);
}

export async function setDocumentLabels(docId: number, labelIds: number[]): Promise<DocumentDto> {
    const res = await fetch(`${base}/documents/${docId}/labels`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ labelIds }),
    });
    return handle<DocumentDto>(res);
}
