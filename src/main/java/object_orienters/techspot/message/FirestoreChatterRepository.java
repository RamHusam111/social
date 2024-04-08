package object_orienters.techspot.message;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Component
public class FirestoreChatterRepository {


    private Firestore firestore;

    @Autowired
    public FirestoreChatterRepository(Firestore firestore) {
        this.firestore = firestore;
    }

//    public FirestoreChatterRepository() {
//        this.firestore = FirestoreClient.getFirestore();
//    }
    public String saveChatter(Chatter chatter) {
        ApiFuture<WriteResult> collectionApiFuture = firestore.collection("chatters").document(chatter.getUsername()).set(chatter);
        return "Chatter saved";
    }

    public Optional<Chatter> getChatter(String username) throws ExecutionException, InterruptedException {
        return Optional.ofNullable(firestore.collection("chatters").document(username).get().get().toObject(Chatter.class));
    }

    public List<Chatter> getChattersByStatus(Status status) throws ExecutionException, InterruptedException {
        return firestore.collection("chatters").whereEqualTo("status", status).get().get().toObjects(Chatter.class);
    }
}