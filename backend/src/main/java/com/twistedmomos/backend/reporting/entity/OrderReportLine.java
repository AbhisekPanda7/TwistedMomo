package com.twistedmomos.backend.reporting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One sold line, flattened for reporting.
 *
 * <p>Deliberately not related to Order or MenuItem: this is a derived read model, and a
 * foreign key would let a deleted menu item rewrite what the report says was sold.
 */
@Entity
@Table(name = "order_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class OrderReportLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Null once the item is hard-deleted; the name and price below still stand. */
    @Column(name = "menu_item_id")
    private Long menuItemId;

    @Column(name = "item_name", nullable = false, length = 150)
    private String itemName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    /** Delivery area only. Street lines and phone are personal data with no analytical value. */
    @Column(length = 100)
    private String city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;
}
