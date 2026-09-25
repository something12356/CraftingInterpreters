package jlox.lox;

import java.util.HashMap;
import java.util.Map;

class Environment {
  final Environment enclosing; // The block this block lives inside!
  private final Map<String, Object> values = new HashMap<>();

  Environment() {
    enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  void define(String name, Object value) {
    values.put(name, value);
  }

  void define(String name) {
    // "Null" initialising, Lox never uses integers so 0 is a free value to use to say a variable is uninitialised
    values.put(name, 0);
  }

  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }

    if (enclosing != null) { 
      assign(name, value); 
      return;
    }

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      if (values.get(name.lexeme) == (Integer) 0) {
        throw new RuntimeError(name, "Uninitialised variable '" + name.lexeme + "'.");
      }
      return values.get(name.lexeme);
    }

    if (enclosing != null) return enclosing.get(name);

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }
}