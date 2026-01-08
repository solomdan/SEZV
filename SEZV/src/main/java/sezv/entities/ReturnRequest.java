package sezv.entities;

import jakarta.persistence.*;
import sezv.entities.enums.ReturnRequestStatus;
import sezv.entities.enums.FulfillmentType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ReturnRequestTable")
public class ReturnRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    @JoinColumn(name = "LostItemTable")
    LostItem lostItem;
    String requesterName; // in full-ver change to User object
    String requesterEmail; // in full-ver change to User object
    String requesterPhone;
    ReturnRequestStatus status; // `PENDING`, `APPROVED`, `REJECTED`, `COMPLETED`, `CANCELLED`
    String comment;
    FulfillmentType fulfillmentType; // `PICKUP`, `DELIVERY_DOMESTIC`, `DELIVERY_INTERNATIONAL`
    String shippingAddress;
    LocalDateTime createdAt;
    LocalDateTime processedAt;
}
