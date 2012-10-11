public class Main {

	/**
	 * Contains the 'main' method. It issues calls to various methods to perform the following functions:
	 *- Read data from input file
	 *- Create a visualisation of list of states
	 *- Create a tree-map of the data
	 *- Create a mashup for the data
	 */
	public static void main(String[] args) {
		ReadFile.run();
		StateMenu.run();
		TreeMap.run();
		MakeSummaryTable.make();
		Mashup.display(MakeSummaryTable.summary);
	}

}
