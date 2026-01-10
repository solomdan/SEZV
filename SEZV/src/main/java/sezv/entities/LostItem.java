package sezv.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import sezv.entities.enums.FoundIn;
import sezv.entities.enums.LostItemStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "LostItemTable")
public class LostItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  id;
    private String title;
    private String description;
    @Enumerated(EnumType.STRING)
    private FoundIn foundIn;
    private String routeNumber;
    private String vehicleNumber;
    private LocalDateTime foundAt;
    private String storageLocation;
    private LostItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "CategoryTable")
    Set<Category> categorySet;

    @OneToMany(mappedBy = "ReturnRequestTable")
    List<ReturnRequest> returnRequestList;

    @Override
    public String toString() {
        return "LostItem{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", description='" + description + '\''
                + ", foundIn=" + foundIn
                + ", routeNumber='" + routeNumber + '\''
                + ", vehicleNumber='" + vehicleNumber + '\''
                + ", foundAt=" + foundAt
                + ", storageLocation='" + storageLocation + '\''
                + ", status=" + status
                + ", createdAt=" + createdAt
                + ", updatedAt=" + updatedAt
                + ", categorySet=" + categorySet
                + ", returnRequestList=" + returnRequestList
                + '}';
    }
}
