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

    Error,
    EOF,
}

pub struct Token<'a> {
    pub ty: TokenType,
    pub token: &'a str,
    pub line: usize,
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
        todo!()
    }
}
