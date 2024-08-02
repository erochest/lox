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
        self.start = self.current;

        if self.is_at_end() {
            return Ok(self.make_token(TokenType::EOF));
        }

        // TODO: Make this an Err(Error).
        Ok(self.error_token("Unexpected character.".to_string()))
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
}
