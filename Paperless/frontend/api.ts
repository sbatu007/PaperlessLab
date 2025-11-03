// src/api.ts
export type DocumentDto = {
    id: number;
    filename: string;
    description?: string | null;
};


const base = '/api';

async function handle<T>(res: Response): Promise<T> {
    if (!res.ok) {
        const text = await res.text().catch(() => '');
        throw new Error(`HTTP ${res.status} ${res.statusText}: ${text}`);
    }
    return (await res.json()) as T;
}

export async function listDocuments(): Promise<DocumentDto[]> {
    const res = await fetch(`${base}/documents/list`, { method: 'GET' });
    return handle<DocumentDto[]>(res);
}

export async function getDocument(id: number): Promise<DocumentDto> {
    const res = await fetch(`${base}/documents/${id}`, { method: 'POST' });
    return handle<DocumentDto>(res);
}

export async function uploadDocument(file: File, description?: string): Promise<DocumentDto> {
    const form = new FormData();
    form.append('file', file);
    if (description) form.append('description', description);
    const res = await fetch(`${base}/documents/upload`, { method: 'POST', body: form });
    return handle<DocumentDto>(res);
}
