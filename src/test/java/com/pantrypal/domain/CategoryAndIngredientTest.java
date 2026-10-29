package com.pantrypal.domain; 
import static org.junit.jupiter.api.Assertions.*;
import com.pantrypal.model.Category;
import com.pantrypal.model.Ingredient;
import com.pantrypal.model.Receipt;
import com.pantrypal.model.User;
import com.pantrypal.support.TestLogger;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
@ExtendWith(TestLogger.class)
class CategoryAndIngredientTest {
    @Test
    void categoryStoresNameAndShelfLife() {
        Category dairy = new Category("Dairy", 7);
        assertEquals("Dairy", dairy.getCategoryName());
        assertEquals(7, dairy.getShelfLifeDays());
        assertTrue(dairy.getIngredients().isEmpty());
    }
    @Test
    void ingredientKnowsItsCategory() {
        Category dairy = new Category("Dairy", 7);
        Ingredient milk = new Ingredient("Milk", dairy);
        assertEquals("Milk", milk.getIngredientName());
        assertSame(dairy, milk.getCategory());
    }
    @Test
void toStringMethodsDoNotFollowRelationships() {
Category dairy = new Category("Dairy", 7);
Ingredient milk = new Ingredient("Milk", dairy);
dairy.getIngredients().add(milk);
// Would recurse forever if Category.toString printed its 
ingredients.
assertDoesNotThrow(dairy::toString);
assertDoesNotThrow(milk::toString);
assertTrue(dairy.toString().contains("Dairy"));
}
}
}
@Test
void receiptStoresItsOwnerAndScanTime() {
User user = new User("A", "a@pantrypal.test", "h");
LocalDateTime scanned = LocalDateTime.of(2026, 10, 5, 17, 30);
Receipt receipt = new Receipt(user, scanned, "receipts/a.jpg");
assertSame(user, receipt.getUser());
assertEquals(scanned, receipt.getScannedAt());
assertEquals("receipts/a.jpg", receipt.getImagePath());
}
