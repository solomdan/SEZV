package sezv.entities;


import lombok.Getter;
import sezv.entities.enums.FoundIn;
import sezv.entities.enums.LostItemStatus;

import java.time.LocalDateTime;


@Getter
public class LostItem {
    private Long  id;
    private String title;
    private String description;
    private FoundIn foundIn;
    private String routeNumber;
    private String vehicleNumber;
    private LocalDateTime foundAt;
    private String storageLocation;
    private LostItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    
}
