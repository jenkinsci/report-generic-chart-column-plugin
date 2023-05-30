# jenkins-report-generic-chart-column on chartjs-api plugin V 1
## issue
 * Chart.js is not working whe protoype.js is loaded - https://github.com/chartjs/Chart.js/issues/11201
 * Jenkins is using prototype.js, but is going to remove it - https://issues.jenkins.io/browse/JENKINS-70906
## temporary workaround
 * While those issues are valid, this branch will be serving chart.js 1 library, to make depndent plugins usable.
 * This branch must be removed once one of those issues is resolved
 * the branch must not diverge from master in anythng else except chart.js 1 code
 * nothing from this branch can go to master
 * except chart.js 1 code, all changes must go from master only
