package easycalc;

import easycalc.grammar.EasyCalcBaseListener;
import easycalc.grammar.EasyCalcParser;
import org.antlr.v4.runtime.tree.ParseTree;
import java.util.List;
import java.util.Stack;

public class PrettyPrinterListener extends EasyCalcBaseListener {

    //delclare necessary variables
    private StringBuilder sb = new StringBuilder();
    private Stack<String> stack = new Stack<>();

    //string builder method that return the clean line of code.
    public String getProgramString() {
        return sb.toString();
    }

    @Override
    public void exitDeclar(EasyCalcParser.DeclarContext ctx) {
        sb.append(ctx.type.getText())
                .append(" ")
                .append(ctx.ID().getText())
                .append(";\n");
    }

    @Override public void exitProgram(EasyCalcParser.ProgramContext ctx) {
        sb.append(ctx.DSTOP().toString());
    }

    //exit assignment method
    @Override
    public void exitAssignStmt(EasyCalcParser.AssignStmtContext ctx) {
        String right = stack.pop();
        sb.append(ctx.children.get(0).toString())
                .append(" ")
                .append(ctx.children.get(1).toString())
                .append(" ")
                .append(right)
                .append(ctx.children.get(3).toString());
        sb.append("\n");
    }

    //exit read statement method
    @Override
    public void exitReadStmt(EasyCalcParser.ReadStmtContext ctx) {
        List<ParseTree> ch = ctx.children;
        sb.append(ch.get(0).toString())
                .append(" ")
                .append(ch.get(1).toString())
                .append(ch.get(2).toString());
        sb.append("\n");
    }

    //exit write statement
    @Override
    public void
    exitWriteStmt(EasyCalcParser.WriteStmtContext ctx) {
        List<ParseTree> ch = ctx.children;
        sb.append(ch.get(0).toString())
                .append(" ")
                .append(stack.pop()) //pop expr
                .append(ch.get(2).toString());
        sb.append("\n");
    }

    //This method is used to push expression 'expr op expr' onto the stack.
    //Since it is LIFO, the first operand popped out is the last one.
    //This method is used for equal,lessgrt, and,or,addsub and muldiv exit methods.
    private void opExpr(EasyCalcParser.ExprContext ctx){
        String op2 = stack.pop();
        String op1 = stack.pop();
        stack.push(op1 + " " + ctx.children.get(1).toString() + " " + op2);
    }

    @Override
    public void exitAndExpr(EasyCalcParser.AndExprContext ctx) {
        opExpr(ctx);
    }

    @Override
    public void exitOrExpr(EasyCalcParser.OrExprContext ctx) {
        opExpr(ctx);
    }

    @Override
    public void exitEqualExpr(EasyCalcParser.EqualExprContext ctx) {
        opExpr(ctx);
    }

    @Override
    public void exitLessGrtrExpr(EasyCalcParser.LessGrtrExprContext ctx) {
        opExpr(ctx);
    }

    @Override
    public void exitAddSubExpr(EasyCalcParser.AddSubExprContext ctx) {
        opExpr(ctx);
    }

    @Override
    public void exitMulDivExpr(EasyCalcParser.MulDivExprContext ctx) {
        opExpr(ctx);
    }

    //exit if expression
    @Override
    public void exitIfExpr(EasyCalcParser.IfExprContext ctx) {
        super.exitIfExpr(ctx);
        String exp3 = stack.pop();
        String exp2 = stack.pop();
        String exp1 = stack.pop();

        List<ParseTree> ch = ctx.children;
        stack.push(ch.get(0).toString()
                + " " + exp1 + " " + ch.get(2).toString()
                + " " + exp2 + " " + ch.get(4).toString() + " " +exp3);
    }

    //exit to expression, with balancing the parentheses
    @Override
    public void exitToExpr(EasyCalcParser.ToExprContext ctx) {
        String exp = stack.pop();
        if (exp.startsWith("(") && exp.endsWith(")")){
            stack.push(ctx.children.get(0).toString()+exp);
        } else {
            stack.push(ctx.children.get(0).toString()+"("+exp+")");
        }
    }

    //exit parentheses expression, with balanced parentheses
    @Override
    public void exitParenExpr(EasyCalcParser.ParenExprContext ctx) {
        String exp = stack.pop();
        if(exp.startsWith("(") && exp.endsWith(")")){
            stack.push(exp);
        }else{
            stack.push("("+exp+")");
        }
    }

    //exit ID expression
    @Override public void exitIdExpr(EasyCalcParser.IdExprContext ctx) {
        stack.push(ctx.ID().toString()); //push the id to the stack
    }

    //exit literal expression
    @Override public void exitLitExpr(EasyCalcParser.LitExprContext ctx) {
        stack.push(ctx.LIT().toString()); //push the literal to the stack
    }
}
