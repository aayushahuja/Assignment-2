import java.util.Date;
import prefuse.data.Table;

/**
 * This class declares a datatype representing a member of parliament, containing all the details of an MP.
 *
 */
public class MP {
	String name;
	boolean isElected;
	Date startOfTerm;
	Date endOfTerm;
	String constituency;
	boolean isMale;
	String educQual;
	String educQualDetails;
	int age;
	Integer debates;
	Integer bills;	
	Integer questions;
	Integer attendence;
	
	public String getName() {
		return name;
	}
	
	public MP(Table t,int row) {
		name=t.getString(row, 0);
		isElected = (t.getString(row,1).equals("Elected")) ? true : false;
		startOfTerm=t.getDate(row, 2);
		
		if(t.getString(row,3).equals("In office")) {
			endOfTerm=null;
		}
		else {
			String[] ddmmyyyy = t.getString(row,3).split("-"); 
			endOfTerm=new Date(Integer.parseInt(ddmmyyyy[0]),Integer.parseInt(ddmmyyyy[1]),Integer.parseInt(ddmmyyyy[2]));
		}
		constituency=t.getString(row, 5);
		isMale = (t.getString(row, 7).equals("Male")) ? true : false;
		educQual=t.getString(row, 8);
		educQualDetails=t.getString(row, 9);
		age=(int)Double.parseDouble(t.getString(row, 10));
		try {
			debates=(int)Double.parseDouble(t.getString(row, 11));
		}
		catch(NullPointerException e) {
			debates=null;
		}
		try {
			bills=(int)Double.parseDouble(t.getString(row, 12));
		}
		catch(NullPointerException e) {
			bills=null;
		}
		try {
			questions=(int)Double.parseDouble(t.getString(row, 13));
		}
		catch(NullPointerException e) {
			questions=null;
		}
		try {
			attendence=(int)Double.parseDouble(t.getString(row, 14));
		}
		catch(NullPointerException e) {
			attendence=null;
		}
	}
}