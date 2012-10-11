/*
This Script Generates Bar Charts ,Column Charts or Bubble Charts with aggregated values of age,attendance,debates etc by Grouping the data based on State,Political Parties,Educational Parties etc.
This is useful to visualize the data.
The variables for the histogram can be manually selected.
Data Source : https://docs.google.com/spreadsheet/ccc?key=0AuT-HkB3kCJydDNiT3owelg0ZHBLbmcyaXZsV2lFclE
*/
google.load('visualization','1.0',{'packages':['controls','table','corechart']});
var query; 
var chart;
var options;

google.setOnLoadCallback(f);

function f(){
	query= new google.visualization.Query('https://docs.google.com/spreadsheet/ccc?key=0AuT-HkB3kCJydDNiT3owelg0ZHBLbmcyaXZsV2lFclE');
	
	}
/*
This function reads the input from the user and converts it into suitable SQL Query amd Querries the Database
*/
function setQuery(x ,y,z,f){
	var msg = 'SELECT '+ x + ' , ' +  y  +  ' GROUP BY ' + x;//SQl Query Construction
	if (f == 'BarChart'){
		chart = new google.visualization.BarChart(document.getElementById('chart_div'));
		options = {width : 800,height : 800};
		}
	else if (f == 'ColumnChart'){
		chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));
		options = {width : 800,height : 800};
		} 
	else {
		chart = new google.visualization.BubbleChart(document.getElementById('chart_div'));
		var msg = 'SELECT '+ x + ' , ' +  y + ','+ z +  ' GROUP BY ' + x;
		options = {width : 800,height : 800, bubble: { textStyle: {fontSize: 8} }, sizeAxis : {minValue: 0,  maxSize: 8}
          	};
	}
	query.setQuery(msg);
	query.send(setDashboard);
	}
/* 
This function recieves the response from the database checks for errors in it and passes it to the chart object.
*/
function setDashboard(response){
	if (response.isError()) {
		alert('Error in query: ' + response.getMessage() + ' ' + response.getDetailedMessage());
		return;
	}

	var data = response.getDataTable();
	chart.draw(data,options);
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
	if (document.getElementById('query-1').value != "" && document.getElementById('query-2').value == ""){
		document.getElementById('query-3').disabled = true;document.getElementById('query-3').value = "";
		}
	if (document.getElementById('query-1').value != "" && document.getElementById('query-2').value != ""){
		document.getElementById('query-3').disabled = false;
		}
	if (document.getElementById('query-1').value != "" && document.getElementById('query-2').value != "" && document.getElementById('query-3').value == ""){
		document.getElementById('q-5').disabled = true;
		}
	if (document.getElementById('query-1').value != "" && document.getElementById('query-2').value != "" && document.getElementById('query-3').value != ""){
		document.getElementById('q-5').disabled = false; document.getElementById('q-5').value = "BubbleChart";
		}
	
	}
