package easycalc;

import easycalc.grammar.EasyCalcBaseListener;
import easycalc.grammar.EasyCalcParser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class AnalysisListener extends EasyCalcBaseListener {

    private SortedMap<String, String> symbolTable;
    private List<String> errorMessages;
    private Stack<String> expressionTypes;
    private Stack<String> stack;
    boolean check = false;

    public AnalysisListener() {
        symbolTable = new TreeMap<>();
        errorMessages = new ArrayList<>();
        expressionTypes = new Stack<>();
        stack = new Stack<>();
    }

    public String getSymbolTableString() {
        StringBuilder sb = new StringBuilder();
        for (String key : symbolTable.keySet()) {
            sb.append(key).append(" -> ").append(symbolTable.get(key)).append("\n");
        }
        return sb.toString();
    }

    public String getErrorMessageString() {
        StringBuilder sb = new StringBuilder();
        for (String errorMessage : errorMessages) {
            sb.append(errorMessage).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void exitDeclar(EasyCalcParser.DeclarContext ctx) {
        if (check == true) {
            check = false;
            return;
        } else {
            String identifier = ctx.ID().getText();
            String type = ctx.type.getText();
            if (symbolTable.containsKey(identifier)) {
                errorMessages.add("redefinition of " + identifier + " at " + ctx.ID().getSymbol().getLine() + ":" + (ctx.ID().getSymbol().getCharPositionInLine()+1));
                check = true;
            } else {
                symbolTable.put(identifier, type.toUpperCase());
            }
        }
        check = false;
    }

    @Override
    public void exitAssignStmt(EasyCalcParser.AssignStmtContext ctx) {
        if (check == true) {
            check = false;
            return;
        } else {
            String identifier = ctx.ID().getText();
            String type = symbolTable.get(identifier);
            if (type == null) {
                errorMessages.add(identifier + " undefined at " + ctx.ID().getSymbol().getLine() + ":" + (ctx.ID().getSymbol().getCharPositionInLine() + 1));
                check = true;
                return;
            } else {
                String expressionType = expressionTypes.pop();
                if (!type.equals(expressionType)) {
                    errorMessages.add("type clash at " + ctx.ID().getSymbol().getLine() + ":" + (ctx.ID().getSymbol().getCharPositionInLine() + 1));
                    check = true;
                    return;
                }
            }
        }
        check = false;
    }

    @Override
    public void exitReadStmt(EasyCalcParser.ReadStmtContext ctx) {
        if (check) {
            check = false;
            return;
        } else {
            String identifier = ctx.ID().getText();
            String type = symbolTable.get(identifier);
            if (!symbolTable.containsKey(identifier)) {
                errorMessages.add(identifier + " undefined at " + ctx.getStart().getLine() + ":" + (ctx.getStart().getCharPositionInLine() + 1));
                check = true;
                return;
            } else if (type == null) {
                errorMessages.add(identifier + " undefined at " + ctx.ID().getSymbol().getLine() + ":" + (ctx.ID().getSymbol().getCharPositionInLine() + 1));
                check = true;
                return;
            }
        }
        check = false;
    }

    @Override
    public void exitWriteStmt(EasyCalcParser.WriteStmtContext ctx) {
        if (check) {
            check = false;
            return;
        } else {
            String expressionType = expressionTypes.pop();
            if (!expressionType.equals("INT") && !expressionType.equals("REAL") && !expressionType.equals("BOOL")) {
                errorMessages.add("type clash at " + ctx.getStart().getLine() + ":" + (ctx.getStart().getCharPositionInLine() + 1));
                check = true;
                return;
            }
        }
        check = false;
    }

    @Override
    public void exitLitExpr(EasyCalcParser.LitExprContext ctx) {
            // Get the value of the literal
            String value = ctx.getText();
            // Determine the type of the literal
            String type;
            if (value.contains(".")) {
                type = "REAL";
            } else if (value.equals("true") || value.equals("false")) {
                type = "BOOL";
            } else {
                type = "INT";
            }
            // Push the type of the literal onto the type stack
            expressionTypes.push(type);
    }

    @Override
    public void exitIdExpr(EasyCalcParser.IdExprContext ctx) {
        if (check) {
            check = false;
            return;
        } else {
            String identifier = ctx.ID().getText();
            String type = symbolTable.get(identifier);
            if (type == null) {
                errorMessages.add(identifier + " undefined at " + ctx.ID().getSymbol().getLine() + ":" + (ctx.ID().getSymbol().getCharPositionInLine() + 1));
                expressionTypes.push("ERROR");
                check = true;
                return;
            } else {
                expressionTypes.push(type);
            }
        }
        check = false;
    }

    @Override
    public void exitAndExpr(EasyCalcParser.AndExprContext ctx) {
        if (check == true) {
            check = false;
            return;
        } else {
            String rightType = expressionTypes.pop();
            String leftType = expressionTypes.pop();
            if (!leftType.equals("BOOL") || !rightType.equals("BOOL")) {
                int line = ctx.start.getLine();
                int pos = ctx.start.getCharPositionInLine() + 1;
                String errorMessage = "and undefined for " + leftType + " at " + line + ":" + pos;
                errorMessages.add(errorMessage);
                check = true;
                return;
            }
            expressionTypes.push("BOOL");
        }
        check = false;
    }

    @Override
    public void exitOrExpr(EasyCalcParser.OrExprContext ctx) {
        if (check == true) {
            check = false;
            return;
        } else {
            String rightType = expressionTypes.pop();
            String leftType = expressionTypes.pop();
            if (!leftType.equals("BOOL") || !rightType.equals("BOOL")) {
                int line = ctx.start.getLine();
                int pos = ctx.start.getCharPositionInLine() + 1;
                String errorMessage = "or undefined for " + leftType + " at " + line + ":" + pos;
                errorMessages.add(errorMessage);
                check = true;
            }
            expressionTypes.push("BOOL");
            check = false;
        }
    }


    @Override
    public void exitAddSubExpr(EasyCalcParser.AddSubExprContext ctx) {
        if (check = true) {
            check = false;
            return;
        } else {
            String operator = ctx.op.getText();
            String rightType = expressionTypes.pop();
            String leftType = expressionTypes.pop();
            System.out.println(rightType);
            System.out.println(leftType);

            if (!leftType.equals(rightType)) {
                int line = ctx.getStart().getLine();
                int pos = ctx.getStart().getCharPositionInLine() + 1;
                String errorMsg = "type clash at " + line + ":" + pos;
                errorMessages.add(errorMsg);
                check = true;
                return;

            } else if (!leftType.equals("INT") && !leftType.equals("REAL")) {
                int line = ctx.getStart().getLine();
                int pos = ctx.getStart().getCharPositionInLine() + 1;
                String errorMsg = operator + " undefined for " + leftType + " at " + line + ":" + pos;
                errorMessages.add(errorMsg);
                check = true;
                return;
            }

            // Push the resulting type onto the stack
            if (leftType.equals("REAL") && rightType.equals("REAL")) {
                expressionTypes.push("REAL");
            } else {
                expressionTypes.push("INT");
            }
        }
        check = false;
    }

    @Override
    public void exitMulDivExpr(EasyCalcParser.MulDivExprContext ctx) {
        if (check == true) {
            check = false;
            return;
        } else {
            String operator = ctx.op.getText();
            String rightType = expressionTypes.pop();
            String leftType = expressionTypes.pop();

            if (leftType.equals("BOOL")) {
                int line = ctx.getStart().getLine();
                int pos = ctx.getStart().getCharPositionInLine() + 1;
                String errorMsg = operator + " undefined for " + leftType + " at " + line + ":" + pos;
                errorMessages.add(errorMsg);
                check = true;
                return;
            } else if (rightType.equals("BOOL")) {
                int line = ctx.getStop().getLine();
                int pos = ctx.getStop().getCharPositionInLine() + 1;
                String errorMsg = operator + " undefined for " + rightType + " at " + line + ":" + pos;
                errorMessages.add(errorMsg);
                check = true;
                return;
            } else if (!leftType.equals(rightType)) {
                int line = ctx.getStart().getLine();
                int pos = ctx.getStart().getCharPositionInLine() + 1;
                String errorMsg = "type clash at " + line + ":" + pos;
                errorMessages.add(errorMsg);
                check = true;
                return;
            }

            // Push the resulting type onto the stack
            if (leftType.equals("REAL") || rightType.equals("REAL")) {
                expressionTypes.push("REAL");
            } else {
                expressionTypes.push("INT");
            }
        }
        check = false;
    }
    @Override
    public void exitToExpr (EasyCalcParser.ToExprContext ctx){
        if (check == true) {
            check = false;
            return;
        } else {
            String operand = ctx.op.getText();
            String type = expressionTypes.pop();
            if (operand.equals("to_real")) {
                if (!type.equals("INT")) {
                    errorMessages.add("to_real undefined for " + type + " at " + ctx.getStart().getLine() + ":" + (ctx.expr().getStart().getCharPositionInLine() + 1));
                    expressionTypes.push("ERROR");
                    check = true;
                    return;
                }
            } else if (operand.equals("to_int")) {
                if (!type.equals("REAL")) {
                    errorMessages.add("to_int undefined for " + type + " at " + ctx.getStart().getLine() + ":" + (ctx.expr().getStart().getCharPositionInLine() + 1));
                    expressionTypes.push("ERROR");
                    check = true;
                    return;
                }
            } else {
                expressionTypes.push(type);
            }
        }
        check = false;
    }
    @Override public void exitParenExpr(EasyCalcParser.ParenExprContext ctx) {
        if (ctx.getChild(1) != null) {
            // get type from the expression inside the parenthesis
            String type = expressionTypes.pop();
            // push type to the stack
            expressionTypes.push(type);
        }
    }
    @Override
    public void exitIfExpr(EasyCalcParser.IfExprContext ctx) {
        if (check) {
            check = false;
            return;
        }

        EasyCalcParser.ExprContext conditionCtx = ctx.expr(0);
        EasyCalcParser.ExprContext thenCtx = ctx.expr(1);
        EasyCalcParser.ExprContext elseCtx = ctx.expr(2);

        String elseType = expressionTypes.pop();
        String thenType = expressionTypes.pop();
        String conditionType = expressionTypes.pop();

        if (!conditionType.equals("BOOL")) {
            // Error: Condition is not a boolean expression
            String errorMsg = "if undefined for " + conditionType +
                    " at " + conditionCtx.getStart().getLine() + ":" + (conditionCtx.getStop().getCharPositionInLine()+1);
            errorMessages.add(errorMsg);
            check = true;
            return;
        }

        // Check then expression
        if (thenType == null) {
            // Error: Then expression has no type
            String errorMsg = "if undefined  " +
                    thenCtx.getStart().getLine() + ";" + (thenCtx.getStop().getCharPositionInLine()+1);
            errorMessages.add(errorMsg);
            check = true;
            return;
        }

        // Check else expression
        if (elseType == null) {
            // Error: Else expression has no type
            String errorMsg = "if undefined at " +
                    elseCtx.getStart().getLine() + ":" + elseCtx.getStop().getCharPositionInLine();
            errorMessages.add(errorMsg);
            check = true;
            return;
        }

        // Determine the resulting type of the if expression
        String resultType;
        if (thenType.equals(elseType)) {
            resultType = thenType;
        } else if (thenType.equals("REAL") || elseType.equals("REAL")) {
            resultType = "REAL";
        } else {
            resultType = "INT";
        }

        // Push the resulting type onto the stack
        expressionTypes.push(resultType);

        check = false;
    }


}

