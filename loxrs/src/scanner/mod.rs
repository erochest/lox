use std::char;

use crate::error::{Error, Result};

pub struct Scanner {
    pub input: String,
    pub start: usize,
    pub current: usize,
    pub line: usize,
}

#[derive(Debug, PartialEq, Copy, Clone)]
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

pub struct Token {
    pub ty: TokenType,
    pub offset: usize,
    pub length: usize,
    pub line: usize,
}

impl Token {
    fn new(ty: TokenType, offset: usize, length: usize, line: usize) -> Self {
        Self {
            ty,
            offset,
            length,
            line,
        }
    }

    pub fn text<'a>(&self, _input: &'a str) -> &'a str {
        todo!()
    }
}

impl Scanner {
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

    pub fn scan_token(&mut self) -> Result<Token> {
        self.skip_whitespace();

        self.start = self.current;

        if self.is_at_end() {
            return Ok(self.make_token(TokenType::EOF));
        }

        let c = self.advance();
        if c.is_ascii_alphabetic() || c == '_' {
            return self.identifier();
        }
        if c.is_ascii_digit() {
            return self.number();
        }

        match c {
            '(' => Ok(self.make_token(TokenType::LeftParen)),
            ')' => Ok(self.make_token(TokenType::RightParen)),
            '{' => Ok(self.make_token(TokenType::LeftBrace)),
            '}' => Ok(self.make_token(TokenType::RightBrace)),
            ';' => Ok(self.make_token(TokenType::Semicolon)),
            ',' => Ok(self.make_token(TokenType::Comma)),
            '.' => Ok(self.make_token(TokenType::Dot)),
            '-' => Ok(self.make_token(TokenType::Minus)),
            '+' => Ok(self.make_token(TokenType::Plus)),
            '/' => Ok(self.make_token(TokenType::Slash)),
            '*' => Ok(self.make_token(TokenType::Star)),

            '!' => self.match_second("=", TokenType::BangEqual, TokenType::Bang),
            '=' => self.match_second("=", TokenType::EqualEqual, TokenType::Equal),
            '<' => self.match_second("=", TokenType::LessEqual, TokenType::Less),
            '>' => self.match_second("=", TokenType::GreaterEqual, TokenType::Greater),

            '"' => self.string(),

            c => Err(self.error_token(c, self.line)),
        }
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

    pub fn advance(&mut self) -> char {
        self.current += 1;
        // TODO: handle if a character is encoded as more than one byte.
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

    fn string(&mut self) -> Result<Token> {
        while self.peek() != '"' && !self.is_at_end() {
            if self.peek() == '\n' {
                self.line += 1;
            }
            self.advance();
        }

        if self.is_at_end() {
            return Err(self.error_token('"', self.line));
        }

        self.advance();
        Ok(self.make_token(TokenType::String))
    }

    fn is_at_end(&self) -> bool {
        self.current >= self.input.len()
    }

    fn make_token(&self, ty: TokenType) -> Token {
        Token::new(ty, self.start, self.current, self.line)
    }

    fn error_token(&self, c: char, line_no: usize) -> Error {
        Error::ScanError(c, line_no)
    }

    fn number(&mut self) -> Result<Token> {
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

    fn identifier(&mut self) -> Result<Token> {
        while self.peek().is_ascii_alphanumeric() || self.peek() == '_' {
            self.advance();
        }
        Ok(self.make_token(self.identifier_type()))
    }

    fn identifier_type(&self) -> TokenType {
        let input = &self.input[self.start..self.current];
        let first_char = input.chars().next().unwrap();

        match first_char {
            'a' => self.check_keyword(input, "and", TokenType::And),
            'c' => self.check_keyword(input, "class", TokenType::Class),
            'e' => self.check_keyword(input, "else", TokenType::Else),
            'i' => self.check_keyword(input, "if", TokenType::If),
            'n' => self.check_keyword(input, "nil", TokenType::Nil),
            'o' => self.check_keyword(input, "or", TokenType::Or),
            'p' => self.check_keyword(input, "print", TokenType::Print),
            'r' => self.check_keyword(input, "return", TokenType::Return),
            's' => self.check_keyword(input, "super", TokenType::Super),
            'v' => self.check_keyword(input, "var", TokenType::Var),
            'w' => self.check_keyword(input, "while", TokenType::While),

            'f' => {
                let second_char = input.chars().nth(1).unwrap();
                match second_char {
                    'a' => self.check_keyword(input, "false", TokenType::False),
                    'o' => self.check_keyword(input, "for", TokenType::For),
                    'u' => self.check_keyword(input, "fun", TokenType::Fun),
                    _ => TokenType::Identifier,
                }
            }

            't' => {
                let second_char = input.chars().nth(1).unwrap();
                match second_char {
                    'h' => self.check_keyword(input, "this", TokenType::This),
                    'r' => self.check_keyword(input, "true", TokenType::True),
                    _ => TokenType::Identifier,
                }
            }

            _ => TokenType::Identifier,
        }
    }

    fn check_keyword(&self, input: &str, rest: &str, ty: TokenType) -> TokenType {
        if input == rest {
            ty
        } else {
            TokenType::Identifier
        }
    }

    pub fn scan_to_next(&mut self) -> (Option<Token>, Vec<(char, usize)>) {
        let mut errors = Vec::new();

        loop {
            let token = self.scan_token();
            match token {
                Ok(token) => return (Some(token), errors),
                Err(Error::ScanError(c, line_no)) => errors.push((c, line_no)),
                Err(_) => {}
            }
        }
    }
}
