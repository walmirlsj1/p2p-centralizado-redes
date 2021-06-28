package projkurose.peer.model;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Shared {

    private Long id, size;
    private String title, path;

    public Shared(Long id, String title, String path, Long size) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shared shared = (Shared) o;
        return Objects.equals(title, shared.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }

    @Override
    public String toString() {
        return "Shared{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size + '\'' +
                ", hashCode=" + hashCode() +
                '}';
    }
}
