// <![CDATA[
var genericChart_ids = document.getElementById('genericChart-ids').textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
for (let i = 0; i < genericChart_ids.length; i++) {
        var id = genericChart_ids[i]
        var data_title = document.getElementById('genericChart-title-'+id).textContent.trim();
        var data_builds = document.getElementById('genericChart-builds-'+id).textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        var data_color = document.getElementById('genericChart-color-'+id).textContent.trim();
        var data_values = document.getElementById('genericChart-values-'+id).textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));

                      var allPerf = {
                        labels: data_builds,
                                datasets: [
                                {
                                        label: data_title,
                                        fillColor: data_color,
                                        strokeColor: data_color,
                                        pointBorderColor: "#808080",
                                        pointHighlightFill: "#fff",
                                        pointHighlightStroke: "rgba(0,0,0,1)",
                                        data: data_values
                                }
                                ]
                      };
                        var options = {
                            bezierCurve: false,
                            multiTooltipTemplate: "<%= datasetLabel + \": \" + value %>"
                        };
                        var ctx = document.getElementById("perChartId"+i).getContext("2d");
                        perfChartJsCharts["perChartId"+i] = new Chart(ctx).Line(allPerf, options);
                        document.getElementById("perChartId"+i).onclick = function (evt) {
                            var lid = event.target.id;
                            var jid = lid.replace("perChartId", "")
                            var activePoints = perfChartJsCharts[lid].getPointsAtEvent(evt);
                            var point = activePoints[0]
                            var datasetIndex = point.datasetIndex //labels are for all data together,  no need to look into exact dataset
                            var index = point.index
                            var result = point.label;
                            var buildId = result.substring(result.lastIndexOf(":") + 1)
                            window.open("" + buildId, "_blank");
                        };

        }
// ]]>