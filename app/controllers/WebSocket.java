package controllers;

import models.User;
import play.libs.F.*;
import play.mvc.Http;
import play.mvc.Http.WebSocketClose;
import play.mvc.WebSocketController;
import static play.libs.F.Matcher.ClassOf;
import static play.libs.F.Matcher.Equals;
import static play.mvc.Http.WebSocketEvent.TextFrame;

/**
 * Created by enkhamgalan on 3/8/15.
 */
public class WebSocket extends WebSocketController {

    public static void join(Long id) {
        Long userId = id;
        UserLiveRoom room = UserLiveRoom.get();
        // Socket connected, join the chat room
        EventStream<UserLiveRoom.Event> roomMessagesStream = room.join(userId);

        // Loop while the socket is open
        while (inbound.isOpen()) {
            // Wait for an event (either something coming on the inbound socket channel, or UserLiveRoom messages)
            Either<Http.WebSocketEvent, UserLiveRoom.Event> e = await(Promise.waitEither(
                    inbound.nextEvent(),
                    roomMessagesStream.nextEvent()
            ));
            // Case: User typed 'quit'
            for (String userMessage : TextFrame.and(Equals("quit")).match(e._1)) {
                room.onlineStateFn(userId,0);
                disconnect();
            }
            for (String userData : TextFrame.match(e._1)) {
                room.webRTCData(userData,userId);
            }
            // Case: Someone receiveCount
            for (UserLiveRoom.NotificationEvent notificationEvent : ClassOf(UserLiveRoom.NotificationEvent.class).match(e._2)) {
                outbound.sendJson(notificationEvent.json);
            }
            for (UserLiveRoom.NotificationReminder reminderTag : ClassOf(UserLiveRoom.NotificationReminder.class).match(e._2)) {
                outbound.sendJson(reminderTag.json);
            }
            for (UserLiveRoom.WebRTCEvents webRTCEvents : ClassOf(UserLiveRoom.WebRTCEvents.class).match(e._2)) {
                if(userId == webRTCEvents.userId)outbound.send(webRTCEvents.data);
            }
            for (UserLiveRoom.OnlineStateEvent onlineState : ClassOf(UserLiveRoom.OnlineStateEvent.class).match(e._2)) {
                outbound.sendJson(onlineState.json);
            }
            // Case: The socket has been closed
            for (WebSocketClose closed : Http.WebSocketEvent.SocketClosed.match(e._1)) {
                room.onlineStateFn(userId, 0);
                disconnect();
            }
        }
    }
}
