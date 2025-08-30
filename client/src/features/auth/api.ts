export type User = { id: number; email: string; fullName?: string };

const BASE = "/api/auth";

export async function signup(data: {
  email: string;
  password: string;
  fullName: string;
  phone?: string;
}): Promise<User> {
  const res = await fetch(`${BASE}/signup`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify(data),
  });
  if (!res.ok) {
    let message = "שגיאה בהרשמה";
    try {
      const body = await res.json();
      if (body && typeof body.message === "string") message = body.message;
    } catch {
      // ignore json parse errors
    }
    const error = new Error(message) as Error & { status?: number };
    error.status = res.status;
    throw error;
  }
  return res.json();
}

export async function login(data: {
  email: string;
  password: string;
}): Promise<User> {
  const res = await fetch(`${BASE}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error((await res.json()).message || "Login failed");
  return res.json();
}

export async function me(): Promise<User | null> {
  const res = await fetch(`${BASE}/me`, { credentials: "include" });
  if (!res.ok) return null;
  return res.json();
}

export async function logout(): Promise<void> {
  await fetch(`${BASE}/logout`, { method: "POST", credentials: "include" });
}
