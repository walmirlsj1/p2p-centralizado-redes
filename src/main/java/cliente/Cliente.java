package cliente;

public class Cliente {
    private Long    id;
    private String  ip;
    private String  key;

    public Cliente() {

    }

    public Cliente(Long id, String ip, String key) {
        this.id = id;
        this.ip = ip;
        this.key = key;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
