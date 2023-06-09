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
                    type: 'line',
                    url_from_job: data_url,
                    data: {
                    labels: data_builds,
                            datasets: [{
                                    label: data_title,
                                    fill: true,
                                    backgroundColor: data_color,
                                    borderColor: data_color,
                                    pointBackgroundColor: data_color,
                                    pointBorderColor: "#fff",
                                    pointHoverBackgroundColor: "#fff",
                                    pointHoverBorderColor: data_color,
                                    pointRadius: 4,
                                    data: data_values
                        }
                        ]
                    },
                    options: {
                      plugins: {
                        legend: { display: false }
                      },
                      interaction: {
                         mode: 'index',
                         intersect: false
                      },
                        onClick: (e) => {
                            var chart = e.chart;
                            var activePoints = chart.getElementsAtEventForMode(e, 'index', { intersect: false }, true);
                            var point = activePoints[0]
                            var datasetIndex = point.datasetIndex //labels are for all data together,  no need to look into exact dataset
                            var index = point.index
                            var result = chart.config.data.labels[index]
                            var buildId = result.substring(result.lastIndexOf(":") + 1)
                            window.open("/"+chart.config._config.url_from_job+buildId, "_blank");
                        }
                    }
                  };
                    var ctx = document.getElementById(id+"-Chart").getContext("2d");
                    chartNameVar[id] = new Chart(ctx, dataPerfView)
        }
    }

window.addEventListener('load', readCharts, false);
// ]]>