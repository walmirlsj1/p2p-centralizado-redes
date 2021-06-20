package app.base;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class Directory {
    private Long id;
    private String title;
    private Long size;
    private ArrayList<Client> client;

    public Directory() {

    }

    public Directory(Long id, String title, Long size) {
        this.id = id;
        this.title = title;
        this.size = size;
        this.client = new ArrayList<>();
    }

    public void addSeed(Client client) {
        this.client.add(client);
    }

    public void removeSeed(Client client) {
        this.client.remove(client);
    }
}