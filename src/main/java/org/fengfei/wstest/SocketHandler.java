package org.fengfei.wstest;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SocketHandler extends TextWebSocketHandler {

    ConcurrentHashMap<String, List> sessionGroup = new ConcurrentHashMap<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Map attr = session.getAttributes();

        Map value = new Gson().fromJson(message.getPayload(), Map.class);
        if (value.get("action").equals("enter")){
            String room = value.get("room").toString();

            // 权限校验

            // 加入聊天室
            handleMemberEnter(room, session);
        }

        else if (value.get("action").equals("leave")){
            String room = value.get("room").toString();
            handleMemberOut(room, session);
        }

        else{
            session.sendMessage(message);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        //
    }

    @Override
    public void afterConnectionClosed(WebSocketSession var1, CloseStatus var2) {
        // handle member out
    }

    private void handleMemberEnter(String room, WebSocketSession session) {
        if (sessionGroup.contains(room)){
            sessionGroup.put(room, new CopyOnWriteArrayList<WebSocketConfig>());
        }

        sessionGroup.get(room).add(session);
        broadcast(sessionGroup.get(room), new TextMessage("Welcome " + session.getPrincipal().getName()));
    }

    private void handleMemberOut(String room, WebSocketSession session) {
        if (!sessionGroup.contains(room)){
            return;
        }

        broadcast(sessionGroup.get(room), new TextMessage("Goodbye " + session.getPrincipal().getName()));
        sessionGroup.get(room).remove(session);
    }

    private void broadcast(List<WebSocketSession> sessions, TextMessage message) {
        for(WebSocketSession webSocketSession : sessions) {
            try {
                webSocketSession.sendMessage(message);
            }catch (IOException e){
            }
        }
    }
}