import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const WS_URL = process.env.NEXT_PUBLIC_WS_URL || "http://localhost:8081/ws";

export function createStompClient(token: string): Client {
  const client = new Client({
    webSocketFactory: () => new SockJS(WS_URL) as WebSocket,
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
  });
  return client;
}
