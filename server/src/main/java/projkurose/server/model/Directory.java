package projkurose.server.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Objects;

@Getter
@Setter
public class Directory {
    private Long id;
    private String title;

    public Directory() {

    }

    public Directory(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Directory directory = (Directory) o;
        return Objects.equals(title, directory.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }
}