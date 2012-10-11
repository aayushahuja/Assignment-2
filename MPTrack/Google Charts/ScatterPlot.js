/*
This Script Generates a Scatter Plot With the variables to choose from Age,Attendance,Debates etc. to Visualise the trends in the data as a whole without aggregating on any parameter
The Scatter Plot Data Ranges can be manually modified using a slider to Select Ranges for the above parameter 
Data Source : https://docs.google.com/spreadsheet/ccc?key=0AuT-HkB3kCJydDNiT3owelg0ZHBLbmcyaXZsV2lFclE
*/


google.load('visualization','1.0',{'packages':['controls','table','corechart']});
var query;
var chart;
var options;
var filterx;
var filtery;
var x0;
var x1;
var y0;
var y1;

google.setOnLoadCallback(f);

function f(){
	query= new google.visualization.Query('https://docs.google.com/spreadsheet/ccc?key=0AuT-HkB3kCJydDNiT3owelg0ZHBLbmcyaXZsV2lFclE');
	
	}
/*
This function reads the input from the user and converts it into suitable SQL Query and querries the database
*/
function setQuery(x ,y){
	var msg = 'SELECT '+ x + ',' +  y;//Generating SQL Query
	chart = new google.visualization.ScatterChart(document.getElementById('chart_div'));//chart variable of type Scatter Chart
	
	if (x == "K"){
		filterx = "Age";
	}
	else if(x == "O" ){
		filterx = "Attendance"
	}
	else if(x == "L" ){
		filterx = "Debates"
	}
	else if(x == "N" ){
		filterx = "Questions"
	}
	else if(x == "M" ){
		filterx = "Private Member Bills"
	}

	if (y == "K"){
		filtery = "Age";
	}
	else if(y == "O" ){
		filtery = "Attendance"
	}
	else if(y == "L" ){
		filtery = "Debates"
	}
	else if(y == "N" ){
		filtery = "Questions"
	}
	else if(y == "M" ){
		filtery = "Private Member Bills"
	}
	options = {width : 1000,height : 1000,legend :'none',title: filterx + ' vs '+ filtery ,hAxis: {title: filterx},vAxis: {title: filtery}};
	
	query.setQuery(msg);
	// Send the query with a callback function.
	query.send(setDashboard);
	}
/* 
This function recieves the response from the database checks for errors in it and passes it to a Dashboard which has options to Modify the range of the data returned using the slider function.
The Dashboard binds the data to various display objects(Chart Wrapper) and the input to the display object can be modified using the  controlwrapper 
*/
function setDashboard(response){
	if (response.isError()) {
		alert('Error in query: ' + response.getMessage() + ' ' + response.getDetailedMessage());
		return;
	}
	var data = response.getDataTable();
	x0 = data.getColumnRange(0).min;
	y0 = data.getColumnRange(1).min
	x1 = data.getColumnRange(0).max
	y1 = data.getColumnRange(1).max
	var dashboard = new google.visualization.Dashboard(document.getElementById('dashboard_div'));
	var stateselector = new google.visualization.ControlWrapper({
	    'controlType': 'NumberRangeFilter',
	    'containerId': 'filter_div',
	    'options': {
	      'filterColumnLabel': filterx
	    }
	  });
	var stateselector1 = new google.visualization.ControlWrapper({
	    'controlType': 'NumberRangeFilter',
	    'containerId': 'filter_div1',
	    'options': {
	      'filterColumnLabel': filtery
	    }
	  });
	var chart  = new google.visualization.ChartWrapper({
	    'chartType': 'ScatterChart',
	    'containerId': 'chart_div',
	    'options': {
	      'width': 800,
	      'height': 800,
	      'legend' :'none','title': filterx + ' vs '+ filtery ,
		'hAxis': {'title': filterx , 'minValue': x0, 'maxValue': x1},
		'vAxis': {'title': filtery , 'minValue': y0, 'maxValue': y1},
	    },
	    'view': {'columns': [0, 1]}
	  });

	dashboard.bind([stateselector,stateselector1],chart);
	dashboard.draw(data, null);
	}


