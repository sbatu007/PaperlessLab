export type DocumentDto = {
    id: number;
    filename: string;
    description?: string;
};

const base = '/api';

export async function listDocuments(): Promise<DocumentDto[]> {
    const res = await fetch(`${base}/documents`);
    if (!res.ok) throw new Error('List failed');
    return res.json();
}

export async function getDocument(id: number): Promise<DocumentDto> {
    const res = await fetch(`${base}/document/${id}`, { method: 'POST' });
    if (!res.ok) throw new Error('Not found');
    return res.json();
}

export async function uploadDocument(file: File, description?: string): Promise<DocumentDto> {
    const form = new FormData();
    form.append('file', file);
    if (description) form.append('description', description);
    const res = await fetch(`${base}/document/upload`, { method: 'POST', body: form });
    if (!res.ok) throw new Error('Upload failed');
    return res.json();
}
