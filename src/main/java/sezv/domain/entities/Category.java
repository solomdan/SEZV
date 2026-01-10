package sezv.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "CategoryTable")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    LocalDateTime createdAt;

    @ManyToMany(mappedBy = "LostItemTable")
    Set<LostItem> lostItemSet;

    public String toString() {
        return "Category{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", createdAt=" + createdAt
                + ", lostItemSet=" + lostItemSet
                + '}';
    }
}
