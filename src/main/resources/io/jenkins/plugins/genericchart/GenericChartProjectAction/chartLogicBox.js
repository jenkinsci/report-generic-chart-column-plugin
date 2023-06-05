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
}