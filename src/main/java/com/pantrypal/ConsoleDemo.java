package com.pantrypal;

import com.pantrypal.dao.DataAccessException;
import com.pantrypal.dao.PantryItemDAO;
import com.pantrypal.dao.UserDAO;
import com.pantrypal.model.ItemStatus;
import com.pantrypal.model.PantryItem;
import com.pantrypal.model.User;
import com.pantrypal.util.HibernateUtil;
import java.time.LocalDate;
import java.util.List;

/** 
 * Runs Create -> Read -> Update -> Read -> Delete -> Read in order, 
 * so you can watch each ORM operation (and its SQL) in the NetBeans Output window. 
 * Run with: right-click file -> Run File (Shift+F6). 
 */ 
public class ConsoleDemo {
    public static void main(String[] args) {
        UserDAO users = new UserDAO();
        PantryItemDAO items = new PantryItemDAO();
        
        try {
            // ---------- READ  ----------
            System.out.println("\n== 1. Daniel's pantry (user 1), soonest expiry first ==");
            printItems(items.findByUser(1));

            System.out.println("\n== 2. Items expiring on or before 2026-10-20 ==");
            printItems(items.findExpiringBy(1, LocalDate.of(2026, 10, 20)));

            // ---------- CREATE ----------
            System.out.println("\n== 3. Create a new user and add 2 cartons of milk ==");
            User demo = new User("Demo Student","demo" + System.currentTimeMillis() + "@pantrypal.test", "hash_demo");
            Integer userId = users.createUser(demo);
            System.out.println("New user id = " + userId);

            // Expiry is null, so the DAO works it out from the Dairy shelf life (7 days).
            PantryItem milk = new PantryItem(LocalDate.now(), null, 2);
            Integer itemId = items.addItem(userId, 1, milk);     
            printItems(items.findByUser(userId));

            // ---------- UPDATE ----------
            System.out.println("\n== 4. Update quantity, then mark CONSUMED ==");
            items.updateQuantity(itemId, 1);
            items.markStatus(itemId, ItemStatus.CONSUMED);
            printItems(items.findByUser(userId));

            users.updateSettings(userId, false, true);
            System.out.println("Settings updated for user " + userId);

            // ---------- DELETE ----------
            System.out.println("\n== 5. Delete the item, then the user ==");
            System.out.println("Item deleted: " + items.deleteItem(itemId));
            System.out.println("User deleted: " + users.deleteUser(userId));
            System.out.println("Deleting again returns: " + users.deleteUser(userId));
            } 
            catch (DataAccessException ex) {
                System.err.println("DAO error: " + ex.getMessage());
            } finally {
                HibernateUtil.shutdown();       // release the connection pool
            }
        }
    private static void printItems(List<PantryItem> list) {
       for (PantryItem p : list) {
           System.out.printf("  - %-12s (%s) qty=%d expires=%s [%s]%n",
                   p.getIngredient().getIngredientName(),
                   p.getIngredient().getCategory().getCategoryName(),
                   p.getQuantity(), p.getExpiryDate(), p.getStatus());
       }
   }
}