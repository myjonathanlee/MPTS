import java.sql.*;
import javax.swing.table.*;
import java.util.Vector;

class ResultsModel extends AbstractTableModel 
{
  String[] columnNames = new String[0]; 
  Vector dataRows;              // Empty vector of rows 
  
  public void setResultSet(ResultSet results)
  {
    try
    {
      ResultSetMetaData metadata = results.getMetaData();

      int columns =  metadata.getColumnCount();    // Get number of columns
      columnNames = new String[columns];           // Array to hold names
      
      // Get the column names
      for(int i = 0; i < columns; i++)
        columnNames[i] = metadata.getColumnLabel(i+1);

      // Get all rows.
      dataRows = new Vector();                     // New Vector to store the data
      String[] rowData;                            // Stores one row
      while(results.next())                        // For each row...
      {
        rowData = new String[columns];             // create array to hold the data
        for(int i = 0; i < columns; i++)           // For each column
           rowData[i] = results.getString(i+1);    // retrieve the data item

        dataRows.addElement(rowData);              // Store the row in the vector
      }
      fireTableChanged(null);           // Signal the table there is new model data
    }
    catch (SQLException sqle)
    {
      System.err.println(sqle);
    }
  }

  public int getColumnCount()
  {
    return columnNames.length; 
  }

  public int getRowCount()
  {
    if(dataRows == null)
      return 0;
    else
      return dataRows.size();
  }

  public Object getValueAt(int row, int column)
  {
    return ((String[])(dataRows.elementAt(row)))[column]; 
  }

  public String getColumnName(int column)
  {
    return columnNames[column] == null ? "No Name" : columnNames[column];
  }
}