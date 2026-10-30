package com.pantrypal.domain;

import static org.junit.jupiter.api.Assertions.*;
import com.pantrypal.model.ItemStatus;
import com.pantrypal.model.PantryItem;
import com.pantrypal.support.TestLogger;
import java.time.LocalDate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestLogger.class)
class PantryItemTest {

    private static final Logger LOG
            = LogManager.getLogger(PantryItemTest.class);

    @Test
    void constructorSetsDatesAndQuantity() {
        PantryItem item = new PantryItem(LocalDate.of(2026, 10, 5),
                LocalDate.of(2026, 10, 12), 2);
        assertEquals(LocalDate.of(2026, 10, 5), item.getPurchaseDate());
        assertEquals(LocalDate.of(2026, 10, 12), item.getExpiryDate());
        assertEquals(2, item.getQuantity());
    }

    @Test
    void newItemIsActiveWithNoStatusDateOrReceipt() {
        PantryItem item = new PantryItem(LocalDate.now(),
                LocalDate.now().plusDays(7), 1);
        assertEquals(ItemStatus.ACTIVE, item.getStatus());
        assertNull(item.getStatusDate());
        assertNull(item.getReceipt(), "an item added by hand has no receipt");
    }

    @Test
    void settersChangeStatusAndQuantity() {
        PantryItem item = new PantryItem(LocalDate.now(),
                LocalDate.now().plusDays(7), 1);
        item.setStatus(ItemStatus.CONSUMED);
        item.setStatusDate(LocalDate.of(2026, 10, 9));
        item.setQuantity(0);
        assertEquals(ItemStatus.CONSUMED, item.getStatus());
        assertEquals(LocalDate.of(2026, 10, 9), item.getStatusDate());
        assertEquals(0, item.getQuantity());
    }

    @Test
    void toStringShowsQuantityDateAndStatus() {
        PantryItem item = new PantryItem(LocalDate.of(2026, 10, 5),
                LocalDate.of(2026, 10, 12), 2);
        String text = item.toString();
        assertTrue(text.contains("qty=2"));
        assertTrue(text.contains("2026-10-12"));
        assertTrue(text.contains("ACTIVE"));
    }

    @Test
    void itemStatusHasTheFourDocumentedValues() {
        assertArrayEquals(
                new ItemStatus[]{ItemStatus.ACTIVE, ItemStatus.CONSUMED,
                    ItemStatus.EXPIRED, ItemStatus.DISCARDED},
                ItemStatus.values());
    }

    @Test
    void unknownStatusTextIsRejected() {
        IllegalArgumentException ex
                = assertThrows(IllegalArgumentException.class, ()
                        -> ItemStatus.valueOf("SPOILED"));

        LOG.error("Expected exception: an unknown status was rejected", ex);
    }
}
