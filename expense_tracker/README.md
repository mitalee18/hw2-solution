# hw1- Manual Review

The homework will be based on this project named "Expense Tracker",where users will be able to add/remove daily transaction. 

## Compile

To compile the code from terminal, use the following command:
```
cd src
javac ExpenseTrackerApp.java
java ExpenseTracker
```

You should be able to view the GUI of the project upon successful compilation. 

## Java Version
This code is compiled with ```openjdk 17.0.7 2023-04-18```. Please update your JDK accordingly if you face any incompatibility issue.

## Undo Functionality
1. Added listener on rows in the table so that if a row is selected we can get it's index as well and enable undo button.
2. Additionally, if there is nothing in the table then nothing can be selected which allows the undo button to be disabled.
3. This feature also allows multiple rows to be selected except for the last row to be undone.

To support this feature we performed following changed
1. Created an undo button in view and added. 
2. Added undo button listener, to undo the selected transactions.
3. Once you add transactions and select rows, undo button would be enabled using refreshUndoBtn method in controller.
4. Once you select some transactions to be undone, and click on the undo button then applyUndo() in the controller would be called.
5. This would remove transactions from the transactions table and as well as apply filter again so that the filtered transactions which are now removed should also remove filter from their row index.

## Test cases   
1. Test Add transaction  
Added transaction to the table through controller and checked if transaction table contents and total is updated through model, view and controller.    

2. Test Invalid Input Handling  
Tried to add an invalid transaction through controller, checked through view that invalid transaction is not added and that the transactions and total remains the same 
Displayed error by catching illegal argument exception  

3.  Test Filter by Amount
Added multiple transactions with different amounts, and applied filter for a specific amount through controller. Checked if correct rows were filtered/highlighted through view.  

4. Test Filter by Category 
Added multiple transactions with different categories, and applied filter for a specific category through controller. Checked if correct rows were filtered/highlighted through view.

5. Test Undo Disallowed  
Checked through model if transaction list is empty the undo button should be disabled. Checked through view if undo button is disabled when there are no transactions selected.

6. Test Undo Allowed  
Added multiple transactions through controller. Selected a transaction to undo through model. Performed undo functionality through controller. Checked if transaction was undone and total was updated through model.
