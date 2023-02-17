# jenkins-report-generic-chart-column
Generic reusable plugin that will show a chart in column based on properties file.

The plugin reads properties file in your archive, specified by glob, and use one value, deffined by key, to draw a chart for both project and view. The plugin was orriginally designed to show results of benchmarks, but canbe missused for anything key-number what destire chart. Eg total and failde tests summaries. The graph is scaled, so you will never miss smallest change.

* [Properties file](#properties-file)
* [Project summary](#project-summary)
* [View summary](#view-summary)
* [Changing build result](#changing-build-result)
  * [Testing the expressions](#testing-the-expression)
  * [Most common expressions](#most-common-expressions)
* [Blacklist and Whitelist](#blacklist-and-whitelist)
* [Project Settings](#project-settings)
* [View Settings](#view-settings)
* [Limitations](#limitations)
* [Future work](#future-work)

## Properties file
To make plugin work, you need a [properties](https://en.wikipedia.org/wiki/.properties) file with results form your job, archived. The properties  file is eg our:
```
lastSuccessfulBuild/artifact/jbb-report/result/specjbb2015-C-20180717-00001/report-00001/specjbb2015-C-20180717-00001.raw 
```
```
# garbage
jbb2015.result.metric.max-jOPS = 22523
jbb2015.result.metric.critical-jOPS = 8902
jbb2015.result.SLA-10000-jOPS = 4774
jbb2015.result.SLA-25000-jOPS = 7442
jbb2015.result.SLA-50000-jOPS = 9643
jbb2015.result.SLA-75000-jOPS = 11833
jbb2015.result.SLA-100000-jOPS = 13791
other garbage
```
The parser is quite forgiving, and will skip garabge. Supports both : and = delimiters.

## Project summary
Hugest graphs are shown in project sumamry.  You can have as much graphs as you wish, and have detailed tooltip:
![project](https://user-images.githubusercontent.com/2904395/43015881-2747cb3a-8c51-11e8-9ccf-c6b4a0189e61.png)
Comparing individual jobs was never more simple:)

## View summary
You can include the graphs to the view:
![view](https://user-images.githubusercontent.com/2904395/43015883-278a339e-8c51-11e8-8656-5165b455d8ef.png)
Comparing individual projects was never more simple:)

You can of course mix it with other propertis or other plugins
![view](https://user-images.githubusercontent.com/2904395/43015875-21c739fc-8c51-11e8-9026-c84127628634.png)

The results in view are sortable - they are sort by last valid result shown in chart.

Comparing individual projects was never ever more simple:)

## Changing build result
Each chart (there can be several by project) can have its own  condition, on which result it can turn the build to unstable, if the condition is met.
The mathematic part is handled by https://github.com/gbenroscience/ParserNG/, the logic part is internal. Examplar expression:
```
avg(..L1)*1.1 <  L0 | L1*1.3 <  L0
```
The expression can be read as: If value of key in last build is bigger then avarage value of all builds before multiplied by 1.1 , or  the last build is bigger then previous build multiplied by 1.3, turn the build to unstable

You can read how it is evaluated here: https://github.com/judovana/jenkins-report-generic-chart-column/blob/master/src/main/resources/hudson/plugins/report/genericchart/ChartModel/help-unstableCondition.html#L10

The built which just ended is L0.  Previous build is L1 and so on... You can use ranges - eg L5..L1 will return values of given **key** for build N-5,N-4-N-3,N-2,N-1  where N is current build - L0.  Ranges can go withot limit - eg L3..  will exapnd as L3,L2,L1,L0. So obviously mmost used is ..L1 which returns you values  of all except latests (L0) build. Count of points is MN.

The L indexes, can be calcualted. To do so, use `L{expression}`. Eg L{MN/2} upon 1,2,3, will expand as L{3/2} -> L{1,5} -> L1 -> 2. The brackets can be  cumulative (eg L{{1+1}}) and can contain Other L or MN. eg L{L0}. The only reason why this was created was to beable wrote ..L{MN/2} and L{MN/2}..

See the logic at: https://github.com/judovana/jenkins-report-generic-chart-column/blob/master/src/main/resources/hudson/plugins/report/genericchart/ChartModel/help-unstableCondition.html#L2

While this work is based on ParserNG, and is slowly contributing functions, LogicalParser and also in future the Expanding parser, once it will be all finishied, the expressions will be possible to be tested directly in ParserNG.
But that is slow process. Now, the Expanding (and thus also Logical) parsers can be teste donly via this plugin, thus main method was added. Try:
```
VALUES_PNG="1 2 3" java  -cp jenkins-report-generic-chart-column.jar:parser-ng-0.1.8.jar  hudson/plugins/report/genericchart/math/ExpandingExpression "sum(..L0) < avg(..L0)"
```
or
```
VALUES_PNG="1 2 3" java  -cp parser-ng-0.1.8.jar:jenkins-report-generic-chart-column.jar  parser.ExpandingExpression  "avg(..L{MN/2}) < avg(L{MN/2}..)"
```

### testing the expression
The jar have a main method, which allows you to test the equations:
```
VALUES_PNG="1 2 3" java  -cp jenkins-report-generic-chart-column.jar:parser-ng-0.1.8.jar  hudson/plugins/report/genericchart/math/ExpandingExpression "sum(..L0) < avg(..L0)"
```
All changes were  moved to ParserNG, includig the `VALUES_PNG` variable. [ParserNG have powerfull CLI](https://github.com/gbenroscience/ParserNG#using-parserng-as-commandline-tool) and since `0.1.9` this expanding parser is here, so you canrun it simply as java -jar:
```
VALUES_PNG='235000 232500 233000 236000 210000'  parser-ng-0.1.9.jar -e " echo(L{MN}..L0) " 
```
or via its interactive CLI
```
$ VALUES_PNG='235000 232500 233000 236000 210000'  java -jar parser-ng-0.1.9.jar -e -i
```
<details> <summary>Output</summary>

 ```
Welcome To ParserNG Command Line
Math Question 1:
______________________________________________________
 echo(L0..L{MN})
Answer
______________________________________________________
210000 236000 233000 232500 235000
Math Question 2:
______________________________________________________
 echo(L{MN}..L0)
Answer
______________________________________________________
235000 232500 233000 236000 210000
```
</details>

```
VALUES_PNG='235000 232500 233000 236000 210000'  java -jar parser-ng-0.1.9.jar -e -i -v
```
<details> <summary>Output</summary>

 ```
Welcome To ParserNG Command Line

Math Question 1:
______________________________________________________
L1<L2
L1<L2
Expression : L1<L2
Upon       : 235000,232500,233000,236000,210000
As         : Ln...L1,L0
MN         = 5
Expanded as: 236000<233000
236000<233000
  brackets: 236000<233000
      evaluating logical: 236000<233000
        evaluating comparison: 236000<233000
          evaluating math: 236000
          is: 236000
          evaluating math: 233000
          is: 233000
        ... 236000 < 233000
        is: false
      is: false
  false
is: false
Answer
______________________________________________________
```
</details>
 
### Most common expressions
#### Divergence from exact pivot
If something should be soem exact result, or mus tnot be an exact result is most easy usage
 * `L0 == 5` if last result is 5, then the job willbecome unstable
 * `L0 != 5` whcih is same as
 * `![L0 == 5]` if last result is NOT 5, then the job will become unstable
#### Immediate regression:
 * `treshold=5;-1*(L1/(L0/100)-100) < -treshold` which is same as
 * `treshold=5;   (L1/(L0/100)-100) >  treshold` for classical benchamrk, where more is better. The treshold is how much % is maximal drop it can bear, and
 * `treshold=5;   (L1/(L0/100)-100) < -treshold` for eg time-based or size benchmark, where less is better. The treshold is how much % is maximal increase it can bear.
 * For stable things 5% should be the biggest regression rate. For  unstable once usually 10% is ok to cover usual oscialtion 
 * Note, that those equation works fine for boith big numbers and small numbers
#### Longer term regression
 * In basic comarsion, you can compare any Lx with any Ly. Eg `(L2/(L0/100)-100) > treshold` or `(L{MN}/(L0/100)-100) > treshold` and so on. 
   * The underlying evaluation is lenient, and eg L4 in size in set of twonumbers, will have simply value of **last valid** (second in this case) number.
   * Thats also why `L{MN}` works, although you shouldbe explicitly writing `L{MN-1}`
   * example: ` VALUES_PNG='3 2 1'  java -jar parser-ng-0.1.9.jar -e  "echo(L5,L6,L7)"` will give you `3 3 3`
 * another,more generic solution, may achieved by simply extension of [Immediate regression](#immediate-regression), only `L0` will be replaced bysomething likje `L0..L{MN/2}` (newer half of the set) and L1 by `L{MN/2}..L{MN}` (older half of the set)
 * You can then call `avg` or `avgN` functions above it or `geom` or `geomN` if you have to diverse data with huge trehsolds. See parserNG help for descriptions of functions (you can type `help` also to the jenkins settings for this equation)
    * `treshold=5;-1*(avg(L{MN/2}..L{MN})/(avg(L0..L{MN/2})/100)-100) < -treshold` which is same as 
    * `treshold=5;   (avg(L{MN/2}..L{MN})/(avg(L0..L{MN/2})/100)-100) >  treshold` for classical benchamrk, where more is better. The treshold is how much % is maximal drop it can bear<br/>
    * `treshold=5;   (avg(L{MN/2}..L{MN})/(avg(L0..L{MN/2})/100)-100) < -treshold` for eg time-based or size benchmark, where less is better. The treshold is how much % is maximal increase it can bear.
#### Gluing it all together
You usually have more expressions which are catching your regressions, to connect them, you can use logical operators:
```
java -jar parser-ng-0.1.9.jar -l   "help"
Comparing operators - allowed with spaces:!=, ==, >=, <=, <, >; not allowed with spaces:le, ge, lt, gt, 
Logical operators - allowed with spaces:, |, &; not allowed with spaces:impl, xor, imp, eq, or, and
As Mathematical parts are using () as brackets, Logical parts must be grouped by [] 
 ```
 Eg:
  * for clasical benchamrks like socre:
 ```
 treshold=5;-1*(L1/(L0/100)-100) < -treshold || treshold=5;-1*(avg(L{MN/2}..L{MN})/(avg(L0..L{MN/2})/100)-100) < -treshold
 ```
  * for inverted benchmarks like time or size
 ```
 treshold=5;   (L1/(L0/100)-100) < -treshold || treshold=5;   (avg(L{MN/2}..L{MN})/(avg(L0..L{MN/2})/100)-100) < -treshold
 ```
Note, that if the variables (eg my treshold above) are filled as they come to hend. If you set it in first logical half, it can be reused in second without declaring it again (as I did in above example). Unluckily, you usualy have tresholds different. You can redeclare (as I did) the variable or have different one (eg trehsoldA and tresholdB) by PArserNG rules.
 
 #### Super complex examples
 Lokign forward for contributions!
                                                
## Blacklist and Whitelist
you could noted, that the graphs are scalled.  Ifyou have run, which escapes the normality, the scale get corrupeted, and youc an easily miss regression. To fix this, you have balcklist (and whitelist). This is list of regexes,  whic filters (first) out and (second) in the (un)desired builds. It works both with custom_built_name and #build_number. Empty blacklist/whitelist means it is not used at all.

## Project Settings
Project settings and view settings are separate - with both pros and cons!

![selection_012](https://user-images.githubusercontent.com/11722903/48773059-8b53ba00-ecc6-11e8-84eb-c0bbdc7774c4.png)
Most important is **Glob pattern for the report file to parse**, which lets you specify not absolute (glob) path to your properties file and of course **Key to look for in the report file** which tetls chart what value to render.  **Chart name** and **color** are  cosmetic, **blacklist** and **whitelist** were already described.  **Number of data points to show** is how many successful builds (counted from end) should be displayed.  If you are in doubts, each suspicious field have help.

## View Settings
Project settings and view settings are separate - with both pros and cons!

![selection_011](https://user-images.githubusercontent.com/11722903/48773095-a292a780-ecc6-11e8-9759-f0d4900fdc33.png)
You can see that the settings of view are same - thus duplicated with all its pros and cons...

## Limitations

The limitations flows from double settings and from fact that each chart can show only only one value. The non-shared blacklist/whitelist is a negative which we are working on to improve. One line only is considered as - due toscalled graph - definitely positive.

## Range around whitelisted

Number of points before and after chosen point using whitelist. For example if you have whitelisted 3 and 4 and range is 2 graph will show points 1 2 3 4 5 6.
![selection_008](https://user-images.githubusercontent.com/11722903/48713596-08bcf300-ec11-11e8-9894-4e8445d612f9.png)
Up is without whitelisted and range. Down is with whitelist (1.8.0.172.\*) and range (3).
![selection_008](https://user-images.githubusercontent.com/11722903/48712892-37d26500-ec0f-11e8-92be-62acf31c6bdd.png)
Up is with whitelist (1.8.0.172.\*) and range (3). Down is with whitelist (1.8.0.172.\*) and without range.
![selection_007](https://user-images.githubusercontent.com/11722903/48713581-fe9af480-ec10-11e8-898f-3ac208b809a8.png)
## Future work
We wish to improve whitelist/balcklist feature, so it can be used to generate wievs comparing selected runs across jobs with some kind of neigbrhood

This plugin depends on https://github.com/judovana/jenkins-chartjs-plugin
