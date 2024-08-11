use std::borrow::BorrowMut;
use std::cell::RefCell;
use std::iter;

use crate::chunk::Chunk;
use crate::error::{Error, Result};
use crate::scanner::{Scanner, Token, TokenType};

struct Compiler<'a> {
    chunk: Option<Chunk>,
    current: Option<Token<'a>>,
    previous: Option<Token<'a>>,
}

pub fn compile<S: AsRef<str>>(source: S) -> Result<Chunk> {
    let source = source.as_ref().to_string();
    let mut scanner = Scanner::new(source);
    let mut compiler = Compiler::new();

    compiler.advance(&mut scanner);
    compiler.expression()?;
    compiler.consume_eof()?;

    compiler.chunk.take().ok_or(Error::MissingChunkError)
}

impl<'a> Compiler<'a> {
    fn new() -> Self {
        Self {
            chunk: None,
            current: None,
            previous: None,
        }
    }

    fn advance(&'a mut self, scanner: &'a mut Scanner) {
        self.previous = self.current.take();
        self.current = scanner.scan_to_next();
    }

    fn expression(&mut self) -> Result<()> {
        todo!()
    }

    fn consume_eof(&self) -> Result<()> {
        todo!()
    }
}

fn error_at_current(pos: usize, token: &str) -> Result<()> {
    todo!()
}
