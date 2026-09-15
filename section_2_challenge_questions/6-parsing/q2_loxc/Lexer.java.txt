package jlox.loxc;

import static jlox.loxc.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map; 

class Lexer {

  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;

  Lexer(String source) {
    this.source = source;
  }

  List<Token> lexTokens() {
    while (!isAtEnd()) {
        // We are at the beginning of the next lexeme.
        start = current;
        lexToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private void lexToken(){
    char c = advance();
    switch(c){
      // Single characters
      case '(' : addToken(LEFT_PAREN); break;
      case ')' : addToken(RIGHT_PAREN); break;
      case '{' : addToken(LEFT_BRACE); break;
      case '}' : addToken(RIGHT_BRACE); break;
      case ',' : addToken(COMMA); break;
      case '.' : addToken(DOT); break;
      case '-' : addToken(MINUS); break;
      case '+' : addToken(PLUS); break;
      case ';' : addToken(SEMICOLON); break;
      case '*' : addToken(STAR); break;
      case '?' : addToken(QUESTION); break;
      case ':' : addToken(COLON); break;

      // One or two characters
      case '!' : addToken(match('=') ? BANG_EQUAL : BANG); break;
      case '=' : addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
      case '>' : addToken(match('=') ? GREATER_EQUAL : GREATER); break;
      case '<' : addToken(match('=') ? LESS_EQUAL : EQUAL); break;

      case '/':
        if (match('/')) {
          // A comment goes until the end of the line.
          while (peek() != '\n' && !isAtEnd()) advance();
        } 
        
        // We are in a block comment!
        else if (match('*')){
          int count = 1; // Keep track of how many /*'s and */'s we've seen.
                         // Exit once we've left as many comments as we've entered
          while (count > 0 && !isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') count--;
            else if (peek() == '/' && peekNext() == '*') count++;
            else if (peekNext() == '\0') Lox.error(line, "Block comment was not exited before end of file");
            advance();
          }

          // Need to advance after exiting so we don't consume the */ as actual tokens
          advance();
        }

        else {
          addToken(SLASH);
        }
        break;

      // Literals
      case '"' : string(); break;

      case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;

      case '\n':
        line++; // Update line count when find a newline
        break;

      default:
        if (isDigit(c)){
          number();
        }
        else if (isAlpha(c)){
          identifier();
        }
        else{
          Lox.error(line, "Unexpected character.");
        };
        break;
    }

  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
            c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if (type == null) type = IDENTIFIER;
    addToken(type);
  }

  private boolean isDigit(char c){
    return c >= '0' && c <= '9';
  }

  private void number(){
    while (isDigit(peek())) {
      advance();
    }

    if (match('.') && isDigit(peekNext())){
      advance();
      while (isDigit(peek())) {
        advance();
      }
    }

    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private void string() {
    // Keep advancing until string is ended
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++;
      advance();
    }

    // Give an error if the string is never closed
    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
      return;
    }

    // The closing ".
    advance();

    // Trim the surrounding quotes.
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }

  private boolean match(char expected){
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }

  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }

  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  } 

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type){
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal){
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and",    AND);
    keywords.put("class",  CLASS);
    keywords.put("else",   ELSE);
    keywords.put("false",  FALSE);
    keywords.put("for",    FOR);
    keywords.put("fun",    FUN);
    keywords.put("if",     IF);
    keywords.put("nil",    NIL);
    keywords.put("or",     OR);
    keywords.put("print",  PRINT);
    keywords.put("return", RETURN);
    keywords.put("super",  SUPER);
    keywords.put("self",   SELF);
    keywords.put("true",   TRUE);
    keywords.put("var",    VAR);
    keywords.put("while",  WHILE);
  }

}

