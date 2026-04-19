# preset equations with theirs comments
 * each preset equation have id, by which it can be called
 * each preset equation can take up to nine parameters, which are expanded as `/*1*/.../*9*/`
 * each preset equation can have comment with description and help
   * Comment can have several lines for readability
```
"id": "ID_OF_EQUATION",
    "comments": [
      "help line1",
      "help line2",
      ...
      "help lineN"
      ]
```
 * each preset equation consists form unlimited number of steps.
 * Each step have name and lines of the equation itself
 ```
 "equations": [
      {
        "name": "main1",
        "equation": [
          "a=/*1*/;",
          "b=/*2*/;",
          "c=/*3*/;",
          "  (a+b+c)*avg(L0..L{MN})"
        ],
```
  * note te name of the equations
  * lines, as in help, are just for readability
  * note usage of numerical `/**/` parameter
  * The equation may result to boolean result or to mathematical result
    * the outcome of the equation is stored for future usage as `/*variable*/` of name of the equation it was caclucalted from
    * in above example, it will be variabel of `main1` 
* The equation may be Logical or Mathematical expression 
* There are major differences between Logical and Mathematical expressions. See the help to the parser-ng itself for this
  * logical `java -jar parser-ng-0.1.9-release.jar  -l help`
  * mathematical `java -jar parser-ng-0.1.9-release.jar  help`
  * https://github.com/gbenroscience/ParserNG/tree/v0.1.9
  * https://github.com/gbenroscience/ParserNG/releases/tag/v0.1.9

* Each step have conditional result descriptions
  * The conditional expression must end as boolean
    * as mentioned, there are are major differences between Logical and Mathematical expressions. Mainly:
      * Comparing operators - allowed with spaces:!=, ==, >=, <=, <, >; not allowed with spaces:le, ge, lt, gt,
        * Logical operators - allowed with spaces:, |, &; not allowed with spaces:impl, xor, imp, eq, or, and
          * So you can use "true" to always show some message, or "false", to never show a message
          * If the equation step returns number, you can use Comparing operators to achieve boolean
          * If the equation step returns boolean, you *must* use Logical operators to achieve boolean
            * if both are mixed, dotn forget to use `[]` brackets.
            * eg `1+1 < (2+0)*1 impl [ [5 == 6 || 33<(22-20)*2 ]xor [ [  5-3 < 2 or 7*(5+2)<=5 ] and 1+1 == 2]] eq [ true && false ]`
          * Negation can be done by single ! strictly close attached to `[;` eg `![true]`  is ... false. Some spaces like `! [` are actually ok too
          * In description, can be used all variables, plus few more
            * `/*RESULT*/` - result of its equation
            * `/*ORIGEQ*/` - original expression
            * `/*EXEQ*` - expanded equation (may be huge!) 
            * `/*CONDO*/` - original condition
            * `/*CONDE*/` - expanded condition
            * `/*CONDR*/` - result of condition (true xor false)
            * If you want toe avoid the expansion, preffix the *line* by `~`
          * Description can appear several times to mimimc multiple lines again.:  
```
        "descriptions": [
          {"condition": "true","description": ["some title alway shown"], "second line"},
          {"condition": "true","description": ["~/*RESULT*/ not expanded"], "/*RESULT*/ expanded"},
          {"condition": "false","description": ["some note never shown"]},
          {"condition": "/*RESULT*/ > 0","description": ["message with condition for mathematical results"]},
          {"condition": "/*RESULT*/ eq false","description": ["message with condition for logical result"]}
```
* The equations are then processed sequentially, passing previous results as described, and printing relevant conclusions as programmed
* Final result is the result of last equation
* TODO use other predeffined calls

# full example
```
[... },
  {
    "id": "TEST",
    "comments": ["Exemple of conditional messages"],
    "equations": [
      {
        "name": "t1",
        "equation": [
          "a=/*1*/;",
          "  a+L1"
        ],
        "descriptions": [
          {"condition": "true","description": ["some title"]},
          {"condition": "/*RESULT*/ > 0","description": ["/*CONDO*/ (/*RESULT*/)"]},
          {"condition": "/*RESULT*/ < 0","description": ["/*CONDO*/ (/*RESULT*/)"]},
          {"condition": "/*RESULT*/ == 0","description": ["first param was same as first arg, only oposite"]}
        ]
      },
      {
        "name": "t2",
        "equation": [
          "b=/*2*/;",
          "  b+L2"
        ],
        "descriptions": [
          {"condition": "false","description": ["no title"]},
          {"condition": "/*RESULT*/ > 0","description": ["/*CONDO*/ (/*RESULT*/)"]},
          {"condition": "/*RESULT*/ < 0","description": ["/*CONDO*/ (/*RESULT*/)"]},
          {"condition": "/*RESULT*/ == 0","description": ["second param was same as second arg, only oposite"]}
        ]
      },
      {
        "name": "main",
        "equation": [
          "/*t1*/ == /*t2*/"
        ],
        "descriptions": [
          {"condition": "true","description": ["final title"]},
          {"condition": "/*RESULT*/ eq true","description": ["both /*t1*/ and /*t2*/ ended same"]},
          {"condition": "/*RESULT*/ eq false","description": ["both /*t1*/ and /*t2*/ ended same"]}
        ]
      }

    ]
  }
]
```