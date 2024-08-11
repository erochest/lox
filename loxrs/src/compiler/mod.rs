use std::borrow::BorrowMut;
use std::cell::RefCell;

use crate::chunk::Chunk;
use crate::error::Result;
use crate::scanner::{Scanner, Token, TokenType};

struct Compiler<'a> {
    scanner: Scanner,
    chunk: Box<&'a Chunk>,
    current: Option<Token<'a>>,
    previous: Option<Token<'a>>,
}

pub fn compile<S: AsRef<str>>(source: S, chunk: &mut Chunk) -> Result<()> {
    let source = source.as_ref().to_string();
    let scanner = Scanner::new(source);
    let mut compiler = Compiler::new(scanner, chunk);

    compiler.advance()?;
    compiler.expression()?;
    compiler.consume_eof()?;

    Ok(())
}

impl<'a> Compiler<'a> {
    fn new(scanner: Scanner, chunk: &'a Chunk) -> Self {
        let chunk = Box::new(chunk);
        Self {
            scanner,
            chunk,
            current: None,
            previous: None,
        }
    }

    fn advance(&mut self) -> Result<()> {
        self.previous = self.current.take();

        loop {
            let current = self.scanner.scan_token();
            if let Ok(token) = current {
                self.current = Some(token);
                return Ok(());
            }
            error_at_current(self.scanner.current, self.current.as_ref().unwrap().token);
        }
    }

    fn expression(&mut self) -> Result<()> {
        self.scanner.expression()
    }

    fn consume_eof(&self) -> Result<()> {
        self.scanner
            .consume(TokenType::EOF, "Expect end of expression.")
    }
}

fn error_at_current(pos: usize, token: &str) -> Result<()> {
    todo!()
}
