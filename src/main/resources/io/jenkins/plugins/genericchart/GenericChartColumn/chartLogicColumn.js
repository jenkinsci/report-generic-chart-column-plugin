// <![CDATA[
function readCharts() {
    var allChartIdElements = document.getElementsByClassName("genericChart-names")
    if (allChartIdElements == null) {
        return;
    }
    for (let i = 0; i < allChartIdElements.length; i++) {
        var keyAttribute = allChartIdElements[i].getAttribute("genericChart_processed")
        if (keyAttribute == null) {
            continue
        }
        if (keyAttribute != "false") {
            continue
        }
        allChartIdElements[i].setAttribute("genericChart_processed", "true")
        var id = allChartIdElements[i].textContent.trim();
        var data_title_element = document.getElementById('genericChart-title-'+id)
        if (data_title_element == null) {
            continue;
        } else {
            var data_title = data_title_element.textContent.trim();
        }
        var data_builds_element = document.getElementById('genericChart-builds-'+id)
        if (data_builds_element == null) {
            continue;
        } else {
            var data_builds = data_builds_element.textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        }
        var data_color_element = document.getElementById('genericChart-color-'+id)
        if (data_color_element == null) {
            continue;
        } else {
            var data_color = data_color_element.textContent.trim();
        }
        var data_values_element = document.getElementById('genericChart-values-'+id)
        if (data_values_element == null) {
            continue;
        } else {
            var data_values = data_values_element.textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        }
        var data_url_element = document.getElementById('genericChart-url-'+id)
        if (data_url_element == null) {
            continue;
        } else {
            var data_url = data_url_element.textContent.trim();
        }


                    if (typeof chartNameVar == 'undefined') {
                      var chartNameVar = {};
                    }

                  var dataPerfView = {
                    labels: data_builds,
                            datasets: [{
                                    label: data_title,
                                    fillColor: data_color,
                                    strokeColor: data_color,
                                    pointColor: data_color,
                                    pointStrokeColor: "#fff",
                                    pointHighlightFill: "#fff",
                                    pointHighlightStroke: data_color,
                                    data: data_values
                        }
                        ]
                  };
                    var options = {
                        url_from_job: data_url,
                        bezierCurve: false,
                        multiTooltipTemplate: "&lt;%= datasetLabel + \": \" + value %&gt;"
                    };
                    var ctx = document.getElementById(id+"-Chart").getContext("2d");
                    chartNameVar[id] = new Chart(ctx).Line(dataPerfView, options);
                    document.getElementById(id+"-Chart").onclick = function (evt) {
                        var lid = event.target.id;
                        var jid = lid.replace("-Chart", "")
                        var chart = chartNameVar[jid]
                        var activePoints = chart.getPointsAtEvent(evt);
                        var point = activePoints[0]
                        var datasetIndex = point.datasetIndex //labels are for all data together,  no need to look into exact dataset
                        var index = point.index
                        var result = point.label;
                        var buildId = result.substring(result.lastIndexOf(":") + 1)
                        window.open("/"+chart.options.url_from_job+buildId, "_blank");
                        //window.open("/${job.url}", "_blank");
                    };
        }
    }

window.addEventListener('load', readCharts, false);
// ]]>