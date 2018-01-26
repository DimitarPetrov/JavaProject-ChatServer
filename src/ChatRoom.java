import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoom {

    private String creator;
    private Set<CommunicationService> participants;
    private ConcurrentHashMap<String, CommunicationService> onlineClients;

    public ChatRoom(String username, ConcurrentHashMap<String, CommunicationService> onlineClients) {
        this.creator = username;
        this.onlineClients = onlineClients;
        participants = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public void addMember(CommunicationService member) {
        participants.add(member);
    }

    public boolean removeMember(CommunicationService member) {
        if (!participants.contains(member)) {
            return false;
        }
        participants.remove(member);
        return true;
    }

    public boolean isMember(CommunicationService client) {
        return participants.contains(client);
    }

    public boolean isActive() {
        for (CommunicationService client : participants) {
            if (onlineClients.values().contains(client)) {
                return true;
            }
        }
        return false;
    }

    public String getCreator() {
        return creator;
    }

}
