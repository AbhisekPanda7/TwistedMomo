package com.twistedmomos.backend.reporting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.twistedmomos.backend.order.event.OrderPlacedEvent;
import com.twistedmomos.backend.reporting.entity.OrderReportLine;
import com.twistedmomos.backend.reporting.repository.OrderReportRepository;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderReportListenerTest {

    private static final Instant PLACED_AT = Instant.parse("2026-08-04T12:00:00Z");

    private static final OrderPlacedEvent.DeliveryAddress ADDRESS =
            new OrderPlacedEvent.DeliveryAddress(
                    "Tester", "9999999999", "1 Test St", null, "Cuttack", "753014");

    private static final OrderPlacedEvent EVENT = new OrderPlacedEvent(
            100L,
            7L,
            PLACED_AT,
            new BigDecimal("260.00"),
            List.of(
                    new OrderPlacedEvent.LineItem(
                            1L, "Veg Momo", 2, new BigDecimal("80.00"), new BigDecimal("160.00")),
                    new OrderPlacedEvent.LineItem(
                            2L, "Chowmein", 1, new BigDecimal("100.00"), new BigDecimal("100.00"))),
            ADDRESS);

    @Mock private OrderReportRepository reportRepository;

    @InjectMocks private OrderReportListener listener;

    @Test
    void recordsOneRowPerOrderLine() {
        when(reportRepository.existsByOrderId(100L)).thenReturn(false);

        listener.on(EVENT);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderReportLine>> saved = ArgumentCaptor.forClass(List.class);
        verify(reportRepository).saveAll(saved.capture());

        assertThat(saved.getValue()).hasSize(2);
        assertThat(saved.getValue())
                .extracting(OrderReportLine::getItemName, OrderReportLine::getLineTotal)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Veg Momo", new BigDecimal("160.00")),
                        org.assertj.core.groups.Tuple.tuple("Chowmein", new BigDecimal("100.00")));
        assertThat(saved.getValue()).allMatch(line -> line.getPlacedAt().equals(PLACED_AT));
        assertThat(saved.getValue()).allMatch(line -> line.getUserId().equals(7L));
    }

    /**
     * The outbox retries an incomplete publication, so the same event can arrive twice.
     * Without this guard a replay would double every figure the report produces.
     */
    @Test
    void ignoresAnOrderItHasAlreadyRecorded() {
        when(reportRepository.existsByOrderId(100L)).thenReturn(true);

        listener.on(EVENT);

        verify(reportRepository, never()).saveAll(any());
    }

    /** A deleted menu item leaves the id null; the name and price still describe the sale. */
    @Test
    void keepsTheLineWhenTheMenuItemIsGone() {
        when(reportRepository.existsByOrderId(anyLong())).thenReturn(false);
        OrderPlacedEvent orphaned = new OrderPlacedEvent(
                101L, 7L, PLACED_AT, new BigDecimal("80.00"),
                List.of(new OrderPlacedEvent.LineItem(
                        null, "Discontinued Momo", 1, new BigDecimal("80.00"), new BigDecimal("80.00"))),
                ADDRESS);

        listener.on(orphaned);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderReportLine>> saved = ArgumentCaptor.forClass(List.class);
        verify(reportRepository).saveAll(saved.capture());
        assertThat(saved.getValue()).singleElement().satisfies(line -> {
            assertThat(line.getMenuItemId()).isNull();
            assertThat(line.getItemName()).isEqualTo("Discontinued Momo");
        });
    }

    @Test
    void recordsDeliveryAreaButNotTheStreetAddress() {
        when(reportRepository.existsByOrderId(100L)).thenReturn(false);

        listener.on(EVENT);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderReportLine>> saved = ArgumentCaptor.forClass(List.class);
        verify(reportRepository).saveAll(saved.capture());
        assertThat(saved.getValue()).allSatisfy(line -> {
            assertThat(line.getCity()).isEqualTo("Cuttack");
            assertThat(line.getPostalCode()).isEqualTo("753014");
        });
    }

    /**
     * The report is analytics: a future field carrying street or phone must fail here, not ship.
     * Lombok-generated synthetic fields are filtered out.
     */
    @Test
    void keepsPersonalDataOutOfTheReportEntity() {
        assertThat(
                        java.util.Arrays.stream(OrderReportLine.class.getDeclaredFields())
                                .filter(f -> !isSynthetic(f))
                                .map(Field::getName)
                                .toList())
                .doesNotContain("addressLine1", "addressLine2", "recipientName", "phone");
    }

    private static boolean isSynthetic(Field f) {
        return f.isSynthetic() || f.getName().contains("$");
    }
}
