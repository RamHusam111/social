package object_orienters.techspot.message;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

   // private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessage>> getChatMessages(@PathVariable String senderId, @PathVariable String recipientId) {
        //return ResponseEntity.ok(chatMessageService.findChatMessages(senderId, recipientId));
        return null;
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
//        ChatMessage saveChatMessage = chatMessageService.saveChatMessage(chatMessage);
//        messagingTemplate.convertAndSendToUser(chatMessage.getRecipientId(), "/queue/messages", ChatNotification.builder()
//                .id(saveChatMessage.getId())
//                .recipientId(saveChatMessage.getRecipientId())
//                .senderId(saveChatMessage.getSenderId())
//                .content(saveChatMessage.getContent())
//                .build()
//        );
    }
}