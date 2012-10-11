/*
This Script Generates Bar Charts or Column Charts With the aggregate data of Age,attendance , debates etc Grouped by State,Political Parties and Educational Qualification.
The Values from the Grouped data can be Manually changed so the user can compare trends in various States,Political Parties or Educational Qualification.
Data Source : https://docs.google.com/spreadsheet/ccc?key=0AuT-HkB3kCJydDNiT3owelg0ZHBLbmcyaXZsV2lFclE
*/
google.load('visualization','1.0',{'packages':['controls','table','corechart']});
var query;
var chart;
var options;
var filter;
var visual;
google.setOnLoadCallback(f);

function f(){
	query= new google.visualization.Query('https://docs.google.com/spreadsheet/ccc?key=0AuT-HkB3kCJydDNiT3owelg0ZHBLbmcyaXZsV2lFclE');
	
	}
/*
This function reads the input from the user and converts it into suitable SQL Query amd Querries the Database
*/
function setQuery(x,y,f){
	var msg = 'SELECT '+ x + ' , ' +  y  +  ' GROUP BY ' + x;
	if (x == "E"){
		filter = "State";
	}
	else if(x == "G" ){
		filter = "Political party"
	}
	else if(x == "I" ){
		filter = "Educational qualifications"
	}
	else if(x == "H" ){
		filter = "Gender"
	}
	visual = f;
	query.setQuery(msg);
	// Send the query with a callback function.
	query.send(setDashboard);
	}
/* 
This function recieves the response from the database checks for errors in it and passes it to a Dashboard which has options to Modify the range of the data returned using the category filter.
The Dashboard binds the data to various display objects(Chart Wrapper) and the input to the display object can be modified using the  controlwrapper 
*/
function setDashboard(response){
	if (response.isError()) {
		alert('Error in query: ' + response.getMessage() + ' ' + response.getDetailedMessage());
		return;
	}
	var data = response.getDataTable();
	var dashboard = new google.visualization.Dashboard(document.getElementById('dashboard_div'));
	var stateselector = new google.visualization.ControlWrapper({
	    'controlType': 'CategoryFilter',
	    'containerId': 'filter_div',
	    'options': {
	      'filterColumnLabel': filter
	    }
	  });
	var chart  = new google.visualization.ChartWrapper({
	    'chartType': visual,
	    'containerId': 'chart_div',
	    'options': {
	      'width': 500,
	      'height': 500,
	    },
	    'view': {'columns': [0, 1]}
	  });
	var stateselector2 = new google.visualization.ControlWrapper({
	    'controlType': 'CategoryFilter',
	    'containerId': 'filter_div2',
	    'options': {
	      'filterColumnLabel': filter
	    }
	  });
	var chart2  = new google.visualization.ChartWrapper({
	    'chartType': visual,
	    'containerId': 'chart_div2',
	    'options': {
	      'width': 500,
	      'height': 500,
	    },
	    'view': {'columns': [0, 1]}
	  });
	dashboard.bind(stateselector,chart);
	dashboard.bind(stateselector2,chart2);

	dashboard.draw(data, null);
	}
/*
This function Validate the data as the user changes the input parameters and disables or enables the fields accordingly.
*/
function Validate(){
	if (document.getElementById('query-1').value == ""){
		document.getElementById('query-2').disabled = true;document.getElementById('query-3').disabled = true;
		document.getElementById('query-2').value = "";document.getElementById('query-3').value = "";
		}
	if (document.getElementById('query-1').value != ""){
		document.getElementById('query-2').disabled = false;
		}
	}

