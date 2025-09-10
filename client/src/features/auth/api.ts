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

  const user = await res.json();

  // Broadcast login to other tabs/windows (since signup logs user in)
  broadcastAuthEvent("LOGIN");

  return user;
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

  const user = await res.json();

  // Broadcast login to other tabs/windows
  broadcastAuthEvent("LOGIN");

  return user;
}

export async function me(): Promise<User | null> {
  const res = await fetch(`${BASE}/me`, { credentials: "include" });
  if (!res.ok) return null;
  return res.json();
}

export async function logout(): Promise<void> {
  await fetch(`${BASE}/logout`, { method: "POST", credentials: "include" });

  // Broadcast logout to other tabs/windows
  broadcastAuthEvent("LOGOUT");
}

// Helper function to broadcast auth events across tabs/windows
function broadcastAuthEvent(eventType: "LOGIN" | "LOGOUT") {
  const timestamp = Date.now().toString();
  console.log(`🔄 Broadcasting ${eventType} event to other tabs/windows`);

  // Method 1: localStorage event
  const storageKey = eventType === "LOGIN" ? "auth_login" : "auth_logout";
  localStorage.setItem(storageKey, timestamp);
  localStorage.removeItem(storageKey);

  // Method 2: BroadcastChannel
  if ("BroadcastChannel" in window) {
    const channel = new BroadcastChannel("auth_channel");
    channel.postMessage({ type: eventType, timestamp });
    channel.close();
    console.log(`📡 ${eventType} event sent via BroadcastChannel`);
  } else {
    console.log(`⚠️ BroadcastChannel not supported, using localStorage only`);
  }
}
