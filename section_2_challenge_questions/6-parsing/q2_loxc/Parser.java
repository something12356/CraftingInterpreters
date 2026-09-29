package jlox.loxc;

import java.util.List;

import static jlox.loxc.TokenType.*;

class Parser {
  private static class ParseError extends RuntimeException {}

  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  Expr parse() {
    try {
      return expression();
    } catch (ParseError error) {
      return null;
    }
  }

  private Expr expression(){
    return comma();
  }

  /*
  Grammar for commas:
  comma → equality ("," equality)*;
  */
  private Expr comma(){
    Expr expr = conditional();

    // Commas are just binary operators so the code is the same as all the other binary operators
    while (match(COMMA)){
      Token operator = previous();
      Expr right = conditional();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  /*
  Grammar for conditionals:
  conditional → equality ("?" (expression) ":" (conditional))?
  */
  // Conditionals are *right associative*
  // You can have any expression between the ? and :
  // This is since a ? b,c : d is unambiguous regardless of the precedence of the operators.
  // It can either evaluate to a ? c : d (applying ?: first) 
  // Or c : d (applying comma operator first)
  // But c : d isn't a valid expresion, so it must be a ? c : d.
  private Expr conditional() {
    Expr expr = equality();

    if (match(QUESTION)) {
      Token op1 = previous();
      Expr left = expression();
      
      if (match(COLON)) {
        Token op2 = previous();
        Expr right = conditional();

        expr = new Expr.Ternary(expr, op1, left, op2, right);
      }
    }

    return expr;
  }

  private Expr equality() {
    Expr expr = comparison();

    while (match(BANG_EQUAL, EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison(){
    Expr expr = term();
    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr term() {
    Expr expr = factor();

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr factor() {
    Expr expr = unary();

    while (match(SLASH, STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary() {
    if (match(BANG, MINUS)) {
      Token operator = previous();
      return new Expr.Unary(operator, unary());
    }

    // Error production, if a binary operator is being used like a unary operator then report an error
    if (match(BANG_EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, PLUS, SLASH, STAR)) {
      Token operator = previous();
      Lox.error(operator, "This is not a valid unary operator!");

      // Discard right hand operand of appropriate precedence based on the attempted unary operator
      switch (operator.type) {
        case BANG_EQUAL :
        case EQUAL_EQUAL : comparison(); break;

        case GREATER :
        case GREATER_EQUAL :
        case LESS :
        case LESS_EQUAL : term(); break;

        case PLUS : {factor();}; break;
        
        case SLASH : unary(); break;
        case STAR : unary(); break;
      }

      // Keep parsing and try again with the next expression
      return expression();
    }

    return primary();
  }

  private Expr primary() {
    if (match(FALSE)) return new Expr.Literal(false);
    if (match(TRUE)) return new Expr.Literal(true);
    if (match(NIL)) return new Expr.Literal(null);

    if (match(NUMBER, STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }

    throw error(peek(), "Expect expression.");
  }

  private Token consume(TokenType type, String message) {
    if (check(type)) return advance();

    throw error(peek(), message);
  }

  private ParseError error(Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }

  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == SEMICOLON) return;

      switch (peek().type) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }
  }


  private boolean match(TokenType... types) {
      for (TokenType type : types) {
      if (check(type)) {
          advance();
          return true;
      }
      }

      return false;
  }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}

