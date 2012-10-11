/*
This Script Generates Bar Charts,Column Charts or Pie Charts With the data Grouped by State,Political Parties and Educational Qualification.
The Values from the Grouped data can be Manually changed so the user can visualise the data of various States,Political Parties or Educational Qualification Grouped by States,Political Parties or Educational Qualification.
Data Source : https://docs.google.com/spreadsheet/ccc?key=0AuT-HkB3kCJydDNiT3owelg0ZHBLbmcyaXZsV2lFclE
*/
var query;
var filter;
var visual;
google.load('visualization','1.0',{'packages':['controls','table']});
google.setOnLoadCallback(f);

function f(){
	query = new google.visualization.Query('https://docs.google.com/spreadsheet/ccc?key=0AuT-HkB3kCJydDNiT3owelg0ZHBLbmcyaXZsV2lFclE');
}
/*
This function reads the input from the user and converts it into suitable SQL Query amd Querries the Database
*/
function setQuery(x,y,z){
	var msg = 'SELECT ' + x + ' , '+ y +' , COUNT(A) GROUP BY ' +x+' , '+y;
	query.setQuery(msg);
	if (x == "E"){
		filter = "State";
	}
	else if(x == "G" ){
		filter = "Political party"
	}
	else if(x == "I" ){
		filter = "Educational qualifications"
	}
	visual = z;
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
      'width': 600,
      'height': 600,
    },
    'view': {'columns': [1, 2]}
  });
dashboard.bind(stateselector,chart);
dashboard.draw(data, null);
}
/*
This function Validate the data as the user changes the input parameters and disables or enables the fields accordingly.
*/
function Validate(){
if(document.getElementById("s-1").value == ""){
	document.getElementById("s-2").disabled = true;document.getElementById("s-2").value = "";
	}
if(document.getElementById("s-1").value != ""){
	document.getElementById("s-2").disabled = false;
	if(document.getElementById("s-1").value == "E"){document.getElementById("O-1").disabled = true;document.getElementById("O-2").disabled = false;document.getElementById("O-3").disabled = false;}
	if(document.getElementById("s-1").value == "G"){document.getElementById("O-1").disabled = false;document.getElementById("O-2").disabled = true;document.getElementById("O-3").disabled = false;}
	if(document.getElementById("s-1").value == "I"){document.getElementById("O-1").disabled = false;document.getElementById("O-2").disabled = false;document.getElementById("O-3").disabled = true;}
	}
if(document.getElementById("s-2").value == ""){
	document.getElementById("b-1").disabled = true;
	}
if(document.getElementById("s-2").value != ""){
	document.getElementById("b-1").disabled = false;
	}

}
