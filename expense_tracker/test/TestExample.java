// package test;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import controller.ExpenseTrackerController;
import model.ExpenseTrackerModel;
import model.Transaction;
import view.ExpenseTrackerView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import model.Filter.AmountFilter;
import model.Filter.CategoryFilter;
import model.Filter.TransactionFilter;


import static org.junit.Assert.*;

public class TestExample {
  
  private ExpenseTrackerModel model;
  private ExpenseTrackerView view;
  private ExpenseTrackerController controller;

  @Before
  public void setup() {
    model = new ExpenseTrackerModel();
    view = new ExpenseTrackerView();
    controller = new ExpenseTrackerController(model, view);
  }

    public double getTotalCost() {
        double totalCost = 0.0;
        List<Transaction> allTransactions = model.getTransactions(); // Using the model's getTransactions method
        for (Transaction transaction : allTransactions) {
            totalCost += transaction.getAmount();
        }
        return totalCost;
    }


    public void checkTransaction(double amount, String category, Transaction transaction) {
	assertEquals(amount, transaction.getAmount(), 0.01);
        assertEquals(category, transaction.getCategory());
        String transactionDateString = transaction.getTimestamp();
        Date transactionDate = null;
        try {
            transactionDate = Transaction.dateFormatter.parse(transactionDateString);
        }
        catch (ParseException pe) {
            pe.printStackTrace();
            transactionDate = null;
        }
        Date nowDate = new Date();
        assertNotNull(transactionDate);
        assertNotNull(nowDate);
        // They may differ by 60 ms
        assertTrue(nowDate.getTime() - transactionDate.getTime() < 60000);
    }


    @Test
    public void testAddTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add a transaction
        double amount = 50.0;
        String category = "food";
        assertTrue(controller.addTransaction(amount, category));
        // Post-condition: List of transactions contains only
	    //                 the added transaction
        assertEquals(1, model.getTransactions().size());
    
        // Check the contents of the list
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);
	
	// Check the total amount
        assertEquals(amount, getTotalCost(), 0.01);
    }


    @Test
    public void testRemoveTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add and remove a transaction
	double amount = 50.0;
	String category = "food";
        Transaction addedTransaction = new Transaction(amount, category);
        model.addTransaction(addedTransaction);
    
        // Pre-condition: List of transactions contains only
	//                the added transaction
        assertEquals(1, model.getTransactions().size());
	Transaction firstTransaction = model.getTransactions().get(0);
	checkTransaction(amount, category, firstTransaction);

	assertEquals(amount, getTotalCost(), 0.01);
	
	// Perform the action: Remove the transaction
        model.removeTransaction(addedTransaction);
    
        // Post-condition: List of transactions is empty
        List<Transaction> transactions = model.getTransactions();
        assertEquals(0, transactions.size());
    
        // Check the total cost after removing the transaction
        double totalCost = getTotalCost();
        assertEquals(0.00, totalCost, 0.01);
    }


    @Test
    public void testAddTransactionCase1() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: Add a transaction
        double amount = 50.0;
        String category = "food";
        assertTrue(controller.addTransaction(amount, category));

        // Post-condition: List of transactions contains only
        //                 the added transaction
        assertEquals(1, model.getTransactions().size());

        // Check the contents of the list
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);

        // Check the total amount
        assertEquals(amount, getTotalCost(), 0.01);

        // check if view is updated
        // Take all the list items from the view
        JTable viewList = view.getTransactionsTable();

        // getting current system time
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String formattedDateTime = currentDateTime.format(formatter);

        // creating a datatable for asserting values
        Object[][] matrix = {
                {1, 50.0 , "food", formattedDateTime },
                {"Total", null , null, 50.0 }
        };

        for (int i = 0; i < viewList.getRowCount(); i++) {
            for (int j = 0; j < viewList.getColumnCount(); j++) {
                assertEquals(matrix[i][j], viewList.getValueAt(i, j));
            }
        }


        //check if model is updated
        Object[][] matrix2 = {
                {1, 50.0 , "food", new Date().getTime() },
                {"Total", null , null, 50.0 }
        };
        DefaultTableModel tableListModel = view.getTableModel();
        Date transactionDate = null;
        try {
            transactionDate = Transaction.dateFormatter.parse((String) tableListModel.getValueAt(0, 3));
        } catch (ParseException e) {
            assertNotNull(transactionDate);
        }

        for (int i = 0; i < tableListModel.getRowCount(); i++) {
            for (int j = 0; j < tableListModel.getColumnCount(); j++) {
                if (i != tableListModel.getRowCount() - 1 && j == tableListModel.getColumnCount() - 1) {
                    assertTrue((long) matrix2[i][j] - transactionDate.getTime() < 60000);
                } else {
                    assertEquals(matrix2[i][j], tableListModel.getValueAt(i, j));
                }
            }
        }

    }

    @Test
    public void inputHandlingCase2(){

        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());


        double invalidAmount = -2;
        String categoryToTest = "food";

        assertFalse(controller.addTransaction(invalidAmount, categoryToTest));

        // check if view is updated
        JTable viewList = view.getTransactionsTable();
        assertEquals(0, viewList.getRowCount());

        // invalid transaction should not be added
        assertEquals(0, model.getTransactions().size());

        // trying to invoke the object creation using the construction to catch the error message
        try{
            Transaction t = new Transaction(invalidAmount, categoryToTest);
        }
        catch (IllegalArgumentException e){
            assertNotNull(e.getMessage());
            assertEquals("The amount is not valid.", e.getMessage());
        }

        //check for invalid category
        assertFalse(controller.addTransaction(50, "invalidCategory"));
        controller.addTransaction(50, "invalidCategory");

        // check if view is updated
        assertEquals(0, viewList.getRowCount());

        // invalid transaction should not be added
        assertEquals(0, model.getTransactions().size());

        try{
            Transaction t = new Transaction(50, "invalidCategory");
        }
        catch (IllegalArgumentException e){
            assertNotNull(e.getMessage());
            assertEquals("The category is not valid.", e.getMessage());
        }

    }

    @Test
    public void filterByAmountCase3(){
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: Add transactions
        controller.addTransaction(50.0, "food");
        controller.addTransaction(100.0, "bills");

        // creating amount filter
        double amountFilterInput = 50.0;
        AmountFilter amountFilter = new AmountFilter(amountFilterInput);
        controller.setFilter(amountFilter);
        controller.applyFilter();

        // getting all the rows from the view
        JTable viewList = view.getTransactionsTable();

        // setting colours
        Color expectedColor = new Color(173, 255, 168);
        Color white = new Color(255, 255, 255);

        // creating a datatable matrix for assertion
        Object[][] matrix = {
                {expectedColor, expectedColor, expectedColor, expectedColor },
                {white,white,white,white },
                {white,white,white,white }
        };

        // checking every cell in the view for it's background colour
        for (int i = 0; i < viewList.getRowCount(); i++) {
            for (int j = 0; j < viewList.getColumnCount(); j++) {
                Component cellRenderer = viewList.getCellRenderer(i, j).getTableCellRendererComponent(viewList, viewList.getValueAt(i, j), false, false, i, j);
                Color cellBackgroundColor = cellRenderer.getBackground();
                assertEquals(matrix[i][j], cellBackgroundColor );
            }
        }
    }


    @Test
    public void filterByCategoryCase4(){
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: Add transactions
        controller.addTransaction(50.0, "food");
        controller.addTransaction(100.0, "bills");

        // creating category filter
        String categoryFilterInput = "bills";
        CategoryFilter categoryfilter = new CategoryFilter(categoryFilterInput);
        controller.setFilter(categoryfilter);
        controller.applyFilter();

        // getting all the rows from the view
        JTable viewList = view.getTransactionsTable();

        // setting colors
        Color expectedColor = new Color(173, 255, 168);
        Color white = new Color(255, 255, 255);

        // creating a datatable matrix for assertion
        Object[][] matrix = {
                {white,white,white,white },
                {expectedColor, expectedColor, expectedColor, expectedColor },
                {white,white,white,white }
        };

        // checking every cell in the view for it's background colour
        for (int i = 0; i < viewList.getRowCount(); i++) {
            for (int j = 0; j < viewList.getColumnCount(); j++) {
                Component cellRenderer = viewList.getCellRenderer(i, j).getTableCellRendererComponent(viewList, viewList.getValueAt(i, j), false, false, i, j);
                Color cellBackgroundColor = cellRenderer.getBackground();
                assertEquals(matrix[i][j], cellBackgroundColor );
            }
        }
    }

    @Test
    public void undoDisallowedCase5(){
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        //since there is nothing in the transaction table button should be disabled
        assertFalse(view.getUndoBtn().isEnabled());

        // adding transaction
        controller.addTransaction(50.0, "food");
        controller.addTransaction(100.0, "bills");

        //since no transaction was selected the button should be disabled
        assertFalse(view.getUndoBtn().isEnabled());

    }

    @Test
    public void undoAllowedCase6(){
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: Add transactions
        controller.addTransaction(50.0, "food");
        controller.addTransaction(10.0, "bills");

        // getting all the list elements from the view
        JTable viewList = view.getTransactionsTable();

        // asserting if there are 2 transactions and 1 total in the view before undo
        assertEquals(3,viewList.getRowCount());

        // now selecting row to undo
        int [] selected = {0};
        model.setSelectedRows(selected);

        // enabling undo button
        controller.refreshUndoBtn();
        assertTrue(view.getUndoBtn().isEnabled());

        //applying undo to the selected row
        controller.applyUndo(selected);

        // getting view elements after undo
        viewList = view.getTransactionsTable();

        // asserting if there is 1 transaction and 1 total in the view after undo
        assertEquals(2,viewList.getRowCount());

        // creating a datatable for asserting
        Object[][] matrix = {
                {1, 10.0 , "bills", new Date().getTime() },
                {"Total", null , null, 10.0 }
        };

        // checking through model if the transactions is undone
        DefaultTableModel tableListModel = view.getTableModel();
        Date transactionDate = null;
        try {
            transactionDate = Transaction.dateFormatter.parse((String) tableListModel.getValueAt(0, 3));
        } catch (ParseException e) {
            assertNotNull(transactionDate);
        }

        for (int i = 0; i < tableListModel.getRowCount(); i++) {
            for (int j = 0; j < tableListModel.getColumnCount(); j++) {
                if (i != tableListModel.getRowCount() - 1 && j == tableListModel.getColumnCount() - 1) {
                    assertTrue((long) matrix[i][j] - transactionDate.getTime() < 60000);
                } else {
                    assertEquals(matrix[i][j], tableListModel.getValueAt(i, j));
                }
            }
        }


    }
    
}



