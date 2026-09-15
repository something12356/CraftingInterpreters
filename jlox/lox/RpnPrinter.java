package jlox.lox;

class RpnPrinter implements Expr.Visitor<String>{
    String print(Expr expr){
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr){
        return rpn(expr.operator.lexeme, expr.left, expr.right);
    }

    public String visitGroupingExpr(Expr.Grouping expr){
        return rpn(expr.expression);
    }

    public String visitLiteralExpr(Expr.Literal expr){
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    public String visitUnaryExpr(Expr.Unary expr){
        return expr.operator.lexeme+rpn(expr.right);
    }

    public String rpn(String operator, Expr... exprs){
        return exprs[0].accept(this) + " " + exprs[1].accept(this) + " " + operator;
    }

    public String rpn(Expr expr){
        return expr.accept(this);
    }

    public static void main(String[] args) {
    Expr expression = new Expr.Binary(
        new Expr.Unary(
            new Token(TokenType.MINUS, "-", null, 1),
            new Expr.Literal(123)),
        new Token(TokenType.STAR, "*", null, 1),
        new Expr.Grouping(
            new Expr.Literal(45.67)));

    System.out.println(new RpnPrinter().print(expression));
  }
}
