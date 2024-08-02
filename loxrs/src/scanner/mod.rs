use std::char;

use crate::error::Result;

pub struct Scanner {
    input: String,
    start: usize,
    current: usize,
    line: usize,
}

#[derive(Debug, PartialEq)]
pub enum TokenType {
    // Single-character tokens.
    LeftParen,
    RightParen,
    LeftBrace,
    RightBrace,
    Comma,
    Dot,
    Minus,
    Plus,
    Semicolon,
    Slash,
    Star,

    // One or two character tokens.
    Bang,
    BangEqual,
    Equal,
    EqualEqual,
    Greater,
    GreaterEqual,
    Less,
    LessEqual,

    // Literals.
    Identifier,
    String,
    Number,

    // Keywords.
    And,
    Class,
    Else,
    False,
    For,
    Fun,
    If,
    Nil,
    Or,
    Print,
    Return,
    Super,
    This,
    True,
    Var,
    While,

    Error(String),
    EOF,
}

pub struct Token<'a> {
    pub ty: TokenType,
    pub token: &'a str,
    pub line: usize,
}

impl<'a> Token<'a> {
    fn error(message: String, line: usize) -> Self {
        Self::new(TokenType::Error(message), "", line)
    }

    fn new(ty: TokenType, token: &'a str, line: usize) -> Self {
        Self { ty, token, line }
    }
}

impl<'a> Scanner {
    pub fn new(input: String) -> Self {
        Self {
            input,
            start: 0,
            current: 0,
            line: 1,
        }
    }

    pub fn with_line(input: String, line: usize) -> Self {
        Self {
            input,
            start: 0,
            current: 0,
            line,
        }
    }

    pub fn scan_token(&'a mut self) -> Result<Token<'a>> {
        self.skip_whitespace();

        self.start = self.current;

        if self.is_at_end() {
            return Ok(self.make_token(TokenType::EOF));
        }

        let c = self.advance();
        if c.is_ascii_digit() {
            return self.number();
        }

        match c {
            '(' => return Ok(self.make_token(TokenType::LeftParen)),
            ')' => return Ok(self.make_token(TokenType::RightParen)),
            '{' => return Ok(self.make_token(TokenType::LeftBrace)),
            '}' => return Ok(self.make_token(TokenType::RightBrace)),
            ';' => return Ok(self.make_token(TokenType::Semicolon)),
            ',' => return Ok(self.make_token(TokenType::Comma)),
            '.' => return Ok(self.make_token(TokenType::Dot)),
            '-' => return Ok(self.make_token(TokenType::Minus)),
            '+' => return Ok(self.make_token(TokenType::Plus)),
            '/' => return Ok(self.make_token(TokenType::Slash)),
            '*' => return Ok(self.make_token(TokenType::Star)),

            '!' => return self.match_second("=", TokenType::BangEqual, TokenType::Bang),
            '=' => return self.match_second("=", TokenType::EqualEqual, TokenType::Equal),
            '<' => return self.match_second("=", TokenType::LessEqual, TokenType::Less),
            '>' => return self.match_second("=", TokenType::GreaterEqual, TokenType::Greater),

            '"' => return self.string(),

            _ => {}
        }

        // TODO: Make this an Err(Error).
        Ok(self.error_token("Unexpected character.".to_string()))
    }

    fn skip_whitespace(&mut self) {
        loop {
            let c = self.peek();
            match c {
                ' ' | '\r' | '\t' => {}
                '\n' => self.line += 1,
                '/' => {
                    if self.peek_next() == '/' {
                        while self.peek() != '\n' && !self.is_at_end() {
                            self.advance();
                        }
                    } else {
                        break;
                    }
                }
                _ => break,
            }
            self.current += 1;
        }
    }

    fn peek(&self) -> char {
        self.input[self.current..self.current + 1]
            .chars()
            .next()
            .unwrap_or('\0')
    }

    fn peek_next(&self) -> char {
        self.input[self.current + 1..self.current + 2]
            .chars()
            .next()
            .unwrap_or('\0')
    }

    fn advance(&mut self) -> char {
        self.current += 1;
        self.input[self.current - 1..self.current]
            .chars()
            .next()
            .unwrap()
    }

    fn match_second(
        &mut self,
        expected: &str,
        matches: TokenType,
        does_not_match: TokenType,
    ) -> Result<Token> {
        if self.match_char(expected) {
            Ok(self.make_token(matches))
        } else {
            Ok(self.make_token(does_not_match))
        }
    }

    fn match_char(&mut self, expected: &str) -> bool {
        if self.is_at_end() {
            return false;
        }
        if self.input[self.current..self.current + 1] != *expected {
            return false;
        }
        self.current += 1;
        true
    }

    fn string(&'a mut self) -> Result<Token<'a>> {
        while self.peek() != '"' && !self.is_at_end() {
            if self.peek() == '\n' {
                self.line += 1;
            }
            self.advance();
        }

        if self.is_at_end() {
            return Ok(self.error_token("Unterminated string.".to_string()));
        }

        self.advance();
        Ok(self.make_token(TokenType::String))
    }

    fn is_at_end(&self) -> bool {
        self.current >= self.input.len()
    }

    fn make_token(&self, ty: TokenType) -> Token {
        Token::new(ty, &self.input[self.start..self.current], self.line)
    }

    fn error_token(&self, message: String) -> Token {
        Token::error(message, self.line)
    }

    fn number(&'a mut self) -> Result<Token<'a>> {
        while self.peek().is_ascii_digit() {
            self.advance();
        }

        if self.peek() == '.' && self.peek_next().is_ascii_digit() {
            self.advance();
            while self.peek().is_ascii_digit() {
                self.advance();
            }
        }

        Ok(self.make_token(TokenType::Number))
    }
}
