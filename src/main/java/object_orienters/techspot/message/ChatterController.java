package object_orienters.techspot.message;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatterController {

    @Autowired
    private ChatterService chatterService;

    @MessageMapping("/user.add")
    @SendTo("/user/public")
    public Chatter addChatter(@Payload Chatter chatter) {
        chatterService.saveChatter(chatter);
        return chatter;
    }

    @MessageMapping("/user.disconnect")
    @SendTo("/user/public")
    public Chatter disconnectChatter(@Payload Chatter chatter) {
        chatterService.disconnectChatter(chatter);
        return chatter;
    }

    @GetMapping("/users")
    public ResponseEntity<List<Chatter>> getConnectedChatters() {
        return ResponseEntity.ok(chatterService.getConnectedChatters());
    }
}
