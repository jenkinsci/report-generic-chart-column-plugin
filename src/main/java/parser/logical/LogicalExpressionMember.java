package parser.logical;

public interface LogicalExpressionMember {

    /**
     * Help for this parser
     * @return
     */
    String getHelp();

    /**
     * @return evaluated expression, usually parsed in constructor
     */
    boolean evaluate();

    /**
     * ParserNG have a habiit, that expression is parsed in constructor, and later evaluated in methood.
     * So this methid is takin parameter, of future expression, created over dummy example, so we know,
     * whether it will be viable for future constructor.
     *
     * @param futureExpression future expression to be passed to constructor
     * @return whether the expression is most likely targeted for this parser
     */
    boolean isLogicalExpressionMember(String futureExpression);

    interface LogicalExpressionMemberFactory {

        LogicalExpressionMember createLogicalExpressionMember(String expression, ExpressionLogger log);

    }
}
