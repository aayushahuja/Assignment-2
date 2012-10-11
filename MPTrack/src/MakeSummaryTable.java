import jxl.Cell;
import jxl.Sheet;
import prefuse.data.Table;
import prefuse.data.Tuple;

/**
 * This class reads the table created after reading the input file, 
 * and constructs a new table that contains the aggregate values of various attributes grouped by states and 
 * political parties.
 * Control enters this class from the 'make' method.
 *
 */
public class MakeSummaryTable {
	public static Table summary;
	public static Sheet sheet ;
	public static int numberColumn;
	public static int numberRow;

	/**
	 * Signature: void putColumns()
	 * Adds columns to the summary table created with their names .
	 * 
	 */
	public static void putColumns(){
		summary.addColumn("State", String.class);
		summary.addColumn("Party", String.class);
		summary.addColumn("StateIndex", Integer.class);
		summary.addColumn("Attendance", Integer.class);
		summary.addColumn("Questions", Integer.class);
		summary.addColumn("Age", Integer.class);
		summary.addColumn("NumberOfEntries", Integer.class);
	}
	
	/*
	 * Signature: void addRowToTable(String state, String party, Integer attendance, Integer questions, Integer age)
	 * Takes the values of various attributes as read from 'table', and updates the row of the summary table with the 
	 * same StateName and PartyName, by adding the passed values to the current values in the table.
	 * If a match is not found , it adds a new row to the summary table 
	 */
	public static void addRowToTable(String state ,String party,Integer attendance,	Integer questions,	Integer age){
		int numberOfRow = summary.getRowCount();
		String state1;
		String party1;
		Integer attendance1;
		Integer questions1;
		Integer age1;
		int number1 ;
		boolean flag = false;
		for(int i = 0 ; i < numberOfRow ; i++){
			state1 = summary.getString(i, 0);
			party1 = summary.getString(i, 1);
			try {
				attendance1 = Integer.parseInt(summary.getString(i, 3));
			}
			catch(NumberFormatException e) {
				attendance1=null;
			}
			try {
				questions1 = Integer.parseInt(summary.getString(i, 4));
			}
			catch(NumberFormatException e) {
				questions1=null;
			}
			try {
				age1 = Integer.parseInt(summary.getString(i, 4));
			}
			catch(NumberFormatException e) {
				age1=null;
			}
			number1 = Integer.parseInt(summary.getString(i, 6));
			if(state1.equals(state) && party1.equals(party)){
				flag = true;
				boolean isNull = false;
				try {
					summary.set(i, 3, attendance1+attendance);
				}
				catch(NullPointerException e) {
					summary.set(i,3,attendance1);
					isNull=true;
				}
				try {
					summary.set(i, 3, questions1+questions);
				}
				catch(NullPointerException e) {
					summary.set(i,3,questions1);
					isNull = true;
				}
				try {
					summary.set(i, 3, age1+age);
				}
				catch(NullPointerException e) {
					summary.set(i,3,age1);
					isNull = true;
				}
				if(!isNull)
					summary.set(i, 6, number1+1);
			}
		}
		if(flag==false){
			summary.addRow();
			int i = summary.getMaximumRow();
			summary.set(i, 0, state);
			summary.set(i, 1, party);
			summary.set(i, 3, attendance);
			summary.set(i, 4, questions);
			summary.set(i, 5, age);
			summary.set(i, 6, 1);
		}
	}
	
	/**
	 * Signature: void addRows()
	 * Reads each row of 'table', and invokes the function addRowToTable().
	 * 
	 */
	public static void addRows(){
		String state ;
		String party;
		Integer attendance;
		Integer questions;
		Integer age;
		int temp ;
		for(int i = 1;i<numberRow ; i++){
			state = ReadFile.getCellContents(i, 4);
			party = ReadFile.getCellContents(i, 6);
			temp = ReadFile.getCellContents(i, 14).length();
			if(!ReadFile.getCellContents(i, 14).substring(0,temp).equals("N/A")){
				attendance = Integer.parseInt(ReadFile.getCellContents(i, 14).substring(0,temp-1));
			}
			else {
				attendance = null;
			}
			temp = ReadFile.getCellContents(i, 13).length();
			if(!ReadFile.getCellContents(i, 13).substring(0,temp).equals("N/A")){
				questions = Integer.parseInt(ReadFile.getCellContents(i, 13));
			}
			else{
				questions=null;
			}
			temp = ReadFile.getCellContents(i, 10).length();
			if(!ReadFile.getCellContents(i, 10).substring(0,temp).equals("N/A")){
				age = Integer.parseInt(ReadFile.getCellContents(i, 10));
			}
			else{
				age = null;
			}
			addRowToTable(state, party, attendance, questions, age);
		}
	}
	
	/**
	 *Signature: void addRow(String state, String party, int stateIndex)
	 *Takes the values of StateName, PartyName and StateIndex. 
	 *Adds a row to the summary table with the values of other aggregate attributes initialised to zero.
	 * 
	 */
	public static void addRow(String state, String party, int stateIndex) {
		int i=summary.addRow();
		summary.set(i, 0, state);
		summary.set(i, 1, party);
		summary.set(i, 2, stateIndex);
		summary.set(i, 3, 0);
		summary.set(i, 4, 0);
		summary.set(i, 5, 0);
		summary.set(i, 6, 0);
	}
	
	/**
	 * Signature: Table removeNullRows()
	 * Creates a new table from the summary table, after filtering out the rows for which the aggregate attributes 
	 * have a value of zero.
	 *  
	 */
	public static Table removeNullRows() {
		Table tbl = new Table();
		tbl.addColumn("State", String.class);
		tbl.addColumn("Party", String.class);
		tbl.addColumn("StateIndex", Integer.class);
		tbl.addColumn("Attendance", Integer.class);
		tbl.addColumn("Questions", Integer.class);
		tbl.addColumn("Age", Integer.class);
		tbl.addColumn("NumberOfEntries", Integer.class);
		
		for(int i=1;i<summary.getRowCount();i++) {
			Tuple row=summary.getTuple(i);
			if(!summary.getString(i,6).equals("0")) {
				int r=tbl.addRow();
				tbl.set(r, 0, summary.getString(i, 0));
				tbl.set(r, 1, summary.getString(i, 1));
				if(summary.getString(i, 2)==null){
					//tbl.set(r, 2, 35);
				}
				else{
					tbl.set(r, 2, Integer.parseInt(summary.getString(i, 2)));
				}
				tbl.set(r, 3, Integer.parseInt(summary.getString(i, 3)));
				tbl.set(r, 4, Integer.parseInt(summary.getString(i, 4)));
				tbl.set(r, 5, Integer.parseInt(summary.getString(i, 5)));
				tbl.set(r, 6, Integer.parseInt(summary.getString(i, 6)));
			}
		} 
		return tbl;
	}
	
	/**
	 * This function controls the main program flow in this file
	 */
	public static void make(){
		String[] statesListDec = {
				"Uttar Pradesh",
				"Maharashtra",
				"West Bengal",
				"Andhra Pradesh",
				"Bihar",
				"Tamil Nadu",
				"Karnataka",
				"Madhya Pradesh",
				"Gujarat",
				"Rajasthan",
				"Orissa",
				"Kerala",
				"Jharkhand",
				"Assam",
				"Punjab",
				"Chhattisgarh",
				"Haryana",
				"Delhi",
				"Jammu and Kashmir",
				"Uttarakhand",
				"Himachal Pradesh",
				"Arunachal Pradesh",
				"Goa",
				"Manipur",
				"Meghalaya",
				"Tripura",
				"Andaman and Nicobar",
				"Chandigarh",
				"Dadra and Nagar Haveli",
				"Daman and Diu",
				"Lakshadweep",
				"Mizoram",
				"Nagaland",
				"Puducherry",
				"Sikkim"
				};
		String[] partiesList = {
				"All India Anna Dravida Munnetra Kazhagam",
				"All India Forward Bloc",
				"All India Majlis-E-Ittehadul Muslimmen",
				"All India Trinamool Congress",
				"Asom Gana Parishad",
				"Assam United Democratic Front",
				"Bahujan Samaj Party",
				"Bahujan Vikas Aaghadi",
				"Bharatiya Janata Party",
				"Biju Janata Dal",
				"Bodoland People's Front",
				"Communist Party of India",
				"Communist Party of India (Marxist)",
				"Dravida Munnetra Kazhagam",
				"Haryana Janhit Congress",
				"Independent",
				"Indian National Congress",
				"Jammu and Kashmir National Conference",
				"Janata Dal (Secular)",
				"Janata Dal (United)",
				"Jharkhand Mukti Morcha",
				"Jharkhand Vikas Morcha (Prajatantrik)",
				"Kerala Congress (M)",
				"Marumalarchi Dravida Munnetra Kazhagam",
				"Muslim League Kerala State Committee",
				"Nagaland People's Front",
				"Nationalist Congress Party",
				"Rashtriya Janata Dal",
				"Rashtriya Lok Dal",
				"Revolutionary Socialist Party",
				"Samajwadi Party",
				"Shiromani Akali Dal",
				"Shiv Sena",
				"Sikkim Democratic Front",
				"Swabhimani Paksha",
				"Telangana Rashtra Samithi",
				"Telugu Desam Party",
				"Viduthalai Chiruthaigal Katchi",
				"Yuvajana Sramika Rythu Congress Party",
		};

		summary = new Table();
		sheet = ReadFile.sheet;
		numberColumn = ReadFile.numberColumn;
		numberRow = ReadFile.numberRow;
		putColumns();
		for(int i=0;i<statesListDec.length;i++)
			for(int j=0;j<partiesList.length;j++)
				addRow(statesListDec[i],partiesList[j],i+1);
		addRows();
		summary=removeNullRows();
	} 
}
