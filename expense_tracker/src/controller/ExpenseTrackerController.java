package controller;

import view.ExpenseTrackerView;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import model.ExpenseTrackerModel;
import model.Transaction;
import model.Filter.TransactionFilter;

public class ExpenseTrackerController {
  
  private ExpenseTrackerModel model;
  private ExpenseTrackerView view;
  /** 
   * The Controller is applying the Strategy design pattern.
   * This is the has-a relationship with the Strategy class 
   * being used in the applyFilter method.
   */
  private TransactionFilter filter;

  public ExpenseTrackerController(ExpenseTrackerModel model, ExpenseTrackerView view) {
    this.model = model;
    this.view = view;
  }

  public void setFilter(TransactionFilter filter) {
    // Sets the Strategy class being used in the applyFilter method.
    this.filter = filter;
  }

  public void refresh() {
    List<Transaction> transactions = model.getTransactions();
    view.refreshTable(transactions);
  }

  public boolean addTransaction(double amount, String category) {
    if (!InputValidation.isValidAmount(amount)) {
      return false;
    }
    if (!InputValidation.isValidCategory(category)) {
      return false;
    }
    
    Transaction t = new Transaction(amount, category);
    model.addTransaction(t);
    view.getTableModel().addRow(new Object[]{t.getAmount(), t.getCategory(), t.getTimestamp()});
    refresh();
    return true;
  }

  public void applyFilter() {
    //null check for filter
    if(filter!=null){
      // Use the Strategy class to perform the desired filtering
      List<Transaction> transactions = model.getTransactions();
      List<Transaction> filteredTransactions = filter.filter(transactions);
      List<Integer> rowIndexes = new ArrayList<>();
      for (Transaction t : filteredTransactions) {
        int rowIndex = transactions.indexOf(t);
        if (rowIndex != -1) {
          rowIndexes.add(rowIndex);
        }
      }
      view.highlightRows(rowIndexes);
    }
    else{
      JOptionPane.showMessageDialog(view, "No filter applied");
      view.toFront();}

  }

  public void applyUndo(int[] rows){
    List<Transaction> transactions = model.getTransactions();
    List<Transaction> transactionsToRemove = new ArrayList<>();
    for(int row : rows){
      transactionsToRemove.add(transactions.get(row));
    }
    for(Transaction transaction: transactionsToRemove){
      model.removeTransaction(transaction);
    }
    refresh();

    // if filter is not null we apply the filter to remove filter from rows we just removed
    if(filter != null){
      applyFilter();
    }

  }

  public void refreshUndoBtn(){
    if(model.getSelectedRows().length > 0){
      view.enableUndoBtn();
    }
    else{
      view.disableUndoBtn();
    }
  }


}
