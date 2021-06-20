package app.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shared {
    private Long id;
    private String title;
    private String path;
    private Long size;

    @Override
    public boolean equals(Object o) {
//        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shared shared = (Shared) o;
        return Objects.equals(title, shared.title) && Objects.equals(size, shared.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, size);
    }

    @Override
    public String toString() {
        return "Shared{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                '}';
    }
}
