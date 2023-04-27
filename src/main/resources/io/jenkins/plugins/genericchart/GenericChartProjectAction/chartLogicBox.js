// <![CDATA[
var genericChart_ids = document.getElementById('genericChart-ids').textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
for (let i = 0; i < genericChart_ids.length; i++) {
        var id = genericChart_ids[i]
        var data_title = document.getElementById('genericChart-title-'+id).textContent.trim();
        var data_builds = document.getElementById('genericChart-builds-'+id).textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        var data_color = document.getElementById('genericChart-color-'+id).textContent.trim();
        var data_values = document.getElementById('genericChart-values-'+id).textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));

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
                          plugins: {
                            legend: { display: false }
                          },
                          interaction: {
                            mode: 'index',
                            intersect: false
                          },
                          onClick: (e) => {
                            var activePoints = perfChartJsCharts["perChartId"+i].getElementsAtEventForMode(e, 'index', { intersect: false }, true);
                            var point = activePoints[0]
                            var datasetIndex = point.datasetIndex //labels are for all data together,  no need to look into exact dataset
                            var index = point.index
                            var result = perfChartJsCharts["perChartId"+i].config.data.labels[index]
                            var buildId = result.substring(result.lastIndexOf(":") + 1)
                            window.open("" + buildId, "_blank");
                        }
                        }
                      };
                        var ctx = document.getElementById("perChartId"+i).getContext("2d");
                        perfChartJsCharts["perChartId"+i] = new Chart(ctx, allPerf);

        }
// ]]>