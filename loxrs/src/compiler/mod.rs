use crate::chunk::Chunk;
use crate::error::{Error, Result};
use crate::scanner::{Scanner, Token};

struct Compiler {
    chunk: Option<Chunk>,
    current: Option<Token>,
    previous: Option<Token>,
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

impl Compiler {
    fn new() -> Self {
        Self {
            chunk: None,
            current: None,
            previous: None,
        }
    }

    fn advance(&mut self, scanner: &mut Scanner) {
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
