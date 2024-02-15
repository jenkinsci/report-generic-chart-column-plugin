// <![CDATA[
var genericChart_ids = document.getElementById('genericChart-ids').textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
if (genericChart_ids != null) {
    for (let i = 0; i < genericChart_ids.length; i++) {
        var id = genericChart_ids[i]
        if (id == null) {
            continue;
        }
        var data_title_element = document.getElementById('genericChart-title-'+id)
        if (data_title_element == null){
            continue;
        } else {
            var data_title = data_title_element.textContent.trim();
        }
        var data_builds_element = document.getElementById('genericChart-builds-'+id)
        if (data_builds_element == null){
            continue;
        } else {
        var data_builds = data_builds_element.textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        }
        var data_color_element = document.getElementById('genericChart-color-'+id)
        if (data_color_element == null){
            continue;
        } else {
        var data_color = data_color_element.textContent.trim();
        }
        var data_values_element = document.getElementById('genericChart-values-'+id)
        if (data_values_element == null){
            continue;
        } else {
            var data_values = data_values_element.textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        }

                      var allPerf = {
                        type: 'line',
                        data: {
                        labels: data_builds,
                                datasets: [
                                {
                                        label: data_title,
                                        fill: true,
                                        backgroundColor: data_color,
                                        borderColor: data_color,
                                        pointBorderColor: "#808080",
                                        pointHoverBackgroundColor: "#fff",
                                        pointHoverBorderColor: "rgba(0,0,0,1)",
                                        pointRadius: 5,
                                        data: data_values
                                }
                                ]
                        },
                        options: {
                          responsive: false,
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
                            window.open("" + buildId, "_blank");
                        }
                        }
                      };
                        var ctx = document.getElementById("perChartId"+i).getContext("2d");
                        perfChartJsCharts["perChartId"+i] = new Chart(ctx, allPerf);

        }
// ]]>
}