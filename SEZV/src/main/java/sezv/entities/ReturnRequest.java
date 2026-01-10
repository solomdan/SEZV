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
    private Long id;
    @ManyToOne
    @JoinColumn(name = "LostItemTable")
    private LostItem lostItem;
    private String requesterName; // in full-ver change to User object
    private String requesterEmail; // in full-ver change to User object
    private String requesterPhone;
    // in full-ver add assigned employ
    @Enumerated(EnumType.STRING)
    private ReturnRequestStatus status; // `PENDING`, `APPROVED`, `REJECTED`, `COMPLETED`, `CANCELLED`
    private String comment;
    @Enumerated(EnumType.STRING)
    private FulfillmentType fulfillmentType; // `PICKUP`, `DELIVERY_DOMESTIC`, `DELIVERY_INTERNATIONAL`
    private String shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public String toString() {
        return "ReturnRequest{"
                + "id=" + id
                + ", lostItem=" + lostItem
                + ", requesterName=" + requesterName
                + ", requesterEmail=" + requesterEmail
                + ", requesterPhone=" + requesterPhone
                + ", status=" + status
                + ", comment=" + comment
                + ", fulfillmentType=" + fulfillmentType
                + ", shippingAddress=" + shippingAddress
                + ", createdAt=" + createdAt
                + ", processedAt=" + processedAt
                + '}';
    }
}
