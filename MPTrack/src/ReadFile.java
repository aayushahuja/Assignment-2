import java.util.*;
import java.applet.Applet;
import java.io.*;

import jxl.*;
import jxl.read.biff.BiffException;
import prefuse.*;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.util.ui.UILib;
import java.lang.*;

import javax.swing.JComponent;
import javax.swing.JFrame;
import prefuse.data.Graph;
import prefuse.data.Tree;

/**
 * This class reads the input data file, in Excel format, into a Prefuse Table.
 * Control enters this class from the 'run' method, which opens the input Excel file, 
 * creates a new table 'table', and issues calls to enter data into the table from the worksheet.
 * 
 */


	public class ReadFile extends Applet{
		public static Workbook workbook;	
		public static Sheet sheet ;
		public static int numberColumn;
		public static int numberRow;
		public static Table table;
		/**
		 * String getCellContents(int row , int col)
		 * Takes a row number and column number, and returns the data at the specified position in the input file as a String.
		 * 
		 */
		public static String getCellContents(int row , int col) {
			String value ="";
			Cell temp ;
			try{
			temp = sheet.getCell(col, row);
			value = temp.getContents();
			}
			catch(Exception e){
				System.out.println("Error reading cell contents");
			}
			return value ;
		}
		
		/**
		 * 
		 * Signature: Class getCellType(int row , int col)
		 * Takes a row number and column number, and returns the type of the data element at the specified position in the input file, as a Java Class.
		 * 
		 */
		private static Class getCellType(int row , int col) {
			Class c;
			String value ="";
			Cell temp ;
			CellType type;
			temp = sheet.getCell(col, row);
			type = temp.getType();
			value = type.toString();
			if(value.equals("Label")){
				value = "String";
				c=String.class;
			}
			else if(value.equals("Date")){
				c=Date.class;
			}
			else if(value.equals("Integer")){
				c=Integer.class;
			}
			else if(value.equals("Number")){
				c=Double.class;
			}
			else{
				c=Double.class;
			}
			return  c;
		}
		/**
		 * Signature: void addColumns()
		 * Takes no input. It determines the number of columns in the input file, and 
		 * adds columns to 'table' corresponding to each column in the input file.
		 * Returns no output.
		 */
		private static void addColumns(){
			int number = sheet.getColumns();
			
			try{
			for(int i=0;i<number;i++){
				table.addColumn(getCellContents(0, i) , getCellType(1, i));		
			}
			}
			catch(Exception e){
				System.out.println("Error reading columns . ");
			}
		}
		/**
		 * Signature: void addRow(int row)
		 * Takes a row number, and adds the data from the cells in that row from the input file, into a new row in 'table', 
		 * after some preprocessing (like removing the "%" sign in the attendance column).	
		 */
		private static void addRow(int row) {
			Class c ;
			for(int i=0;i<numberColumn;i++){
				c = table.getColumnType(i);
				String con ;
				int len;
				
				if(c.equals(Integer.class)){
					con = sheet.getCell(i, row).getContents();
					len = con.length();
					if(con.charAt(len-1)=='%'){
						con=con.substring(0, len-1);
						table.set(row, i, Integer.parseInt(con));
					}
				}
				if(c.equals(Double.class)){
					con = sheet.getCell(i, row).getContents();
					len = con.length();
					if(con.charAt(len-1)=='%'){
						con=con.substring(0, len-1);
						table.set(row, i, Double.parseDouble(con));
					}
					
				}
				else if(c.equals(String.class)){
					String con1 = sheet.getCell(i, row).getContents();
					table.set(row, i, con1);
				}
				else if(c.equals(Date.class)){
					table.set(row, i, Date.parse(sheet.getCell(i, row).getContents()));
				}
				else if(c.equals(Number.class)){
					table.set(row, i, Integer.parseInt(sheet.getCell(i, row).getContents()));
				}
				else{
					table.set(row, i, Double.parseDouble(sheet.getCell(i, row).getContents()));
					
				}
			}
		 
		}
		/**
		 * Signature: void addRows()
		 * Takes no input. It determines the number of rows in the input file. 
		 * For each row, a new empty row is added to the table 'table', and the method 
		 * 'addRow' is invoked to populate the cells of the row.
		 */
		private static void addRows(){
			int number = sheet.getRows();
			for(int i=1;i<number;i++){
				table.addRow();
				try{
					addRow(i);
				}
				catch(Exception e){
					System.out.println("Error adding rows to the table .");
				}
			}
		}
		
		/**
		 * This is the main function in this file that controls the flow of the execution
		 */
		public static void run(){
			try {
				workbook = Workbook.getWorkbook(new File("data/mp.xls"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			sheet = workbook.getSheet(0);
			numberColumn = sheet.getColumns();
			numberRow = sheet.getRows();
			table = new Table(numberRow , numberColumn);
			addColumns();
			addRows();
		}
	}